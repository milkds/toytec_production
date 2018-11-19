package toytec;

import org.hibernate.Session;
import org.openqa.selenium.WebDriver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/***
 * Check site for updates - checks all categories and if any new/deleted item found -
 * parses new item/changes status to deleted. Than it loads all items from db and checks stock
 *
 * Check Stock for updates - loads all items from db and checks stock.
 */

public class Controller {


    public static void main(String[] args) {
       //  new TestClass().sendMail();
        // new Controller().testStatistics();
         new Controller().checkSiteForUpdates();
        // new Controller().checkStockForUpdates();

       // new TestClass().getOptionPrices();
    }

    private void checkDupes(){
        Session session = ToyDao.getSession();
        List<ToyItem> items = ToyDao.getAllItems(session);
        Map<String, ToyItem> itemMap = new HashMap<>();
        for (ToyItem item: items){
            String link = item.getItemLink();
            if (!itemMap.containsKey(link)){
                itemMap.put(link,item);
            }
            else {
                String newCategory = item.getItemCategory();
                String oldCategory = itemMap.get(link).getItemCategory();
                if (newCategory.equals(oldCategory)){
                    System.out.println(item.getItemID());
                }
            }
        }

        HibernateUtil.shutdown();
    }

    private void testStatistics(){
        Session session = ToyDao.getSession();
        LogStatistics statistics = new LogStatistics(session);
        HibernateUtil.shutdown();
    }

    private void checkStockForUpdates(){
        Session session = ToyDao.getSession();
        ToyUtil.checkAllItemsForStockUpdates(session);
        HibernateUtil.shutdown();
    }

    private void checkSiteForUpdates(){
        Session session = ToyDao.getSession();
        Statistics statistics = new Statistics(session);

        WebDriver driver = null;
        List<CategoryInfoKeeper> categoriesWithItemsToReparse = new Controller().checkCategoriesForItemListChanges(driver, session);

        driver = SileniumUtil.initDriver();
        for (CategoryInfoKeeper keeper: categoriesWithItemsToReparse){
            ToyUtil.setDeletedStatus(keeper, session, statistics);
            ToyUtil.parseNewItems(driver, keeper, session, statistics);
        }

        driver.close();

        ToyUtil.checkAllItemsForStockUpdates(session);
        StringBuilder sb = statistics.showStatistics();
        sendResultsByEmail(sb, session);

        HibernateUtil.shutdown();
    }

    private void sendResultsByEmail(StringBuilder sb, Session session) {
        List<File> files = new ArrayList<>();
        File file = null;
        try
        {
            file = File.createTempFile("parseReport", ".txt");
            //write data on temporary file
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(sb.toString());
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        files.add(file);
        File excelDB = ExcelExporter.prepareReportForEmail(session);
        files.add(excelDB);

        EmailSender.sendMail(files);
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
            System.out.println("Category name " + categoryName + "; DB List size: " + itemLinksFromDB.size()+"; Web List Size: " + itemLinksFromCategory.size());

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

    private List<CategoryInfoKeeper> getSubCategories2(WebDriver driver, CategoryInfoKeeper keeper, Session session) {
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

    private List<CategoryInfoKeeper> getSubCategories(WebDriver driver, CategoryInfoKeeper keeper, Session session) {
        List<CategoryInfoKeeper> subCatKeepers = new ArrayList<>();
        String catLink = keeper.getCategoryLink();
        List<String> subCatLinks = SileniumUtil.getCategoryList(catLink);

        //if no subCategoryLinks found - list would be empty.
        if (subCatLinks.size()==0){
            keeper.setSubCategoryName("NO SUBCATEGORY");
            keeper.setSubCategoryLink(keeper.getCategoryLink());
            subCatKeepers.add(keeper);
        }
        else {
            for (String subCatLink: subCatLinks){
                CategoryInfoKeeper subCatKeeper = new CategoryInfoKeeper();
                subCatKeeper.setSubCategoryLink(subCatLink);
                driver = SileniumUtil.getCategory(driver,subCatLink);

                //getCategoryName works identically either for category or for subcategory
                String subCategoryName = SileniumUtil.getCategoryName(driver);
                subCatKeeper.setCategoryName(subCategoryName);

                List<String> itemLinksFromCategory = SileniumUtil.getItemsFromCategory(driver);
                List<String> itemLinksFromDB = ToyDao.getItemLinksFromSubCategory(subCategoryName, session);

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
