package qa.orderflow.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;

public class OrderHistory {

    WebDriver driver;
    WebDriverWait wait;

    public OrderHistory(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    By historyBtn = By.xpath("//*[@id=\"nav-order-history\"]");
    By exportCsvBtn = By.xpath("//*[@id=\"btn-export-csv\"]");

    public void clickHistoryBtn() {
        wait.until(ExpectedConditions.elementToBeClickable(historyBtn)).click();
    }

    public void clickExportCsv() {
        WebElement exportButton = wait.until(ExpectedConditions.presenceOfElementLocated(exportCsvBtn));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});",
                exportButton
        );

        try {
            wait.until(ExpectedConditions.elementToBeClickable(exportButton)).click();
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", exportButton);
        }
    }

    public boolean isExportCsvEnabled() {
        return !driver.findElements(exportCsvBtn).isEmpty()
                && driver.findElement(exportCsvBtn).isEnabled();
    }

    public boolean isCsvFileDownloaded(String downloadPath) {
        long endTime = System.currentTimeMillis() + 10000;

        while (System.currentTimeMillis() < endTime) {
            File folder = new File(downloadPath);
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.getName().startsWith("order_history")
                            && file.getName().endsWith(".csv")
                            && file.length() > 0) {
                        return true;
                    }
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return false;
    }
}
