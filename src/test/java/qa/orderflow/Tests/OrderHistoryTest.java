package qa.orderflow.Tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import qa.orderflow.examples.pom_test_cases.base_test_class;
import qa.orderflow.pages.OrderHistory;

import java.util.Scanner;

public class OrderHistoryTest extends base_test_class {

    String downloadPath = "C:\\First Degree\\fourth year\\SemsterB\\QA\\qa3";
    ChromeDriver driver = new ChromeDriver();

    @Before
    public void setUp() throws Exception{
        driver.get("https://nano-flow-order-direct.base44.app/");
    }

    @Test
    public void exportCsvWithoutOrders() {

        OrderHistory orderHistory = new OrderHistory(driver);
        orderHistory.clickHistoryBtn();
        Assert.assertFalse(orderHistory.isExportCsvEnabled());
    }

    @Test
    public void exportCsvWithOneOrder() {
        OrderHistory orderHistory = new OrderHistory(driver);
        orderHistory.clickHistoryBtn();
        orderHistory.clickExportCsv();
        Assert.assertTrue(orderHistory.isCsvFileDownloaded(downloadPath));
    }

    @Test
    public void exportCsvWithManyOrders() {
        OrderHistory orderHistory = new OrderHistory(driver);
        orderHistory.clickHistoryBtn();
        orderHistory.clickExportCsv();
        Assert.assertTrue(orderHistory.isCsvFileDownloaded(downloadPath));
    }
}