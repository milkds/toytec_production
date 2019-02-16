package toytec;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.math.BigDecimal;

public class ItemUpdatesChecker {
    private static final Logger logger = LogManager.getLogger(ItemUpdatesChecker.class.getName());
    private ToyItem item;
    private Statistics statistics;
    private Document doc;

    private WebDriver driver;

    public ItemUpdatesChecker(ToyItem item, WebDriver driver, Statistics statistics) {
        this.item = item;
        this.driver = driver;
        this.statistics = statistics;
    }


    public boolean stockChangeDetected() {
        String itemLink = item.getItemLink();
        try {
            doc = Jsoup.connect(itemLink).get();
        } catch (IOException e) {
            logger.info("Couldn't load item " + itemLink);
            return false;
        }

        Element stockSKUel = doc.getElementsByClass("product-info-stock-sku").first();
        if (stockSKUel==null){
            logger.info("Couldn't get stock block for item: " + itemLink);
            return false;
        }

        String availability = this.getAvailability(stockSKUel, itemLink);

        //if page contains configurable block - stock desc is expected in it
        Elements configurable = stockSKUel.getElementsByAttributeValue("id","availability-configurable");
        if (configurable.size()>0){
            stockSKUel = configurable.first();
        }
        
        String stockDesc = this.getStockDesc(stockSKUel);

        this.checkForUnexpectedInfo(stockSKUel);

        if (!availability.equals(item.getAvailability())||!stockDesc.equals(item.getStockDesc())){
            item.setAvailability(availability);
            item.setStockDesc(stockDesc);

            return true;
        }

        return false;
    }

    private void checkForUnexpectedInfo(Element stockSKUel) {
        Elements stockEls = stockSKUel.getElementsByTag("div");
        for (Element stockEl: stockEls){
            String checkEl = stockEl.attr("class");

            //stock info is located in stock sku section. Sku section starts from next attribute:
            if (checkEl.equals("product attribute sku")){
                break;
            }
            switch (checkEl){
                case "product-info-stock-sku": break;
                case "stock available": break;
                case "stock unavailable": break;
                case "product-availability-backorder": break;
                case "product-availability-in-stock": break;
                case "product-availability-out-of-stock": break;
                case "": break;
                default: logger.info("unexpected class attribute in stock section: "+checkEl+ " ;; " + item.getItemLink());
            }

        }
    }

    private String getStockDesc(Element stockSKUel) {
        String stockDesc;

        Element stockDescEl = stockSKUel.getElementsByClass("product-availability-backorder").first();
        if (stockDescEl!=null){
            stockDesc = stockDescEl.text();
        }
        else{
            Element inStockEl = stockSKUel.getElementsByClass("product-availability-in-stock").first();
            if (inStockEl!=null){
                stockDesc = inStockEl.text();
            }
            else {
                Element outStockEl = stockSKUel.getElementsByClass("product-availability-out-of-stock").first();
                if (outStockEl!=null){
                    stockDesc = outStockEl.text();
                }
                else {
                    stockDesc = "NO STOCK DESC";
                }
            }
        }

        return stockDesc;
    }

    private String getAvailability(Element stockSKUel, String itemLink) {
        Element availabilityEL = stockSKUel.getElementsByAttributeValueContaining("class", "stock available").first();
        if  (availabilityEL==null){
            availabilityEL = stockSKUel.getElementsByAttributeValueContaining("class", "stock unavailable").first();
            if  (availabilityEL==null){
                logger.info("No info about availability found for " + itemLink);
                return "NO STOCK AVAILABILITY INFO";
            }
        }

        return availabilityEL.text();
    }

    public WebDriver getDriver() {
        return driver;
    }
    public ToyItem getItem() {
        return item;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public boolean priceChangeDetected() {
        //for case if itemLink is not available any more.
        try {
            Element priceEl = doc.getElementsByClass("price").first();
        }
        catch (NullPointerException e){
            return false;
        }
        BigDecimal oldPriceFrom = item.getPriceFrom();
        BigDecimal oldPriceTo = item.getPriceTo();

        BigDecimal newPriceFrom = getNewPrice("from");
        BigDecimal newPriceTo = getNewPrice("to");
        if (oldPriceFrom.compareTo(newPriceFrom)==0&&oldPriceTo.compareTo(newPriceTo)==0){
            return false;
        }
        else {
            PriceChangeKeeper keeper = new PriceChangeKeeper();
            keeper.setOldPriceFrom(oldPriceFrom);
            keeper.setOldPriceTo(oldPriceTo);

            item.setPriceFrom(newPriceFrom);
            item.setPriceTo(newPriceTo);

            if (item.getOptions().size()>0){
                ToyUtil.updateOptions(driver, item);
            }
            keeper.setUpdatedItem(item);
            statistics.getChangedPrices().add(keeper);
        }

        return true;
    }

    private BigDecimal getNewPrice(String tagPart) {
        String price = "";
        Element priceEl;
        try {
            priceEl = doc.getElementsByClass("price-"+tagPart).first();
            priceEl = priceEl.getElementsByClass("price").first();
        }
        catch (NullPointerException e){
            priceEl = doc.getElementsByClass("price").first();
        }
        price = priceEl.text();
        price = StringUtils.substringAfter(price,"$");
        price = price.replaceAll(",", "");

        return new BigDecimal(price);
    }

}
