import Pages.LoginPage;
import Utilities.BrowserFactory;
import Utilities.utility;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

public class BaseTest {

    WebDriver driver;
    @BeforeMethod
    public void setup(){
        driver = BrowserFactory.browerSetup("chrome");
        utility.implicitWait(driver);
        driver.get("https://bstackdemo.com/");
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);





    }




    @AfterMethod
    public void quit(){
        if (driver != null){

            driver.quit();

        }
    }



}
