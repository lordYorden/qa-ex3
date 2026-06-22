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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import qa.orderflow.pages.NavigationBarPage;

import java.io.FileReader;
import java.io.IOException;

public class NavigationBarTest {
    private WebDriver driver;
    private JSONArray navigationSteps;
    Logger logger = LogManager.getLogger(NavigationBarTest.class);

    public void loadNavigationStepsFromJson(String fileName) {
        try {
            JSONParser jsonParser = new JSONParser();
            FileReader reader = new FileReader(fileName);

            //Read JSON file
            navigationSteps = (JSONArray) jsonParser.parse(reader);
            logger.info("Loading navigation steps from {}",
                    fileName);
        } catch (ParseException | IOException e) {
            logger.error("Error loading navigation steps from {}\nwith error: {}",
                    fileName, e);
            Assert.fail("Failed to load navigation steps from JSON: " + e.getMessage());
        }
    }

    @Before
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @After
    public void tearDown() {
        navigationSteps.clear();
        //driver.quit();
    }

    @Test
    public void navigationBar() throws InterruptedException {
        String jsonFile = "navigation-bar.json";
        loadNavigationStepsFromJson(jsonFile);

        NavigationBarPage page = new NavigationBarPage(driver);

        for (Object step : navigationSteps) {
            JSONObject obj = (JSONObject) step;

            String description = (String) obj.get("description");
            String startPath = (String) obj.get("startPath");
            String buttonId = (String) obj.get("buttonId");
            String expectedPath = (String) obj.get("expectedPath");
            boolean expectChange = (Boolean) obj.get("expectChange");

            logger.info("starting navigation step: {}",
                    description);
            page.openPage(startPath);
            Thread.sleep(2000);
            String pathBeforeClick = page.getCurrentPath();
            logger.info("current page before click: {}",
                    pathBeforeClick);

            page.clickNavigationButton(buttonId);
            Thread.sleep(2000);
            page.waitForPath(expectedPath);

            String pathAfterClick = page.getCurrentPath();
            logger.info("current page after click: {}",
                    pathAfterClick);

            Assert.assertEquals(expectedPath, pathAfterClick);

            if (expectChange) {
                Assert.assertNotEquals(pathBeforeClick, pathAfterClick);
                logger.info("navigation from {} to {} completed successfully",
                        pathBeforeClick, pathAfterClick);
            } else {
                Assert.assertEquals(pathBeforeClick, pathAfterClick);
                logger.info("navigation button kept user on the same page: {}",
                        pathAfterClick);
            }
        }
    }
}
