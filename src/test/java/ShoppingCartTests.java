import Pages.LoginPage;
import Pages.ProductPage;
import Utilities.utility;

import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;



public class ShoppingCartTests extends BaseTest {



    private static final String VALID_USER = "demouser";
    private static final String VALID_PASS = "testingisfun99";



    private void login(){

        LoginPage loginPage =
                new LoginPage(driver);


        loginPage.open();


        loginPage.login(
                VALID_USER,
                VALID_PASS
        );


        utility.scrollingByJsExecutor(
                driver,
                By.cssSelector(".shelf-item__buy-btn")
        );
    }




    @Test(priority = 1)
    @Description("SC-001 Add single product")
    @Severity(SeverityLevel.CRITICAL)
    public void addSingleProduct(){


        ProductPage page = new ProductPage();


        login();


        page.addProductToCartByIndex(driver,5);


        Assert.assertEquals(
                page.getCartBadgeCount(driver),
                "1"
        );
    }





    @Test(priority = 2)
    @Description("SC-002 Add multiple products")
    @Severity(SeverityLevel.CRITICAL)
    public void addMultipleProducts(){


        ProductPage page =
                new ProductPage();


        login();


        page.addProductToCartByIndex(driver,4);
        page.addProductToCartByIndex(driver,5);
        page.addProductToCartByIndex(driver,6);



        Assert.assertEquals(
                page.getCartBadgeCount(driver),
                "3"
        );

    }






    @Test(priority = 3)
    @Description("SC-003 Open cart")
    @Severity(SeverityLevel.NORMAL)
    public void openCart(){


        ProductPage page =
                new ProductPage();


        login();


        page.addProductToCartByIndex(driver,4);


        page.GoToCartPage(driver);



        Assert.assertTrue(
                page.isCheckoutDisplayed(driver),
                "Expected checkout button to be visible after opening the cart"
        );

    }






    @Test(priority = 4)
    @Description("SC-004 Remove product")
    @Severity(SeverityLevel.CRITICAL)
    public void removeProduct(){


        ProductPage page =
                new ProductPage();



        login();



        page.addProductToCartByIndex(driver,4);


        page.GoToCartPage(driver);


        page.removeFirstItem(driver);



        Assert.assertEquals(
                page.getCartItemsCount(driver),
                0
        );

    }





    @Test(priority = 5)
    @Description("SC-005 Verify subtotal")
    @Severity(SeverityLevel.NORMAL)
    public void verifySubtotal(){


        ProductPage page =
                new ProductPage();



        login();



        page.addProductToCartByIndex(driver, 4);



        page.GoToCartPage(driver);



        Assert.assertFalse(
                page.getSubtotal(driver).isEmpty(),
                "Expected a non-empty subtotal value once an item is in the cart"
        );

    }

}










