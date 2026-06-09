package qa.orderflow.examples;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class loggingSouce {
	
public static WebDriver driver;
	
    public static void main(String[] args) {
    	
         // TODO Auto-generated method stub
    	//System.setProperty("webdriver.chrome.driver","C:\\Users\\acer\\Downloads\\chromedriver_win32\\chromedriver.exe");
    	
    	driver = new ChromeDriver();
        Logger logger=LogManager.getLogger(loggingSouce.class);
         
         driver.get("https://www.saucedemo.com");
		 logger.info("opening website");
       //  driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
		 logger.debug("entering user name");
		
		 driver.findElement(By.id("user-name")).sendKeys("standard_user");
         logger.debug("entering password");
        
         
         driver.findElement(By.xpath("//*[@id=\"password\"]")).sendKeys("secret_sauce");
         
         logger.debug("hitting Button");
         
        
         driver.findElement(By.xpath("//*[@id=\"login-button\"]")).click();
         
		driver.quit();
	}

}
