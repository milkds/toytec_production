package toytec;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ItemBuilder {
    private static final String PROBLEM_LOG_PATH = "src\\main\\resources\\problemlog";

    private String category;
    private String categoryLink;
    private String subCategoryName;

    public ToyItem buildToyItem (WebDriver driver){
        ToyItem item = new ToyItem();
      //  System.out.println("getting item: " + driver.getCurrentUrl());

        String sku = getSku(driver);
        String itemName = getItemName(driver);
        String description = getDescription(driver);
        String mainImgUrl = getMainImgUrl(driver);
        String imgUrls = "";
        if (mainImgUrl.length()>0){
            imgUrls = getImgUrls(driver);
        }
        String itemMake = getItemMake(driver);
        BigDecimal priceFrom = getPriceFrom(driver);
        BigDecimal priceTo = getPriceTo(driver);
        String itemLink = driver.getCurrentUrl();
        String availability = getStockAvailability(driver);
        String stockDesc = getStockDesc(driver);
        String instructions = getInstructions(driver);
        String subCategory = subCategoryName;
        String metaKeywords = getMetaKeywords(driver);
        String metaDesc = getMetaDesc(driver);
        List<ToyOption> options = getOptions(driver);

        //notification part

        /*System.out.println("SKU: " + sku);
        System.out.println("Item Name:" + itemName);
        System.out.println("Description: " + description);
        System.out.println("Main Img URL: " + mainImgUrl);
        System.out.println("IMG URLS: " + imgUrls);
        System.out.println("Instructions: " + instructions);
        System.out.println("Price from: " + priceFrom);
        System.out.println("Price to: " + priceTo);
        System.out.println("Item Link: " + itemLink);
        System.out.println("Item Make: " + itemMake);
        System.out.println("Stock Availability: " + availability);
        System.out.println("Stock Desc: " + stockDesc);
        System.out.println("Subcategory: " + subCategory);
        System.out.println("Meta Keywords: " + metaKeywords);
        System.out.println("Meta Description: " + metaDesc);

        for (ToyOption option : options){
            System.out.println(option);
        }*/

        item.setSku(sku);
        item.setItemName(itemName);
        item.setDesc(description);
        item.setMainImg(mainImgUrl);
        item.setImgLinks(imgUrls);
        item.setItemMake(itemMake);
        item.setInstructions(instructions);
        item.setPriceFrom(priceFrom);
        item.setPriceTo(priceTo);
        item.setItemLink(itemLink);
        item.setOptions(options);
        item.setAvailability(availability);
        item.setStockDesc(stockDesc);
        item.setMetaKeywords(metaKeywords);
        item.setMetaDescription(metaDesc);

        Date today = new Date();
        item.setAvailUpdateDate(today);
        item.setStockDescUpdateDate(today);

        item.setItemCategory(category);
        item.setItemCategoryLink(categoryLink);
        item.setItemSubCategory(subCategory);
        item.setItemStatus("ACTIVE");

       return item;
    }

    private String getMetaDesc(WebDriver driver) {
        WebElement metaEl = driver.findElement(By.cssSelector("meta[name='description']"));
        return metaEl.getAttribute("content");
    }

    private String getMetaKeywords(WebDriver driver) {
        WebElement metaEl = driver.findElement(By.cssSelector("meta[name='keywords']"));
        return metaEl.getAttribute("content");
    }

    private String getSubCategory(WebDriver driver) {
        String subCategory = "";

        WebElement categoryBlockEl = driver.findElement(By.className("breadcrumbs"));
        List<WebElement> categoryElements = categoryBlockEl.findElements(By.cssSelector("li[class^='item category'])"));
        for (WebElement catEl : categoryElements){
            String catText = catEl.getText();
            if (!catText.equals(category)){
               // System.out.println("Getting subCat: category is: " + category);
              //  System.out.println("Getting subCat: categoryElementText is: " + catText);
                subCategory = catText;
            }
        }

        return subCategory;
    }

    private String getStockDesc(WebDriver driver) {
        String stockDesc = "";
        WebElement stockSkuEl = driver.findElement(By.className("product-info-stock-sku"));
        WebElement stockDescEl = null;
        try {
            stockDescEl =  stockSkuEl.findElement(By.cssSelector("div[class^='product-availability']"));
        }
        catch (NoSuchElementException e){
            return "NO DESC";
        }
        stockDesc = stockDescEl.getText();

        return stockDesc;
    }

    private String getStockAvailability(WebDriver driver) {
        String availability = "";
        WebElement stockSkuEl = driver.findElement(By.className("product-info-stock-sku"));
        WebElement availabilityEL;
        try{
            availabilityEL = stockSkuEl.findElement(By.cssSelector("div[title='Availability']"));
        }
        catch (NoSuchElementException e){
            try {
                availabilityEL = stockSkuEl.findElement(By.cssSelector("p[title='Availability']"));
            }
            catch (NoSuchElementException ex){
                return "";
            }
        }
        availability = availabilityEL.getText();


        return availability;
    }

    public List<ToyOption> getOptions(WebDriver driver) {
        List<ToyOption> options = new ArrayList<>();
        try  {
            WebElement customizeButton = driver.findElement(By.id("bundle-slide"));
            customizeButton.click();
        }
        catch (NoSuchElementException e){
            return options;
        }
        bad_sleep(5000);

        WebElement optionsEl = driver.findElement(By.className("bundle-options-container"));
        // System.out.println("options found");
        optionsEl = optionsEl.findElement(By.id("product_addtocart_form"));
        // System.out.println("form found");
        optionsEl = optionsEl.findElement(By.className("bundle-options-wrapper"));
        //  System.out.println("inner options element found");
        List<WebElement> optionsList = optionsEl.findElements(By.cssSelector("div[class^='field option ']"));


        for (WebElement opt: optionsList){
            WebElement label = null;
            while (true){
                label = opt.findElement(By.className("label"));
                if (label.getText().length()>0){
                    break;
                }
            }
            By by = By.cssSelector("div[class='nested options-list']");

            WebElement innerOptHold = SileniumUtil.waitTillElementAvailable(opt,by,20);
            if (innerOptHold==null){
                by = By.className("control");
                innerOptHold = SileniumUtil.waitTillElementAvailable(opt,by,20);
                if (innerOptHold==null){
                    logUnexpectedData("No options in options group found.", driver.getCurrentUrl());
                    continue;
                }
            }
            List<WebElement> innerOptions = new ArrayList<>();
            innerOptions = innerOptHold.findElements(By.cssSelector("div[class='field choice']"));

            //checking if there single option
            if (innerOptions.size()==0){
                String fullOptText = innerOptHold.getText();
                if (fullOptText.contains("$")){
                    logUnexpectedData("Single option with price", driver.getCurrentUrl());
                    innerOptions.add(innerOptHold);
                }
                else {
                    innerOptions.add(innerOptHold.findElement(By.className("product-name")));
                }
            }


            for (WebElement choice: innerOptions){
                ToyOption option = new ToyOption();
                option.setOptionGroup(label.getText());
                String choiceText = choice.getText();
                if (choiceText.contains("$")){
                    String choiceName = StringUtils.substringBefore(choiceText, " [");
                    String price = "";
                    price = StringUtils.substringBetween(choiceText," $", "]");
                    BigDecimal priceNum = new BigDecimal(price);
                    if (choiceText.contains("- $")){
                        priceNum = priceNum.negate();
                    }
                    option.setPrice(priceNum);
                    option.setOptionName(choiceName);
                }
                else {
                    option.setOptionName(choiceText);
                }
                options.add(option);
            }

        }

        return options;
    }

    private BigDecimal getPriceTo(WebDriver driver) {
        WebElement priceToEl = null;
        String price = "";
        try {
            priceToEl = driver.findElement(By.className("price-to"));
        }
        catch (NoSuchElementException e){
            price = driver.findElement(By.className("price")).getText();
            price = StringUtils.substringAfter(price,"$");
            price = price.replaceAll(",", "");
            return new BigDecimal(price);
        }
        price = priceToEl.findElement(By.className("price")).getText();
        price = StringUtils.substringAfter(price,"$");
        price = price.replaceAll(",", "");

        return new BigDecimal(price);
    }

    private BigDecimal getPriceFrom(WebDriver driver) {
        WebElement priceFromEl = null;
        String price = "";
        try {
            priceFromEl = driver.findElement(By.className("price-from"));
        }
        catch (NoSuchElementException e){
            price = driver.findElement(By.className("price")).getText();
            price = StringUtils.substringAfter(price,"$");
            price = price.replaceAll(",", "");
            return new BigDecimal(price);
        }

        price = priceFromEl.findElement(By.className("price")).getText();
        price = StringUtils.substringAfter(price,"$");
        price = price.replaceAll(",", "");

        return new BigDecimal(price);
    }

    private String getInstructions(WebDriver driver) {
        String instructions = "";
        WebElement instructionsTab = null;
        try{
            instructionsTab = driver.findElement(By.id("tab-label-mageworx_product_attachments-title"));
        }
        catch (NoSuchElementException e){
            return "";
        }
        instructionsTab.click();
        while (driver.findElements(By.className("item-link")).size()==0){
            bad_sleep(50);
        }
        WebElement instLink = driver.findElement(By.className("item-link"));
        instLink = instLink.findElement(By.tagName("a"));
        instructions = instLink.getAttribute("href");

        return instructions;
    }

    private String getItemMake(WebDriver driver) {
        String itemMake = "";

        WebElement moreInfo;
        try{
            moreInfo = driver.findElement(By.id("tab-label-additional-title"));
        }
        catch (NoSuchElementException e){
            return "";
        }
        moreInfo.click();

        while (true){
            WebElement manufacturerEl = driver.findElement(By.cssSelector("td[data-th='Manufacturer']"));
            itemMake = manufacturerEl.getText();
            if (itemMake!=null&&itemMake.length()>0){
                break;
            }
            else {
                bad_sleep(100);
            }
        }

        WebElement additionalRowEl = driver.findElement(By.id("product-attribute-specs-table"));
        List<WebElement> additionalRows = additionalRowEl.findElements(By.tagName("tr"));
        if (additionalRows.size()>1){
            logUnexpectedData("unexpected additional info(rows qty)", driver.getCurrentUrl());
        }
       // System.out.println("additional rows qty: "+additionalRows.size());

        return itemMake;
    }

    private String getImgUrls(WebDriver driver) {
        StringBuilder imgUrls = new StringBuilder();
        String mainImgUrl = "";
        WebElement mainImgEl = driver.findElement(By.cssSelector("div[class='gallery-placeholder']"));
        try{
            mainImgEl = mainImgEl.findElement(By.cssSelector("div[class='fotorama__stage__shaft fotorama__grab']"));
        }
        catch (NoSuchElementException e){
            return "";
        }

        mainImgEl = mainImgEl.findElement(By.cssSelector("div[data-active='true']"));
        while (mainImgEl.findElements(By.tagName("img")).size()==0){
            bad_sleep(50);
        }
        mainImgEl = mainImgEl.findElement(By.tagName("img"));

        mainImgUrl = mainImgEl.getAttribute("src");
        String prevImgUrl = mainImgUrl;
        WebElement arrowEl = null;
        while (true){

            arrowEl = driver.findElement(By.cssSelector("div[class='gallery-placeholder']"));
            List<WebElement> arrows =arrowEl.findElements(By.className("fotorama__arr__arr"));
            arrowEl = arrows.get(1);

            String tempUrl = "";
            try{
                arrowEl.click();
            }
            catch (WebDriverException e){
                String imgUrlsStr = imgUrls.toString();
                int size = imgUrlsStr.length();
                if (size>0){
                    imgUrls.deleteCharAt(size-1);
                }


                return imgUrls.toString();
            }
            while (true){
                mainImgEl = driver.findElement(By.cssSelector("div[class='gallery-placeholder']"));
                mainImgEl = mainImgEl.findElement(By.cssSelector("div[class='fotorama__stage__shaft fotorama__grab']"));
                mainImgEl = mainImgEl.findElement(By.cssSelector("div[data-active='true']"));
                while (mainImgEl.findElements(By.tagName("img")).size()==0){
                    bad_sleep(50);
                }
                mainImgEl = mainImgEl.findElement(By.tagName("img"));
                tempUrl = mainImgEl.getAttribute("src");
                if (!tempUrl.equals(prevImgUrl)){
                    break;
                }
            }
            prevImgUrl = tempUrl;
            if (mainImgUrl.equals(tempUrl)){
                break;
            }
            else {
                imgUrls.append(tempUrl);
                imgUrls.append("\n");
            }

        }

        String imgUrlsStr = imgUrls.toString();
        int size = imgUrlsStr.length();
        if (size>0){
            imgUrls.deleteCharAt(size-1);
        }


        return imgUrls.toString();
    }

    private String getMainImgUrl(WebDriver driver) {
        String mainImgUrl = "";
        WebElement mainImgEl = null;
       // System.out.println("waiting for gallery");
        int counter = 0;
        while(true){
            try {
                mainImgEl = driver.findElement(By.cssSelector("div[class='gallery-placeholder']"));
                break;
            }
            catch(NoSuchElementException e){
                    counter++;
                    if (counter==20){
                        return "";
                    }
                    bad_sleep(500);
            }
        }

        counter = 0;
        while (mainImgEl.findElements(By.cssSelector("div[class='fotorama__stage__shaft fotorama__grab']")).size()==0){
            bad_sleep(100);
            counter++;
            if (counter==100){
                break;
            }
        }
        if (counter!=100){
            mainImgEl = mainImgEl.findElement(By.cssSelector("div[class='fotorama__stage__shaft fotorama__grab']"));
        }
        else {
            mainImgEl = mainImgEl.findElement(By.cssSelector("div[class='fotorama__stage__shaft']"));
        }
        mainImgEl = mainImgEl.findElement(By.cssSelector("div[data-active='true']"));
       // System.out.println("searching for img tag");
        while (mainImgEl.findElements(By.tagName("img")).size()==0){
            bad_sleep(50);
        }
        mainImgEl = mainImgEl.findElement(By.tagName("img"));
        mainImgUrl = mainImgEl.getAttribute("src");

        return mainImgUrl;
    }

    private String getDescription(WebDriver driver) {
        String desc = "";
        WebElement descEl = driver.findElement(By.cssSelector("div[class='product attribute description']"));
        descEl = descEl.findElement(By.className("value"));
        desc = descEl.getAttribute("innerHTML");
        desc = StringUtils.substringAfter(desc,"\n");

        return desc;
    }

    private String getItemName(WebDriver driver) {
        String itemName = "";
        WebElement itemNameEl = driver.findElement(By.cssSelector("span[data-ui-id='page-title-wrapper']"));

        itemName = itemNameEl.getText();
        itemName = StringUtils.substringAfter(itemName," - ");

        return itemName;
    }

    private String getSku(WebDriver driver) {
        WebElement skuEl = driver.findElement(By.cssSelector("div[class='value']"));

        return skuEl.getText();
    }

    private void logUnexpectedData(String message, String currentUrl) {
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

    private void bad_sleep(int delay) {
        try {
            Thread.sleep(delay);
        } catch (Exception ignored) {
        }
    }

    public ItemBuilder(String category, String categoryLink, String subCategoryName) {
        this.category = category;
        this.categoryLink = categoryLink;
        this.subCategoryName = subCategoryName;
    }
}
