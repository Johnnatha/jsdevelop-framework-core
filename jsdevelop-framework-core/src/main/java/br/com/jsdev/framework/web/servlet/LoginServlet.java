package br.com.jsdev.framework.web.servlet;

import java.io.IOException;
import java.text.MessageFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginServlet extends HttpServlet{
	
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;
		
		final String username = httpRequest.getParameter("j_username");
		final String password = httpRequest.getParameter("j_password");
		
		try{
			httpResponse.sendRedirect(getSecurityCheckURL(username, password));
		}catch(Exception e) {
			httpResponse.reset();
			e.printStackTrace();
		}
	}
	
	private static String getSecurityCheckURL(final String username, final String password){
		final String template = "./j_security_check?j_username={0}&j_password={1}";
		return MessageFormat.format(template, username, password);
	}
}
