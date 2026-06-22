package qa.orderflow.Tests;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import qa.orderflow.pages.ReturnProductPage;
import qa.orderflow.pages.new_order_page;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class ReturnProductTest {
    private WebDriver driver;
    private JSONArray products;
    Logger logger = LogManager.getLogger(ReturnProductTest.class);

    public void loadProductsFromJson(String fileName) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        FileReader reader;
        reader = new FileReader(fileName);

        //Read JSON file
        products = (JSONArray) jsonParser.parse(reader);
    }

    @Before
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @After
    public void tearDown() {
        driver.quit();
    }

    public void createOrderFromJson(String jsonFile) {
        try {
            loadProductsFromJson(jsonFile);
            logger.info("Loading products from {}",
                    jsonFile);
        } catch (ParseException | IOException e) {
            logger.error("Error loading products from {}\nwith error: {}",
                    jsonFile, e);
            Assert.fail("Failed to load products from JSON: " + e.getMessage());
        }

        new_order_page page = new new_order_page(driver);

        for (Object product : products) {
            JSONObject obj = (JSONObject) product;

            String category = (String) obj.get("category");
            int quantity = ((Number) obj.getOrDefault("orderedQuantity", obj.get("quantity"))).intValue();
            String name = (String) obj.get("name");

            logger.info("adding product {} ({})",
                    name, quantity);

            try {
                page.selectCategory(category);
            } catch (NoSuchElementException e) {
                logger.error("category {} doesn't exist", category);
                continue;
            }

            String id = page.addProductToCart(name);
            page.addQuantityToCart(id, quantity);

            logger.debug("product added to cart",
                    name);
        }

        logger.debug("finished adding products");

        page.submitOrder();

        List<String> errors = page.getErrorsIfPresent();
        if (!errors.isEmpty()) {
            errors.forEach(error ->
                    logger.error("website error: {}", error));
            Assert.fail("Website validation errors:\n" + String.join("\n", errors));
        }

        page.confirmOrder();
        logger.info("confirmed order");
    }

    @Test
    public void returnProductWithoutOrders() {
        String jsonFile = "return-product-without-orders.json";

        try {
            loadProductsFromJson(jsonFile);
            logger.info("Loading products from {}",
                    jsonFile);
        } catch (ParseException | IOException e) {
            logger.error("Error loading products from {}\nwith error: {}",
                    jsonFile, e);
            Assert.fail("Failed to load products from JSON: " + e.getMessage());
        }

        ReturnProductPage returnPage = new ReturnProductPage(driver);
        driver.get("https://nano-flow-order-direct.base44.app/returns");
        Assert.assertFalse(returnPage.isReturnAvailable());
        logger.info("return product is not available without orders");
    }

    @Test
    public void returnTwoProductsWhenFiveWereOrdered() {
        String jsonFile = "return-product-two-of-five.json";
        createOrderFromJson(jsonFile);

        JSONObject obj = (JSONObject) products.get(0);
        String name = (String) obj.get("name");
        int quantity = ((Number) obj.get("quantity")).intValue();

        ReturnProductPage returnPage = new ReturnProductPage(driver);
        returnPage.clickReturnsBtn();
        returnPage.selectProductByText(name);
        returnPage.enterReturnQuantity(String.valueOf(quantity));
        returnPage.clickSubmitReturn();

        boolean returnProcessed = returnPage.isReturnProcessedSuccessfully();
        if (!returnProcessed && returnPage.isErrorMessageDisplayed()) {
            logger.error("return was rejected by the website instead of completed successfully");
        }
        Assert.assertTrue(returnProcessed);
        logger.info("returned {} products from {} successfully",
                quantity, name);
    }

    @Test
    public void returnFiveProductsWhenOnlyTwoWereOrdered() {
        String jsonFile = "return-product-five-of-two.json";
        createOrderFromJson(jsonFile);

        JSONObject obj = (JSONObject) products.getFirst();
        String name = (String) obj.get("name");
        int quantity = ((Number) obj.get("quantity")).intValue();

        ReturnProductPage returnPage = new ReturnProductPage(driver);
        returnPage.clickReturnsBtn();
        returnPage.selectProductByText(name);
        returnPage.enterReturnQuantity(String.valueOf(quantity));
        returnPage.clickSubmitReturn();

        Assert.assertTrue(returnPage.isErrorMessageDisplayed());
        logger.info("return of {} products from {} was rejected",
                quantity, name);
    }

    @Test
    public void returnZeroProducts() {
        String jsonFile = "return-product-zero.json";
        createOrderFromJson(jsonFile);

        JSONObject obj = (JSONObject) products.getFirst();
        String name = (String) obj.get("name");
        int quantity = ((Number) obj.get("quantity")).intValue();

        ReturnProductPage returnPage = new ReturnProductPage(driver);
        returnPage.clickReturnsBtn();
        returnPage.selectProductByText(name);
        returnPage.enterReturnQuantity(String.valueOf(quantity));

        String actualQuantityValue = returnPage.getReturnQuantityValue();
        Assert.assertNotEquals("0", actualQuantityValue);
        logger.info("return quantity {} for {} was prevented by the website. actual input value is {}",
                quantity, name, actualQuantityValue);
    }
}
