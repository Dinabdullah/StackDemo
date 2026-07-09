package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


public class CheckoutPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    // ===================== LOCATORS =====================

    // ---- Bag panel (before checkout) ----
    private final By bagHeader = By.xpath("//*[normalize-space()='Bag']");
    // Confirmed via DevTools: <div class="buy-btn">Checkout</div>
    private final By proceedToCheckoutBtn = By.cssSelector(".buy-btn");

    // ---- Shipping form (bstackdemo.com/checkout) - ALL confirmed via DevTools ----
    private final By firstNameInput = By.id("firstNameInput");
    private final By lastNameInput  = By.id("lastNameInput");
    private final By addressLine1Input = By.id("addressLine1Input");
    private final By provinceInput  = By.id("provinceInput");
    private final By postCodeInput  = By.id("postCodeInput");
    private final By submitButton   = By.id("checkout-shipping-continue"); // text is "SUBMIT"

    // ---- Confirmation page (bstackdemo.com/confirmation) ----
    private final By orderPlacedHeading = By.xpath("//*[contains(normalize-space(),'successfully placed')]");
    private final By orderNumberText    = By.xpath("//*[contains(normalize-space(),'order number is')]");
    private final By downloadReceiptLink = By.xpath("//*[contains(normalize-space(),'Download order receipt')]");
    // Confirmed via DevTools: <button class="... optimizedCheckout-buttonSecondary">Continue Shopping »</button>
    private final By continueShoppingButton = By.xpath("//button[contains(normalize-space(),'Continue Shopping')]");
    // "Order Summary" box appears on BOTH the checkout form and the confirmation
    // page, with a "Total (USD)" line - use case-insensitive matching since case
    // isn't guaranteed to be identical everywhere the term appears.
    // Excludes "SUBTOTAL" (used in the Bag panel) by checking it's not a substring.
    private final By totalValue = By.xpath(
            "//*[starts-with(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'TOTAL')"
                    + " and not(contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'SUBTOTAL'))]"
                    + "/following::*[contains(text(),'$')][1]");

    // Bag panel shows "SUBTOTAL" (all caps, confirmed via screenshot) instead of "Total".
    private final By bagSubtotalValue = By.xpath(
            "//*[translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')='SUBTOTAL']"
                    + "/following::*[contains(text(),'$')][1]");

    // Confirmed via DevTools: <p class="desc">Apple <br>Quantity: 1</p>
    // Each item row has exactly one of these (brand + quantity together,
    // separated by a <br>), making it a reliable, precise anchor per row.
    private final By cartItemDescAnchor = By.cssSelector("p.desc");

    // ===================== BAG PANEL =====================

    public void waitForBagPanelOpen() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(bagHeader));
    }

    public boolean isBagOpen() {
        try {
            return driver.findElement(bagHeader).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public int getCartItemCount() {
        waitForCartItemsToRender();
        return driver.findElements(cartItemDescAnchor).size();
    }


    private void waitForCartItemsToRender() {
        try {
            wait.until(d -> !d.findElements(cartItemDescAnchor).isEmpty());
        } catch (TimeoutException ignored) {
            // Genuinely empty cart - let the caller see 0 rather than throwing here.
        }
    }


    public List<String> getCartItemNames() {
        waitForCartItemsToRender();
        List<String> names = new ArrayList<>();
        for (WebElement descEl : driver.findElements(cartItemDescAnchor)) {
            try {
                WebElement row = descEl.findElement(By.xpath(".."));
                String[] lines = row.getText().split("\\R");
                names.add(lines.length > 0 ? lines[0].trim() : "");
            } catch (Exception e) {
                names.add("");
            }
        }
        return names;
    }

    public void proceedToCheckout() {
        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(proceedToCheckoutBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkoutBtn);
    }

    // ===================== SHIPPING FORM (/checkout) =====================

    public boolean isOnCheckoutPage() {
        return driver.getCurrentUrl().contains("/checkout");
    }

    public void waitForCheckoutFormLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(firstNameInput));
    }

    public void enterFirstName(String value) {
        if (value == null) return;
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(firstNameInput));
        el.clear();
        el.sendKeys(value);
    }

    public void enterLastName(String value) {
        if (value == null) return;
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(lastNameInput));
        el.clear();
        el.sendKeys(value);
    }

    public void enterZipCode(String value) {
        if (value == null) return;
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(postCodeInput));
        el.clear();
        el.sendKeys(value);
    }


    public void fillCheckoutForm(String firstName, String lastName, String zipCode) {
        enterFirstName(firstName);
        enterLastName(lastName);
        fillField(addressLine1Input, "123 Main St");
        fillField(provinceInput, "NY");
        enterZipCode(zipCode);
    }

    private void fillField(By locator, String value) {
        try {
            WebElement el = driver.findElement(locator);
            el.clear();
            el.sendKeys(value);
        } catch (NoSuchElementException e) {
            // Field doesn't exist on this form -> nothing to do.
        }
    }

    public void clickSubmit() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public boolean isSubmitButtonDisplayed() {
        try {
            return driver.findElement(submitButton).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isSubmitButtonClickable() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.elementToBeClickable(submitButton));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }


    public String getFieldLabelText(String field) {
        By locator = resolveFieldLocator(field);
        WebElement input = driver.findElement(locator);

        String placeholder = input.getAttribute("placeholder");
        if (placeholder != null && !placeholder.isEmpty()) {
            return placeholder;
        }

        String id = input.getAttribute("id");
        try {
            return driver.findElement(By.cssSelector("label[for='" + id + "']")).getText().trim();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    private By resolveFieldLocator(String field) {
        switch (field.toLowerCase()) {
            case "firstname": return firstNameInput;
            case "lastname":  return lastNameInput;
            case "zip":
            case "zipcode":
            case "postcode":  return postCodeInput;
            default: throw new IllegalArgumentException("Unknown field: " + field);
        }
    }


    public boolean isFieldInvalid(String field) {
        WebElement el = driver.findElement(resolveFieldLocator(field));
        Object isValid = ((JavascriptExecutor) driver).executeScript("return arguments[0].checkValidity();", el);
        return !(Boolean) isValid;
    }

    public String getFieldValidationMessage(String field) {
        WebElement el = driver.findElement(resolveFieldLocator(field));
        Object message = ((JavascriptExecutor) driver).executeScript("return arguments[0].validationMessage;", el);
        return message == null ? "" : message.toString();
    }


    public boolean hasValidationError() {
        return isFieldInvalid("firstName") || isFieldInvalid("lastName") || isFieldInvalid("zip");
    }

    // ===================== CONFIRMATION PAGE (/confirmation) =====================

    public boolean isOnConfirmationPage() {
        try {
            wait.until(d -> d.getCurrentUrl().contains("/confirmation"));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isOrderPlacedMessageDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(orderPlacedHeading)) != null;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public String getOrderPlacedMessageText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(orderPlacedHeading)).getText().trim();
    }

    public String getOrderNumberText() {
        try {
            return driver.findElement(orderNumberText).getText().trim();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    public boolean isDownloadReceiptLinkDisplayed() {
        try {
            return driver.findElement(downloadReceiptLink).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }


    public double getTotal() {
        String raw = wait.until(ExpectedConditions.visibilityOfElementLocated(totalValue)).getText();
        return parsePrice(raw);
    }


    public double getBagSubtotal() {
        String raw = wait.until(ExpectedConditions.visibilityOfElementLocated(bagSubtotalValue)).getText();
        return parsePrice(raw);
    }

    private double parsePrice(String raw) {
        String digitsOnly = raw.replaceAll("[^0-9.]", "");
        return digitsOnly.isEmpty() ? 0.0 : Double.parseDouble(digitsOnly);
    }

    public void clickContinueShopping() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(continueShoppingButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }
}
