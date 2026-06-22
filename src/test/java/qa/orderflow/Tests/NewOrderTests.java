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
import qa.orderflow.pages.new_order_page;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class NewOrderTests {
    private WebDriver driver;
    private JSONArray products;
    Logger logger = LogManager.getLogger(NewOrderTests.class);

    public void loadProductsFromJson(String fileName){
        try {
            JSONParser jsonParser = new JSONParser();
            FileReader reader;
            reader = new FileReader(fileName);

            //Read JSON file
            products = (JSONArray) jsonParser.parse(reader);
            logger.info("Loading products from {}",
                    fileName);
        } catch (ParseException | IOException e ) {
            logger.error("Error loading products from {}\nwith error: {}",
                    fileName, e);
        }
    }

    @Before
    public void setUp()
    {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @After
    public void tearDown()
    {
        products.clear();
        driver.quit();
    }

    @Test
    public void orderBelowSum(){
        loadProductsFromJson("order-below-sum.json");
        preformOrder();
    }

    @Test
    public void orderAboveSum(){
        loadProductsFromJson("order-above-sum.json");
        preformOrder();
    }

    @Test
    public void orderAboveStock(){
        loadProductsFromJson("order-above-stock.json");
        preformOrder();
    }

    @Test
    public void orderBelowStock(){
        loadProductsFromJson("order-below-stock.json");
        preformOrder();
    }

    @Test
    public void orderCategoryGroceries(){
        loadProductsFromJson("order-category-groceries.json");
        preformOrder();
    }

    @Test
    public void orderCategoryFurniture(){
        loadProductsFromJson("order-category-furniture.json");
        preformOrder();
    }

    @Test
    public void orderCategoryCombined(){
        loadProductsFromJson("order-category-combined.json");
        preformOrder();
    }

    public void preformOrder(){
        new_order_page page = new new_order_page(driver);
        preformOrder(products, page, logger);
    }

    public static void preformOrder(JSONArray products, new_order_page page, Logger logger) {
        for (Object product : products) {
            JSONObject obj = (JSONObject) product;

            String category = (String) obj.get("category");
            int quantity = ((Number) obj.get("quantity")).intValue();
            String name = (String) obj.get("name");

            logger.info("adding product {} ({})",
                    name, quantity);

            try{
                page.selectCategory(category);
            }catch(NoSuchElementException e){
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
}
