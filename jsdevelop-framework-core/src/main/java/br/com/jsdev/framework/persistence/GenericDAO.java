package br.com.jsdev.framework.persistence;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;

import org.hibernate.NonUniqueObjectException;

import br.com.jsdev.framework.persistence.exception.BusinessException;

public abstract class GenericDAO  {

	EntityManager entityManager;

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	public <E extends EntityAbstract> E find(Class<E> entityClass, Serializable id){
		return this.entityManager.find(entityClass, id);
	}
	
	protected QueryBuilder createQuery() {
		return new QueryBuilder(entityManager);
	}

	protected QueryBuilder createQuery(String sqlStmt) {
		QueryBuilder queryBuilder = new QueryBuilder(entityManager);
		queryBuilder.append(sqlStmt);

		return queryBuilder;
	}
	
	protected QueryBuilder createNativeQuery() {
		return new NativeQueryBuilder(entityManager);
	}

	protected QueryBuilder createNativeQuery(String sqlStmt) {
		QueryBuilder queryBuilder = new NativeQueryBuilder(entityManager);
		queryBuilder.append(sqlStmt);

		return queryBuilder;
	}
	
	public void persist(Object entity) throws RuntimeException {
		entityManager.persist(entity);
		entityManager.flush();	
	}
	
	public void update(EntityAbstract entity) {
		
		try {
			if (entityManager.find(entity.getClass(), entity.getId()) == null){
				throw new BusinessException("Registro não encontrado. Entidade: " + entity.getClass() + " Chave: " + entity.getId());
			}
				
			entityManager.find(entity.getClass(), entity.getId());
			
			entityManager.merge(entity);
			entityManager.flush();
		
		}catch (RuntimeException e) {
			throw tryCatchException(e);
		}
	}
	
	public void remove(EntityAbstract entity) {
		try {
			entityManager.remove(entityManager.contains(entity) ? entity : entityManager.getReference(entity.getClass(), entity.getId()));
			entityManager.flush();
			
		} catch (EntityNotFoundException e) {
			System.err.println("Exceção tratada: " + e.getMessage());
		} catch (RuntimeException e) {
			throw tryCatchException(e);
		}
	}	
	
	protected RuntimeException tryCatchException(RuntimeException exception) {

		Throwable cause = exception instanceof PersistenceException ? exception.getCause() : exception;
		
		if (cause instanceof BusinessException) {
			return (BusinessException)cause;
		}

		else if (cause instanceof NonUniqueObjectException) {
			return new BusinessException("Duplicidade de chave", exception);
		}
		
		else if (cause instanceof EntityNotFoundException) {
			return new BusinessException("Registro não encontrado", exception);
		}

		else  {
			return new BusinessException("Erro inesperado.", exception);
		}
	}	
	
	public RepositoryDomain getRepository(){
		return RepositoryDomain.PORTAL_REPO;
	}
}
