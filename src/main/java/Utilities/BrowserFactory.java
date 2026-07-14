package Utilities;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.time.Duration;

public class BrowserFactory {
    //browser options

    public static WebDriver browerSetup(String browserType){
        WebDriver driver = null;
        if(browserType.equalsIgnoreCase("chrome")){
            ChromeOptions options = new ChromeOptions();
            options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
            options.addArguments("--incognito");
            options.addArguments("--start-maximized");
            options.addArguments("--disable-blink-features=AutomationControlled");

            driver = new ChromeDriver(options);
        }
        else if(browserType.equalsIgnoreCase("edge")){
            EdgeOptions options = new EdgeOptions();
            options.setPageLoadStrategy(PageLoadStrategy.NONE);
            options.addArguments("--inPrivate");
            driver = new EdgeDriver(options);
        }
        return driver;
    }

}
