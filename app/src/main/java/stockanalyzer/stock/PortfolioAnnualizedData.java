package stockanalyzer.stock;

public class PortfolioAnnualizedData {
    private double annualizedReturn;
    private String maxReturnStock;
    private double maxReturn;
    private String minReturnStock;
    private double minReturn;
    private double annualizedStandardDeviation;
    private double annualizedSharpeRatio;

    private double mostProfitPossible;
    public PortfolioAnnualizedData(double annualizedReturn,double annualizedStandardDeviation, double annualizedSharpeRatio) {
        this.annualizedReturn = annualizedReturn;
        this.annualizedStandardDeviation = annualizedStandardDeviation;
        this.annualizedSharpeRatio = annualizedSharpeRatio;
    }

    public void setMostProfitPossible(double mostProfitPossible) {
        this.mostProfitPossible = mostProfitPossible;
    }

    public double getMostProfitPossible() {
        return mostProfitPossible;
    }
    public void setMaxReturnStock(String maxReturnStock, double maxReturn) {
        this.maxReturnStock = maxReturnStock;
        this.maxReturn = maxReturn;
    }

    public void setMinReturnStock(String minReturnStock, double minReturn) {
        this.minReturnStock = minReturnStock;
        this.minReturn = minReturn;
    }

    public double getMaxReturn() {
        return maxReturn;
    }

    public double getMinReturn() {
        return minReturn;
    }

    public String getMaxReturnStock() {
        return maxReturnStock;
    }

    public String getMinReturnStock() {
        return minReturnStock;
    }
    public double getAnnualizedReturn() {
        return annualizedReturn;
    }

    public double getAnnualizedStandardDeviation() {
        return annualizedStandardDeviation;
    }

    public double getAnnualizedSharpeRatio() {
        return annualizedSharpeRatio;
    }
}
