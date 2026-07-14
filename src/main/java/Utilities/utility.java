package Utilities;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class utility {
    //common used methods
    //click, scroll.


   public static void explicitWait(WebDriver driver, By locator) {
       new WebDriverWait(driver,Duration.ofSeconds(10)).until(ExpectedConditions.visibilityOfElementLocated(locator));
   }

   public static void implicitWait(WebDriver driver) {
       driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
   }


   public static boolean geturl(WebDriver driver, String url){
      return new WebDriverWait(driver, Duration.ofSeconds(5))
               .until(ExpectedConditions.urlContains(url));
   }

   public static void uploudFile(WebDriver driver, By locator, String relativeFilePath){
       driver.findElement(locator).sendKeys(System.getProperty("user.dir") + "/" + relativeFilePath);
   }

   public static void dropDownByValue(WebDriver driver, By locator, String value){
       new Select(driver.findElement(locator)).selectByValue(value);
   }

   public static void switchToNewWindow(WebDriver driver, Set<String> handles, int index){
       List<String> h = new ArrayList<String>(getWindowHandles(driver));
       driver.switchTo().window(h.get(index));
   }

   public static Set<String> getWindowHandles(WebDriver driver){
       return driver.getWindowHandles();
   }

   public static void scrollingByJsExecutor(WebDriver driver, By locator){
       ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
               driver.findElement(locator));
   }

   public static WebElement byToWebElement(WebDriver driver, By locator){
       return driver.findElement(locator);
   }

   public static void sendKeys(WebDriver driver, By locator, String text){
       new WebDriverWait(driver, Duration.ofSeconds(5))
               .until(ExpectedConditions.visibilityOfElementLocated(locator)).sendKeys(text);
   }

   public static void click(WebDriver driver, By locator){
       new WebDriverWait(driver, Duration.ofSeconds(5))
               .until(ExpectedConditions.elementToBeClickable(locator)).click();
   }


   public static void clear(WebDriver driver, By locator){
         new WebDriverWait(driver, Duration.ofSeconds(5))
               .until(ExpectedConditions.visibilityOfElementLocated(locator)).clear();
   }

   public static String getText(WebDriver driver, By locator){
       return new WebDriverWait(driver, Duration.ofSeconds(5))
               .until(ExpectedConditions.elementToBeClickable(locator)).getText();
   }

   public static boolean isSelected(WebDriver driver, By locator){
       return new WebDriverWait(driver, Duration.ofSeconds(5))
               .until(ExpectedConditions.elementToBeClickable(locator)).isSelected();
   }

   public static boolean isEnabled(WebDriver driver, By locator){
       return new WebDriverWait(driver, Duration.ofSeconds(5))
               .until(ExpectedConditions.elementToBeClickable(locator)).isEnabled();
   }

    public static boolean isDisplayed(WebDriver driver, By locator){
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(locator)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }



}
