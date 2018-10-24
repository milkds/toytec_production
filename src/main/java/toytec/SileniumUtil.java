package toytec;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SileniumUtil {
    private static final String CATEGORY_SETTINGS = "?product_list_mode=list&product_list_limit=all";
    private static final String PROBLEM_LOG_PATH = "src\\main\\resources\\problemlog";
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
}
