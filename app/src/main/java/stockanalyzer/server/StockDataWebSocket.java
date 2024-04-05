package stockanalyzer.server;

import stockanalyzer.gmail.SendEmail;
import stockanalyzer.stock.StockData;
import org.eclipse.jetty.websocket.api.CloseStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@WebSocket
public class StockDataWebSocket {
    private static final StockData stockData = new StockData();
    private static final AtomicReference<String> data = new AtomicReference<>(null);
    private AtomicBoolean fetching = new AtomicBoolean(false); // Indicates an ongoing fetch operation

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Connected: " + session.getRemoteAddress().getHostString());
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.println("Closed: " + statusCode + " - " + reason);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        System.out.println("Received: " + message);

        try {
            if ("show".equals(message) || "sendEmail".equals(message)) {
                if (data.get() == null) {  // Check for no fetched data
                    if (fetching.compareAndSet(false, true)) {  // First thread initiates the fetching process
                        // Inform the user that data is being fetched - applies to first requester
                        session.getRemote().sendString("Fetching data, please wait...");

                        CompletableFuture.supplyAsync(stockData::getMessage)
                                .thenAccept(fetchedData -> {
                                    data.set(fetchedData);
                                    respondBasedOnMessage(session, message, fetchedData);  // Send newly fetched data once available
                                    fetching.set(false);  // Fetching complete
                                }).exceptionally(e -> {
                                    e.printStackTrace();
                                    return null;
                                });
                    } else {
                        // For subsequent requesters while fetching in progress
                        session.getRemote().sendString("Fetching data, please wait...");
                    }
                } else {
                    // If data already available, use it without fetching.
                    respondBasedOnMessage(session, message, data.get());
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            // Close connection if there's an error while sending messages.
            session.close(new CloseStatus(1011,"Internal Error"));
        }
    }

    private void respondBasedOnMessage(Session session, String message, String fetchedData) {
        // Make sure session is still open before attempting to send a message
        if (!session.isOpen()) return;
        try {
            switch (message) {
                case "show" -> session.getRemote().sendString(fetchedData);
                case "sendEmail" -> {
                    SendEmail.sendEmail(fetchedData);
                    session.getRemote().sendString("Email sent!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
