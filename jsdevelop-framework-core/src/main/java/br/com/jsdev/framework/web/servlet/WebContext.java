package br.com.jsdev.framework.web.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import br.com.jsdev.framework.web.servlet.config.SSOConfig;
import br.com.jsdev.framework.web.servlet.config.UserConfig;
import br.com.jsdev.framework.web.servlet.config.WebConfig;

public class WebContext {
	 
	private static ThreadLocal<WebContext> instance = new ThreadLocal<WebContext>();
	
	private ServletContext context;
	
	private HttpServletRequest request;
	
	private WebConfig webConfig;
	
	private SSOConfig SSOConfig;
	
	private UserConfig userConfig;
		
	public WebContext(ServletContext context, HttpServletRequest request) {
		this.context = context;
		this.request = request;
		this.webConfig = (WebConfig) context.getAttribute(WebConfig.class.getName());
		this.SSOConfig = (SSOConfig) context.getAttribute(SSOConfig.class.getName());
	}	
	
	public static ThreadLocal<WebContext> getInstance() {
		return instance;
	}

	public static void setInstance(ThreadLocal<WebContext> instance) {
		WebContext.instance = instance;
	}

	public ServletContext getContext() {
		return context;
	}

	public void setContext(ServletContext context) {
		this.context = context;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public WebConfig getWebConfig() {
		return webConfig;
	}

	public void setWebConfig(WebConfig webConfig) {
		this.webConfig = webConfig;
	}

	public SSOConfig getSSOConfig() {
		return SSOConfig;
	}
	
	public void setSSOConfig(SSOConfig sSOConfig) {
		SSOConfig = sSOConfig;
	}

	public UserConfig getUserConfig() {
		return userConfig;
	}

	public void setUserConfig(UserConfig userConfig) {
		this.userConfig = userConfig;
	}

	public static WebContext getCurrentInstance() {
		 return instance.get();
	}
	
	public static void setCurrentInstance(WebContext context) {
		if (context == null) {
			instance.remove();
		} else {
			instance.set(context);
		}
	}
	
}