package stockanalyzer.stock;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class StockData {
    private final int POLYGON_FREE_TIER_MAX_REQUESTS = 5;
    private final int POLYGON_WAIT_TIME = 60000;
    private final String polygonApiKey;
    private int requestsMade;
    private HashMap<String,GroupedStockData> stockDataMap;
    private OkHttpClient client;
    private final int POLYGON_HISTORICAL_DATA_YEAR_LIMIT = 2;
    private final int POLYGON_BUFFER_DAYS = 1;
    public StockData() {
        this.requestsMade = 0;
        this.client = new OkHttpClient();
        Dotenv dotenv = Dotenv.load();
        this.polygonApiKey = dotenv.get("POLYGON_API_KEY");
        this.stockDataMap = new HashMap<>();
    }
    private void freeTierCheck(){
        if(requestsMade == POLYGON_FREE_TIER_MAX_REQUESTS) {
            try {
                Thread.sleep(POLYGON_WAIT_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            requestsMade = 0;
        }
    }

    public String printMyData(Set<String> stocks){
        try {
            StringBuilder stockData = new StringBuilder();
            for(String stock : stocks) {
                GroupedStockData data = stockDataMap.get(stock);
                if(data == null) {
                    if(Objects.equals(stock, "META")){
                        // META is the new name for Facebook
                        data = stockDataMap.get("FB");
                    }
                    else {
                        stockData.append("\nStock: ").append(stock).append(" not found");
                        continue;
                    }
                }
                stockData.append("\n");
                stockData.append("Stock: ").append(data.getStock());
                stockData.append(", Open: ").append(data.getOpen());
                stockData.append(", Close: ").append(data.getClose());
                stockData.append(", High: ").append(data.getHigh());
                stockData.append(", Low: ").append(data.getLow());
            }
            return stockData.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public String getDailyOpenCloseInfo(String stock, String date) throws Exception {
            //The Polygon API URL for the open-close endpoint
            Request request = new Request.Builder()
                    .url("https://api.polygon.io/v1/open-close/"+stock+"/"+date+"?apiKey="+polygonApiKey)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                requestsMade++;
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                if(response.body() == null) throw new IOException("Response body is null");
                return response.body().string();
            }
    }

    public void getAggregateData(String startDate, String endDate, String stock){
        Request request = new Request.Builder()
                .url("https://api.polygon.io/v2/aggs/ticker/"+stock+"/range/1/day/"+startDate+"/"+endDate+"?apiKey="+polygonApiKey)
                .build();
        freeTierCheck();
        try (Response response = client.newCall(request).execute()) {
            requestsMade++;
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            if(response.body() == null) throw new IOException("Response body is null");
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(response.body().string(), JsonObject.class);
            JsonArray jsonArray = jsonObject.getAsJsonArray("results");
            String open = null, close = null;
            double high = Integer.MIN_VALUE,low = Integer.MAX_VALUE;
            for(int i = 0; i < jsonArray.size(); i++) {
                JsonElement element = jsonArray.get(i);
                JsonObject stockData = element.getAsJsonObject();
                if (i == 0) open = stockData.get("o").getAsString();
                if (i == jsonArray.size() - 1) close = stockData.get("c").getAsString();
                if(stockData.get("h").getAsDouble() > high) high = stockData.get("h").getAsDouble();
                if(stockData.get("l").getAsDouble() < low) low = stockData.get("l").getAsDouble();
            }
            stockDataMap.put(stock, new GroupedStockData(stock, String.valueOf(high), String.valueOf(low), open, close));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadData(Set<String> firstSet, Set<String> secondSet) {
        try {
            String startDate = java.time.LocalDate.now().minusYears(POLYGON_HISTORICAL_DATA_YEAR_LIMIT).plusDays(POLYGON_BUFFER_DAYS).toString();
            String endDate = java.time.LocalDate.now().minusDays(POLYGON_BUFFER_DAYS).toString();
            for(String stock : firstSet) {
                getAggregateData(startDate,endDate, stock);
            }
            for(String stock : secondSet) {
                getAggregateData(startDate, endDate, stock);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[] getDailyGroupedData(String date) throws Exception {
        Request request = new Request.Builder()
                .url("https://api.polygon.io/v2/aggs/grouped/locale/us/market/stocks/" + date + "?apiKey=" + polygonApiKey)
                .build();
        freeTierCheck();
        try (Response response = client.newCall(request).execute()) {
            requestsMade++;
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            if (response.body() == null) throw new IOException("Response body is null");
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(response.body().string(), JsonObject.class);

            JsonArray jsonArray = jsonObject.getAsJsonArray("results");
            String[] stringArray = new String[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); i++) {
                stringArray[i] = jsonArray.get(i).getAsString();
            }
            return stringArray;
        }
    }

    public PortfolioAnnualizedData calculatePerformance(HashMap<String,Double> stocks) {
        double averageAnnualizedReturn = 0;
        double min = Integer.MAX_VALUE;
        double max = Integer.MIN_VALUE;
        String maxStock = "";
        String minStock = "";
        double maxReturn = 0;
        for(String stock : stocks.keySet()) {
            GroupedStockData data = stockDataMap.get(stock);
            if (data == null) {
                System.out.println("\nStock: " + stock + " not found");
                if (Objects.equals(stock, "META")) {
                    // META is the new name for Facebook
                    data = stockDataMap.get("FB");
                } else {
                    continue;
                }
            }
            double open = Double.parseDouble(data.getOpen());
            double close = Double.parseDouble(data.getClose());
            double high = Double.parseDouble(data.getHigh());
            double low = Double.parseDouble(data.getLow());
            double weight = stocks.get(stock);
            double totalReturn = ((close - open) / open);
            maxReturn += ((high - low) / low) * 100;
            double annualizedReturn =  (Math.pow(1 + totalReturn, 1.0 / POLYGON_HISTORICAL_DATA_YEAR_LIMIT) - 1)*100;
            averageAnnualizedReturn += annualizedReturn*weight;
            if (annualizedReturn < min) {
                min = annualizedReturn;
                minStock = stock;
            }
            if (annualizedReturn > max) {
                max = annualizedReturn;
                maxStock = stock;
            }
        }
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.DOWN);
        averageAnnualizedReturn = Double.parseDouble(df.format(averageAnnualizedReturn));
        maxReturn = Double.parseDouble(df.format(maxReturn));
        max = Double.parseDouble(df.format(max));
        min = Double.parseDouble(df.format(min));
        PortfolioAnnualizedData portfolioAnnualizedData = new PortfolioAnnualizedData(averageAnnualizedReturn,0,0);
        portfolioAnnualizedData.setMaxReturnStock(maxStock,max);
        portfolioAnnualizedData.setMinReturnStock(minStock,min);
        portfolioAnnualizedData.setMostProfitPossible(maxReturn);
        return portfolioAnnualizedData;
    }

    private String compareStocks(HashMap<String,Double> stockMapOne, HashMap<String,Double> stockMapTwo) {
        try {
            StockData stockData = new StockData();
            stockData.loadData(stockMapOne.keySet(), stockMapTwo.keySet());
            LocalDate today = LocalDate.now();
            PortfolioAnnualizedData fidelityData = stockData.calculatePerformance(stockMapOne);
            PortfolioAnnualizedData magnificentSevenData = stockData.calculatePerformance(stockMapTwo);
            double fidelityAverageAnnualizedReturn = fidelityData.getAnnualizedReturn();
            double magnificentSevenAverageAnnualizedReturn = magnificentSevenData.getAnnualizedReturn();
            String message =
                    "\nPast 2 years average annualized return from " + today.minusYears(2).plusDays(1) + " to " + today.minusDays(1) + ":" +
                            "\nMy Fidelity Portfolio: " + fidelityAverageAnnualizedReturn + "%" +
                            "\nBest Performer: " + fidelityData.getMaxReturnStock()+ " at " + fidelityData.getMaxReturn() + "%"+
                            "\nWorst Performer: " + fidelityData.getMinReturnStock() + " at " + fidelityData.getMinReturn() + "%" +
                            "\nTheoretical Max Profit: " + fidelityData.getMostProfitPossible() + "%" +
                            "\nMagnificient Seven: " + magnificentSevenAverageAnnualizedReturn + "%" +
                            "\nBest Performer: " + magnificentSevenData.getMaxReturnStock() + " at " + magnificentSevenData.getMaxReturn() + "%"+
                            "\nWorst Performer: " + magnificentSevenData.getMinReturnStock() + " at " + magnificentSevenData.getMinReturn()  + "%"+
                            "\nTheoretical Max Profit: " + magnificentSevenData.getMostProfitPossible() + "%";
            return message;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized String getMessage(){
        Double EQUAL_WEIGHT_FOR_7_STOCKS = 1.0/7.0;
        HashMap<String,Double> myFidelityPortfolio = new HashMap<>(Map.of("VTI", 0.55, "AVDV", 0.05, "AVUV", 0.2, "SCHE", 0.05, "VXUS", 0.15));
        HashMap<String,Double> magnificentSeven = new HashMap<>(Map.of("AAPL",EQUAL_WEIGHT_FOR_7_STOCKS,"AMZN",EQUAL_WEIGHT_FOR_7_STOCKS,"GOOGL",EQUAL_WEIGHT_FOR_7_STOCKS,"MSFT",EQUAL_WEIGHT_FOR_7_STOCKS,"TSLA",EQUAL_WEIGHT_FOR_7_STOCKS,"META",EQUAL_WEIGHT_FOR_7_STOCKS,  "NVDA",EQUAL_WEIGHT_FOR_7_STOCKS));
        //This comparison assumes that each stock is equally weighted in each portfolio
        return compareStocks(myFidelityPortfolio,magnificentSeven);
    }
}
