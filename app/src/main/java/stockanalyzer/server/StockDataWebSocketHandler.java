package stockanalyzer.server;

import org.eclipse.jetty.websocket.servlet.*;

// Define your web socket handler
public class StockDataWebSocketHandler extends WebSocketServlet {
    @Override
    public void configure(WebSocketServletFactory factory) {
        System.out.println("WebSocket configured");
        // Set the maximum idle timeout for connections in milliseconds
        factory.getPolicy().setIdleTimeout(300000); // 5 minute

        // Set the custom WebSocketCreator to handle WebSocket creation
        factory.setCreator(new BackendWebSocketCreator());
    }

    public static class BackendWebSocketCreator implements WebSocketCreator {
        @Override
        public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
            System.out.println("WebSocket created");
                return new StockDataWebSocket();
        }
    }
}
