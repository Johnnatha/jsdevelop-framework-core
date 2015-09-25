package br.com.jsdev.framework.core.wrapper;

import java.io.Serializable;

public class InDataPager implements Serializable {

	private static final long serialVersionUID = 1L;
	private Integer firstResult;
	private Integer maxResults;

	public Integer getFirstResult() {
		return firstResult;
	}

	public void setFirstResult(Integer firstResult) {
		this.firstResult = firstResult;
	}

	public Integer getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
	}

}
