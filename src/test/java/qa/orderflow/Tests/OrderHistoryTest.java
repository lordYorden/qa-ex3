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
import org.openqa.selenium.chrome.ChromeOptions;
import qa.orderflow.pages.OrderHistory;
import qa.orderflow.pages.new_order_page;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderHistoryTest {
    private WebDriver driver;
    private JSONArray products;

    // Create a path reference
    Path path = Path.of("downloads");
    String downloadPath = path.toAbsolutePath().toString();
    Logger logger = LogManager.getLogger(OrderHistoryTest.class);

    public void loadProductsFromJson(String fileName) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        FileReader reader;
        reader = new FileReader(fileName);

        //Read JSON file
        products = (JSONArray) jsonParser.parse(reader);
    }

    @Before
    public void setUp() {

        logger.info("download path is: {}", downloadPath);

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloadPath);
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.directory_upgrade", true);
        prefs.put("safebrowsing.enabled", true);

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", prefs);

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
    }

    @After
    public void tearDown() {
        driver.quit();
    }

    public void createOrdersFromJson(String jsonFile) {
        try {
            loadProductsFromJson(jsonFile);
            logger.info("Loading products from {}",
                    jsonFile);
        } catch (ParseException | IOException e) {
            logger.error("Error loading products from {}\nwith error: {}",
                    jsonFile, e);
            Assert.fail("Failed to load products from JSON: " + e.getMessage());
        }

        if (products.isEmpty()) {
            logger.info("no orders to create from {}",
                    jsonFile);
            return;
        }

        int orderCount = products.size();
        new_order_page page = new new_order_page(driver);

        for (int i = 0; i < products.size(); i++) {
            JSONObject obj = (JSONObject) products.get(i);

            String category = (String) obj.get("category");
            int quantity = ((Number) obj.get("quantity")).intValue();
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

            logger.debug("finished adding products");

            page.submitOrder();

            List<String> errors = page.getErrorsIfPresent();
            if (!errors.isEmpty()) {
                errors.forEach(error ->
                        logger.error("website error: {}", error));
                logger.error("order creation failed before csv export. The csv validation did not run because the website rejected the order.");
                Assert.fail("Website validation errors:\n" + String.join("\n", errors));
            }

            page.confirmOrder();
            logger.info("confirmed order {} from {}",
                    i + 1, orderCount);
        }
    }

    public void deleteCsvFiles() {
        File folder = new File(downloadPath);
        File[] files = folder.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.getName().startsWith("order_history") && file.getName().endsWith(".csv")) {
                logger.info("deleting old csv file {}",
                        file.getName());
                file.delete();
            }
        }
    }

    public File getDownloadedCsvFile() {
        File folder = new File(downloadPath);
        File[] files = folder.listFiles();

        if (files == null) {
            return null;
        }

        for (File file : files) {
            if (file.getName().startsWith("order_history") && file.getName().endsWith(".csv")) {
                return file;
            }
        }

        return null;
    }

    public void assertCsvMatchesJson() {
        File csvFile = getDownloadedCsvFile();
        Assert.assertNotNull("CSV file was not found", csvFile);
        logger.info("validating csv file {} matches json data",
                csvFile.getName());

        try {
            List<String> lines = Files.readAllLines(Path.of(csvFile.getPath()));
            Assert.assertFalse("CSV file is empty", lines.isEmpty());
            Assert.assertEquals("\"Order ID\",\"Date\",\"Status\",\"Products\",\"Total\"",
                    lines.get(0));
            logger.info("csv header is valid: {}",
                    lines.get(0));

            List<String> csvRows = lines.subList(1, lines.size());
            List<String> unmatchedCsvRows = new ArrayList<>(csvRows);
            List<String> expectedProducts = new ArrayList<>();
            logger.info("csv contains {} order rows, json contains {} products",
                    csvRows.size(), products.size());
            Assert.assertTrue("CSV has fewer rows than JSON orders",
                    csvRows.size() >= products.size());

            for (Object product : products) {
                JSONObject obj = (JSONObject) product;
                String name = (String) obj.get("name");
                int quantity = ((Number) obj.get("quantity")).intValue();
                expectedProducts.add(name + " x" + quantity);
                logger.info("expected product from json: {} x{}",
                        name, quantity);
            }

            int matchedOrders = 0;
            for (String expectedProduct : expectedProducts) {
                boolean found = false;

                for (String row : new ArrayList<>(unmatchedCsvRows)) {
                    if (row.contains("\"Confirmed\"") && row.contains("\"" + expectedProduct + "\"")) {
                        logger.info("matched csv row with json product: {}",
                                row);
                        unmatchedCsvRows.remove(row);
                        matchedOrders++;
                        found = true;
                        break;
                    }
                }

                Assert.assertTrue("CSV does not contain product from JSON: " + expectedProduct,
                        found);
                logger.info("csv product matches json product: {}",
                        expectedProduct);
            }
            Assert.assertEquals("CSV matched order count is different from JSON order count",
                    products.size(), matchedOrders);
            logger.info("csv validation completed successfully: matched {} json orders with {} csv rows",
                    products.size(), matchedOrders);
        } catch (IOException e) {
            logger.error("Error reading csv file {}\nwith error: {}",
                    csvFile.getName(), e);
            Assert.fail("Failed to read CSV file: " + e.getMessage());
        }
    }

    @Test
    public void exportCsvWithoutOrders() {
        String jsonFile = "order-history-export-csv-without-orders.json";
        createOrdersFromJson(jsonFile);

        OrderHistory orderHistory = new OrderHistory(driver);
        driver.get("https://nano-flow-order-direct.base44.app/");
        orderHistory.clickHistoryBtn();

        Assert.assertFalse(orderHistory.isExportCsvEnabled());
        logger.info("export csv is not available without orders");
    }

    @Test
    public void exportCsvWithOneOrder() {
        String jsonFile = "order-history-export-csv-one-order.json";
        createOrdersFromJson(jsonFile);
        deleteCsvFiles();
        OrderHistory orderHistory = new OrderHistory(driver);
        orderHistory.clickHistoryBtn();
        orderHistory.clickExportCsv();
        Assert.assertTrue(orderHistory.isCsvFileDownloaded(downloadPath));
        assertCsvMatchesJson();
        logger.info("csv file exported successfully with one order");
    }

    @Test
    public void exportCsvWithManyOrders() {
        String jsonFile = "order-history-export-csv-many-orders.json";
        createOrdersFromJson(jsonFile);
        deleteCsvFiles();

        OrderHistory orderHistory = new OrderHistory(driver);
        orderHistory.clickHistoryBtn();
        orderHistory.clickExportCsv();

        Assert.assertTrue(orderHistory.isCsvFileDownloaded(downloadPath));
        assertCsvMatchesJson();
        logger.info("csv file exported successfully with many orders");
    }
}
