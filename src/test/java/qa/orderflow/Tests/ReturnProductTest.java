package qa.orderflow.Tests;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.bidi.log.Log;
import org.openqa.selenium.chrome.ChromeDriver;
import qa.orderflow.pages.ReturnProductPage;

import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class ReturnProductTest {
    private JSONArray products;
    ChromeDriver driver = new ChromeDriver();

    public void loadProductsFromJson(String fileName) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        FileReader reader;
        reader = new FileReader(fileName);
        products = (JSONArray) jsonParser.parse(reader);
    }

    @Before
    public void setUp() throws Exception{
        driver.get("https://nano-flow-order-direct.base44.app/returns");
        driver.manage().window().maximize();
        try {
            loadProductsFromJson("ReturnProduct.json");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            Assert.fail("Failed to load products from JSON: " + e.getMessage());

        }
    }

    @Test
    public void returnProductWithoutOrders() {
        ReturnProductPage returnPage = new ReturnProductPage(driver);
        Assert.assertFalse(returnPage.isReturnAvailable());
    }

    @Test
    public void returnTwoProductsWhenFiveWereOrdered() {
        ReturnProductPage returnPage = new ReturnProductPage(driver);
        System.out.println("Create order manually with quantity 5.");
        System.out.println("After the order is approved, press ENTER here.");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        returnPage.clickReturnsBtn();
        returnPage.selectProductByIndex(1);
        returnPage.enterReturnQuantity("2");
        returnPage.clickSubmitReturn();
        Assert.assertTrue(returnPage.isSuccessMessageDisplayed());
    }

    @Test
    public void returnFiveProductsWhenOnlyTwoWereOrdered() {
        ReturnProductPage returnPage = new ReturnProductPage(driver);
        System.out.println("Create order manually with quantity 2.");
        System.out.println("After the order is approved, press ENTER here.");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        returnPage.clickReturnsBtn();
        returnPage.selectProductByIndex(1);
        returnPage.enterReturnQuantity("5");
        returnPage.clickSubmitReturn();
        Assert.assertTrue(returnPage.isErrorMessageDisplayed());
    }

    @Test
    public void returnZeroProducts() {
        ReturnProductPage returnPage = new ReturnProductPage(driver);
        System.out.println("Create order manually with quantity 1 or more.");
        System.out.println("After the order is approved, press ENTER here.");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        returnPage.clickReturnsBtn();
        returnPage.selectProductByIndex(1);
        returnPage.enterReturnQuantity("0");
        returnPage.clickSubmitReturn();
        Assert.assertTrue(returnPage.isErrorMessageDisplayed());
    }

}