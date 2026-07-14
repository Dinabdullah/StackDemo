import Pages.LoginPage;
import Pages.ProductPage;
import Utilities.BrowserFactory;
import Utilities.utility;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

public class NavAndUi extends BaseTest {


    private static final String VALID_USER = "demouser";
    private static final String VALID_PASS = "testingisfun99";

    @Test(priority = 1)
    public void LogoRedirectsToProductListing() {
        new LoginPage(driver).open();
        new LoginPage(driver).login(VALID_USER, VALID_PASS);
        new ProductPage().GoToOrdersPage(driver);
        new ProductPage().GoToProductPage(driver);
        Assert.assertTrue(driver.getCurrentUrl().equalsIgnoreCase("https://bstackdemo.com/"),
                "The Logo doesnt Return The User To The Home Page");
    }


    @Test(priority = 2)
    public void TheCartIconOpensTheCartPage() {
        new ProductPage().GoToCartPage(driver);
        Assert.assertTrue(driver.findElement(By.className("float-cart__header")).isDisplayed(),
                "The cart is not displayed");

    }


    @Test(priority = 3)
    public void SignInLinkNavigatesToSignInPage() {
        new ProductPage().GoToSignInPage(driver);
        Assert.assertTrue(utility.isDisplayed(driver, By.id("login-btn")),
                "Expected to land on sign-in page with login button visible");
    }


    @Test(priority = 4)
    public void SignOutLinkNavigatesToSignInPage() {
        new LoginPage(driver).open();
        new LoginPage(driver).login(VALID_USER, VALID_PASS);
        new LoginPage(driver).logout();
        Assert.assertTrue(new LoginPage(driver).isOnSigninPage(), "Sign out should clear session and redirect to /signin");

    }


    @Test(priority = 5)
    public void HomePageNavigatesToHomePage() {

        new ProductPage().GoToOrdersPage(driver);
        String detailUrl = driver.getCurrentUrl();


        driver.navigate().back();
        Assert.assertTrue(new ProductPage().isLoaded(driver),
                "Expected to land back on product listing");

        driver.navigate().forward();
        Assert.assertEquals(driver.getCurrentUrl(), detailUrl,
                "Expected forward navigation to reload the same product detail page");

    }


    @Test(priority = 6)
    public void UnknowmOrInvalidRouteShowsError() {
        driver.get("https://bstackdemo.com/" + "/xyz123");
        String bodyText = driver.findElement(By.tagName("body")).getText().trim();
        Assert.assertFalse(bodyText.isEmpty(),
                "Expected a visible 404/error message, not a blank white page");


    }

    @Test(priority = 7)
    public void signOutClearsCartContents() {

        new LoginPage(driver).open();
        new LoginPage(driver).login(VALID_USER, VALID_PASS);
        utility.scrollingByJsExecutor(driver, By.cssSelector(".shelf-item__buy-btn"));

        new ProductPage().addProductToCartByIndex(driver, 5);
        new ProductPage().addProductToCartByIndex(driver, 6);

        new LoginPage(driver).logout();

        new ProductPage().GoToSignInPage(driver);
        new LoginPage(driver).login(VALID_USER, VALID_PASS);
        new ProductPage().GoToCartPage(driver);

        Assert.assertTrue(utility.isDisplayed(driver, By.xpath("//*[text()='Add some products in the bag ']")),
                "Cart should be empty after logout/login but items persisted");


    }


    @Test(priority = 8)
    public void HeadersRemainsVisible() {

        utility.scrollingByJsExecutor(driver, By.cssSelector(".shelf-item__buy-btn"));
        new ProductPage().isHeaderVisible(driver);
        Assert.assertTrue(new ProductPage().isHeaderVisible(driver),
                "Expected logo, cart icon, and sign-in to remain visible after scrolling");
    }


    @Test(priority = 9)
    public void ProductGridShowsSingleColumnOnMobile() {

        new LoginPage(driver).open();
        new LoginPage(driver).login(VALID_USER, VALID_PASS);

        driver.manage().window().setSize(new Dimension(375, 812));

        int columns = new ProductPage().getColumnsInGrid(driver);
        Assert.assertEquals(columns, 1,
                "Expected a single-column product grid at 375px width");

    }

    @Test(priority = 10)
    public void CartPageIsFullyUsableOnMobile() {
        new LoginPage(driver).open();
        new LoginPage(driver).login(VALID_USER, VALID_PASS);

        driver.manage().window().setSize(new Dimension(375, 812));
        new ProductPage().addProductToCartByIndex(driver, 0);
        Assert.assertFalse(utility.isDisplayed(driver, By.xpath("//*[text()='Add some products in the bag ']")),
                "Cart should be empty after logout/login but items persisted");


    }

    @Test(priority = 11)
    public void DirectURLAccessToCartPageWhileLoggedIn() {

        new LoginPage(driver).open();
        new LoginPage(driver).login(VALID_USER, VALID_PASS);
        driver.get("https://bstackdemo.com/cart");
        Assert.assertFalse(driver.getCurrentUrl().contains("/signin"),
                "Expected cart page to load directly without redirecting to sign-in");

    }

    @Test(priority = 12)
    public void CartIconBadgeUpdatesAfterAddingItem() {
        new LoginPage(driver).open();
        new LoginPage(driver).login(VALID_USER, VALID_PASS);
        utility.scrollingByJsExecutor(driver, By.cssSelector(".shelf-item__buy-btn"));

        new ProductPage().addProductToCartByIndex(driver, 5);


        Assert.assertEquals(new ProductPage().getCartBadgeCount(driver), "1",
                "Expected cart badge to increment to 1 without a page reload");


    }

    @Test(priority = 13)
    public void AllFooterorSocialLinksOpenWithout404() {
        new LoginPage(driver).open();
        new LoginPage(driver).login(VALID_USER, VALID_PASS);

        new ProductPage().scrollToBottom(driver);

        java.util.List<org.openqa.selenium.WebElement> footerLinks =
                driver.findElements(By.cssSelector("footer a"));

        Assert.assertFalse(footerLinks.isEmpty(), "Expected footer links to be present");


        String originalWindow = driver.getWindowHandle();
        for (org.openqa.selenium.WebElement link : footerLinks) {
            String href = link.getDomAttribute("href");
            if (href == null || href.isEmpty() || href.startsWith("javascript")) continue;

            String bodyBefore = driver.findElement(By.tagName("body")).getText();
            link.click();


            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    String pageText = driver.findElement(By.tagName("body")).getText();
                    Assert.assertFalse(pageText.toLowerCase().contains("404"),
                            "BG_N04: Link " + href + " returned a 404 page");
                    driver.close();
                    driver.switchTo().window(originalWindow);

                }


            }
        }

    }
}