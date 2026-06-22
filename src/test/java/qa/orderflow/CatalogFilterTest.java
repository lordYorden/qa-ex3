package qa.orderflow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import qa.orderflow.pages.new_order_page;

import java.io.FileReader;
import java.io.IOException;

import static qa.orderflow.NewOrderTests.preformOrder;

public class CatalogFilterTest {
    private WebDriver driver;
    private JSONArray queries;
    Logger logger = LogManager.getLogger(CatalogFilterTest.class);

    public void loadQueriesFromJson(String fileName){
        try {
            JSONParser jsonParser = new JSONParser();
            FileReader reader;
            reader = new FileReader(fileName);

            //Read JSON file
            queries = (JSONArray) jsonParser.parse(reader);
            logger.info("Loading queries from {}",
                    fileName);
        } catch (ParseException | IOException e ) {
            logger.error("Error loading queries from {}\nwith error: {}",
                    fileName, e);
        }
    }

    public JSONArray loadProductsFromJson(String fileName){
        JSONArray products = null;
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

        return products;
    }

    @Before
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @After
    public void tearDown() {
        //driver.quit();
    }


    @Test
    public void testBasicSearch(){
        loadQueriesFromJson("search-apple.json");
        preformSearch();
    }

    @Test
    public void testPriceRange(){
        loadQueriesFromJson("search-range.json");
        preformSearch();
    }

    @Test
    public void testStockSearch(){
        loadQueriesFromJson("search-stock.json");
        new_order_page page = new new_order_page(driver);
        JSONArray products = loadProductsFromJson("order-sold-out.json");
        preformOrder(products, page, logger);
        preformSearch(page);
    }

    public void preformSearch(){
        new_order_page page = new  new_order_page(driver);
        preformSearch(page);
    }

    public void preformSearch(new_order_page page) {
        for (Object query : queries) {
            JSONObject obj = (JSONObject) query;

            String category = (String) obj.get("category");
            String term = (String) obj.get("term");
            int price = ((Number) obj.getOrDefault("price", -1)).intValue();
            boolean inStock = (boolean) obj.getOrDefault("inStock", false);

            page.selectCategory(category);
            page.searchCatalog(term);

            logger.info("searching for {} in category {}", term, category);

            if(inStock){
                page.toggleStock();
                logger.debug("searching only products in stock only");
            }

            if(price > 0){
                page.setPrice(price);
                logger.debug("price rage is set to 0-{}$",price);
            }

            if(page.isCatalogEmpty()){
                logger.error("no items were found");
            }
            else {
                logger.info("items were found");
            }
        }
    }
}
