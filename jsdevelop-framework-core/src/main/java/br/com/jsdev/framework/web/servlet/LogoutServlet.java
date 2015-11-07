package br.com.jsdev.framework.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;

public class LogoutServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType(MediaType.TEXT_HTML);
		PrintWriter out = response.getWriter();
		request.getRequestDispatcher("/").include(request, response);
		HttpSession session = request.getSession();
		session.invalidate();
		out.close();
	}
}
