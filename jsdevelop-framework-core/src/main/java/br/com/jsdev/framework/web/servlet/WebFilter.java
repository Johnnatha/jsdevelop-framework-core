package br.com.jsdev.framework.web.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import br.com.jsdev.framework.web.servlet.config.SSOConfig;
import br.com.jsdev.framework.web.servlet.config.UserConfig;
import br.com.jsdev.framework.web.servlet.config.WebConfig;

public class WebFilter implements Filter {

	private static final String SPA_USERNAME = "app-username";

	public void init(FilterConfig filterConfig) throws ServletException {	
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;
		
		final WebContext context = WebContext.getCurrentInstance();
		final SSOConfig ssoConfig = context.getSSOConfig();
		final UserConfig userConfig = context.getUserConfig();
		
		if(userConfig != null) 	{
			httpResponse.setHeader(SPA_USERNAME, userConfig.getUsername());
		}
		
		if(userConfig == null && ssoConfig.getMode() == SSOConfig.Mode.REQUIRED){
			httpResponse.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
			return;
		}
				
		if("true".equals(httpRequest.getHeader("spa-tracking"))){
			httpResponse.setStatus(200);
			return;
		}
		
		final String requestPath = httpRequest.getRequestURI().replaceFirst(httpRequest.getContextPath(), "");
		
		//Quando for acessado um restful e headers de usuários forem diferentes não autorizar
		//isso pode ocorrer quando em outra aba do browser o usuário faz logoff e login com outro usuário
/*		if(ssoConfig.getMode() != SSOConfig.Mode.NONE && isRestful(requestPath) && validateSpaUsername(httpRequest, userConfig, httpResponse)){
			httpResponse.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
			return;
		}*/
		
		//Quando tiver modo produção os fontes dos recursos não são acessíveis
		if(context.getWebConfig().getMode() == WebConfig.Mode.PRODUCTION && 
				(isResource(requestPath) && !isResourceWithVersion(requestPath))){
			httpResponse.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
			return;
		}
		
		
		if(context.getWebConfig().getMode() == WebConfig.Mode.DEVELOPMENT && isResource(requestPath)){
			httpResponse.setHeader("Pragma", "no-cache");
			httpResponse.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidatee");
		}
		
		
		if(isIndex(requestPath) || isResource(requestPath) || isRestful(requestPath) || isWebsocket(requestPath) ){
			response.setCharacterEncoding("UTF-8");
			chain.doFilter(request, response);
			return;
		}

		httpResponse.sendRedirect(httpRequest.getContextPath() + "/#"+ requestPath + (httpRequest.getQueryString() != null ? "?" + httpRequest.getQueryString() : ""));
	}


	@SuppressWarnings("unused")
	private boolean validateSpaUsername(final HttpServletRequest httpRequest, final UserConfig userConfig, final HttpServletResponse httpResponse) throws UnsupportedEncodingException {
		String requestUserName = httpRequest.getHeader(SPA_USERNAME);
		// No caso de não haver header, testa cookies:
		if (requestUserName == null && httpRequest.getCookies() != null) {
			for (Cookie cookie : httpRequest.getCookies()) {
				if (SPA_USERNAME.equals(cookie.getName()) && cookie.getValue() != null) {
					// Se achou, 'queima' o cookie:
					requestUserName = URLDecoder.decode(cookie.getValue(), "UTF-8");
					cookie.setMaxAge(0);
					cookie.setPath(httpRequest.getContextPath());
					cookie.setValue("");
					httpResponse.addCookie(cookie);
					break;
				}
			}
		}

		return !StringUtils.equals(requestUserName, userConfig != null ? userConfig.getUsername() : null);
	}

	public void destroy() {	
	}
	
	private boolean isIndex(String requestPath){
		return requestPath.equals("/") || requestPath.equals("/index.html");
	}
	
	private boolean isResource(String requestPath){
		return requestPath.startsWith("/" + WebContext.getCurrentInstance().getWebConfig().getResourceUriPrefix() + "/"); 
	}
	
	private boolean isResourceWithVersion(String requestPath){
		return requestPath.startsWith("/" + WebContext.getCurrentInstance().getWebConfig().getResourceUriPrefix() + "/" + WebContext.getCurrentInstance().getWebConfig().getProjectVersion() + "/"); 
	}
	
	private boolean isRestful(String requestPath){
		return requestPath.startsWith("/" + WebContext.getCurrentInstance().getWebConfig().getRestfulUriPrefix() + "/"); 
	}
	
	private boolean isWebsocket(String requestPath){
		return requestPath.startsWith("/" + WebContext.getCurrentInstance().getWebConfig().getWebsocketUriPrefix() + "/"); 
	}
}