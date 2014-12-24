package org.sample.sendmessage;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Errors
 */
@WebServlet(
		description = "Error handler", 
		urlPatterns = { "/errors", "/not_found" },
		loadOnStartup = 1,
		name = "errors",
		displayName = "errors",
		initParams = { 
				@WebInitParam(name = "name", value = "errors")
		})
public class Errors extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Errors() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>jms sample</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("Something went wrong.");
            out.println("</body>");
            out.println("</html>");
        }
	}

}
