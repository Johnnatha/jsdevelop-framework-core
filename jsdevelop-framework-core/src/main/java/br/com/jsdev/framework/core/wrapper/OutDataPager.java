package br.com.jsdev.framework.core.wrapper;

import java.util.List;

public class OutDataPager<T> {

	private List<T> results;
	private Integer totalResults;
	
	public OutDataPager(Integer totalResults, List<T> results){
		this.results = results;
		this.totalResults = totalResults;
	}

	public List<T> getResults() {
		return results;
	}

	public void setResults(List<T> results) {
		this.results = results;
	}

	public Integer getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(Integer totalResults) {
		this.totalResults = totalResults;
	}
}
