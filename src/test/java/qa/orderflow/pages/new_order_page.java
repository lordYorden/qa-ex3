package qa.orderflow.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class new_order_page {

    WebDriver driver;
    private final WebDriverWait wait;

    public new_order_page(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get("https://nano-flow-order-direct.base44.app/order");
    }

    By catSelect = By.xpath("//*[@id=\"category-select\"]");
    
    By submitBtn = By.xpath("//*[@id=\"btn-submit-order\"]");

    By confirmBtn = By.xpath("//*[@id=\"btn-confirm-order\"]");

    By validationDialog = By.id("validation-dialog");

    By search = By.id("product-search");
    By stock_toggle = By.id("in-stock-toggle");
    By price_slider = By.id("price-range-slider");
    By price_label = By.id("price-range-label");
    By empty = By.id("products-empty");

    By errorMsg = By.cssSelector("#validation-errors p[id^='validation-error-']");

    By emptyCart = By.xpath("//*[normalize-space(.)='No products selected yet. Browse and add products above.']");

    public void selectCategory(String category){
        WebElement categoryDropdown = wait.until(ExpectedConditions.elementToBeClickable(catSelect));
        Select drpCat = new Select(categoryDropdown);
        drpCat.selectByVisibleText(category);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.id("product-grid")),
                ExpectedConditions.presenceOfElementLocated(empty)
        ));
    }

    public String addProductToCart(String name){
        String productCardXpath =
                "//div[@id='product-grid']" +
                "//div[starts-with(@id,'product-card-') and .//h3[normalize-space(.)=" + xpathText(name) + "]]";

        WebElement productCard = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath(productCardXpath))
        );
        String productId = productCard.getAttribute("id").replace("product-card-", "");

        WebElement addButton = productCard.findElement(By.xpath(".//button[normalize-space(.)='Add to Order']"));
        clickElement(addButton);

        return productId;
    }

    public void addQuantityToCart(String productId, int quantity){
        By quantityInput = By.id("quantity-input-" + productId);

        WebElement input = wait.until(
                ExpectedConditions.presenceOfElementLocated(quantityInput));

        input.sendKeys(Keys.CONTROL + "a");
        input.sendKeys(String.valueOf(quantity));
    }

    public void removeProductFromCart(String productId){
        By quantityInput = By.id("remove-item-" + productId);

        WebElement input = wait.until(
                ExpectedConditions.presenceOfElementLocated(quantityInput));

        input.click();
    }

    public boolean isProductInCart(String productId){
        try {
            By cardId = By.id("order-item-" + productId);
            WebElement productCard = driver.findElement(cardId);

            return productCard.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCartEmpty(){
        try {
            WebElement msg = wait.until(ExpectedConditions.presenceOfElementLocated(emptyCart));
            return msg.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCatalogEmpty(){
        try {
            WebElement msg = wait.until(ExpectedConditions.presenceOfElementLocated(empty));
            return msg.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void searchCatalog(String searchTerm) {
        WebElement input = wait.until(
                ExpectedConditions.presenceOfElementLocated(search));
        input.sendKeys(Keys.CONTROL + "a");
        input.sendKeys(String.valueOf(searchTerm));
    }

    public void toggleStock() {
        WebElement input = wait.until(
                ExpectedConditions.presenceOfElementLocated(stock_toggle));
        input.click();
    }

    public void setPrice(int price) {
        WebElement input = wait.until(
                ExpectedConditions.elementToBeClickable(price_slider));

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});" +
                        "const nativeInputValueSetter = Object.getOwnPropertyDescriptor(" +
                        "HTMLInputElement.prototype, 'value').set;" +
                        "nativeInputValueSetter.call(arguments[0], arguments[1]);" +
                        "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
                        "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));" +
                        "arguments[0].blur();",
                input,
                String.valueOf(price)
        );

        wait.until(ExpectedConditions.attributeToBe(input, "value", String.valueOf(price)));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(price_label, "$" + price));

        ((JavascriptExecutor) driver).executeScript("document.body.focus();");
    }

    public void submitOrder(){
        clickButtonWithSelector(submitBtn);
    }

    public void confirmOrder(){
        clickButtonWithSelector(confirmBtn);
    }

    public void clickButtonWithSelector(By btn){
        clickElement(wait.until(ExpectedConditions.elementToBeClickable(btn)));
    }

    private void clickElement(WebElement element) {
        WebElement clickableElement = wait.until(ExpectedConditions.elementToBeClickable(element));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});",
                clickableElement
        );
        wait.until(ExpectedConditions.elementToBeClickable(clickableElement)).click();
    }

    public List<String> getErrorsIfPresent(){
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(1));

        try {
            shortWait.until(ExpectedConditions.visibilityOfElementLocated(validationDialog));
        } catch (org.openqa.selenium.TimeoutException e) {
            return List.of();
        }

        return driver.findElements(errorMsg)
                .stream()
                .map(WebElement::getText)
                .toList();
    }

    private String xpathText(String text) {
        if (!text.contains("'")) {
            return "'" + text + "'";
        }

        if (!text.contains("\"")) {
            return "\"" + text + "\"";
        }

        String[] parts = text.split("'");
        StringBuilder xpath = new StringBuilder("concat(");

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                xpath.append(", \"'\", ");
            }
            xpath.append("'").append(parts[i]).append("'");
        }

        xpath.append(")");
        return xpath.toString();
    }
}
