package toytec;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.xml.sax.Locator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLOutput;
import java.util.*;

public class SileniumUtil {
    private static final String CATEGORY_SETTINGS = "?product_list_mode=list&product_list_limit=all";
    private static final String PROBLEM_LOG_PATH = "src\\main\\resources\\problemlog";
    private static final String TOYTEC_DEFAULT_PAGE = "https://www.toyteclifts.com/";
    private static boolean skippedCountrySel = false;


    public static WebDriver getCategory(WebDriver driver, String categoryUrl){
        driver.get(categoryUrl+CATEGORY_SETTINGS);
        waitTillCategoryOpen(driver);

        return driver;
    }

    public static WebDriver getDriverWithCategory(String categoryUrl){
        WebDriver driver = getDriver();

        String url = categoryUrl+CATEGORY_SETTINGS;
        driver.get(url);

        if (!skippedCountrySel){
            skippedCountrySel = skipCountrySelect(driver);
        }

        waitTillCategoryOpen(driver);

        return driver;
    }

    private static void waitTillCategoryOpen(WebDriver driver){
        int retry = 0;
        WebDriverWait wait = new WebDriverWait(driver, 10);
        while (true){
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[class='products wrapper list products-list']")));
                break;
            }
            catch (TimeoutException e){
                retry++;
                if (retry == 5){
                    logUnexpectedData("couldn't load category", driver.getCurrentUrl());
                    System.exit(0);
                }
            }
        }

    }

    private static boolean skipCountrySelect(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        WebElement countrySelectEl = null;

        //waiting for country select window
        try {
            countrySelectEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("igSplashElement")));
        }
        catch (TimeoutException e){
            return false;
        }

        //waiting for close element to be visible
        wait = new WebDriverWait(driver, 10);
        WebElement close = null;
        try {
            close = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("i[class='material-icons']")));
        }
        catch (TimeoutException e){
            return false;
        }

        close.click();
        System.out.println("close clicked");

        return true;
    }

    private static void logUnexpectedData(String message, String currentUrl) {
        try(FileWriter fw = new FileWriter(PROBLEM_LOG_PATH, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(message+"-----"+currentUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("problem logged for "+currentUrl);
    }

    private static WebDriver getDriver(){
        System.setProperty("webdriver.chrome.driver", "src\\main\\resources\\chromedriver.exe");

        return new ChromeDriver();
    }

    public static List<String> getItemsFromCategory(WebDriver driver) {
        List<String> itemLinks = new ArrayList<>();
        List<WebElement> items = driver.findElements(By.cssSelector("li[class='item product product-item']"));
        for (WebElement item: items){
            WebElement itemLinkEl = item.findElement(By.className("product-item-link"));
            itemLinks.add(itemLinkEl.getAttribute("href"));
        }
        //itemLinks.forEach(System.out::println);

        return itemLinks;
    }

    public static String getCategoryName(WebDriver driver) {
        WebElement headEl = driver.findElement(By.cssSelector("span[data-ui-id='page-title-wrapper']"));

        return headEl.getText();
    }

    public static void getItemPage(String link, WebDriver driver) {
        driver.get(link);

        if (!skippedCountrySel){
            skippedCountrySel = skipCountrySelect(driver);
        }

        int retry = 0;
        WebDriverWait wait = new WebDriverWait(driver, 10);
        while (true){
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated
                        (By.id("maincontent")));
                break;
            }
            catch (TimeoutException e){
                retry++;
                if (retry == 5){
                    logUnexpectedData("couldn't load item", driver.getCurrentUrl());
                    System.exit(0);
                }
            }
        }

    }

    public static WebDriver initDriver() {
        skippedCountrySel = false;
        return getDriver();
    }

    public static WebDriver getToytecDefaultPageDriver() {
        WebDriver driver = getDriver();
        driver.get(TOYTEC_DEFAULT_PAGE);
        skipCountrySelect(driver);

        return driver;
    }

    public static List<String> getAllCategoriesLinks(WebDriver driver) {
        List<String> catLinks = new ArrayList<>();

        List<WebElement> catEls = getCategoryElements(driver);

        //System.out.println("categories qty = " + catEls.size());
        for (WebElement element: catEls){
            By by = By.cssSelector("a[class='level-top ui-corner-all']");
            WebElement innerCatEl = waitTillElementAvailable(element,by,120);
            if (innerCatEl==null){
                System.out.println("Couldn't load category elements from start page, check internet connection or site code");
            }
            System.out.println(innerCatEl.getAttribute("href"));
            catLinks.add(innerCatEl.getAttribute("href"));
        }

        driver.close();

        return catLinks;
    }

    public static Map<String,String> getCategoryMap(String catLink) {
        Map<String,String> catMap = new HashMap<>();
        WebDriver driver = getToytecDefaultPageDriver();

        // System.out.println(menuEl.getText());
        List<WebElement> catEls = getCategoryElements(driver);

        for (WebElement element: catEls){
            //todo: write wait here - some times "level-top ui-corner-all" can be unavailable.
              WebElement innerCatEl = element.findElement(By.cssSelector("a[class='level-top ui-corner-all']"));

            if (innerCatEl.getAttribute("href").equals(catLink)){
                List<WebElement> subCatEls = element.findElements(By.cssSelector("li[class^='level1 nav-']"));
                System.out.println("subCatEls size " + subCatEls.size());
                if (subCatEls.size()==0){
                    catMap.put("NO SUBCATEGORY", "");
                }
                else {
                    int counter = 0;
                    for (WebElement subCatEl: subCatEls){
                        counter++;
                        String subCatName = subCatEl.getText()+counter;
                        WebElement linkEl = subCatEl.findElement(By.className("ui-corner-all"));
                        System.out.println(linkEl.getText());

                        WebElement nameEl = linkEl.findElement(By.tagName("span"));
                        System.out.println(nameEl.getText());
                        String subCatLink = linkEl.getAttribute("href");
                        catMap.put(subCatName,subCatLink);
                    }
                }
                break;
            }
        }

        for (Map.Entry<String, String> entry: catMap.entrySet()){
            System.out.println("SubCategory name: " + entry.getKey() + ". SubCategory link: " + entry.getValue());
        }

        driver.close();

        return catMap;
    }

    private static List<WebElement> getCategoryElements(WebDriver driver){
        WebElement menuEl = driver.findElement(By.id("store.menu"));
        List<WebElement> catEls = menuEl.findElements(By.cssSelector("li[class^='level0 nav-']"));
        int tryCounter = 0;
        while (catEls.size()==0){
            //if no result in 5 minutes - we quit.
            if (tryCounter >600){
                System.out.println("Could not get category list - check internet connection, or site code");
                System.exit(1);
            }
            try {
                Thread.currentThread().wait(500);
                menuEl = driver.findElement(By.id("store.menu"));
                catEls = menuEl.findElements(By.cssSelector("li[class^='level0 nav-']"));
                tryCounter++;
            } catch (InterruptedException e) {
            }
        }

        return catEls;
    }

    public static List<String> getCategoryList(String catLink) {
        List<String> catList = new ArrayList<>();
        WebDriver driver = getToytecDefaultPageDriver();
        List<WebElement> catEls = getCategoryElements(driver);

        for (WebElement element: catEls){
            //innerCatEl not always available - sometimes need to wait.
            By by = By.cssSelector("a[class='level-top ui-corner-all']");
            WebElement innerCatEl = waitTillElementAvailable(element, by, 120);
           if (innerCatEl==null){
               System.out.println("Could not get category list - check internet connection, or site code");
               System.exit(1);
           }
            if (innerCatEl.getAttribute("href").equals(catLink)){
                List<WebElement> subCatEls = element.findElements(By.cssSelector("li[class^='level1 nav-']"));
                if (subCatEls.size() != 0) {
                    for (WebElement subCatEl: subCatEls){
                        WebElement linkEl = subCatEl.findElement(By.className("ui-corner-all"));
                        String subCatLink = linkEl.getAttribute("href");
                        catList.add(subCatLink);
                    }
                }
                break;
            }
        }

        driver.close();

        return catList;
    }

    private static WebElement waitTillElementAvailable(WebElement element, By locator, int retryQuantity) {
        WebElement awaitedElement = null;
        int counter = 0;
        while (true){
            try {
                awaitedElement = element.findElement(locator);
                break;
            }
            catch (NoSuchElementException e){
                counter++;
                if (counter==retryQuantity){
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (Exception interruptE) {
                }
            }
        }


        return awaitedElement;
    }


}
