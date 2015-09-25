package br.com.jsdev.framework.web.servlet.config;


public class SSOConfig {

	private Mode mode;
	
	private String headerParam;
	
	private String logoutUrl;

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public String getHeaderParam() {
		return headerParam;
	}

	public void setHeaderParam(String headerParam) {
		this.headerParam = headerParam;
	}

	public String getLogoutUrl() {
		return logoutUrl;
	}

	public void setLogoutUrl(String logoutUrl) {
		this.logoutUrl = logoutUrl;
	}

	public static enum Mode {
		
		REQUIRED, OPTIONAL, NONE;
		
		public static Mode modeOf(String value){
			try{
				return value != null ? Mode.valueOf(value.toUpperCase()) : NONE;
			
			}catch(IllegalArgumentException e){
				return NONE;
			}			
		}
		
	}

}