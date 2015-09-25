package br.com.jsdev.framework.web.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import br.com.jsdev.framework.web.servlet.config.SSOConfig;
import br.com.jsdev.framework.web.servlet.config.UserConfig;
import br.com.jsdev.framework.web.servlet.config.WebConfig;

public class WebListener implements ServletContextListener, ServletRequestListener {

	public void contextInitialized(ServletContextEvent sce) {
		final ServletContext context = sce.getServletContext();

		final SSOConfig ssoConfig = new SSOConfig();
		ssoConfig.setHeaderParam(StringUtils.defaultIfBlank(context.getInitParameter("jsdev.sso_header_param"), "oam_remote_user"));
		ssoConfig.setMode(SSOConfig.Mode.modeOf(context.getInitParameter("jsdev.sso_mode")));
		ssoConfig.setLogoutUrl(context.getInitParameter("jsdev.sso_logout_url"));
		
		
		final WebConfig webConfig = new WebConfig();
		webConfig.setMode(WebConfig.Mode.modeOf(context.getInitParameter("jsdev.mode")));
		webConfig.setProjectVersion(context.getInitParameter("jsdev.project_version"));
		webConfig.setRestfulUriPrefix(StringUtils.defaultIfBlank(context.getInitParameter("jsdev.restful_uri_prefix"), "rest"));
		webConfig.setResourceUriPrefix("static");
		webConfig.setWebsocketUriPrefix(StringUtils.defaultIfBlank(context.getInitParameter("jsdev.websocket_uri_prefix"), "websocket"));

		context.setAttribute(SSOConfig.class.getName(), ssoConfig);
		context.setAttribute(WebConfig.class.getName(), webConfig);
	}

	public void contextDestroyed(ServletContextEvent sce) {
		final ServletContext servletContext = sce.getServletContext();
		servletContext.removeAttribute(SSOConfig.class.getName());
		servletContext.removeAttribute(WebConfig.class.getName());
	}
	
	
	public void requestInitialized(ServletRequestEvent sre) {
		final ServletContext context = sre.getServletContext();
		final HttpServletRequest httpRequest = (HttpServletRequest) sre.getServletRequest();
		final SSOConfig ssoConfig = (SSOConfig) context.getAttribute(SSOConfig.class.getName());
		
		final WebContext webContext = new WebContext(context, httpRequest);
		WebContext.setCurrentInstance(webContext);
		
		if(ssoConfig.getMode() != SSOConfig.Mode.NONE && StringUtils.isNotBlank(httpRequest.getHeader(ssoConfig.getHeaderParam()))){
			final UserConfig userConfig = new UserConfig();
			userConfig.setUsername(httpRequest.getHeader(ssoConfig.getHeaderParam()));
			webContext.setUserConfig(userConfig);
		}
	} 	
	
	public void requestDestroyed(ServletRequestEvent sre) {
		WebContext.setCurrentInstance(null);
	}

}