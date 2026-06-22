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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CartRemoveTest {
    private WebDriver driver;
    private JSONArray products;
    Logger logger = LogManager.getLogger(CartRemoveTest.class);

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
        driver.quit();
    }

    public List<String> addProductsToCart(new_order_page page){

        ArrayList<String> productId = new ArrayList<>();

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
            productId.add(id);
            page.addQuantityToCart(id, quantity);

            logger.debug("product added to cart",
                    name);
        }

        logger.debug("finished adding products");

        return productId;
    }

    @Test
    public void removeItems() {
        new_order_page page = new new_order_page(driver);
        loadProductsFromJson("cart-remove.json");

        List<String> productIds = addProductsToCart(page);

        int rnd = ThreadLocalRandom.current().nextInt(0, productIds.size());
        String productId = productIds.get(rnd);

        logger.debug("removing product with id {} from cart", productId);
        page.removeProductFromCart(productIds.get(rnd));

        logger.debug("testing product is removed");

        if(page.isProductInCart(productId)){
            logger.error("product with id: {} wasn't removed",  productId);
            Assert.fail("product is wasn't removed!");
        }

        logger.info("product removed from cart");
    }

    @Test
    public void removeWithEmptyCart(){
        new_order_page page = new new_order_page(driver);
        if(page.isCartEmpty()){
            logger.error("Cart is empty couldn't remove items");
            Assert.fail("cart is empty");
        }
    }
}
