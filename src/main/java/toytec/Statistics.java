package toytec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

public class Statistics {

    private Session session;
    private static final Logger logger = LogManager.getLogger(Statistics.class.getName());

    private long totalItemsQuantityBeforeCheck;
    private long totalItemsQuantityAfterCheck;

    private Instant start;
    private Instant finish;

    private Map<String, Long> categoryStartMap;
    private Map<String, Long> categoryFinishMap;

    private List<ToyItem> addedItems = new ArrayList<>();
    private List<ToyItem> deletedItems = new ArrayList<>();

    private StringBuilder statisticsKeeper = new StringBuilder();

    private Map<String, List<ToyItem>> addedItemsMap;
    private Map<String, List<ToyItem>> deletedItemsMap;

    private List<PriceChangeKeeper> changedPrices = new ArrayList<>();



    public Statistics(Session session) {
        this.session = session;
        init(session);
    }

    private void init(Session session) {
        start = Instant.now();
        totalItemsQuantityBeforeCheck =  ToyStatisticsDAO.getTotalItemsQty(session);
        categoryStartMap = getCategoryMap();
    }

    private Map<String,Long> getCategoryMap() {
        Map<String, Long> result = new HashMap<>();
        List<String> categoryNames = ToyStatisticsDAO.getCategoryNames(session);
        categoryNames.forEach(catName->{
            Long itemQty = ToyStatisticsDAO.getItemsQuantityByCategory(session,catName);
            result.put(catName, itemQty);
        });

        return result;
    }

    public StringBuilder showStatistics() {
        fixateResults();
        printStatistics();
        System.out.println(statisticsKeeper.toString());

        return statisticsKeeper;
    }

    private void fixateResults() {
        finish = Instant.now();
        sortAddedItems();
        sortDeletedItems();
        totalItemsQuantityAfterCheck = ToyStatisticsDAO.getTotalItemsQty(session);
        categoryFinishMap = getCategoryMap();
    }

    private void sortDeletedItems() {
        deletedItemsMap = sortItems(deletedItems);
    }

    private void sortAddedItems() {
        addedItemsMap = sortItems(addedItems);
    }

    private Map<String, List<ToyItem>> sortItems(List<ToyItem> items){
        Map<String, List<ToyItem>> result = new HashMap<>();
        items.forEach(item->{
            String categoryName = item.getItemCategory();
            List<ToyItem> itemsFromCategory = new ArrayList<>();
            if (result.containsKey(categoryName)){
                itemsFromCategory = result.get(categoryName);
                itemsFromCategory.add(item);
            }
            else {
                itemsFromCategory.add(item);
                result.put(categoryName, itemsFromCategory);
            }
        });
        return result;
    }

    private void printStatistics() {
        printChangesByCategories();
        printChangesByItems();
        printPriceChanges();
        printTotals();
        printTime();
    }

    private void printPriceChanges() {
        if (changedPrices.size()>0){
            statisticsKeeper.append("Prices changed:");
            changedPrices.forEach(priceChangeKeeper -> {
                ToyItem item = priceChangeKeeper.getUpdatedItem();
                statisticsKeeper.append(System.lineSeparator());
                statisticsKeeper.append("------------------------------");
                statisticsKeeper.append(System.lineSeparator());
                appendItem(item);
                BigDecimal newPriceFrom = item.getPriceFrom();
                BigDecimal oldPriceFrom = priceChangeKeeper.getOldPriceFrom();
                statisticsKeeper.append(System.lineSeparator());
                statisticsKeeper.append("Old price from: ");
                statisticsKeeper.append(oldPriceFrom);
                statisticsKeeper.append("$. New price from: ");
                statisticsKeeper.append(newPriceFrom);
                statisticsKeeper.append("$. Difference from: ");
                statisticsKeeper.append(newPriceFrom.subtract(oldPriceFrom));
                statisticsKeeper.append("$.");
                statisticsKeeper.append(System.lineSeparator());

                BigDecimal newPriceTo = item.getPriceTo();
                BigDecimal oldPriceTo = priceChangeKeeper.getOldPriceTo();
                statisticsKeeper.append("Old price __to: ");
                statisticsKeeper.append(oldPriceTo);
                statisticsKeeper.append("$. New price __to: ");
                statisticsKeeper.append(newPriceTo);
                statisticsKeeper.append("$. Difference __to: ");
                try {
                    statisticsKeeper.append(newPriceTo.subtract(oldPriceTo));
                }
                catch (NullPointerException e){
                    logger.error("NULL PRICE - check item! " + item.getItemLink());
                }

                statisticsKeeper.append("$.");
                statisticsKeeper.append(System.lineSeparator());
                statisticsKeeper.append("------------------------------");
                statisticsKeeper.append(System.lineSeparator());
                statisticsKeeper.append(System.lineSeparator());
            });

            appendVisualSep();
        }
    }

    private void printChangesByCategories() {
        categoryFinishMap.forEach((k, v)->{
            statisticsKeeper.append("Category ");
            statisticsKeeper.append(k);
            statisticsKeeper.append(" : Items before check: ");
            if (categoryStartMap.containsKey(k)){
                statisticsKeeper.append(categoryStartMap.get(k));
            }
            else {
                statisticsKeeper.append("0");
            }
            statisticsKeeper.append(". Added: ");
            int added = 0;
            if (addedItemsMap.containsKey(k)){
                added = addedItemsMap.get(k).size();
            }
            statisticsKeeper.append(added);
            statisticsKeeper.append(". Deleted: ");
            int deleted = 0;
            if (deletedItemsMap.containsKey(k)){
                deleted = deletedItemsMap.get(k).size();
            }
            statisticsKeeper.append(deleted);
            statisticsKeeper.append(System.lineSeparator());
        });

        statisticsKeeper.append(System.lineSeparator());
    }

    private void printTime() {
        statisticsKeeper.append("Parse started at: ");
        statisticsKeeper.append(formatTime(start));
        statisticsKeeper.append(System.lineSeparator());
        statisticsKeeper.append("Parse finished at: ");
        statisticsKeeper.append(formatTime(finish));
        statisticsKeeper.append(System.lineSeparator());
    }

    public static String formatTime(Instant instant) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofLocalizedDateTime( FormatStyle.MEDIUM )
                        .withLocale( Locale.UK )
                        .withZone( ZoneId.systemDefault() );

        return formatter.format(instant);
    }

    private void printTotals() {
        statisticsKeeper.append("Total Items before check: ");
        statisticsKeeper.append(totalItemsQuantityBeforeCheck);
        statisticsKeeper.append(System.lineSeparator());
        statisticsKeeper.append("Total Items after check: ");
        statisticsKeeper.append(totalItemsQuantityAfterCheck);
        statisticsKeeper.append(System.lineSeparator());
        statisticsKeeper.append("Total Elapsed time in minutes: ");
        statisticsKeeper.append(Duration.between(start, finish).toMinutes());
        statisticsKeeper.append(System.lineSeparator());
    }

    private void printChangesByItems() {
        addedItemsMap.forEach((k,v)->{
            statisticsKeeper.append("Added ");
            appendCategory(k);
            v.forEach(item -> {
                statisticsKeeper.append("------------------------------");
                statisticsKeeper.append(System.lineSeparator());
                appendItem(item);
                statisticsKeeper.append(System.lineSeparator());
                statisticsKeeper.append("------------------------------");
                statisticsKeeper.append(System.lineSeparator());
            });
           /* v.forEach(this::appendItem);
            appendVisualSep();*/
            if (deletedItemsMap.containsKey(k)){
                statisticsKeeper.append("Deleted ");
                appendCategory(k);
                deletedItemsMap.get(k).forEach(this::appendItem);
                appendVisualSep();
            }
        });
        deletedItemsMap.forEach((k,v)->{
            if (!addedItemsMap.containsKey(k)){
                statisticsKeeper.append("Deleted ");
                appendCategory(k);
                v.forEach(this::appendItem);
                appendVisualSep();
            }
        });
    }

    private void appendVisualSep() {
        statisticsKeeper.append(System.lineSeparator());
        statisticsKeeper.append(System.lineSeparator());
        statisticsKeeper.append("------------------------------");
        statisticsKeeper.append(System.lineSeparator());
    }

    private void appendItem(ToyItem item){
        statisticsKeeper.append("SKU: ");
        statisticsKeeper.append(item.getSku());
        statisticsKeeper.append(System.lineSeparator());
        statisticsKeeper.append(getCanadaSitelink(item.getSku()));
        statisticsKeeper.append(System.lineSeparator());
        statisticsKeeper.append(item.getItemName());
        statisticsKeeper.append(System.lineSeparator());
        statisticsKeeper.append(item.getItemLink());
        statisticsKeeper.append(System.lineSeparator());
    }

    private String getCanadaSitelink(String sku) {
        StringBuilder sb = new StringBuilder();
        sb.append("https://www.bilsteinlifts.com/?post_type=product&s=");
        sb.append(sku);
        sb.append("&asp_active=1&p_asid=1&p_asp_data=Y3VycmVudF9wYWdlX2lkPTk3Mjg4Jndvb19jdXJyZW5jeT1VU0QmcXRyYW5zbGF0ZV9sYW5nPTAmZmlsdGVyc19jaGFuZ2VkPTAmZmlsdGVyc19pbml0aWFsPTEmYXNwX2dlbiU1QiU1RD10aXRsZSZhc3BfZ2VuJTVCJTVEPWNvbnRlbnQmYXNwX2dlbiU1QiU1RD1leGNlcnB0JmN1c3RvbXNldCU1QiU1RD1wcm9kdWN0X3ZhcmlhdGlvbiZjdXN0b21zZXQlNUIlNUQ9cHJvZHVjdA==");

        return sb.toString();
    }

    private void appendCategory(String categoryName){
        statisticsKeeper.append(categoryName);
        statisticsKeeper.append(":");
        statisticsKeeper.append(System.lineSeparator());
    }

    public List<ToyItem> getAddedItems() {
        return addedItems;
    }
    public List<ToyItem> getDeletedItems() {
        return deletedItems;
    }
    public List<PriceChangeKeeper> getChangedPrices() {
        return changedPrices;
    }
    public Instant getFinish() {
        return finish;
    }
    public StringBuilder getStatisticsKeeper() {
        return statisticsKeeper;
    }
    public long getTotalItemsQuantityBeforeCheck() {
        return totalItemsQuantityBeforeCheck;
    }
    public long getTotalItemsQuantityAfterCheck() {
        return totalItemsQuantityAfterCheck;
    }


}
