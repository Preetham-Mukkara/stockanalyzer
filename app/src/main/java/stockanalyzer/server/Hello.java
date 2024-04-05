package stockanalyzer.server;

import java.io.IOException;

public class Hello extends javax.servlet.http.HttpServlet {

    protected void doGet(javax.servlet.http.HttpServletRequest request,
                         javax.servlet.http.HttpServletResponse response)
            throws javax.servlet.ServletException, IOException {
        // Allow requests from any origin
        response.setHeader("Access-Control-Allow-Origin", "*");

        // Allow specific HTTP methods
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, PATCH, DELETE");

        // Allow specific headers
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        response.setContentType("text/plain");
        response.setStatus(javax.servlet.http.HttpServletResponse.SC_OK);
        response.getWriter().println("Hello from the Backend!");
    }
}
