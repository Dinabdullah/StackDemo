package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.TimeoutException;

import java.time.Duration;
import java.util.List;

public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final String BASE_URL = "https://bstackdemo.com/signin";

    private final By usernameContainer = By.id("username");
    private final By passwordContainer = By.id("password");

    private final By loginButton   = By.id("login-btn");
    private final By logoutButton =
            By.xpath("//span[@role='link' and normalize-space()='Logout']");

    private final By errorMessage  = By.cssSelector(".api-error");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void open() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameContainer));
    }

    public void selectUsername(String username) {
        if (username != null && !username.isEmpty()) {
            selectFromReactSelect(usernameContainer, username);
        }
    }

    public void selectPassword(String password) {
        if (password != null && !password.isEmpty()) {
            selectFromReactSelect(passwordContainer, password);
        }
    }

    private void selectFromReactSelect(By containerLocator, String value) {
        WebElement container = wait.until(
                ExpectedConditions.elementToBeClickable(containerLocator));

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView(true);", container);
        container.click();

        By optionLocator = By.xpath(
                "//div[contains(@class,'-option') and normalize-space(text())='" + value + "']");

        WebElement option = wait.until(
                ExpectedConditions.elementToBeClickable(optionLocator));
        option.click();

        By singleValueLocator = By.cssSelector(
                "#" + getContainerId(containerLocator) + " [class*='-singleValue']");

        wait.until(ExpectedConditions.textToBePresentInElementLocated(singleValueLocator, value));
    }

    private String getContainerId(By containerLocator) {
        String s = containerLocator.toString();
        return s.contains(": ") ? s.substring(s.indexOf(": ") + 2).trim() : s;
    }

    public void clickLogin() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public void login(String username, String password) {
        selectUsername(username);
        selectPassword(password);
        try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        clickLogin();
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("?signin=true"),
                    ExpectedConditions.visibilityOfElementLocated(errorMessage)
            ));
        } catch (TimeoutException e) {
        }
    }

    public void logout() {
        WebElement logout =
                wait.until(ExpectedConditions.elementToBeClickable(logoutButton));

        logout.click();

        wait.until(ExpectedConditions.urlToBe("https://bstackdemo.com/"));
    }

    public String getErrorMessage() {
        return wait.until(
                ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText().trim();
    }

    public boolean isErrorMessageDisplayed() {
        try {
            return driver.findElement(errorMessage).isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    public boolean waitForErrorMessage() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isLoginButtonEnabled() {
        return driver.findElement(loginButton).isEnabled();
    }

    public boolean isLoginButtonDisabled() {
        WebElement btn = driver.findElement(loginButton);
        String ariaDisabled = btn.getAttribute("aria-disabled");
        String classAttr    = btn.getAttribute("class");
        boolean byAttr  = "true".equalsIgnoreCase(ariaDisabled);
        boolean byClass = classAttr != null && classAttr.toLowerCase().contains("disabled");
        return !btn.isEnabled() || byAttr || byClass;
    }

    public boolean isLoggedIn() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(logoutButton)) != null;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isOnSigninPage() {
        return driver.getCurrentUrl().contains("/signin")
                || driver.getCurrentUrl().contains("signin=true");
    }

    public boolean isOnProductsPage() {
        return driver.getCurrentUrl().contains("?signin=true");
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public void waitForProductsPage() {
        wait.until(ExpectedConditions.urlContains("?signin=true"));
    }

    public void waitForSigninPage() {
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/signin"),
                ExpectedConditions.urlContains("signin=true")
        ));
    }

    public void navigateDirectlyToSignin() {
        driver.get(BASE_URL);
    }

    public void clickBrowserBack() {
        driver.navigate().back();
    }

    public void waitForUrlToSettle(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}