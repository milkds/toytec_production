package toytec;

import org.hibernate.Session;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Statistics {

    private Session session;

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
        printTotals();
        printTime();
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
        statisticsKeeper.append(Timestamp.from(start));
        statisticsKeeper.append(System.lineSeparator());
        statisticsKeeper.append("Parse finished at: ");
        statisticsKeeper.append(Timestamp.from(finish));
        statisticsKeeper.append(System.lineSeparator());
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
            v.forEach(this::appendItem);
            appendVisualSep();
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
        statisticsKeeper.append("--------------------------------------------------------------------");
        statisticsKeeper.append(System.lineSeparator());
    }

    private void appendItem(ToyItem item){
        statisticsKeeper.append("SKU: ");
        statisticsKeeper.append(item.getSku());
        statisticsKeeper.append(". Item Name: ");
        statisticsKeeper.append(item.getItemName());
        statisticsKeeper.append(".  Link: ");
        statisticsKeeper.append(item.getItemLink());;
        statisticsKeeper.append(System.lineSeparator());
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
}
