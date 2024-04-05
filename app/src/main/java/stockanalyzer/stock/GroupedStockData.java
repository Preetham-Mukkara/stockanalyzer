package stockanalyzer.stock;

public class GroupedStockData {
    private String stock;
    private String high;

    private String low;
    private String open;
    private String close;

    public GroupedStockData(String stock, String high,String low, String open, String close) {
        this.stock = stock;
        this.low = low;
        this.high = high;
        this.open = open;
        this.close = close;
    }

    public String getStock() {
        return stock;
    }

    public String getHigh() {
        return high;
    }

    public String getOpen() {
        return open;
    }

    public String getClose() {
        return close;
    }

    public String getLow() {
        return low;
    }
}
