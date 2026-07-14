package Pages;

import Utilities.utility;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.time.Duration;

public class ProductPage {
    WebDriver driver;
    By ordersContainer = By.id("orders");
    By SignUpButton = By.id("signup-btn");
    By logo = By.className("Navbar_logo__26S5Y");
    By cartBadge = By.cssSelector(".bag__quantity, .Cart-icon__badge");
    By signInLink = By.cssSelector("#signin, .signin-btn");
    By addToCartBtn = By.cssSelector(".shelf-item__buy-btn");
    By productCards = By.cssSelector(".shelf-item");


    By cartItems =
        By.cssSelector(".float-cart__content .shelf-item");


    By removeButton = By.cssSelector(".shelf-item__del");

    By checkoutButton = By.cssSelector(".buy-btn");

    By continueShoppingButton = By.className("float-cart__close-btn");

    By subtotal = By.cssSelector(".sub-price__val");

    By totalPrice = By.cssSelector(".sub-price__val");






    public void GoToOrdersPage(WebDriver driver)
    {
        utility.click(driver, ordersContainer);
    }
    public void GoToProductPage(WebDriver driver)
    {
        utility.click(driver, logo);
    }

    public void GoToCartPage(WebDriver driver){
        utility.click(driver, cartBadge);
    }

    public void GoToSignInPage(WebDriver driver){
        utility.click(driver, signInLink);
    }


    public boolean isLoaded(WebDriver driver) {
        return utility.isDisplayed(driver, ordersContainer);
    }

    public void goToSignUpPage(WebDriver driver) {
        utility.click(driver, SignUpButton);
    }

    public boolean isHeaderVisible(WebDriver driver) {
        return utility.isDisplayed(driver,logo) && utility.isDisplayed(driver,cartBadge);
    }



    public List<WebElement> getProductCards(WebDriver driver) {
        return driver.findElements(productCards);
    }

    public void addFirstProductToCart(WebDriver driver) {
        List<WebElement> cards = getProductCards(driver);
        cards.get(0).findElement(addToCartBtn).click();
    }

    public void addProductToCartByIndex(WebDriver driver ,int index) {
        List<WebElement> cards = getProductCards(driver);
        cards.get(index).findElement(addToCartBtn).click();
    }




    public int getColumnsInGrid(WebDriver driver) {

        List<WebElement> cards = getProductCards(driver);
        if (cards.size() < 2) return 1;
        int x1 = cards.get(0).getLocation().getX();
        int x2 = cards.get(1).getLocation().getX();
        return x1 == x2 ? 1 : 2;
    }

    public String getCartBadgeCount(WebDriver driver) {
        return utility.isDisplayed(driver, cartBadge) ? driver.findElement(cartBadge).getText().trim() : "0";

    }

    public void scrollToBottom(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    public void removeFirstItem(WebDriver driver){

        utility.click(driver,removeButton);


    }

    public int getCartItemsCount(WebDriver driver) {

        return driver.findElements(cartItems)
                .size();

    }



    public String getSubtotal(WebDriver driver) {


        return utility.getText(driver, subtotal);

    }

    public boolean isCheckoutDisplayed(WebDriver driver) {

        return utility.isDisplayed(driver, checkoutButton);


    }


}
