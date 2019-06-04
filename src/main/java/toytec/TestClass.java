package toytec;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.hibernate.Session;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TestClass {


    public static void checkOptionsFromDao(){
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<ToyItem> items = ToyDao.getItemsByWebLink("https://www.toyteclifts.com/ttbosstac-2005-toytec-boss-suspension-system-for-05-tacoma.html", session);
        System.out.println(items.size());
        items.get(0).getOptions().forEach(System.out::println);
        session.close();
        HibernateUtil.shutdown();
    }

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

    public void sendMail(){
        //File file = new File("D:\\IdeaProjects\\toytec_production\\src\\main\\resources\\6-18.xlsx");

        File file = null;
        try
        {
            file = File.createTempFile("myTempFile", ".txt");

            //write data on temporary file
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write("This is the temporary data written to temp file");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<File> files = new ArrayList<>();
        files.add(file);
       // EmailSender.sendMail(files, new Statistics());
    }

    public static void testInstant(){
        Instant now =  Instant.now();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofLocalizedDateTime( FormatStyle.MEDIUM )
                        .withLocale( Locale.UK )
                        .withZone( ZoneId.systemDefault() );

        System.out.println(formatter.format(now));
    }

    public static void testBigDecCompare(){
        BigDecimal nullDec = null;
        BigDecimal zeroPrice = new BigDecimal(0);
        BigDecimal zeroPrice2 = new BigDecimal("");
        System.out.println(zeroPrice.compareTo(zeroPrice2)==0);
        System.out.println(zeroPrice.subtract(zeroPrice2));
    }

}
