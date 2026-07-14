import Pages.LoginPage;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

public class LoginTests {

    private WebDriver driver;
    private LoginPage loginPage;

    private static final String VALID_USER = "demouser";
    private static final String VALID_PASS = "testingisfun99";
    private static final String LOCKED_USER = "locked_user";

    @BeforeMethod
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().deleteAllCookies();

        loginPage = new LoginPage(driver);
        loginPage.open();
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test(priority = 1)
    @Description("TC-001: Successful login with valid credentials")
    @Severity(SeverityLevel.BLOCKER)
    public void tc001_successfulLoginWithValidCredentials() {
        loginPage.login(VALID_USER, VALID_PASS);
        if (loginPage.isErrorMessageDisplayed()) {
            Assert.fail("Login failed with error: " + loginPage.getErrorMessage());
        }
        Assert.assertTrue(loginPage.isOnProductsPage(),
                "Expected redirect to ?signin=true  after valid login. Current URL: " + loginPage.getCurrentUrl());
    }

    @Test(priority = 2)
    @Description("TC-002: Correct redirect URL after successful login")
    @Severity(SeverityLevel.CRITICAL)
    public void tc002_correctRedirectUrlAfterLogin() {
        loginPage.login(VALID_USER, VALID_PASS);
        if (loginPage.isErrorMessageDisplayed()) {
            Assert.fail("Login failed with error: " + loginPage.getErrorMessage());
        }
        loginPage.waitForProductsPage();
        Assert.assertTrue(
                loginPage.getCurrentUrl().contains("?signin=true"),
                "Expected URL to contain ?signin=true. Actual: " + loginPage.getCurrentUrl()
        );
    }

    @Test(priority = 3)
    @Description("TC-003: Session persists after login")
    @Severity(SeverityLevel.NORMAL)
    public void tc003_sessionPersistsAfterLogin() {
        loginPage.login(VALID_USER, VALID_PASS);
        if (loginPage.isErrorMessageDisplayed()) {
            Assert.fail("Login failed with error: " + loginPage.getErrorMessage());
        }
        loginPage.waitForProductsPage();

        String originalTab = driver.getWindowHandle();

        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("window.open(arguments[0]);", "https://bstackdemo.com/?signin=true");

        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalTab)) {
                driver.switchTo().window(handle);
                break;
            }
        }
        Assert.assertTrue(loginPage.isOnProductsPage(), "New tab should show products page");
    }

    @Test(priority = 4)
    @Description("TC-004: Log In button enabled only when both fields are selected (BUG - expected to fail)")
    @Severity(SeverityLevel.NORMAL)
    public void tc004_loginButtonEnabledOnlyWhenBothFieldsSelected() {
        Assert.assertTrue(loginPage.isLoginButtonDisabled(),
                "BUG (TC-004): Log In button should be disabled with no fields selected.");

        loginPage.selectUsername(VALID_USER);
        Assert.assertTrue(loginPage.isLoginButtonDisabled(),
                "BUG (TC-004): Log In button should still be disabled with only username selected.");

        loginPage.open();
        loginPage.selectPassword(VALID_PASS);
        Assert.assertTrue(loginPage.isLoginButtonDisabled(),
                "BUG (TC-004): Log In button should still be disabled with only password selected.");

        loginPage.selectUsername(VALID_USER);
        Assert.assertFalse(loginPage.isLoginButtonDisabled(),
                "Log In button should be enabled once both username and password are selected.");
    }

    @Test(priority = 5)
    @Description("TC-005: Login with username only — no password selected")
    @Severity(SeverityLevel.CRITICAL)
    public void tc005_loginWithUsernameOnly() {
        loginPage.selectUsername(VALID_USER);
        loginPage.clickLogin();
        loginPage.waitForErrorMessage();
        Assert.assertEquals(loginPage.getErrorMessage(), "Invalid Password");
        Assert.assertTrue(loginPage.isOnSigninPage(), "User should remain on /signin.");
    }

    @Test(priority = 6)
    @Description("TC-006: Login with password only — no username selected")
    @Severity(SeverityLevel.CRITICAL)
    public void tc006_loginWithPasswordOnly() {
        loginPage.selectPassword(VALID_PASS);
        loginPage.clickLogin();
        loginPage.waitForErrorMessage();
        Assert.assertEquals(loginPage.getErrorMessage(), "Invalid Username");
        Assert.assertTrue(loginPage.isOnSigninPage(), "User should remain on /signin.");
    }

    @Test(priority = 7)
    @Description("TC-007: Login with a locked user account")
    @Severity(SeverityLevel.CRITICAL)
    public void tc007_loginWithLockedUserAccount() {
        loginPage.login(LOCKED_USER, VALID_PASS);
        loginPage.waitForErrorMessage();
        Assert.assertEquals(loginPage.getErrorMessage(), "Your account has been locked.");
        Assert.assertTrue(loginPage.isOnSigninPage(), "Locked user should remain on /signin.");
    }

    @Test(priority = 8)
    @Description("TC-008: Login with no fields selected at all")
    @Severity(SeverityLevel.CRITICAL)
    public void tc008_loginWithNoFieldsSelected() {
        loginPage.clickLogin();
        loginPage.waitForErrorMessage();
        Assert.assertEquals(loginPage.getErrorMessage(), "Invalid Username");
        Assert.assertTrue(loginPage.isOnSigninPage(), "Login should be prevented; user stays on /signin.");
    }

    @Test(priority = 9)
    @Description("TC-009: Error messages are clear and descriptive")
    @Severity(SeverityLevel.NORMAL)
    public void tc009_errorMessagesAreClearAndDescriptive() {
        loginPage.selectPassword(VALID_PASS);
        loginPage.clickLogin();
        loginPage.waitForErrorMessage();
        Assert.assertEquals(loginPage.getErrorMessage(), "Invalid Username");

        loginPage.open();
        loginPage.selectUsername(VALID_USER);
        loginPage.clickLogin();
        loginPage.waitForErrorMessage();
        Assert.assertEquals(loginPage.getErrorMessage(), "Invalid Password");

        loginPage.open();
        loginPage.login(LOCKED_USER, VALID_PASS);
        loginPage.waitForErrorMessage();
        Assert.assertEquals(loginPage.getErrorMessage(), "Your account has been locked.");
    }

    @Test(priority = 10)
    @Description("TC-010: Error message clears on a valid re-attempt")
    @Severity(SeverityLevel.NORMAL)
    public void tc010_errorMessageClearsOnValidReattempt() {
        loginPage.login(LOCKED_USER, VALID_PASS);
        Assert.assertTrue(loginPage.waitForErrorMessage(), "Error message should be visible after a failed login.");

        loginPage.selectUsername(VALID_USER);
        loginPage.selectPassword(VALID_PASS);
        loginPage.clickLogin();
        loginPage.waitForProductsPage();
        Assert.assertTrue(loginPage.isOnProductsPage(), "User should be redirected to /products on valid retry.");
    }

    @Test(priority = 11)
    @Description("TC-011: Login again after logout")
    @Severity(SeverityLevel.NORMAL)
    public void tc011_loginAgainAfterLogout() {

        loginPage.login(VALID_USER, VALID_PASS);

        if (loginPage.isErrorMessageDisplayed()) {
            Assert.fail("First login failed: " + loginPage.getErrorMessage());
        }

        loginPage.waitForProductsPage();

        loginPage.logout();

        loginPage.open();

        loginPage.login(VALID_USER, VALID_PASS);

        if (loginPage.isErrorMessageDisplayed()) {
            Assert.fail("Second login failed: " + loginPage.getErrorMessage());
        }

        loginPage.waitForProductsPage();

        Assert.assertTrue(loginPage.isOnProductsPage(),
                "Second login should redirect to the products page.");
    }

    @Test(priority = 12)
    @Description("TC-012: Browser Back button pressed after login (BUG - expected to fail)")
    @Severity(SeverityLevel.CRITICAL)
    public void tc012_browserBackButtonAfterLogin() {
        loginPage.login(VALID_USER, VALID_PASS);
        if (loginPage.isErrorMessageDisplayed()) {
            Assert.fail("Login failed: " + loginPage.getErrorMessage());
        }
        loginPage.waitForProductsPage();

        loginPage.clickBrowserBack();
        loginPage.waitForUrlToSettle(Duration.ofSeconds(2));

        Assert.assertFalse(loginPage.isOnSigninPage(),
                "BUG (TC-012): User should NOT be taken back to /signin while still logged in; "
                        + "should stay on ?signin=true or be redirected appropriately. Actual URL: "
                        + loginPage.getCurrentUrl());
    }

    @Test(priority = 13)
    @Description("TC-013: Navigate directly to /signin while already logged in (BUG - expected to fail)")
    @Severity(SeverityLevel.CRITICAL)
    public void tc013_navigateDirectlyToSigninWhileLoggedIn() {
        loginPage.login(VALID_USER, VALID_PASS);
        if (loginPage.isErrorMessageDisplayed()) {
            Assert.fail("Login failed: " + loginPage.getErrorMessage());
        }
        loginPage.waitForProductsPage();

        loginPage.navigateDirectlyToSignin();
        loginPage.waitForUrlToSettle(Duration.ofSeconds(2));

        Assert.assertTrue(loginPage.isOnProductsPage(),
                "BUG (TC-013): A logged-in user navigating to /signin should be auto-redirected to ?signin=true; "
                        + "the sign-in page should not be accessible while authenticated. Actual URL: "
                        + loginPage.getCurrentUrl());
    }

    @Test(priority = 14)
    @Description("TC-014: Change selection before submitting")
    @Severity(SeverityLevel.NORMAL)
    public void tc014_changeSelectionBeforeSubmitting() {
        loginPage.selectUsername(VALID_USER);
        loginPage.selectPassword(VALID_PASS);

        loginPage.selectUsername(LOCKED_USER);
        loginPage.clickLogin();
        loginPage.waitForErrorMessage();
        Assert.assertEquals(loginPage.getErrorMessage(), "Your account has been locked.",
                "System should use the final selection (locked_user) and show the locked account error.");
    }
}