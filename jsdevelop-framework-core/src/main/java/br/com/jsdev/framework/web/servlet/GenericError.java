package br.com.jsdev.framework.web.servlet;

import java.util.List;

public class GenericError {

	private String message;

	private String code;

	private String stackTrace;

	private List<ObjectError> errors;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public List<ObjectError> getErrors() {
		return errors;
	}
	
	public void setErrors(List<ObjectError> errors) {
		this.errors = errors;
	}

	public static class ObjectError {

		private String field;

		private String message;

		private String code;

		public String getField() {
			return field;
		}

		public void setField(String field) {
			this.field = field;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}
	}

}