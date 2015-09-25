package br.com.jsdev.framework.persistence;

import javax.persistence.EntityManager;

public abstract class BusinessAbstract {
	
	EntityManager entityManagerPortal;
	EntityManager entityManagerEndeca;

	public <D extends GenericDAO> D getDAO(Class<D> daoClass) {
		try {
			D dao = daoClass.newInstance();	
			
			switch (dao.getRepository()) {
			case PORTAL_REPO:
				dao.setEntityManager(entityManagerPortal);
				break;
				
			case ENDECA_REPO:
				dao.setEntityManager(entityManagerEndeca);
				break;	

			default:
				throw new RuntimeException("Não foi possível localizar repositório: " + dao.getRepository());
			}
			return dao;
		} catch (Exception e) {
			throw new RuntimeException("Não foi possível instanciar a classe: " + daoClass, e);
		}
	}
	
	public void setEntityManagerPortal(EntityManager entityManagerPortal) {
		this.entityManagerPortal = entityManagerPortal;
	}


	public void setEntityManagerEndeca(EntityManager entityManagerEndeca) {
		this.entityManagerEndeca = entityManagerEndeca;
	}
}
