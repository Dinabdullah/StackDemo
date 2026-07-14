package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.List;

public class HomePage extends BasePage {

    @FindBy(id = "signin")
    private WebElement signInLink;

    @FindBy(css = ".sort select")
    private WebElement sortDropdown;

    @FindBy(css = ".shelf-item")
    private List<WebElement> productItems;

    @FindBy(css = ".shelf-item__title")
    private List<WebElement> productTitles;

    @FindBy(css = ".val b")
    private List<WebElement> productPrices;

    @FindBy(css = ".shelf-item__thumb img")
    private List<WebElement> productImages;

    @FindBy(xpath = "//span[text()='Apple']")
    private WebElement appleFilter;

    @FindBy(xpath = "//span[text()='Samsung']")
    private WebElement samsungFilter;

    // The count text is inside <h3> within .products-found
    private final By productCountLocator = By.cssSelector(".products-found h3");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public LoginPage clickSignIn() {
        click(signInLink);
        return new LoginPage(driver);
    }

    public void selectSortOption(String text) {
        waitForVisibility(sortDropdown);
        Select select = new Select(sortDropdown);
        select.selectByVisibleText(text);
        try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public String getProductCountText() {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(productCountLocator));
        return el.getText().trim();
    }

    public int getProductsCount() {
        getProductCountText();
        return driver.findElements(By.cssSelector(".shelf-item")).size();
    }

    public List<String> getProductNames() {
        getProductCountText();
        List<String> names = new ArrayList<>();
        for (WebElement title : productTitles) {
            names.add(title.getText().trim());
        }
        return names;
    }

    public List<Double> getProductPrices() {
        getProductCountText();
        List<Double> prices = new ArrayList<>();
        for (WebElement priceElement : productPrices) {
            String text = priceElement.getText().trim();
            try {
                prices.add(Double.parseDouble(text));
            } catch (NumberFormatException e) {
                prices.add(0.0);
            }
        }
        return prices;
    }

    public void clickAppleFilter() {
        click(appleFilter);
        try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public void clickSamsungFilter() {
        click(samsungFilter);
        try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public ProductDetailPage clickProductImage(int index) {
        getProductCountText();
        WebElement img = productImages.get(index);
        // Use JavaScript click to ensure React event fires
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", img);
        return new ProductDetailPage(driver);
    }

    public ProductDetailPage clickProductTitle(int index) {
        getProductCountText();
        WebElement title = productTitles.get(index);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", title);
        return new ProductDetailPage(driver);
    }

    public boolean areAllImagesLoaded() {
        getProductCountText();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        for (WebElement img : productImages) {
            boolean loaded = (Boolean) js.executeScript(
                "return arguments[0].complete && typeof arguments[0].naturalWidth !== 'undefined' && arguments[0].naturalWidth > 0",
                img
            );
            if (!loaded) return false;
        }
        return true;
    }

    public boolean hasAnyBrokenImage() {
        getProductCountText();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        for (WebElement img : productImages) {
            boolean loaded = (Boolean) js.executeScript(
                "return arguments[0].complete && typeof arguments[0].naturalWidth !== 'undefined' && arguments[0].naturalWidth > 0",
                img
            );
            if (!loaded) return true;
        }
        return false;
    }

    public WebElement getProductTitleElement(int index) {
        getProductCountText();
        return productTitles.get(index);
    }

    public WebElement getProductPriceElement(int index) {
        getProductCountText();
        return productPrices.get(index);
    }
}
