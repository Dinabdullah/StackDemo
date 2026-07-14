import Pages.CheckoutPage;
import Pages.LoginPage;
import Pages.ProductPage;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Automation for the CheckOut component - Group B test cases (TC-01 .. TC-20).
 *
 * CONFIRMED REAL FLOW (see CheckoutPage.java header for full detail):
 *   Bag panel -> "CHECKOUT" (.buy-btn) -> /checkout shipping form (Submit) ->
 *   /confirmation page directly (no separate Order-Summary-with-Finish step).
 *
 * A few test cases from the original sheet don't map 1:1 onto this real app,
 * so they've been adapted (documented per-test below):
 *   - TC-08 / TC-11 ("Cancel"): no Cancel button exists on the shipping form
 *     (confirmed via screenshot) - browser back navigation is used instead.
 *   - TC-10 (price calc): the site shows Total (USD) only, no separate
 *     Subtotal/Tax lines - so this verifies the Bag panel's SUBTOTAL matches
 *     the checkout page's Total (USD) instead.
 *   - TC-12 ("Finish"): Submit IS Finish - there's no separate step.
 */
public class CheckoutTest {

    private WebDriver driver;
    private LoginPage loginPage;
    private ProductPage productPage;
    private CheckoutPage checkoutPage;

    private static final String VALID_USER = "demouser";
    private static final String VALID_PASS = "testingisfun99";

    @BeforeMethod
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().deleteAllCookies();

        loginPage = new LoginPage(driver);
        productPage = new ProductPage();
        checkoutPage = new CheckoutPage(driver);

        loginPage.open();
        loginPage.login(VALID_USER, VALID_PASS);
        loginPage.waitForProductsPage();
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ===================== TC-01 =====================
    @Test(priority = 1)
    @Description("TC-01: Add single product to cart")
    @Severity(SeverityLevel.BLOCKER)
    public void tc01_addSingleProductToCart() {
        addProductToCartSafely(driver, productPage, 0);

        Assert.assertEquals(productPage.getCartBadgeCount(driver), "1",
                "Cart badge should show 1 after adding a single product");

        productPage.GoToCartPage(driver);
        checkoutPage.waitForBagPanelOpen();
        Assert.assertTrue(checkoutPage.isBagOpen(), "Bag panel should open");
    }

    // ===================== TC-02 =====================
    @Test(priority = 2)
    @Description("TC-02: Add multiple products to cart")
    @Severity(SeverityLevel.CRITICAL)
    public void tc02_addMultipleProductsToCart() {
        addProductToCartSafely(driver, productPage, 0);
        addProductToCartSafely(driver, productPage, 1);
        addProductToCartSafely(driver, productPage, 2);

        Assert.assertEquals(productPage.getCartBadgeCount(driver), "3",
                "Cart badge should equal 3 after adding 3 different products");

        productPage.GoToCartPage(driver);
        checkoutPage.waitForBagPanelOpen();
        Assert.assertEquals(checkoutPage.getCartItemCount(), 3,
                "All 3 items should be listed in the Bag panel");
    }

    // ===================== TC-03 =====================
    @Test(priority = 3)
    @Description("TC-03: Complete checkout with valid info")
    @Severity(SeverityLevel.BLOCKER)
    public void tc03_completeWithValidInfo() {
        addOneProductAndOpenCheckoutForm();

        checkoutPage.fillCheckoutForm("John", "Doe", "10001");
        checkoutPage.clickSubmit();

        Assert.assertTrue(checkoutPage.isOnConfirmationPage(),
                "Valid info should submit the order and land on /confirmation");
    }

    // ===================== TC-04 =====================
    @Test(priority = 4)
    @Description("TC-04: Submit with empty First Name")
    @Severity(SeverityLevel.CRITICAL)
    public void tc04_submitWithEmptyFirstName() {
        addOneProductAndOpenCheckoutForm();

        checkoutPage.fillCheckoutForm("", "Doe", "10001");
        checkoutPage.clickSubmit();

        Assert.assertTrue(checkoutPage.isFieldInvalid("firstName"),
                "First Name field should be flagged invalid by the browser. Message: "
                        + checkoutPage.getFieldValidationMessage("firstName"));
        Assert.assertFalse(checkoutPage.isOnConfirmationPage(), "Form should NOT submit while First Name is empty");
    }

    // ===================== TC-05 =====================
    @Test(priority = 5)
    @Description("TC-05: Submit with empty Last Name")
    @Severity(SeverityLevel.CRITICAL)
    public void tc05_submitWithEmptyLastName() {
        addOneProductAndOpenCheckoutForm();

        checkoutPage.fillCheckoutForm("John", "", "10001");
        checkoutPage.clickSubmit();

        Assert.assertTrue(checkoutPage.isFieldInvalid("lastName"),
                "Last Name field should be flagged invalid by the browser. Message: "
                        + checkoutPage.getFieldValidationMessage("lastName"));
        Assert.assertFalse(checkoutPage.isOnConfirmationPage(), "Form should NOT submit while Last Name is empty");
    }

    // ===================== TC-06 =====================
    @Test(priority = 6)
    @Description("TC-06: Submit with empty Zip Code")
    @Severity(SeverityLevel.CRITICAL)
    public void tc06_submitWithEmptyZipCode() {
        addOneProductAndOpenCheckoutForm();

        checkoutPage.fillCheckoutForm("John", "Doe", "");
        checkoutPage.clickSubmit();

        Assert.assertTrue(checkoutPage.isFieldInvalid("zip"),
                "Zip/Postal Code field should be flagged invalid by the browser. Message: "
                        + checkoutPage.getFieldValidationMessage("zip"));
        Assert.assertFalse(checkoutPage.isOnConfirmationPage(), "Form should NOT submit while Zip Code is empty");
    }

    // ===================== TC-07 =====================
    @Test(priority = 7)
    @Description("TC-07: Submit all fields empty")
    @Severity(SeverityLevel.CRITICAL)
    public void tc07_submitAllFieldsEmpty() {
        addOneProductAndOpenCheckoutForm();

        checkoutPage.clickSubmit();

        Assert.assertTrue(checkoutPage.hasValidationError(),
                "Browser should flag at least the First Name field as invalid when all fields are empty");
        Assert.assertFalse(checkoutPage.isOnConfirmationPage(), "Form should NOT submit when all fields are empty");
    }

    // ===================== TC-08 =====================
    @Test(priority = 8)
    @Description("TC-08: Cancel returns to cart (no Cancel button exists on this form - using browser back)")
    @Severity(SeverityLevel.NORMAL)
    public void tc08_cancelReturnsToCart() {
        addOneProductAndOpenCheckoutForm();
        checkoutPage.fillCheckoutForm("John", "Doe", "10001");

        driver.navigate().back();

        Assert.assertFalse(checkoutPage.isOnCheckoutPage(), "Navigating back should leave the /checkout form");
        Assert.assertEquals(productPage.getCartBadgeCount(driver), "1", "Item should still be in the cart");
    }

    // ===================== TC-09 =====================
    @Test(priority = 9)
    @Description("TC-09: Verify item details on the Bag panel")
    @Severity(SeverityLevel.NORMAL)
    public void tc09_verifyItemDetails() {
        addProductToCartSafely(driver, productPage, 0);
        productPage.GoToCartPage(driver);
        checkoutPage.waitForBagPanelOpen();

        List<String> names = checkoutPage.getCartItemNames();
        Assert.assertEquals(names.size(), 1, "Correct item count should be displayed");
        Assert.assertFalse(names.get(0).isEmpty(), "Item name should be displayed");
    }

    // ===================== TC-10 =====================
    @Test(priority = 10)
    @Description("TC-10: Verify price calculation (Bag SUBTOTAL matches checkout page Total - this app has no separate Tax line)")
    @Severity(SeverityLevel.CRITICAL)
    public void tc10_verifyPriceCalculation() {
        addProductToCartSafely(driver, productPage, 0);
        productPage.GoToCartPage(driver);
        checkoutPage.waitForBagPanelOpen();

        double bagTotal = checkoutPage.getBagSubtotal();
        Assert.assertTrue(bagTotal > 0, "Bag total should be a positive amount");

        checkoutPage.proceedToCheckout();
        checkoutPage.waitForCheckoutFormLoaded();

        double checkoutPageTotal = checkoutPage.getTotal();
        Assert.assertEquals(checkoutPageTotal, bagTotal, 0.01,
                "Order Summary Total on the checkout page should match the Bag total. Bag=" + bagTotal
                        + " Checkout=" + checkoutPageTotal);
    }

    // ===================== TC-11 =====================
    @Test(priority = 11)
    @Description("TC-11: Order Summary on the checkout page reflects the cart before submitting")
    @Severity(SeverityLevel.NORMAL)
    public void tc11_orderSummaryReflectsCartBeforeSubmit() {
        addOneProductAndOpenCheckoutForm();

        double totalBeforeSubmit = checkoutPage.getTotal();
        Assert.assertTrue(totalBeforeSubmit > 0,
                "Order Summary on the checkout page should show a total before submitting");
        Assert.assertTrue(checkoutPage.isOnCheckoutPage(), "Should still be on /checkout before submitting");
    }

    // ===================== TC-12 =====================
    @Test(priority = 12)
    @Description("TC-12: Finish order successfully (Submit = Finish on this app - no separate step)")
    @Severity(SeverityLevel.BLOCKER)
    public void tc12_finishOrderSuccessfully() {
        addOneProductAndOpenCheckoutForm();
        checkoutPage.fillCheckoutForm("John", "Doe", "10001");
        checkoutPage.clickSubmit();

        Assert.assertTrue(checkoutPage.isOnConfirmationPage(), "Should land on /confirmation");
        Assert.assertTrue(checkoutPage.isOrderPlacedMessageDisplayed(),
                "'Your Order has been successfully placed.' message should be shown");
    }

    // ===================== TC-13 =====================
    @Test(priority = 13)
    @Description("TC-13: Verify success message content")
    @Severity(SeverityLevel.NORMAL)
    public void tc13_verifySuccessMessageContent() {
        addOneProductAndOpenCheckoutForm();
        checkoutPage.fillCheckoutForm("John", "Doe", "10001");
        checkoutPage.clickSubmit();

        String message = checkoutPage.getOrderPlacedMessageText();
        Assert.assertTrue(message.toLowerCase().contains("successfully placed"),
                "Heading should say the order was successfully placed. Actual: " + message);
        Assert.assertTrue(checkoutPage.getOrderNumberText().toLowerCase().contains("order number"),
                "Order number line should be visible");
        Assert.assertTrue(checkoutPage.isDownloadReceiptLinkDisplayed(), "'Download order receipt' link should be visible");
    }

    // ===================== TC-14 =====================
    @Test(priority = 14)
    @Description("TC-14: Back to Products page after finish (via 'CONTINUE SHOPPING »')")
    @Severity(SeverityLevel.NORMAL)
    public void tc14_backToProductsAfterFinish() {
        addOneProductAndOpenCheckoutForm();
        checkoutPage.fillCheckoutForm("John", "Doe", "10001");
        checkoutPage.clickSubmit();
        Assert.assertTrue(checkoutPage.isOnConfirmationPage(), "Order should complete first");

        checkoutPage.clickContinueShopping();

        Assert.assertTrue(productPage.isLoaded(driver),
                "User should be redirected to the Products page after Continue Shopping");
    }

    // ===================== TC-15: Firefox =====================
    @Test(priority = 15)
    @Description("TC-15: Checkout on Firefox")
    @Severity(SeverityLevel.NORMAL)
    public void tc15_checkoutOnFirefox() {
        WebDriver firefoxDriver = null;
        try {
            FirefoxOptions options = new FirefoxOptions();
            firefoxDriver = new FirefoxDriver(options);
            firefoxDriver.manage().window().maximize();

            boolean completed = runFullCheckoutFlow(firefoxDriver);

            Assert.assertTrue(completed, "Checkout should complete successfully on Firefox");
        } finally {
            if (firefoxDriver != null) {
                firefoxDriver.quit();
            }
        }
    }

    // ===================== TC-16: Safari (macOS only) =====================
    @Test(priority = 16)
    @Description("TC-16: Checkout on Safari (macOS)")
    @Severity(SeverityLevel.NORMAL)
    public void tc16_checkoutOnSafari() {
        // NOTE: requires macOS Safari -> Settings -> Advanced -> "Show features for
        // web developers", then Develop menu -> "Allow Remote Automation", then
        // run `safaridriver --enable` once in Terminal.
        WebDriver safariDriver = null;
        try {
            safariDriver = new SafariDriver();
            safariDriver.manage().window().maximize();

            boolean completed = runFullCheckoutFlow(safariDriver);

            Assert.assertTrue(completed, "Checkout should complete successfully on Safari");
        } finally {
            if (safariDriver != null) {
                safariDriver.quit();
            }
        }
    }

    // ===================== TC-17: Mobile Chrome (device emulation) =====================
    @Test(priority = 17)
    @Description("TC-17: Checkout on Mobile Chrome (responsive layout)")
    @Severity(SeverityLevel.NORMAL)
    public void tc17_checkoutOnMobileChrome() {
        WebDriver mobileDriver = null;
        try {
            Map<String, String> mobileEmulation = new HashMap<>();
            mobileEmulation.put("deviceName", "iPhone 12 Pro");

            ChromeOptions options = new ChromeOptions();
            options.setExperimentalOption("mobileEmulation", mobileEmulation);

            mobileDriver = new ChromeDriver(options);

            boolean completed = runFullCheckoutFlow(mobileDriver);

            Assert.assertTrue(completed, "Checkout should complete and remain responsive on Mobile Chrome");
        } finally {
            if (mobileDriver != null) {
                mobileDriver.quit();
            }
        }
    }

    // ===================== TC-18 =====================
    @Test(priority = 18)
    @Description("TC-18: Checkout/Submit button visible & clickable")
    @Severity(SeverityLevel.NORMAL)
    public void tc18_checkoutButtonVisibleAndClickable() {
        addProductToCartSafely(driver, productPage, 0);
        productPage.GoToCartPage(driver);
        checkoutPage.waitForBagPanelOpen();

        Assert.assertTrue(checkoutPage.isBagOpen(), "Bag should be visible with 1 item in it");
        checkoutPage.proceedToCheckout();
        checkoutPage.waitForCheckoutFormLoaded();

        Assert.assertTrue(checkoutPage.isSubmitButtonDisplayed(), "Submit button should be visible");
        Assert.assertTrue(checkoutPage.isSubmitButtonClickable(), "Submit button should be enabled and clickable");
    }

    // ===================== TC-19 =====================
    @Test(priority = 19)
    @Description("TC-19: Form labels and placeholders")
    @Severity(SeverityLevel.MINOR)
    public void tc19_formLabelsAndPlaceholders() {
        addOneProductAndOpenCheckoutForm();

        String firstNameLabel = checkoutPage.getFieldLabelText("firstName");
        String lastNameLabel = checkoutPage.getFieldLabelText("lastName");
        String zipLabel = checkoutPage.getFieldLabelText("zip");

        Assert.assertTrue(firstNameLabel != null && firstNameLabel.toLowerCase().contains("first"),
                "First Name field should be labeled clearly. Actual label: " + firstNameLabel);
        Assert.assertTrue(lastNameLabel != null && lastNameLabel.toLowerCase().contains("last"),
                "Last Name field should be labeled clearly. Actual label: " + lastNameLabel);
        Assert.assertTrue(zipLabel != null && !zipLabel.isEmpty(),
                "Zip/Postal Code field should be labeled clearly. Actual label: " + zipLabel);
    }

    // ===================== TC-20 =====================
    @Test(priority = 20)
    @Description("TC-20: Checkout with special characters in name")
    @Severity(SeverityLevel.MINOR)
    public void tc20_checkoutWithSpecialCharsInName() {
        addOneProductAndOpenCheckoutForm();

        checkoutPage.fillCheckoutForm("'@#$%", "Doe", "10001");
        checkoutPage.clickSubmit();

        boolean handledGracefully = checkoutPage.isOnConfirmationPage() || checkoutPage.hasValidationError();
        Assert.assertTrue(handledGracefully,
                "System should either accept special chars and proceed, or show a clear validation error - not crash");
    }

    // ===================== HELPERS =====================

    /**
     * Adds the product at the given index to the cart WITHOUT the
     * ElementClickIntercepted issue caused by the fixed navbar.
     * scrollIntoView(true) snaps the element flush against the top of the
     * viewport -> right under the sticky header -> the header intercepts the
     * click instead of the button. Here we scroll to the CENTER of the
     * viewport instead, then click via JavaScript so nothing can intercept it.
     */
    private void addProductToCartSafely(WebDriver targetDriver, ProductPage targetProductPage, int index) {
        // Wait for the product grid to actually have enough cards rendered.
        // Without this, an occasionally-slower page load leaves the list empty
        // or too short at the moment we try to index into it, causing an
        // IndexOutOfBoundsException instead of a clean, reliable wait.
        List<WebElement> cards = new WebDriverWait(targetDriver, Duration.ofSeconds(15)).until(d -> {
            List<WebElement> found = targetProductPage.getProductCards(d);
            return found.size() > index ? found : null;
        });

        WebElement addToCartBtn = cards.get(index).findElement(By.cssSelector(".shelf-item__buy-btn"));

        ((JavascriptExecutor) targetDriver).executeScript(
                "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", addToCartBtn);

        new WebDriverWait(targetDriver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(addToCartBtn));

        ((JavascriptExecutor) targetDriver).executeScript("arguments[0].click();", addToCartBtn);
    }

    private void addOneProductAndOpenCheckoutForm() {
        addProductToCartSafely(driver, productPage, 0);
        productPage.GoToCartPage(driver);
        checkoutPage.waitForBagPanelOpen();
        checkoutPage.proceedToCheckout();
        checkoutPage.waitForCheckoutFormLoaded();
    }

    /**
     * Runs a full login -> add to cart -> checkout -> submit flow on any given
     * WebDriver instance. Used by the cross-browser (TC-15/16/17) tests since
     * they each need their own driver rather than the shared one from setUp().
     */
    private boolean runFullCheckoutFlow(WebDriver localDriver) {
        localDriver.manage().timeouts().implicitlyWait(Duration.ZERO);
        localDriver.manage().deleteAllCookies();

        LoginPage localLoginPage = new LoginPage(localDriver);
        ProductPage localProductPage = new ProductPage();
        CheckoutPage localCheckoutPage = new CheckoutPage(localDriver);

        localLoginPage.open();
        localLoginPage.login(VALID_USER, VALID_PASS);
        localLoginPage.waitForProductsPage();

        // Mobile viewport check for TC-17: make sure grid collapses to a single column.
        Dimension size = localDriver.manage().window().getSize();
        if (size.getWidth() < 500) {
            int columns = localProductPage.getColumnsInGrid(localDriver);
            Assert.assertEquals(columns, 1, "Expected single-column layout on mobile viewport");
        }

        addProductToCartSafely(localDriver, localProductPage, 0);
        localProductPage.GoToCartPage(localDriver);
        localCheckoutPage.waitForBagPanelOpen();

        localCheckoutPage.proceedToCheckout();
        localCheckoutPage.waitForCheckoutFormLoaded();
        localCheckoutPage.fillCheckoutForm("John", "Doe", "10001");
        localCheckoutPage.clickSubmit();

        return localCheckoutPage.isOnConfirmationPage() && localCheckoutPage.isOrderPlacedMessageDisplayed();
    }
}
