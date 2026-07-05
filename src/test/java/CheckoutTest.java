import Pages.CheckoutPage;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;
import java.time.Duration;

public class CheckoutTest {

    WebDriver driver;
    CheckoutPage checkoutPage;

    @BeforeMethod
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().deleteAllCookies();

        // Login
        driver.get("https://www.saucedemo.com");
        driver.findElement(By.id("user-name")).sendKeys("standard_user");
        driver.findElement(By.id("password")).sendKeys("secret_sauce");
        driver.findElement(By.id("login-button")).click();

        checkoutPage = new CheckoutPage(driver);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }


    @Test
    public void testSuccessfulCheckout() {
        checkoutPage.addProductToCart();
        checkoutPage.goToCart();
        checkoutPage.clickCheckout();
        checkoutPage.fillInfo("Nada", "Mohamed", "12345");
        checkoutPage.clickContinue();
        checkoutPage.clickFinish();

        String msg = checkoutPage.getSuccessMessage();
        Assert.assertEquals(msg, "Thank you for your order!");
    }


    @Test
    public void testCheckoutEmptyFirstName() {
        checkoutPage.addProductToCart();
        checkoutPage.goToCart();
        checkoutPage.clickCheckout();

        // Fluent Wait
        FluentWait<WebDriver> fluentWait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(10))
                .pollingEvery(Duration.ofMillis(300))
                .ignoring(NoSuchElementException.class);

        fluentWait.until(ExpectedConditions
                .visibilityOfElementLocated(By.id("first-name")));

        driver.findElement(By.id("last-name")).sendKeys("Mohamed");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        Assert.assertTrue(checkoutPage.isErrorDisplayed());
    }


    @Test
    public void testCheckoutEmptyLastName() {
        checkoutPage.addProductToCart();
        checkoutPage.goToCart();
        checkoutPage.clickCheckout();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions
                .visibilityOfElementLocated(By.id("first-name")));

        driver.findElement(By.id("first-name")).sendKeys("Nada");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        Assert.assertTrue(checkoutPage.isErrorDisplayed());
    }
}