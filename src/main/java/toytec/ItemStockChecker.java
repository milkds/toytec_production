package toytec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class ItemStockChecker {
    private static final Logger logger = LogManager.getLogger(ItemStockChecker.class.getName());
    private ToyItem item;

    public ToyItem getItem() {
        return item;
    }

    public ItemStockChecker(ToyItem item) {
        this.item = item;
    }


    public boolean stockChangeDetected() {
        String itemLink = item.getItemLink();
        Document doc;
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
}
