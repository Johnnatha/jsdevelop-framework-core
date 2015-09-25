package br.com.jsdev.framework.persistence;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.MappedSuperclass;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@MappedSuperclass
public abstract class EntityAbstract implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract Serializable getId();

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(Arrays.asList(this.getClass(), this.getId()));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof EntityAbstract) {
			return EqualsBuilder.reflectionEquals(this.getId(), ((EntityAbstract) obj).getId());
		}
		return false;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
