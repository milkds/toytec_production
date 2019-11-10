package toytec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.openqa.selenium.WebDriver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/***
 * Check site for updates - checks all categories and if any new/deleted item found -
 * parses new item/changes status to deleted. Than it loads all items from db and checks stock
 *
 * Check Stock for updates - loads all items from db and checks stock.
 */

public class Controller {
    private static final Logger logger = LogManager.getLogger(Controller.class.getName());


    public static void main(String[] args) {
      //  TestClass.checkOptionsFromDao();
         new Controller().checkSiteForUpdates();
      //   new Controller().checkOptionsForUpdates();
    }


    private void checkOptionsForUpdates(){
        //db backup
        DBSaver.backupDB();

        OptionStatistics statistics = new OptionStatistics();

        //first driver initialisation
        WebDriver driver = SileniumUtil.getToytecDefaultPageDriver();

        //getting item links from db to check
        List<String> itemWithOptionsLinks = ToyDao.getItemsWithOptionsLinkList();

        //getting current options for each item
        Map<String, List<ToyOption>> itemOptionMap = new HashMap<>();
        itemWithOptionsLinks.forEach(itemLink->{
            List<ToyOption> options;
            try {
                options = new OptionBuilder().getOptions(driver, itemLink);
                itemOptionMap.put(itemLink, options);
            } catch (UnavailableOptionsException ignored) {
            }
        });

        //matching options
        List<OptionChangeKeeper> changeList = new ArrayList<>();
        Session session = HibernateUtil.getSessionFactory().openSession();
        itemOptionMap.forEach((k,v)->{
            OptionChangeKeeper keepr = new OptionMatcher(k).matchOptions(v, session);
            if (keepr.hasChanges()){
                changeList.add(keepr);
            }
        });

        //building and sending report
        String report = statistics.buildReport(changeList);
        List<File> filesForEmail = new ArrayList<>();
        filesForEmail.add(createReportTxt(report));
        File excelDB = ExcelExporter.prepareReportForEmail(session);
        filesForEmail.add(excelDB);
        EmailSender.sendOptionReportByMail(filesForEmail, report);

        //closing resources
        session.close();
        HibernateUtil.shutdown();
        driver.quit();
    }

    private File createReportTxt(String report) {
        File file = null;
        try
        {
            String fName = Statistics.formatTime(Instant.now());
            fName = fName.replaceAll(":", "-");
            fName = fName.substring(0, fName.length()-3);
            fName = fName+"_ToyTec_parseReport.txt";
            fName = "C:/Dropbox/Shared_ServerGrisha/ToyTecParse/"+ fName;
            file = new File(fName);
            //write data on temporary file
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(report);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
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
        WebDriver driver = SileniumUtil.initDriver();
        Statistics statistics = new Statistics(session);

        ToyUtil.checkAllItemsForUpdates(session, driver, statistics);

        driver.close();
        HibernateUtil.shutdown();
    }

    private void checkSiteForUpdates(){
        DBSaver.backupDB();
        Session session = ToyDao.getSession();
        Statistics statistics = new Statistics(session);

        WebDriver driver = null;
        List<CategoryInfoKeeper> categoriesWithItemsToReparse = new Controller().checkCategoriesForItemListChanges(driver, session);

        driver = SileniumUtil.initDriver();
        for (CategoryInfoKeeper keeper: categoriesWithItemsToReparse){
            ToyUtil.setDeletedStatus(keeper, session, statistics);
            ToyUtil.parseNewItems(driver, keeper, session, statistics);
        }

        ToyUtil.checkAllItemsForUpdates(session, driver, statistics);
        StringBuilder sb = statistics.showStatistics();
        sendResultsByEmail(sb, session, statistics);

        driver.close();
        HibernateUtil.shutdown();
    }

    private void sendResultsByEmail(StringBuilder sb, Session session, Statistics statistics) {
        List<File> files = new ArrayList<>();
        File file = createReportTxt(sb.toString());
        files.add(file);
        File excelDB = ExcelExporter.prepareReportForEmail(session);
        files.add(excelDB);

        EmailSender.sendMail(files, statistics);
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
