# Test Configuration Guide

## Why the YAML file doesn't work

The `a.yaml` file does not work for the following reasons:

1. **TestNG doesn't support YAML format**: TestNG expects XML configuration files (like `testng.xml`) for test suite definitions, not YAML files.

2. **Class name mismatch**: The original YAML file referenced `com.gecom.WishlistTest.AddWishlists`, but the actual class name is `com.gecom.WishlistTest.AddToWishList`.

3. **Method name mismatch**: The YAML file referenced `testAddWishlistValidRequest`, but the actual method name is `testUserCanAddProductToWishlist`.

## Proper TestNG Configuration

TestNG uses XML files for test suite configuration. The correct format is shown in `WishlistTest.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="ITI E-Commerce Wishlist Suite">
    <listeners>
        <listener class-name="com.gecom.utils.TestListener"/>
    </listeners>

    <test name="WishlistTest Function Tests">
        <groups>
            <run>
                <include name="Valid-Wishlist-Test"/>
                <include name="Invalid-Wishlist-Test"/>
            </run>
        </groups>
        <classes>
            <class name="com.gecom.WishlistTest.AddToWishList"/>
            <class name="com.gecom.WishlistTest.GetWishlists"/>
            <class name="com.gecom.WishlistTest.DeleteWishlists"/>
        </classes>
    </test>
</suite>
```

## Running Tests

To run the tests using the XML configuration file:

```bash
mvn test -DsuiteXmlFile=src/test/resources/Run-Tests/WishlistTest.xml
```

Or using Maven Surefire plugin:

```bash
mvn surefire:test -Dsuite=src/test/resources/Run-Tests/WishlistTest.xml
```

## Supported File Types

- ✅ XML files (`.xml`) - Supported by TestNG
- ❌ YAML files (`.yaml` or `.yml`) - Not supported by TestNG
- ❌ JSON files (`.json`) - Not supported by TestNG for test suite configuration