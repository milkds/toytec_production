package toytec;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.hibernate.Session;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.util.List;

public class TestClass {


    public void getOptionPrices(){
        System.setProperty("webdriver.chrome.driver", "src\\main\\resources\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        String itemLink = "https://www.toyteclifts.com/25boss-4r210p-boss-performance-suspension-system-10-4runner.html";
        SileniumUtil.getItemPage(itemLink,driver);

        String category = "Complete Lift Kits";
        String categoryLink = "https://www.toyteclifts.com/complete-lift-kits.html";
        String subCatName = "NO SUBCATEGORY";

        ToyItem item = new ItemBuilder(category,categoryLink,subCatName).buildToyItem(driver);

        System.out.println(item);

        driver.close();
    }

    public void getSubCategories(){
        String catLink = "https://www.toyteclifts.com/front-lifts-coilovers.html";
       // String catLink = "https://www.toyteclifts.com/complete-lift-kits.html";
        SileniumUtil.getCategoryList(catLink);
    }

    public void getCategoryNames(){
        Session session = ToyDao.getSession();
        List<String> catNames = ToyStatisticsDAO.getCategoryNames(session);
        HibernateUtil.shutdown();

        catNames.forEach(System.out::println);
    }

    public void testStatisticsInit(){
        Session session = ToyDao.getSession();
        Statistics statistics = new Statistics(session);
        HibernateUtil.shutdown();


    }

    public void exportToExcel(){
        Session session = ToyDao.getSession();
        try {
            ExcelExporter.saveToExcel(session);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HibernateUtil.shutdown();
    }
}
