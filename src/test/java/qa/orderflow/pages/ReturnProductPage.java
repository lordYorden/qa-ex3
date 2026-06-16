package qa.orderflow.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class ReturnProductPage {

    WebDriver driver;
    WebDriverWait wait;

    public ReturnProductPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    By returnsBtn = By.xpath("//*[@id=\"nav-returns\"]");

    By productDropdown = By.xpath("//*[@id=\"return-product-select\"]");

    By quantityInput = By.xpath("//*[@id=\"return-quantity-input\"]");

    By submitReturnBtn = By.xpath("//*[@id=\"btn-submit-return\"]");

    By successMessage = By.xpath(
            "//*[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'success')" +
                    " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'returned')" +
                    " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'return processed')" +
                    " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'return completed')]"
    );

    By errorMessage = By.xpath(
            "//*[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'error')" +
                    " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'rejected')" +
                    " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'cannot')" +
                    " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'invalid')" +
                    " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'exceeds')]"
    );

    public void clickReturnsBtn() {
        wait.until(ExpectedConditions.elementToBeClickable(returnsBtn)).click();
    }

    public void selectProductByText(String productName) {
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(productDropdown));
        Select select = new Select(dropdown);

        for (WebElement option : select.getOptions()) {
            if (option.getText().contains(productName)) {
                select.selectByVisibleText(option.getText());
                return;
            }
        }

        String availableOptions = select.getOptions()
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.joining(", "));
        throw new NoSuchElementException(
                "Cannot locate option containing text: " + productName
                        + ". Available options: " + availableOptions
        );
    }

    public void selectProductByIndex(int index) {
        Select select = new Select(wait.until(ExpectedConditions.elementToBeClickable(productDropdown)));
        select.selectByIndex(index);
    }

    public boolean isReturnAvailable() {
        List<WebElement> buttons = driver.findElements(submitReturnBtn);
        return !buttons.isEmpty() && buttons.getFirst().isEnabled();
    }

    public void enterReturnQuantity(String quantity) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(quantityInput));
        input.clear();
        input.sendKeys(quantity);
    }

    public String getReturnQuantityValue() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(quantityInput))
                .getAttribute("value");
    }

    public void clickSubmitReturn() {
        wait.until(ExpectedConditions.elementToBeClickable(submitReturnBtn)).click();
    }

    public boolean isSuccessMessageDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isReturnProcessedSuccessfully() {
        try {
            wait.until(driver -> {
                WebElement dropdown = driver.findElement(productDropdown);
                String selectedValue = dropdown.getAttribute("value");
                return selectedValue == null || selectedValue.isEmpty();
            });
            return driver.findElements(quantityInput).isEmpty()
                    && driver.findElements(submitReturnBtn).isEmpty();
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isErrorMessageDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }
}
