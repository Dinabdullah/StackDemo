package Pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;

public class CheckoutPage {

    WebDriver driver;
    WebDriverWait wait;

    // Locators - bstackdemo.com
    By addToCartBtn = By.xpath("(//div[@class='shelf-item__buy-btn'])[1]");
    By cartIcon = By.className("shopping-cart");
    By checkoutBtn = By.className("buy-btn");
    By firstNameField = By.id("firstNameInput");
    By lastNameField = By.id("lastNameInput");
    By addressField = By.id("addressLine1Input");
    By continueBtn = By.id("checkout-shipping-continue");
    By finishBtn = By.cssSelector("button[data-cy='place-order']");
    By successMessage = By.className("SuccessMessage_successMessage__3BMmq");
    By errorMessage = By.cssSelector(".InputField_error__3ajOt");

    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void addProductToCart() {
        wait.until(ExpectedConditions.elementToBeClickable(addToCartBtn));
        driver.findElement(addToCartBtn).click();
    }

    public void goToCart() {
        wait.until(ExpectedConditions.elementToBeClickable(cartIcon));
        driver.findElement(cartIcon).click();
    }

    public void clickCheckout() {
        wait.until(ExpectedConditions.elementToBeClickable(checkoutBtn));
        driver.findElement(checkoutBtn).click();
    }

    public void fillInfo(String first, String last, String address) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(firstNameField));
        driver.findElement(firstNameField).sendKeys(first);
        driver.findElement(lastNameField).sendKeys(last);
        driver.findElement(addressField).sendKeys(address);
    }

    public void clickContinue() {
        driver.findElement(continueBtn).click();
    }

    public void clickFinish() {
        wait.until(ExpectedConditions.elementToBeClickable(finishBtn));
        driver.findElement(finishBtn).click();
    }

    public String getSuccessMessage() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
        return driver.findElement(successMessage).getText();
    }

    public boolean isErrorDisplayed() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        return driver.findElement(errorMessage).isDisplayed();
    }
}