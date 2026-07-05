package Pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;

public class CheckoutPage {

    WebDriver driver;
    WebDriverWait wait;


    By cartIcon = By.className("shopping_cart_link");
    By checkoutBtn = By.id("checkout");
    By firstNameField = By.id("first-name");
    By lastNameField = By.id("last-name");
    By postalCodeField = By.id("postal-code");
    By continueBtn = By.id("continue");
    By finishBtn = By.id("finish");
    By successMessage = By.className("complete-header");
    By errorMessage = By.cssSelector(".error-message-container");
    By addToCartBtn = By.id("add-to-cart-sauce-labs-backpack");

    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void addProductToCart() {
        driver.findElement(addToCartBtn).click();
    }

    public void goToCart() {
        driver.findElement(cartIcon).click();
    }

    public void clickCheckout() {
        wait.until(ExpectedConditions
                .elementToBeClickable(checkoutBtn));
        driver.findElement(checkoutBtn).click();
    }

    public void fillInfo(String first, String last, String zip) {
        wait.until(ExpectedConditions
                .visibilityOfElementLocated(firstNameField));
        driver.findElement(firstNameField).sendKeys(first);
        driver.findElement(lastNameField).sendKeys(last);
        driver.findElement(postalCodeField).sendKeys(zip);
    }

    public void clickContinue() {
        driver.findElement(continueBtn).click();
    }

    public void clickFinish() {
        wait.until(ExpectedConditions
                .visibilityOfElementLocated(finishBtn));
        driver.findElement(finishBtn).click();
    }

    public String getSuccessMessage() {
        wait.until(ExpectedConditions
                .visibilityOfElementLocated(successMessage));
        return driver.findElement(successMessage).getText();
    }

    public boolean isErrorDisplayed() {
        return driver.findElement(errorMessage).isDisplayed();
    }
}