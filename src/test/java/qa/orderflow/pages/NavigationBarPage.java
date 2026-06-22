package qa.orderflow.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

public class NavigationBarPage {
    WebDriver driver;
    WebDriverWait wait;
    String baseUrl = "https://nano-flow-order-direct.base44.app";

    public NavigationBarPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void openPage(String path) {
        driver.get(baseUrl + path);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("root")));
    }

    public void clickNavigationButton(String buttonId) {
        By buttonSelector = By.id(buttonId);
        WebElement button = wait.until(ExpectedConditions.presenceOfElementLocated(buttonSelector));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});",
                button
        );

        try {
            wait.until(ExpectedConditions.elementToBeClickable(button)).click();
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
        }
    }

    public void waitForPath(String expectedPath) {
        wait.until(driver -> getCurrentPath().equals(expectedPath));
    }

    public String getCurrentPath() {
        return URI.create(Objects.requireNonNull(driver.getCurrentUrl())).getPath();
    }
}
