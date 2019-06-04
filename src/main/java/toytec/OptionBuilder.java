package toytec;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OptionBuilder {
    private static final Logger logger = LogManager.getLogger(OptionBuilder.class.getName());

    /**
     *
     * @param driver - Driver with opened item page were options surely present.
     * @param itemLink
     * @return
     */
    public List<ToyOption> getOptions(WebDriver driver, String itemLink) throws UnavailableOptionsException {
        openItemPageWithOptions(driver, itemLink);

        return parseOptionsFromPage(driver);
    }

    private List<ToyOption> parseOptionsFromPage(WebDriver driver) throws UnavailableOptionsException {
        List<ToyOption> result = new ArrayList<>();
        List<WebElement> optionGroupsEls = driver.findElements(By.cssSelector("div[class^='field option']"));
        if (optionGroupsEls.size()==0){
            logger.error("No options in opened option tab at " + driver.getCurrentUrl());
            throw new UnavailableOptionsException();
        }
        for (WebElement optionGroupsEl : optionGroupsEls) {
            result.addAll(getOptionsFromGroupEl(optionGroupsEl));
        }


        return result;
    }

    private List<ToyOption> getOptionsFromGroupEl(WebElement optionGroupsEl) throws UnavailableOptionsException {
        List<ToyOption> result = new ArrayList<>();
        String groupName = optionGroupsEl.findElement(By.cssSelector("label")).getText();
        //todo: change locator to make it find only direct child element
        logger.debug("getting options element list");
        List<WebElement> optionList = optionGroupsEl.findElements(By.cssSelector("div[class='field choice']"));
        if (optionList.size()==0){
          optionList.add(optionGroupsEl);
        }
        for (WebElement optionEl : optionList) {
            result.add(buildOption(optionEl, groupName));
        }
        result.forEach(System.out::println);


        return result;
    }

    private ToyOption buildOption(WebElement optionEl, String groupName) throws UnavailableOptionsException {
        ToyOption result = new ToyOption();
        result.setOptionGroup(groupName);

        String optionText = "";
        try {
            optionText = optionEl.findElement(By.className("product-name")).getText();
            result.setOptionName(optionText);
        }
        catch (NoSuchElementException e){
            optionText = optionEl.getText();
            result.setOptionName(optionText);
            return result;
        }
        String rawText = optionEl.getText();
        if (rawText.contains("$")){
            setPrice(rawText, result);
        }
        else {
            result.setPrice(new BigDecimal(0));
        }
        setRedText(optionEl, result);

        return result;
    }

    private void setRedText(WebElement optionEl, ToyOption result) {
        WebElement redTextEl = null;
        try {
            redTextEl = optionEl.findElement(By.cssSelector("font[color='red']"));
            result.setRedText(redTextEl.getText());
        }
        catch (NoSuchElementException e){
            result.setRedText("");
        }
    }

    private void setPrice(String rawText, ToyOption result) {
        String priceStr = StringUtils.substringBetween(rawText, "$", "]");
        if (priceStr==null){
            result.setPrice(new BigDecimal(0));
            logger.error("couldn't get price from string: " + rawText);
        }
        else {
            priceStr = priceStr.replaceAll(",", "");
            priceStr = priceStr.trim();
            double priceDouble;
            try {
                priceDouble = Double.parseDouble(priceStr);
                result.setPrice(new BigDecimal(priceDouble).setScale(2, RoundingMode.HALF_UP));
            }
            catch (NumberFormatException e){
                logger.error("Couldn't extract price from string: " + priceStr);
                result.setPrice(new BigDecimal(0));
            }
        }
    }

    private void openItemPageWithOptions(WebDriver driver, String itemLink) throws UnavailableOptionsException {
        driver.get(itemLink);
        logger.debug("Opening url " + itemLink);

        //waiting for page to load
        WebElement switchButton = null;
        while (true){
            try {
                switchButton = new FluentWait<>(driver)
                        .withTimeout(Duration.ofSeconds(10))
                        .pollingEvery(Duration.ofMillis(2))
                        .ignoring(WebDriverException.class)
                        .until(ExpectedConditions.elementToBeClickable(By.id("bundle-slide")));
                break;
            }
            catch (TimeoutException e){
                if (SileniumUtil.hasConnection()){
                    logger.error("couldn't open item page " + itemLink);
                    throw new UnavailableOptionsException();
                }
            }
        }
        logger.info("Opened item url " + itemLink);

        //switching to options
        switchButton.click();
        logger.debug("[switch to options] button clicked");
        while (true){
            try {
                 new FluentWait<>(driver)
                        .withTimeout(Duration.ofSeconds(10))
                        .pollingEvery(Duration.ofMillis(2))
                        .ignoring(WebDriverException.class)
                        .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("div[class^='field option ']")));
                break;
            }
            catch (TimeoutException e){
                if (SileniumUtil.hasConnection()){
                    logger.error("couldn't open options page for " + itemLink);
                    throw new UnavailableOptionsException();
                }
            }
        }
        logger.info("options opened for item " + itemLink);
    }
}
