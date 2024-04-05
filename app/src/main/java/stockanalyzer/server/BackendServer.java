package stockanalyzer.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class BackendServer {
    public void start() throws Exception {
        try{
            Server server = new Server(8080); // Port number

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");

            context.addServlet(Hello.class, "/hello");
            ServletHolder webSocketServletHolder = new ServletHolder(new StockDataWebSocketHandler());
            context.addServlet(webSocketServletHolder, "/ws");

            server.setHandler(context);

            // Start Jetty server.
            server.start();

            System.out.println("Server is started and running!");

            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
