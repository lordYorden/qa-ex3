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

    By errorMsg = By.cssSelector("#validation-errors p[id^='validation-error-']");

    public void selectCategory(String category){
        WebElement categoryDropdown = wait.until(ExpectedConditions.elementToBeClickable(catSelect));
        Select drpCat = new Select(categoryDropdown);
        drpCat.selectByVisibleText(category);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("product-grid")));
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
