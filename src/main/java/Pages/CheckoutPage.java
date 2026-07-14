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

/**
 * Page Object for the CheckOut flow on this bstackdemo instance.
 *
 * CONFIRMED REAL FLOW (verified via screenshots + DevTools with Nada on 09 Jul 2026):
 *   1. Product page -> "Add to cart" -> opens the "Bag" side panel.
 *   2. Bag panel shows: item rows (image, name, brand, "Quantity: N", price),
 *      SUBTOTAL, and a "CHECKOUT" button (confirmed HTML: <div class="buy-btn">Checkout</div>).
 *   3. Clicking Checkout NAVIGATES to bstackdemo.com/checkout - a full "Shipping
 *      Address" form: First Name, Last Name, Address, State/Province, Postal Code,
 *      and a black "SUBMIT" button (confirmed id: checkout-shipping-continue).
 *   4. Clicking Submit NAVIGATES DIRECTLY to bstackdemo.com/confirmation - there is
 *      NO separate "Order Summary review + Finish button" step. Submit = Finish.
 *   5. The confirmation page shows: "Your Order has been successfully placed.",
 *      "Your order number is N.", a "Download order receipt" link, an Order
 *      Summary box (item(s) + Total (USD) only - no separate Subtotal/Tax lines),
 *      and a "CONTINUE SHOPPING »" button that goes back to the product page.
 *
 * Because step 2/4 is a real page navigation (not a JS-driven single page app
 * step), the most reliable way to assert "did we reach step X" is the URL
 * itself (driver.getCurrentUrl()), not a specific container class.
 */
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

    /**
     * The Bag panel header can appear before the item rows finish rendering
     * (they load slightly after the panel itself opens). Without this wait,
     * getCartItemCount()/getCartItemNames() can occasionally read 0 items
     * even though items were actually added successfully.
     */
    private void waitForCartItemsToRender() {
        try {
            wait.until(d -> !d.findElements(cartItemDescAnchor).isEmpty());
        } catch (TimeoutException ignored) {
            // Genuinely empty cart - let the caller see 0 rather than throwing here.
        }
    }

    /**
     * Returns the item names shown in the Bag panel. p.desc holds "Brand /
     * Quantity: N" - the product NAME is a sibling element (typically
     * rendered just above it, e.g. a "title" paragraph). We read the full
     * text of the immediate parent container and take the first line, which
     * matches the visual top-to-bottom layout (name, then brand+quantity).
     */
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

    /**
     * Fills the whole shipping form. First/Last/Zip come from the caller (these
     * are the 3 fields the team's test-case sheet cares about); Address and
     * Province are filled with sensible defaults since the real form requires
     * them but the sheet doesn't track them.
     */
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

    /**
     * Returns the visible label text for a field: "firstName" | "lastName" | "zip".
     * These inputs have NO placeholder attribute (confirmed via DevTools) - the
     * label is a separate <label for="..."> element, so we read that instead.
     */
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

    /**
     * These 3 fields use native HTML5 "required" validation (confirmed: the
     * browser shows its own "Please fill out this field" bubble, not a custom
     * error rendered in the DOM). So instead of searching the page for error
     * text, we ask the browser directly via JS whether a field is invalid.
     */
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

    /** True if ANY of the three fields we care about is currently invalid. */
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

    /** Total (USD) shown in the Order Summary box - works on /checkout AND /confirmation. */
    public double getTotal() {
        String raw = wait.until(ExpectedConditions.visibilityOfElementLocated(totalValue)).getText();
        return parsePrice(raw);
    }

    /** SUBTOTAL shown in the Bag panel (before proceeding to checkout). */
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
