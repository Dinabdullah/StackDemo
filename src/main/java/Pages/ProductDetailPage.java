package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ProductDetailPage extends BasePage {

    // On bstackdemo PDP, the product name appears in a prominent h1 or title element
    // and the "Add to cart" button exists in the shelf-item__buy-btn div
    @FindBy(css = ".shelf-item__buy-btn")
    private WebElement addToCartBtn;

    public ProductDetailPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        try {
            // Wait up to 5 sec for URL to change from home or for PDP element
            wait.until(driver -> {
                String url = driver.getCurrentUrl();
                // Bstackdemo navigates to /?id=N or /product/N for PDP
                return url.contains("?id=") || url.contains("/product/") || url.contains("#product");
            });
            return true;
        } catch (Exception e) {
            // If URL never changed, check if float-cart opened (alternative PDP view)
            try {
                WebElement floatCart = driver.findElement(By.cssSelector(".float-cart--open, .float-cart__content"));
                return floatCart.isDisplayed();
            } catch (Exception e2) {
                return false;
            }
        }
    }
}
