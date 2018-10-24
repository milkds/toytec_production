package toytec;

import org.hibernate.Session;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class LogStatistics {

    private Session session;

    private long totalItemsQuantityBeforeCheck;
    private long totalItemsQuantityAfterCheck;

    private long accessoriesItemQty;
    private long liftKitItemQty;
    private long frontSuspItemQty;
    private long lightItemQty;
    private long performanceItemQty;
    private long protectionItemQty;
    private long rearSuspItemQty;
    private long shocksItemQty;

    private List<ToyItem> addedItems;
    private List<ToyItem> deletedItems;

    private List<ToyItem> addedAccessories;
    private List<ToyItem> deletedAccessories;
    private List<ToyItem> addedLiftKit;
    private List<ToyItem> deletedLiftKit;
    private List<ToyItem> addedFrontSusp;
    private List<ToyItem> deletedFrontSusp;
    private List<ToyItem> addedLight;
    private List<ToyItem> deletedLight;
    private List<ToyItem> addedPerformance;
    private List<ToyItem> deletedPerformance;
    private List<ToyItem> addedProtection;
    private List<ToyItem> deletedPotection;
    private List<ToyItem> addedRearSusp;
    private List<ToyItem> deletedRearSusp;
    private List<ToyItem> addedShocks;
    private List<ToyItem> deletedShocks;

    private Instant start;
    private Instant finish;



    public LogStatistics(Session session) {
        this.session = session;
        init(session);
    }

    public void showStatistics() {
        fixResults();
        printStatistics();
    }

    private void printStatistics() {
        printCategories();
        printChangedItems();
        printTotalItems();
        printTime();
    }

    private void printTime() {
        System.out.println("Parse started at: " + Timestamp.from(start));
        System.out.println("Parse finished at: " + Timestamp.from(finish));
        System.out.println("Total Elapsed time in minutes: " + Duration.between(start, finish).toMinutes());
    }

    private void printTotalItems() {
        System.out.println("Total Items before check: " + totalItemsQuantityBeforeCheck);
        System.out.println("Total Items after check: " + totalItemsQuantityAfterCheck);
    }

    private void printChangedItems() {

        System.out.println("Added ACCESSORIES: ");
        for (ToyItem item: addedAccessories){
            System.out.println("SKU: "+item.getSku()+ " Item Name: " + item.getItemName()+ " Link: " + item.getItemLink());
        }
        printVisualBreak();
        System.out.println("Deleted ACCESSORIES: ");
        for (ToyItem item: deletedAccessories){
            System.out.println("SKU: "+item.getSku()+ " Item Name: " + item.getItemName()+ " Link: " + item.getItemLink());
        }
        printVisualBreak();
        System.out.println("Added COMPLETE LIFT KITS: ");
        for (ToyItem item: addedLiftKit){
            System.out.println("SKU: "+item.getSku()+ " Item Name: " + item.getItemName()+ " Link: " + item.getItemLink());
        }
        printVisualBreak();
        System.out.println("Deleted COMPLETE LIFT KITS: ");
        for (ToyItem item: deletedLiftKit){
            System.out.println("SKU: "+item.getSku()+ " Item Name: " + item.getItemName()+ " Link: " + item.getItemLink());
        }
        printVisualBreak();
        System.out.println("Added FRONT SUSPENSION: ");
        for (ToyItem item: addedFrontSusp){
            System.out.println("SKU: "+item.getSku()+ " Item Name: " + item.getItemName()+ " Link: " + item.getItemLink());
        }
        printVisualBreak();
        System.out.println("Deleted FRONT SUSPENSION: ");
        for (ToyItem item: deletedFrontSusp){
            System.out.println("SKU: "+item.getSku()+ " Item Name: " + item.getItemName()+ " Link: " + item.getItemLink());
        }
        printVisualBreak();
        System.out.println("Added LIGHTING: ");
        for (ToyItem item: addedLight){
            System.out.println("SKU: "+item.getSku()+ " Item Name: " + item.getItemName()+ " Link: " + item.getItemLink());
        }
        printVisualBreak();
        System.out.println("Deleted LIGHTING: ");
        for (ToyItem item: deletedLight){
            System.out.println("SKU: "+item.getSku()+ " Item Name: " + item.getItemName()+ " Link: " + item.getItemLink());
        }
        printVisualBreak();
        System.out.println("Added PERFORMANCE: ");
        for (ToyItem item: addedPerformance){
            System.out.println("SKU: "+item.getSku()+ " Item Name: " + item.getItemName()+ " Link: " + item.getItemLink());
        }
        printVisualBreak();
        System.out.println("Deleted PERFORMANCE: ");
        for (ToyItem item: deletedPerformance){
            System.out.println("SKU: "+item.getSku()+ " Item Name: " + item.getItemName()+ " Link: " + item.getItemLink());
        }
        printVisualBreak();
        System.out.println("Added PROTECTION & LOCKERS: ");
        for (ToyItem item: addedProtection){
            System.out.println("SKU: "+item.getSku()+ " Item Name: " + item.getItemName()+ " Link: " + item.getItemLink());
        }
        printVisualBreak();
        System.out.println("Deleted PROTECTION & LOCKERS: ");
        for (ToyItem item: deletedPotection){
            System.out.println("SKU: "+item.getSku()+ " Item Name: " + item.getItemName()+ " Link: " + item.getItemLink());
        }
        printVisualBreak();
        System.out.println("Added REAR SUSPENSION: ");
        for (ToyItem item: addedRearSusp){
            System.out.println("SKU: "+item.getSku()+ " Item Name: " + item.getItemName()+ " Link: " + item.getItemLink());
        }
        printVisualBreak();
        System.out.println("Deleted REAR SUSPENSION: ");
        for (ToyItem item: deletedRearSusp){
            System.out.println("SKU: "+item.getSku()+ " Item Name: " + item.getItemName()+ " Link: " + item.getItemLink());
        }
        printVisualBreak();
        System.out.println("Added SHOCKS: ");
        for (ToyItem item: addedShocks){
            System.out.println("SKU: "+item.getSku()+ " Item Name: " + item.getItemName()+ " Link: " + item.getItemLink());
        }
        printVisualBreak();
        System.out.println("Deleted SHOCKS: ");
        for (ToyItem item: deletedShocks){
            System.out.println("SKU: "+item.getSku()+ " Item Name: " + item.getItemName()+ " Link: " + item.getItemLink());
        }
    }

    private void printVisualBreak() {
        System.out.println();
        System.out.println();
        System.out.println("----------------------------------------------------------------------");
    }

    private void printCategories() {
        System.out.printf("Category ACCESSORIES: Items before check: %d. Added: %d. Deleted: %d",
                accessoriesItemQty, addedAccessories.size(), deletedAccessories.size());
        System.out.println();
        System.out.printf("Category COMPLETE LIFT KITS: Items before check: %d. Added: %d. Deleted: %d",
                liftKitItemQty, addedLiftKit.size(), deletedLiftKit.size());
        System.out.println();
        System.out.printf("Category FRONT SUSPENSION: Items before check: %d. Added: %d. Deleted: %d",
                frontSuspItemQty, addedFrontSusp.size(), deletedFrontSusp.size());
        System.out.println();
        System.out.printf("Category LIGHTING: Items before check: %d. Added: %d. Deleted: %d",
                lightItemQty, addedLight.size(), deletedLight.size());
        System.out.println();
        System.out.printf("Category PERFORMANCE: Items before check: %d. Added: %d. Deleted: %d",
                performanceItemQty, addedPerformance.size(), deletedPerformance.size());
        System.out.println();
        System.out.printf("Category PROTECTION & LOCKERS: Items before check: %d. Added: %d. Deleted: %d",
                protectionItemQty, addedProtection.size(), deletedPotection.size());
        System.out.println();
        System.out.printf("Category REAR SUSPENSION: Items before check: %d. Added: %d. Deleted: %d",
                rearSuspItemQty, addedRearSusp.size(), deletedRearSusp.size());
        System.out.println();
        System.out.printf("Category SHOCKS: Items before check: %d. Added: %d. Deleted: %d",
                shocksItemQty, addedShocks.size(), deletedShocks.size());
        System.out.println();
    }

    private void fixResults() {
        finish = Instant.now();
        sortAddedItems();
        sortDeletedItems();
        totalItemsQuantityAfterCheck = ToyStatisticsDAO.getTotalItemsQty(session);
    }

    private void sortDeletedItems() {
        for (ToyItem item: deletedItems){
            String category = item.getItemCategory();
            switch (category){
                case "ACCESSORIES": deletedAccessories.add(item); break;
                case "COMPLETE LIFT KITS": deletedLiftKit.add(item); break;
                case "FRONT SUSPENSION": deletedFrontSusp.add(item); break;
                case "LIGHTING": deletedLight.add(item); break;
                case "PERFORMANCE": deletedPerformance.add(item); break;
                case "PROTECTION & LOCKERS": deletedPotection.add(item); break;
                case "REAR SUSPENSION": deletedRearSusp.add(item); break;
                case "SHOCKS": deletedShocks.add(item);
            }
        }
    }

    private void sortAddedItems() {
        for (ToyItem item: addedItems){
            String category = item.getItemCategory();
            switch (category){
                case "ACCESSORIES": addedAccessories.add(item); break;
                case "COMPLETE LIFT KITS": addedLiftKit.add(item); break;
                case "FRONT SUSPENSION": addedFrontSusp.add(item); break;
                case "LIGHTING": addedLight.add(item); break;
                case "PERFORMANCE": addedPerformance.add(item); break;
                case "PROTECTION & LOCKERS": addedProtection.add(item); break;
                case "REAR SUSPENSION": addedRearSusp.add(item); break;
                case "SHOCKS": addedShocks.add(item);
            }
        }
    }

    private void init(Session session) {
        start = Instant.now();
        totalItemsQuantityBeforeCheck =  ToyStatisticsDAO.getTotalItemsQty(session);

        getCategoryQuantities();
        initArrays();
    }

    private void getCategoryQuantities() {
        accessoriesItemQty = ToyStatisticsDAO.getItemsQuantityByCategory(session, "ACCESSORIES");
        liftKitItemQty = ToyStatisticsDAO.getItemsQuantityByCategory(session, "COMPLETE LIFT KITS");
        frontSuspItemQty = ToyStatisticsDAO.getItemsQuantityByCategory(session, "FRONT SUSPENSION");
        lightItemQty = ToyStatisticsDAO.getItemsQuantityByCategory(session, "LIGHTING");
        performanceItemQty = ToyStatisticsDAO.getItemsQuantityByCategory(session, "PERFORMANCE");
        protectionItemQty = ToyStatisticsDAO.getItemsQuantityByCategory(session, "PROTECTION & LOCKERS");
        rearSuspItemQty = ToyStatisticsDAO.getItemsQuantityByCategory(session, "REAR SUSPENSION");
        shocksItemQty = ToyStatisticsDAO.getItemsQuantityByCategory(session, "SHOCKS");
    }

    private void initArrays() {
        addedItems = new ArrayList<>();
        deletedItems = new ArrayList<>();

        addedAccessories = new ArrayList<>();
        deletedAccessories = new ArrayList<>();
        addedLiftKit = new ArrayList<>();
        deletedLiftKit = new ArrayList<>();
        addedFrontSusp = new ArrayList<>();
        deletedFrontSusp = new ArrayList<>();
        addedLight = new ArrayList<>();
        deletedLight = new ArrayList<>();
        addedPerformance = new ArrayList<>();
        deletedPerformance = new ArrayList<>();
        addedProtection = new ArrayList<>();
        deletedPotection = new ArrayList<>();
        addedRearSusp = new ArrayList<>();
        deletedRearSusp = new ArrayList<>();
        addedShocks = new ArrayList<>();
        deletedShocks = new ArrayList<>();
    }


    public List<ToyItem> getAddedAccessories() {
        return addedAccessories;
    }

    public List<ToyItem> getDeletedAccessories() {
        return deletedAccessories;
    }

    public List<ToyItem> getAddedLiftKit() {
        return addedLiftKit;
    }

    public List<ToyItem> getDeletedLiftKit() {
        return deletedLiftKit;
    }

    public List<ToyItem> getAddedFrontSusp() {
        return addedFrontSusp;
    }

    public List<ToyItem> getDeletedFrontSusp() {
        return deletedFrontSusp;
    }

    public List<ToyItem> getAddedLight() {
        return addedLight;
    }

    public List<ToyItem> getDeletedLight() {
        return deletedLight;
    }

    public List<ToyItem> getAddedPerformance() {
        return addedPerformance;
    }

    public List<ToyItem> getDeletedPerformance() {
        return deletedPerformance;
    }

    public List<ToyItem> getAddedProtection() {
        return addedProtection;
    }

    public List<ToyItem> getDeletedPotection() {
        return deletedPotection;
    }

    public List<ToyItem> getAddedRearSusp() {
        return addedRearSusp;
    }

    public List<ToyItem> getDeletedRearSusp() {
        return deletedRearSusp;
    }

    public List<ToyItem> getAddedShocks() {
        return addedShocks;
    }

    public List<ToyItem> getDeletedShocks() {
        return deletedShocks;
    }

    public List<ToyItem> getAddedItems() {
        return addedItems;
    }

    public List<ToyItem> getDeletedItems() {
        return deletedItems;
    }

    @Override
    public String toString() {
        return "LogStatistics{" +
                "totalItemsQuantityBeforeCheck=" + totalItemsQuantityBeforeCheck +
                ", totalItemsQuantityAfterCheck=" + totalItemsQuantityAfterCheck +
                ", accessoriesItemQty=" + accessoriesItemQty +
                ", liftKitItemQty=" + liftKitItemQty +
                ", frontSuspItemQty=" + frontSuspItemQty +
                ", lightItemQty=" + lightItemQty +
                ", performanceItemQty=" + performanceItemQty +
                ", protectionItemQty=" + protectionItemQty +
                ", rearSuspItemQty=" + rearSuspItemQty +
                ", shocksItemQty=" + shocksItemQty +
                '}';
    }
}
