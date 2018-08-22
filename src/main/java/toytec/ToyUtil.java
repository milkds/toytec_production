package toytec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ToyUtil {
    private static final String CATEGORIES_PATH = "src\\main\\resources\\categories";
    private static final String CATEGORIES_MAP_PATH = "src\\main\\resources\\cats_subcats";
    private static final Logger logger = LogManager.getLogger(ToyUtil.class.getName());
    public static List<String> getCategoriesForCheck() {
        List<String> categories = new ArrayList<>();
        try {
            categories = Files.readAllLines(Paths.get(CATEGORIES_PATH));
        } catch (IOException e) {
            System.out.println(e);
        }

        return categories;
    }

    public static void logCategoryCheck(CategoryInfoKeeper keeper, boolean changesDetected) {
        String categoryName = keeper.getCategoryName();
        List<String> itemLinksFromCategory = keeper.getItemLinksFromCategory();
        List<String> itemLinksFromDB = keeper.getItemLinksFromDB();
        if (changesDetected){
            logger.info(categoryName + " category changes detected.");
            Set<String> dbLinkSet = new HashSet<>(itemLinksFromDB);
            Set<String> webLinkSet = new HashSet<>(itemLinksFromCategory);
            for (String link: itemLinksFromCategory){
                if (!dbLinkSet.contains(link)){
                    logger.info("ADDED ITEM: " + link);
                }
            }
            for (String link: itemLinksFromDB){
                if (!webLinkSet.contains(link)){
                    logger.info("DELETED ITEM: " + link);
                }
            }
        }
        else {
            logger.info(categoryName+" category is up to date");
        }

    }

    public static void setDeletedStatus(CategoryInfoKeeper keeper, Session session) {
        List<String> itemLinksFromCategory = keeper.getItemLinksFromCategory();
        List<String> itemLinksFromDB = keeper.getItemLinksFromDB();

        //making Set of item links from web page - for quicker search.
        Set<String> webLinkSet = new HashSet<>(itemLinksFromCategory);
        for (String link: itemLinksFromDB){
            if (!webLinkSet.contains(link)){
                ToyItem item = ToyDao.getItemByWebLink(link, session);
                item.setItemStatus("DELETED");
                ToyDao.updateItem(session,item);
                logger.info("Status set to DELETED for: "+item);
            }
        }

    }

    public static void parseNewItems(WebDriver driver, CategoryInfoKeeper keeper, Session session) {
        List<String> itemLinksFromCategory = keeper.getItemLinksFromCategory();
        List<String> itemLinksFromDB = keeper.getItemLinksFromDB();

        //making Set of item links from db - for quicker search.
        Set<String> dbLinkSet = new HashSet<>(itemLinksFromDB);

        for (String link: itemLinksFromCategory){
            if (!dbLinkSet.contains(link)){
                SileniumUtil.getItemPage(link, driver);
                ItemBuilder builder = new ItemBuilder(keeper.getCategoryName(), keeper.getCategoryLink(), keeper.getSubCategoryName());
                ToyItem item = builder.buildToyItem(driver);
                ToyDao.addNewItem(item, session);
                logger.info("Added new item: "+item);
            }
        }
    }

    public static void checkAllItemsForStockUpdates(Session session) {
        List<ToyItem> items = ToyDao.getAllItems(session);

        int total = items.size();
        int counter = 0;

        for (ToyItem item: items){
            ItemStockChecker checker = new ItemStockChecker(item);
            if (checker.stockChangeDetected()){
                ToyDao.updateItem(session, item);
            }
            counter++;
            logger.info("Checked item "+ counter + " of " + total);
        }
    }

    public static Map<String,String> getCategoryMap(String categoryName) {
        Map<String,String> catMap = new HashMap<>();
        List<String> catLines = new ArrayList<>();
        try {
            catLines = Files.readAllLines(Paths.get(CATEGORIES_MAP_PATH));
        } catch (IOException e) {
            System.out.println(e);
        }
        for (String catLine: catLines){
            if (catLine.startsWith(categoryName)){
                String split[] = catLine.split(";;");
                if (split[1].equals("NO SUBCATEGORY")){
                    catMap.put(split[1],"");
                    return catMap;
                }
                else {
                    catMap.put(split[1],split[2]);
                }
            }
        }
        return catMap;
    }
}
