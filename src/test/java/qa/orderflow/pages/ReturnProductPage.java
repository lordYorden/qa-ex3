package qa.orderflow.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class ReturnProductPage {

    WebDriver driver;

    public ReturnProductPage(WebDriver driver) {
        this.driver = driver;
    }

    By returnsBtn = By.xpath("//*[@id=\"nav-returns\"]");

    By productDropdown = By.xpath("//*[@id=\"return-product-select\"]");

    By quantityInput = By.xpath("//*[@id=\"return-quantity-input\"]");

    By submitReturnBtn = By.xpath("//*[@id=\"btn-submit-return\"]");

    By successMessage = By.xpath("//*[contains(text(),'success') or contains(text(),'בוצע') or contains(text(),'הוחזר')]");

    By errorMessage = By.xpath("//*[contains(text(),'error') or contains(text(),'שגיאה') or contains(text(),'נדחתה') or contains(text(),'לא ניתן')]");

    public void clickReturnsBtn() {
        driver.findElement(returnsBtn).click();
    }

    public void selectProductByText(String productName) {
        Select select = new Select(driver.findElement(productDropdown));
        select.selectByVisibleText(productName);
    }

    public void selectProductByIndex(int index) {
        Select select = new Select(driver.findElement(productDropdown));
        select.selectByIndex(index);
    }

    public boolean isReturnAvailable() {
        List<WebElement> buttons = driver.findElements(submitReturnBtn);
        return !buttons.isEmpty() && buttons.getFirst().isEnabled();
    }

    public void enterReturnQuantity(String quantity) {
        driver.findElement(quantityInput).clear();
        driver.findElement(quantityInput).sendKeys(quantity);
    }

    public void clickSubmitReturn() {
        driver.findElement(submitReturnBtn).click();
    }

    public boolean isSuccessMessageDisplayed() {
        return !driver.findElements(successMessage).isEmpty();
    }

    public boolean isErrorMessageDisplayed() {
        return !driver.findElements(errorMessage).isEmpty();
    }
}