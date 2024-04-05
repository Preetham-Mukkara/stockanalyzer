package stockanalyzer;

import stockanalyzer.server.BackendServer;


public class App {
  public static void main(String[] args) {
         try{
             BackendServer server = new BackendServer();
             server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}