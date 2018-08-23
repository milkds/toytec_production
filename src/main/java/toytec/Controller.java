package toytec;

import org.hibernate.Session;
import org.openqa.selenium.WebDriver;

import java.util.*;

/***
 * Check site for updates - checks all categories and if any new/deleted item found -
 * parses new item/changes status to deleted. Than it loads all items from db and checks stock
 *
 * Check Stock for updates - loads all items from db and checks stock.
 */

public class Controller {


    public static void main(String[] args) {
      // new Controller().checkSiteForUpdates();
        // new Controller().checkStockForUpdates();
    }

    private void checkStockForUpdates(){
        Session session = ToyDao.getSession();
        ToyUtil.checkAllItemsForStockUpdates(session);
        HibernateUtil.shutdown();
    }

    private void checkSiteForUpdates(){
        Session session = ToyDao.getSession();
        WebDriver driver = null;
        List<CategoryInfoKeeper> categoriesWithItemsToReparse = new Controller().checkCategoriesForItemListChanges(driver, session);

        driver = SileniumUtil.initDriver();
        for (CategoryInfoKeeper keeper: categoriesWithItemsToReparse){
            ToyUtil.setDeletedStatus(keeper, session);
            ToyUtil.parseNewItems(driver, keeper, session);
        }

        driver.close();

        ToyUtil.checkAllItemsForStockUpdates(session);
        HibernateUtil.shutdown();
    }

    private List<CategoryInfoKeeper> checkCategoriesForItemListChanges(WebDriver driver, Session session){
        List<String> categories = ToyUtil.getCategoriesForCheck();
        List<CategoryInfoKeeper> categoryLinksToReparse = new ArrayList<>();

        boolean categoryOpened = false;

        for (String categoryUrl : categories){
            if (!categoryOpened){
                driver = SileniumUtil.getDriverWithCategory(categoryUrl);
                categoryOpened = true;
            }
            else {
                driver = SileniumUtil.getCategory(driver,categoryUrl);
            }
            String categoryName = SileniumUtil.getCategoryName(driver);
            List<String> itemLinksFromCategory = SileniumUtil.getItemsFromCategory(driver);
            List<String> itemLinksFromDB = ToyDao.getItemLinksFromCategory(categoryName, session);

            CategoryInfoKeeper keeper = new CategoryInfoKeeper();
            keeper.setItemLinksFromCategory(itemLinksFromCategory);
            keeper.setItemLinksFromDB(itemLinksFromDB);
            keeper.setCategoryName(categoryName);
            keeper.setCategoryLink(driver.getCurrentUrl());

            if (itemListChangeDetected(keeper)){
                List<CategoryInfoKeeper> subCatKeepers = this.getSubCategories(driver, keeper, session);
                categoryLinksToReparse.addAll(subCatKeepers);
                ToyUtil.logCategoryCheck(keeper, true);
            }
            else {
                ToyUtil.logCategoryCheck(keeper, false);
            }
           /* driver.close();
            System.exit(0);*/
        }
            driver.close();
        return categoryLinksToReparse;
    }

    private List<CategoryInfoKeeper> getSubCategories(WebDriver driver, CategoryInfoKeeper keeper, Session session) {
        List<CategoryInfoKeeper> subCatKeepers = new ArrayList<>();
        Map<String, String> categoryMap = ToyUtil.getCategoryMap(keeper.getCategoryName());
        if (categoryMap.size()==1&&categoryMap.containsKey("NO SUBCATEGORY")){
            keeper.setSubCategoryName("NO SUBCATEGORY");
            keeper.setSubCategoryLink(keeper.getCategoryLink());
            subCatKeepers.add(keeper);
        }
        else {
            for (Map.Entry<String,String> entry : categoryMap.entrySet()){
                CategoryInfoKeeper subCatKeeper = new CategoryInfoKeeper();
                subCatKeeper.setSubCategoryName(entry.getKey());
                subCatKeeper.setSubCategoryLink(entry.getValue());
                driver = SileniumUtil.getCategory(driver,entry.getValue());

                List<String> itemLinksFromCategory = SileniumUtil.getItemsFromCategory(driver);
                List<String> itemLinksFromDB = ToyDao.getItemLinksFromSubCategory(entry.getKey(), session);

                subCatKeeper.setItemLinksFromCategory(itemLinksFromCategory);
                subCatKeeper.setItemLinksFromDB(itemLinksFromDB);
                subCatKeeper.setCategoryName(keeper.getCategoryName());
                subCatKeeper.setCategoryLink(keeper.getCategoryLink());

                subCatKeepers.add(subCatKeeper);
            }
        }

     return subCatKeepers;
    }

    private boolean itemListChangeDetected(CategoryInfoKeeper keeper) {
        List<String> itemLinksFromCategory = keeper.getItemLinksFromCategory();
        List<String> itemLinksFromDB = keeper.getItemLinksFromDB();

        if (itemLinksFromCategory.size()!=itemLinksFromDB.size()){
            return true;
        }
        Set<String> linkSet = new HashSet<>(itemLinksFromCategory);
        for (String link: itemLinksFromDB){
            if (!linkSet.contains(link)){
                return true;
            }
        }

        return false;
    }

}
