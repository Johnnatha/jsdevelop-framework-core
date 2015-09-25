package br.com.jsdev.framework.persistence;

import java.beans.PropertyDescriptor;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.hibernate.HibernateException;
import org.hibernate.property.ChainedPropertyAccessor;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.PropertyAccessorFactory;
import org.hibernate.property.Setter;
import org.hibernate.transform.AliasedTupleSubsetResultTransformer;


public class HibernateResultTransformer extends AliasedTupleSubsetResultTransformer {
	private static final long serialVersionUID = 2637736734891835739L;

	private static final char NESTED_PROPERTY_SEPARATOR = '_';
	private static final int MIN_NUMBER_NESTED = 1;
	private PropertyUtilsBean util = new PropertyUtilsBean();

	/**
	 * Seta a property quando esta for nested
	 * 
	 * @param aliases
	 * @param tuple
	 */
	public void setProperty(Object result, String aliases, Object tuple) {
		String correctAlias = aliases.replace(NESTED_PROPERTY_SEPARATOR, '.');

		try {
			// Inicializa a property, caso seja nula
			this.instantiateNestedProperties(result, correctAlias);
			// Seta o valor da mesma
			util.setNestedProperty(result, correctAlias, tuple);
		} catch (Exception e) {
			throw new RuntimeException("Erro ao settar a property " + aliases);
		}

	}

	private final Class<?> resultClass;

	public HibernateResultTransformer(Class<?> resultClass) {
		if (resultClass == null) {
			throw new IllegalArgumentException("resultClass cannot be null");
		}
		this.resultClass = resultClass;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isTransformedValueATupleElement(String[] aliases, int tupleLength) {
		return false;
	}

	public Object transformTuple(Object[] tuple, String[] aliases) {
		Object result;

		try {

			result = resultClass.newInstance();

			PropertyAccessor propertyAccessor = new ChainedPropertyAccessor(new PropertyAccessor[] { PropertyAccessorFactory.getPropertyAccessor(resultClass, null),
					PropertyAccessorFactory.getPropertyAccessor("field") });

			for (int i = 0; i < aliases.length; i++) {
				if (aliases[i].indexOf(NESTED_PROPERTY_SEPARATOR) > 0) {
					setProperty(result, aliases[i], tuple[i]);
				} else {
					Setter setter = propertyAccessor.getSetter(resultClass, aliases[i]);
					if (setter != null) {
						setter.set(result, tuple[i], null);
					}
				}
			}
		} catch (InstantiationException e) {
			throw new HibernateException("Could not instantiate resultclass: " + resultClass.getName());
		} catch (IllegalAccessException e) {
			throw new HibernateException("Could not instantiate resultclass: " + resultClass.getName());
		}

		return result;
	}

	/**
	 * Instancia a property para para o <code>setNestedProperty</code> ficar null-safe.
	 * 
	 * @param obj
	 * @param fieldName
	 */
	private void instantiateNestedProperties(Object obj, String fieldName) {
		try {
			String[] fieldNames = fieldName.split("\\.");
			if (fieldNames.length > MIN_NUMBER_NESTED) {
				StringBuilder nestedProperty = new StringBuilder();
				for (int i = 0; i < fieldNames.length - 1; i++) {
					String nestedFieldName = fieldNames[i];
					if (i != 0) {
						nestedProperty.append('.');
					}
					nestedProperty.append(nestedFieldName);
					String theProperty = nestedProperty.toString();
					Object value = util.getProperty(obj, theProperty);
					if (value == null) {
						PropertyDescriptor propertyDescriptor = util.getPropertyDescriptor(obj, theProperty);
						Class<?> propertyType = propertyDescriptor.getPropertyType();
						Object newInstance = propertyType.newInstance();
						util.setProperty(obj, theProperty, newInstance);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Erro ao inicializar a property " + fieldName);
		}
	}

}