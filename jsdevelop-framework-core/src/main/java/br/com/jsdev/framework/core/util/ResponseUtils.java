package br.com.jsdev.framework.core.util;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.math.NumberUtils;

import br.com.jsdev.framework.core.wrapper.InDataPager;
import br.com.jsdev.framework.core.wrapper.OutDataPager;

public class ResponseUtils {
	
	public static ResponseBuilder getPagerResponseBuilder(@SuppressWarnings("rawtypes") OutDataPager response, InDataPager request) {
		
		Integer limit = request.getMaxResults();
		Integer page =  (request.getFirstResult() / request.getMaxResults()) + 1;
		
		final int pages = (int) (response.getTotalResults() / limit);

		return Response
				.status(javax.ws.rs.core.Response.Status.OK)
				.header("x-jsdev-meta-total-count", response.getTotalResults())
				.header("x-jsdev-meta-total-pages", response.getTotalResults() % limit == 0 ? pages : pages+1)
				.header("x-jsdev-meta-current-limit", limit)
				.header("x-jsdev-meta-current-page", page);
	}

	public static Response buildPager(@SuppressWarnings("rawtypes") OutDataPager response, InDataPager request) {
		return getPagerResponseBuilder(response, request).entity(response.getResults()).build();
	}
	
	public static InDataPager parsePager(UriInfo info){
		
		InDataPager inDataPager = new InDataPager();
		
		try {			
			final Integer limit = NumberUtils.toInt(info.getQueryParameters().getFirst("limit"), 10);
			final Integer page =  Math.max(NumberUtils.toInt(info.getQueryParameters().getFirst("page"), 1), 1);
						
			inDataPager.setFirstResult((page - 1) * limit);
			inDataPager.setMaxResults(limit);			
			
			return inDataPager;
		
		}catch (Exception e) {
			throw new WebApplicationException(e, javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
}