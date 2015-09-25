package br.com.jsdev.framework.web.servlet.config;


public class WebConfig {

	private Mode mode;

	private String projectVersion;

	private String restfulUriPrefix;

	private String websocketUriPrefix;

	private String resourceUriPrefix;

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public String getProjectVersion() {
		return projectVersion;
	}

	public void setProjectVersion(String projectVersion) {
		this.projectVersion = projectVersion;
	}

	public String getRestfulUriPrefix() {
		return restfulUriPrefix;
	}

	public void setRestfulUriPrefix(String restfulUriPrefix) {
		this.restfulUriPrefix = restfulUriPrefix;
	}

	public String getWebsocketUriPrefix() {
		return websocketUriPrefix;
	}

	public void setWebsocketUriPrefix(String websocketUriPrefix) {
		this.websocketUriPrefix = websocketUriPrefix;
	}

	public String getResourceUriPrefix() {
		return resourceUriPrefix;
	}

	public void setResourceUriPrefix(String resourceUriPrefix) {
		this.resourceUriPrefix = resourceUriPrefix;
	}

	public static enum Mode {

		DEVELOPMENT, PRODUCTION;

		
		public static Mode modeOf(String value){
			try{
				return value != null ? Mode.valueOf(value.toUpperCase()) : DEVELOPMENT;
			
			}catch(IllegalArgumentException e){
				return DEVELOPMENT;
			}			
		}
	}

}
