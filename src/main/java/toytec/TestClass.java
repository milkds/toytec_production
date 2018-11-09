package toytec;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

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
}
