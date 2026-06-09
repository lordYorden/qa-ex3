package qa.orderflow.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.List;

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
        wait.until(ExpectedConditions.elementToBeClickable(exportCsvBtn)).click();
    }

    public boolean isExportCsvEnabled() {

        List<WebElement> buttons = driver.findElements(exportCsvBtn);

        if (buttons.isEmpty()) {
            return false;
        }

        return buttons.getFirst().isEnabled();
    }

    public boolean isCsvFileDownloaded(String downloadPath) {
        File folder = new File(downloadPath);
        File[] files = folder.listFiles();

        if (files == null) {
            return false;
        }

        for (File file : files) {
            if (file.getName().endsWith(".csv")) {
                return true;
            }
        }

        return false;
    }
}