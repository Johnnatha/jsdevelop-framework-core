package br.com.jsdev.framework.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.annotations.QueryHints;
import org.hibernate.type.DateType;
import org.hibernate.type.TimeType;
import org.hibernate.type.TimestampType;
import org.hibernate.type.Type;

import br.com.jsdev.framework.core.wrapper.InDataPager;
import br.com.jsdev.framework.core.wrapper.OutDataPager;

public class QueryBuilder implements Cloneable {

	private static final String SELECT_COUNT_PART = "select count(#ALIAS#) ";
	private final EntityManager entityManager;
	
	private String namedQuery;
	
	private Integer queryTimeout;

	private StringBuilder queryString;//NOPMD

	private Map<String, Object> parameters;

	private Query cachedQuery = null;
	
	private HashMap<String, Type> typesScalar;
	
	public QueryBuilder(final EntityManager oEntityManager) {
		entityManager = oEntityManager;
		this.queryString = new StringBuilder();
		this.parameters = new HashMap<String, Object>();
		this.typesScalar = new HashMap<String, Type>();
	}

	@Override
	public QueryBuilder clone() {
		try {
			final QueryBuilder clone = (QueryBuilder) super.clone();

			// Clona a namedQuery
			clone.namedQuery = namedQuery;
			// Clona Query:
			clone.queryString = new StringBuilder(this.queryString);
			// Clona parameters:
			clone.parameters = new HashMap<String, Object>(this.parameters);
			// Limpa cachedQuery:
			clone.cachedQuery = null;

			return clone;

		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}

	public void reseta() {
		this.queryString.setLength(0);
		this.parameters.clear();
		this.cachedQuery = null;
	}
	
	public QueryBuilder namedQuery(final String namedQuery) {
		this.namedQuery = namedQuery;
		this.queryString.setLength(0);
		this.cachedQuery = null;
		return this;
	}

	public QueryBuilder append(final String parte) {
		if (StringUtils.isNotBlank(parte)) {
			this.queryString.append(parte).append(' ');
			this.cachedQuery = null;
		}
		return this;
	}

	public QueryBuilder setParameterIn(String nomeParameter, Collection<?> valores) {
		Iterator<?> iterator = valores.iterator();
		int indice = 0;
		while (iterator.hasNext()) {
			final String nomeCorrente = nomeParameter + "_" + indice;

			this.queryString.append(':').append(nomeCorrente);
			this.addParameter(nomeCorrente, iterator.next());

			indice++;
			
			if (indice < valores.size()) {
				this.queryString.append(", ");
			}
			
		}
		this.cachedQuery = null;
		return this;
	}

	/**
	 * TODO
	 * 
	 * @return
	 */
	public QueryBuilder removeLastChar() {
		this.queryString.deleteCharAt(this.queryString.length() - 1);
		this.cachedQuery = null;
		return this;
	}
	
	/**
	 * Inicializa a named query
	 * 
	 * @param namedQuery
	 *            - Nome da query
	 * @param nomeParameter
	 *            o nome do parÃ¢metro a ser adicionado.
	 * @param valorParameter
	 *            o valor do parÃ¢metro a ser adicionado.
	 * @return o prÃ³prio montador, referÃªncia <code>this</code>.
	 */
	public QueryBuilder namedQuery(final String namedQuery, String nomeParameter, Object valorParameter) {
		this.namedQuery(namedQuery);
		this.addParameter(nomeParameter, valorParameter);
		return this;
	}

	/**
	 * Adiciona uma parte no final da query.
	 * 
	 * @param parte
	 *            PedaÃ§o a ser adicionado no final.
	 * @param nomeParameter
	 *            o nome do parÃ¢metro a ser adicionado.
	 * @param valorParameter
	 *            o valor do parÃ¢metro a ser adicionado.
	 * @return o prÃ³prio montador, referÃªncia <code>this</code>.
	 */
	public QueryBuilder append(String parte, String nomeParameter, Object valorParameter) {
		this.append(parte);
		addParameter(nomeParameter, valorParameter);
		return this;
	}
	
	/**
	 * Inicializa a named query
	 * 
	 * @param namedQuery
	 *            - Nome da query
	 * @param nomeParameter
	 *            o nome do parÃ¢metro a ser adicionado.
	 * @param valorParameter
	 *            o valor do parÃ¢metro a ser adicionado.
	 * @param tipoParameter
	 *            define a forma como deve ser considerada a data.
	 * @return o prÃ³prio montador, referÃªncia <code>this</code>.
	 */
	public QueryBuilder namedQuery(final String namedQuery, String nomeParameter, Date valorParameter, TemporalType tipoParameter) {
		this.namedQuery(namedQuery);
		this.setParameter(nomeParameter, valorParameter, tipoParameter);
		return this;
	}

	/**
	 * Adiciona uma parte no final da query.
	 * 
	 * @param parte
	 *            PedaÃ§oo a ser adicionado no final.
	 * @param nomeParameter
	 *            o nome do parÃ¢metro a ser adicionado.
	 * @param valorParameter
	 *            o valor do parÃ¢metro a ser adicionado.
	 * @param tipoParameter
	 *            define a forma como deve ser considerada a data.
	 * @return o prÃ³prio montador, referÃªncia <code>this</code>.
	 */
	public QueryBuilder append(String parte, String nomeParameter, Date valorParameter, TemporalType tipoParameter) {
		this.append(parte);
		this.setParameter(nomeParameter, valorParameter, tipoParameter);
		return this;
	}

	/**
	 * Adiciona mais um parÃ¢metro, sobrepondo se tiver o mesmo nome.
	 * 
	 * @param nomeParameter
	 *            o nome do parÃ¢metro a ser adicionado.
	 * @param valorParameter
	 *            o valor do parÃ¢metro a ser adicionado.
	 * @throws IllegalArgumentException
	 *             Se o nome, ou valor, for nulos.
	 */
	public void addParameter(String nomeParameter, Object valorParameter) {
		if (StringUtils.isBlank(nomeParameter)) {
			throw new IllegalArgumentException("Nome do parÃ¢metro nÃ£o pode ser nulo.");
		}
		// para namedQuery aceita Parameter nulo
		if (valorParameter == null && namedQuery == null) { 
			throw new IllegalArgumentException("Valor do parÃ¢metro nÃ£o pode ser nulo.");
		}
		this.parameters.put(nomeParameter, valorParameter);
	}

	/**
	 * Adiciona mais um parÃ¢metro, sobrepondo se tiver o mesmo nome.
	 * 
	 * @param nomeParameter
	 *            o nome do parÃ¢metro a ser adicionado.
	 * @param valorParameter
	 *            o valor do parÃ¢metro a ser adicionado.
	 * @param tipo
	 *            define a forma como deve ser considerada a data.
	 */
	public void setParameter(String nomeParameter, Date valorParameter, TemporalType tipo) {
		this.addParameter(nomeParameter, new ParameterData(valorParameter, tipo));
	}

	/**
	 * Adiciona uma parte no final da query.
	 * 
	 * @param parte
	 *            PedaÃ§o a ser adicionado no final.
	 * @return o prÃ³prio montador, referÃªncia <code>this</code>.
	 */
	public QueryBuilder appendNoInicio(final String parte) {
		if (StringUtils.isNotBlank(parte)) {
			this.queryString.insert(0, parte + ' ');
			this.cachedQuery = null;
		}
		return this;
	}


	/**
	 * TODO.
	 * 
	 * @return
	 */
	public Integer getResultCount() {
		return getResultCount(null);
	}

	/**
	 * TODO.
	 * 
	 * @param alias
	 * @return
	 */
	public Integer getResultCount(String alias) {
		final Query query = this.createQueryCount(alias);
		Number contagem;
		try {
			contagem = (Number) query.getSingleResult();
		} catch (NoResultException nre) {
			contagem = 0;
		}
		return contagem.intValue();
	}

	/**
	 * ConstrÃ³i e configura uma Native Query JPA.
	 * 
	 * @param classe
	 *            Deve ser uma classe que estÃ¡ mapeada como objeto persistente. Repassado ao mÃ©todo
	 *            <code>javax.persistence.EntityManager#createNativeQuery(java.lang.String, java.lang.Class)</code> junto com o conteÃºdo da query em String.
	 * 
	 * @return query JPA, JÃ¡ com os parÃ¢metros configurados.
	 * 
	 * @see javax.persistence.EntityManager#createNativeQuery(java.lang.String, java.lang.Class)
	 */
	public org.hibernate.Query createNativeQuery(Class<?>/* <? extends Entidade> */classe) {
		
		SQLQuery sqlQuery = ((Session) this.entityManager.getDelegate()).createSQLQuery(this.queryString.toString());
		setTypesScalar(sqlQuery, this.typesScalar);
		
		org.hibernate.Query query = sqlQuery;
		setParametersQuery(query, this.parameters, queryTimeout);
		query.setResultTransformer(new HibernateResultTransformer(classe));
		
		return query;
	}
	
	public static void setTypesScalar(final SQLQuery sqlQuery, final Map<String, Type> typesMap) {		
		if (typesMap != null && typesMap.size() > 0){
			for (Map.Entry<String, Type> entry : typesMap.entrySet()) {
				sqlQuery.addScalar(entry.getKey(), entry.getValue());
			}
		}
	}
	
	public void addScalar(String name, Type typeInstance){
		this.typesScalar.put(name, typeInstance);
	}
	
	public Query createNativeQuery(Class<?>/* <? extends Entidade> */classe, final Integer start, final Integer max) {
		final Query query = this.entityManager.createNativeQuery(this.queryString.toString(), classe);
		setParametersQuery(query, this.parameters, queryTimeout);
		setParametersPager(query, start, max);
		this.cachedQuery = query;
		return query;
	}

	/**
	 * TODO.
	 * 
	 * @return
	 */
	public Query createNativeQuery() {
		final Query query = this.entityManager.createNativeQuery(this.queryString.toString());
		setParametersQuery(query, this.parameters, queryTimeout);
		this.cachedQuery = query;
		return query;
	}
	
	public Query createNativeQuery(final Integer start, final Integer max) {
		final Query query = this.entityManager.createNativeQuery(this.queryString.toString());
		setParametersPager(query, start, max);
		setParametersQuery(query, this.parameters, queryTimeout);
		this.cachedQuery = query;
		return query;
	}
	

	/**
	 * ConstrÃ³i e configura a Query JPA.
	 * 
	 * @return query JPA, JÃ¡ com os parÃ¢metros configurados.
	 * 
	 * @see javax.persistence.EntityManager#createQuery(String)
	 */
	public Query createQuery() {
		if (this.cachedQuery == null) {
			this.cachedQuery = this.entityManager.createQuery(this.queryString.toString());
		}
		setParametersQuery(this.cachedQuery, this.parameters, queryTimeout);
		return this.cachedQuery;
	}
	
	/**
	 * ConstrÃ³i e configura a TypedQuery JPA.
	 * 
	 * @return typedQuery JPA, JÃ¡ com os parÃ¢metros configurados.
	 * 
	 * @see javax.persistence.EntityManager#createQuery(String)
	 */
	public <T> TypedQuery<T> construirQuery(Class<T> clazz) {
		TypedQuery<T> typedQuery = this.entityManager.createQuery(this.queryString.toString(), clazz); 
		if (this.cachedQuery == null) {
			this.cachedQuery = typedQuery;
		}
		setParametersQuery(typedQuery, this.parameters, queryTimeout);
		return typedQuery;
	}

	/**
	 * ConstrÃ³i e configura a Query Hibernate.
	 * 
	 * @return query Hibernate, JÃ¡ com os parÃ¢metros configurados.
	 * 
	 * @see org.hibernate.Query#createQuery(String)
	 */
	public org.hibernate.Query createQueryDTO(Class<?> dtoClass) {
		org.hibernate.Query query = ((Session) this.entityManager.getDelegate()).createQuery(this.queryString.toString());
		setParametersQuery(query, this.parameters, queryTimeout);
		query.setResultTransformer(new HibernateResultTransformer(dtoClass));
		return query;
	}

	/**
	 * ConstrÃ³i e configura a Query JPA. Repassando os parÃ¢metros de paginaÃ§Ã£o.
	 * 
	 * @param start
	 * @param max
	 * @return
	 */
	public Query construirQuery(final Integer start, final Integer max) {
		final Query query = this.createQuery();
		setParametersPager(query, start, max);
		return query;
	}
	
	/**
	 * ConstrÃ³i e configura a Named Query JPA.
	 * 
	 * @return
	 */
	public Query createNamedQuery() {
		final Query query = this.entityManager.createNamedQuery(namedQuery);
		setParametersQuery(query, this.parameters, queryTimeout);
		this.cachedQuery = query;
		return query;
	}
	
	/**
	 * ConstrÃ³i e configura a Named Query JPA. Repassando os parÃ¢metros de paginaÃ§Ã£o.
	 * @param start
	 * @param max
	 * @return
	 */
	public Query createNamedQuery(final Integer start, final Integer max) {
		final Query query = this.createNamedQuery();
		setParametersPager(query, start, max);
		return query;
	}

	/**
	 * ConstrÃ³i e configura a Query Hibernate. Repassando os parÃ¢metros de paginaÃ§Ã£o.
	 * 
	 * @param dtoClass
	 * @param start
	 * @param max
	 * @return
	 */
	public org.hibernate.Query createQueryDTO(Class<?> dtoClass, final Integer start, final Integer max) {
		org.hibernate.Query query = this.createQueryDTO(dtoClass);
		setParametersPager(query, start, max);
		return query;
	}

	public Query createQueryCount() {
		return createQueryCount(null);
	}

	public Query createQueryCount(String alias) {
		String countPart = SELECT_COUNT_PART;
		if (StringUtils.isNotBlank(alias)) {
			alias = "distinct " + alias;
		} else {
			alias = "*";
		}
		countPart = countPart.replaceAll("#ALIAS#", alias);

		// Remove a clÃ¡usula 'select':
		String queryStr = this.queryString.toString();
		queryStr = queryStr.substring(queryStr.toLowerCase().indexOf("from"));

		final int indexOfOrderBy = queryStr.toLowerCase().indexOf("order by");
		if (indexOfOrderBy >= 0) {
			queryStr = queryStr.substring(0, indexOfOrderBy - 1);
		}

		// Remove os 'fetch', para contar:
		queryStr = new StringBuilder(countPart).append(queryStr).toString().replaceAll(" join fetch ", " join ");
		final Query query = this.entityManager.createQuery(queryStr);
		setParametersQuery(query, this.parameters, queryTimeout);
		return query;
	}

	/**
	 * Criado para poder testar a montagem correta da query.
	 * 
	 * @return a query.
	 */
	public String getQueryString() {
		return this.queryString.toString();
	}

	/**
	 * Criado para testar a montagem correta dos parÃ¢metros.
	 * 
	 * @return os parÃ¢metros.
	 */
	/* package-private */Map<String, Object> getParameters() {
		return Collections.unmodifiableMap(this.parameters);
	}

	/**
	 * Classe que agrupa o valor de um parÃ¢metro do tipo Date, mais sua especificaÃ§Ã£o de TemporalType.
	 * 
	 */
	/* package-private */static final class ParameterData {
		/* package-private */final Date valor;
		/* package-private */final TemporalType tipo;

		public ParameterData(Date valor, TemporalType tipo) {
			this.valor = valor;
			this.tipo = tipo;
		}

		public org.hibernate.type.Type getHibernateType() {
			switch (tipo) {
			case TIMESTAMP:
				return TimestampType.INSTANCE;
			case TIME:
				return TimeType.INSTANCE;
			case DATE:
				return DateType.INSTANCE;
			default:
				throw new IllegalStateException("TemporalType invÃ¡lido: " + tipo);
			}
		}
	}

	/**
	 * Configura os parÃ¢metros na Query JPA.
	 * 
	 * @param query
	 *            Query JPA.
	 * @param Parameters
	 *            parÃ¢metros para setar na query.
	 * @param timeout TODO
	 */
	public static void setParametersQuery(final Query query, final Map<String, Object> Parameters, Integer timeout) {
		for (Map.Entry<String, Object> entry : Parameters.entrySet()) {
			final Object value = entry.getValue();
			if (value instanceof ParameterData) {
				final ParameterData dateParameter = (ParameterData) value;
				query.setParameter(entry.getKey(), dateParameter.valor, dateParameter.tipo);
			} else {
				query.setParameter(entry.getKey(), value);
			}
		}
		if (timeout != null) {
			query.setHint(QueryHints.TIMEOUT_JPA, Integer.valueOf(timeout * 1000));
		}
	}

	/**
	 * Configura os parÃ¢metros na Query JPA.
	 * 
	 * @param query
	 *            Query Hibernate.
	 * @param Parameters
	 *            parÃ¢metros para setar na query.
	 * @param timeout TODO
	 */
	public static void setParametersQuery(final org.hibernate.Query query, final Map<String, Object> Parameters, Integer timeout) {
		for (Map.Entry<String, Object> entry : Parameters.entrySet()) {
			final Object value = entry.getValue();
			if (value instanceof ParameterData) {
				final ParameterData dateParameter = (ParameterData) value;
				query.setParameter(entry.getKey(), dateParameter.valor, dateParameter.getHibernateType());
			} else if (value instanceof Collection) {
				query.setParameterList(entry.getKey(), (Collection<?>) value);
			} else {
				query.setParameter(entry.getKey(), value);
			}
		}
		if (timeout != null) {
			query.setTimeout(timeout);
		}
	}

	/**
	 * Configura os parÃ¢metros de paginaÃ§Ã£o na Query.
	 * 
	 * @param query
	 *            Query a ser configurada.
	 * @param start
	 *            Ã�ndice do primeiro a ser retornado, comeÃ§ando em zero.
	 * @param max
	 *            O nÃºmero de registros a serem retornados.
	 */
	public static void setParametersPager(final Query query, final Integer start, final Integer max) {
		if (start != null && start >= 0) {
			query.setFirstResult(start);
		}

		if (max != null && max >= 0) {
			query.setMaxResults(max);
		}
	}

	/**
	 * Configura os parÃ¢metros de paginaÃ§Ã£o na Query.
	 * 
	 * @param query
	 *            Query a ser configurada.
	 * @param start
	 *            Ã�ndice do primeiro a ser retornado, comeÃ§ando em zero.
	 * @param max
	 *            O nÃºmero de registros a serem retornados.
	 */
	public static void setParametersPager(final org.hibernate.Query query, final Integer start, final Integer max) {
		if (start != null && start >= 0) {
			query.setFirstResult(start);
		}

		if (max != null && max >= 0) {
			query.setMaxResults(max);
		}
	}

	/**
	 * Aplica o mÃ©todo <code>trim</code> e depois retorna com '%' para filtros do tipo 'like'.
	 * 
	 * @param filtro
	 *            ConteÃºdo do filtro.
	 * @return ConteÃºdo para setado como parÃ¢metro na query.
	 */
	public static final String filterSubstring(final String filtro) {
		return "%" + filtro.trim() + "%";
	}

	/**
	 * Aplica o mÃ©todo <code>trim</code> e depois retorna com '%' para filtros do tipo 'like'.
	 * 
	 * @param filtro
	 *            ConteÃºdo do filtro.
	 * @return ConteÃºdo para setado como parÃ¢metro na query.
	 */
	public static final String startFilter(final String filtro) {
		return filtro.trim() + "%";
	}
	
	/**
	 * Aplica o mÃ©todo <code>trim</code> e depois retorna com '%' para filtros do tipo 'like'.
	 * 
	 * @param filtro
	 *            ConteÃºdo do filtro.
	 * @return ConteÃºdo para setado como parÃ¢metro na query.
	 */
	public static final String startExactFilter(final String filtro) {
		return filtro.trim() + " %";
	}

	/**
	 * Aplica o mÃ©todo <code>trim</code> e depois retorna com '%' para filtros do tipo 'like'.
	 * 
	 * @param filtro
	 *            ConteÃºdo do filtro.
	 * @return ConteÃºdo para setado como parÃ¢metro na query.
	 */
	public static final String endFilter(final String filtro) {
		return "%" + filtro.trim();
	}

	protected static final int MAX_TAM_QUEBRA = 999;

	public static <T> Collection<Collection<T>> breakLineForIn(Collection<T> lista) {
		final Collection<Collection<T>> quebras = new ArrayList<Collection<T>>();

		Collection<T> ultimaQuebra = new ArrayList<T>();
		for (T elemento : lista) {
			if (ultimaQuebra.size() == MAX_TAM_QUEBRA) {
				quebras.add(ultimaQuebra);
				ultimaQuebra = new LinkedList<T>();
			}
			ultimaQuebra.add(elemento);
		}
		if (ultimaQuebra.size() > 0) {
			quebras.add(ultimaQuebra);
		}

		return quebras;
	}
	
	/**
	 * Chama o executeUpdate de Query e um flush do entityManager
	 * 
	 * @param consulta
	 */
	public int executeUpdate() {
		int updated = this.createQuery().executeUpdate();
		entityManager.flush();
		return updated;
	}
	
	/**
	 * Executa um <code>getResultList</code> de <code>Query</code> com <code>start</code> e <code>max</code>
	 * 
	 * @param start
	 * @param max
	 * @return
	 */
	public List<?> getResultList(final Integer start, final Integer max) {
		return this.construirQuery(start, max).getResultList();
	}

	/**
	 * Executa um <code>getResultList</code> de <code>Query</code>
	 * 
	 * @return
	 */
	public List<?> getResultList() {
		return this.createQuery().getResultList();
	}
	
	public OutDataPager<?> getPagerResultList(InDataPager inDataPager) {
		
		Integer first = inDataPager != null ? inDataPager.getFirstResult() : null;
		Integer maxResults = inDataPager != null ? inDataPager.getMaxResults() : null;
		
		return new OutDataPager(this.getResultCount(), (List<?>)this.getResultList(first,maxResults));
	}
	
	public OutDataPager<?> getPagerResultListDTO(InDataPager inDataPager, final Class<?> dtoClass) {
		Integer first = inDataPager != null ? inDataPager.getFirstResult() : null;
		Integer maxResults = inDataPager != null ? inDataPager.getMaxResults() : null;
		
		return new OutDataPager(this.getResultCount(), this.getResultListDTO(dtoClass, first,maxResults));
	}	

	/**
	 * Executa um <code>getSingleResult</code> de <code>Query</code>
	 * 
	 * @return
	 */
	public Object getSingleResult() {
		return this.createQuery().getSingleResult();
	}

	/**
	 * Executa um <code>getResultList</code> de <code>Query</code> buscando apenas o primeiro elemento
	 * 
	 * @return
	 */
	public Object getFirstResult() {
		List<?> lista = this.construirQuery(0, 1).getResultList();
		if (lista != null && !lista.isEmpty()) {
			return lista.get(0);
		}
		return null;
	}
	
	/**
	 * Executa um <code>getResultList</code> de <code>Query</code> buscando apenas o primeiro elemento
	 * 
	 * @return
	 */
	
	public <T> T getFirstResult(Class<T> clazz) {
		final TypedQuery<T> query = this.construirQuery(clazz);
		setParametersPager(query, 0, 1);
		List<T> lista = query.getResultList();
		if (lista != null && !lista.isEmpty()) {
			return lista.get(0);
		}
		return null;
	}

	/**
	 * Executa um <code>list</code> de <code>org.hibernate.Query</code> com <code>start</code> e <code>max</code>
	 * 
	 * @param consulta
	 * @param dtoClass
	 * @param start
	 * @param max
	 * @return
	 */
	public List<?> getResultListDTO(final Class<?> dtoClass, final Integer start, final Integer max) {
		return this.createQueryDTO(dtoClass, start, max).list();
	}
	
	/**
	 * Executa um <code>list</code> de <code>org.hibernate.Query</code>
	 * 
	 * @param consulta
	 * @param dtoClass
	 * @return
	 */
	public List<?> getResultListDTO(final Class<?> dtoClass) {
		return this.createQueryDTO(dtoClass).list();
	}

	/**
	 * Executa um <code>uniqueResult</code> de <code>org.hibernate.Query</code>
	 * 
	 * @return
	 */
	public Object getSingleResultDTO(final Class<?> dtoClass) {
		return this.createQueryDTO(dtoClass).uniqueResult();
	}

	/**
	 * Executa um <code>list</code> de <code>org.hibernate.Query</code>, buscando apenas o primeiro elemento
	 * 
	 * @param consulta
	 * @param dtoClass
	 * @return
	 */
	public Object getFirstResultDTO(final Class<?> dtoClass) {
		List<?> lista = this.getResultListDTO(dtoClass, 0, 1);
		if (lista != null && !lista.isEmpty()) {
			return lista.get(0);
		}
		return null;
	}
	
	public Integer getQueryTimeout() {
		return queryTimeout;
	}

	public void setQueryTimeout(Integer queryTimeout) {
		this.queryTimeout = queryTimeout;
	}	
	
}
