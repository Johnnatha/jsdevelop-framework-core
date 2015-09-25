package br.com.jsdev.framework.persistence;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import br.com.jsdev.framework.core.wrapper.InDataPager;
import br.com.jsdev.framework.core.wrapper.OutDataPager;

public class NativeQueryBuilder extends QueryBuilder {
	
	private static final String SELECT_COUNT_PART = "select count(#ALIAS#) ";

	public NativeQueryBuilder(EntityManager oEntityManager) {
		super(oEntityManager);
	}

	@Override
	public List<?> getResultList(Integer start, Integer max) {
		return this.createNativeQuery(start, max).getResultList();
	}

	@Override
	public List<?> getResultList() {
		return this.createNativeQuery().getResultList();
	}

	@Override
	public Object getSingleResult() {
		return this.createNativeQuery().getSingleResult();
	}

	@Override
	public Object getFirstResult() {
		List<?> lista = this.createNativeQuery(0, 1).getResultList();
		if (lista != null && !lista.isEmpty()) {
			return lista.get(0);
		}
		return null;
	}

	@Deprecated
	@Override
	public <T> T getFirstResult(Class<T> clazz) {
		return null;
	}

	@Override
	public List<?> getResultListDTO(Class<?> dtoClass, Integer start,
			Integer max) {
		return this.createNativeQuery(dtoClass, start, max).getResultList();
	}

	@Override
	public List<?> getResultListDTO(Class<?> dtoClass) {
		return this.createNativeQuery(dtoClass).list();
	}

	@Override
	public Object getSingleResultDTO(Class<?> dtoClass) {
		return this.createNativeQuery(dtoClass).uniqueResult();
	}
	
	@Override
	public OutDataPager<?> getPagerResultList(InDataPager inDataPager) {
		
		Integer first = inDataPager != null ? inDataPager.getFirstResult() : null;
		Integer maxResults = inDataPager != null ? inDataPager.getMaxResults() : null;
		
		return new OutDataPager(this.getResultCount(), (List<?>)this.getResultList(first,maxResults));
	}
	
	
	@Override
	public OutDataPager<?> getPagerResultListDTO(InDataPager inDataPager, final Class<?> dtoClass) {
		Integer first = inDataPager != null ? inDataPager.getFirstResult() : null;
		Integer maxResults = inDataPager != null ? inDataPager.getMaxResults() : null;
		
		return new OutDataPager(this.getResultCount(), this.getResultListDTO(dtoClass, first,maxResults));
	}

	@Override
	public Object getFirstResultDTO(Class<?> dtoClass) {
		List<?> lista = this.getResultListDTO(dtoClass, 0, 1);
		if (lista != null && !lista.isEmpty()) {
			return lista.get(0);
		}
		return null;
	}
	
}
