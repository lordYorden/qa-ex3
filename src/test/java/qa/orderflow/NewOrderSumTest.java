package qa.orderflow;

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

public class NewOrderSumTest {
    private WebDriver driver;
    private JSONArray products;
    Logger logger = LogManager.getLogger(NewOrderSumTest.class);

    public void loadProductsFromJson(String fileName) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        FileReader reader;
        reader = new FileReader(fileName);

        //Read JSON file
        products = (JSONArray) jsonParser.parse(reader);
    }

    @Before
    public void setUp()
    {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        String jsonFile = "category-normal.json";

        try {
            loadProductsFromJson(jsonFile);
            logger.info("Loading products from {}",
                    jsonFile);

        } catch (ParseException | IOException e ) {
            logger.error("Error loading products from {}\nwith error: {}",
                    jsonFile, e);
        }
    }

    @After
    public void tearDown()
    {
        driver.quit();
    }

    @Test
    public void sumTest() {
        new_order_page page = new new_order_page(driver);

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
