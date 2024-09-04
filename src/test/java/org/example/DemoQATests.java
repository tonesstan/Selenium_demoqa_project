package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.security.SecureRandom;
import java.nio.file.*;

public class DemoQATests {
    private WebDriver driver;

    @BeforeTest
    public void configureDriver() {
        // 1. Указать путь к скаченному chromedriver: можно через PATH или Java Properties
        // 2. Создание объекта драйвер, который будет общаться с chromedriver
        ChromeOptions options = (ChromeOptions) new ChromeOptions().setPageLoadStrategy(PageLoadStrategy.EAGER).addArguments("--headless", "--window-size=1920,1080", "--disable-notifications", "--disable-gpu", "--disable-dev-tools");
        this.driver = new ChromeDriver(options);

        // 3. Зададим время неявного ожидания при поиске любого элемента
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @BeforeMethod
    public void clearCookiesAndOpenLoginPage() {
        driver.manage().deleteAllCookies();
        driver.get("https://demoqa.com/login");
    }

    @Test
    public void loginUser() {
        System.out.println("Проверяем вход по валидным данным");
        // блок констант с данными зарегистрированного пользователя
        final String userName = "m5";
        final String password = "P@ssw0rd";

        // переходим на страницу логина
        // находим поле UserName и вводим значение
        // находим поле Password и вводим значение
        // скролим до конца страницы, чтобы реклама не перекрывала кнопку Login
        //scrollToBottomUsing(driver);
        // находим кнопку Login и нажимаем её
        login(driver, userName, password);

        // проверяем успешность логина;
        // критерий - наличие лэйбла UserName, содержащий значение текущего пользователя, а также кнопки Log out на открывшейся странице
        WebElement userNameValue = driver.findElement(By.id("userName-value"));
        Assert.assertEquals(userNameValue.getText(), userName, "Некорректное значение UserName обнаружено на странице после логина");

        // по id будут найдены несколько элементов, поэтому применяем другой локатор
        List<WebElement> possibleLogOutButtons = driver.findElements(By.xpath("//*[text()='Log out']"));
        Assert.assertFalse(possibleLogOutButtons.isEmpty(), "Log Out button was not found on the page");
    }

    @Test
    public void loginUser_useNonExistentUserName() {
        System.out.println("Проверяем вход по неверному логину");
        // 1. Сгенерируйте несуществующий UserName: можете подобрать вручную, а можно через код, используя "UUID.randomUUID().toString();"
        // 2. Заполните поле UserName сгенерированным значением
        // 3. Заполните поле Password любым значением
        // 4. Нажмите кнопку Login
        login(driver, generateRandomString(6, 15, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-"), "P@ssw0rd");

        // 5. Проверьте наличие ошибки на экране с текстом "Invalid username or password!"
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("mb-1")));
        Assert.assertEquals(element.getText(), "Invalid username or password!");
    }

    @Test
    public void loginUser_incorrectPassword() {
        System.out.println("Проверяем вход верного пользователя по неверному паролю");
        // 1. Заполните поле UserName значением, совпадающим со значением созданного вами пользователя
        // 2. Намеренно заполните поле Password некорректным значением
        // 3. Нажмите кнопку Login
        login(driver, "m5", generateRandomPassword());

        // 4. Проверьте наличие ошибки на экране с текстом "Invalid username or password!"
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("mb-1")));
        Assert.assertEquals(element.getText(), "Invalid username or password!");
    }

    // Задание со звёздочкой
    @Test
    public void createNewUser() throws Exception {
        System.out.println("Проверяем создание нового пользователя");
        // создадим блок констант с данными нового пользователя
        // в качестве имени и фамилии используем случайные имена и фамилии
        final String firstName = getRandomLineFromFile("FirstNames.MD");
        final String lastName = getRandomLineFromFile("LastNames.MD");
        // в качестве логина и пароля используем допустимые случайные значения
        final String userName = generateRandomString(6, 15, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-");
        final String password = generateRandomPassword();

        // скролим до конца страницы, чтобы реклама не перекрывала кнопку Login
        scrollToBottomUsing(driver);

        // заходим на страницу регистрации
        WebElement newUserButton = driver.findElement(By.id("newUser"));
        newUserButton.click();

        // проверим, что мы на странице с регистрацией; используем tag name локатор
        WebElement registrationHeader = driver.findElement(By.tagName("h4"));
        Assert.assertEquals(registrationHeader.getText(), "Register to Book Store", "Couldn't access register page");

        // поиск по id и ввод
        WebElement firstNameInput = driver.findElement(By.id("firstname"));
        firstNameInput.sendKeys(firstName);

        // поиск по id, но через css-селектор, и ввод
        WebElement lastNameInput = driver.findElement(By.cssSelector("#lastname"));
        lastNameInput.sendKeys(lastName);

        // поиск по id, но через XPath, и ввод
        WebElement userNameInput = driver.findElement(By.xpath("//input[@id='userName']"));
        userNameInput.sendKeys(userName);

        // поиск по compound class name (через css-селектор) и ввод
        WebElement passwordInput = driver.findElements(By.cssSelector(".mr-sm-2.form-control")).get(3);
        passwordInput.sendKeys(password);

        // скролим до конца страницы, чтобы реклама не перекрывала iframe с чекбоксом для captcha
        scrollToBottomUsing(driver);

        // переключаем контекст браузера на него, чтобы иметь возможность нажать на чекбокс
        WebElement recaptchaIframe = driver.findElement(By.cssSelector("#g-recaptcha iframe"));
        driver.switchTo().frame(recaptchaIframe);

        // поиск по id и чек
        WebElement captchaCheckbox = driver.findElement(By.id("recaptcha-anchor"));
        captchaCheckbox.click();

        // ждём, пока captcha будет пройдена
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {wait.until(ExpectedConditions.attributeToBe(captchaCheckbox, "aria-checked", "true"));}
        catch (TimeoutException e) {Assert.fail("Captcha was not checked");}

        // переключаемся с iframe обратно
        driver = driver.switchTo().defaultContent();

        // поиск по id и клик
        WebElement registerButton = driver.findElement(By.id("register"));
        registerButton.click();

        // проверяем наличие алерта с сообщением об успешной регистрации
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());

        // переключаемся на алерт и проверяем наличие сообщения об успешной регистрации
        Assert.assertEquals(alert.getText(), "User Register Successfully.", "Алерт содержит некорректное сообщение об успешной регистрации");

        alert.accept();
    }

    @AfterTest
    public void tearDownDriver() {if (driver != null) {driver.quit();}}

    // скролим страницу до конца, используя JavaScript
    private static void scrollToBottomUsing(WebDriver webDriver) {
        JavascriptExecutor js = (JavascriptExecutor) webDriver;
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    //авторизация с заданным логином и паролем
    private static void login(WebDriver driver, String login, String password) {
        //находим поле UserName и вводим заданное значение
        WebElement loginInput = driver.findElement(By.id("userName"));
        loginInput.sendKeys(login);
        //находим поле Password и вводим заданное значение
        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.sendKeys(password);
        // скролим до конца страницы, чтобы реклама не перекрывала кнопку Login
        scrollToBottomUsing(driver);
        //находим кнопку Login и нажимаем её
        WebElement loginButton = driver.findElement(By.id("login"));
        loginButton.click();
    }

    //извлекаем случайную строчку из текстового файла
    private static String getRandomLineFromFile(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        SecureRandom random = new SecureRandom();
        int index = random.nextInt(lines.size());
        return lines.get(index);
    }

    //генерируем случайную строку с заданными допустимыми длиной и символами
    private static String generateRandomString(int minLength, int maxLength, String allowedChars) {
        SecureRandom random = new SecureRandom();
        int length = random.nextInt(maxLength - minLength) + minLength;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(allowedChars.length());
            sb.append(allowedChars.charAt(index));
        }
        return sb.toString();
    }

    //генерируем случайный пароль, соответствующий допустимым значениям и требованиям
    private static String generateRandomPassword() {
        String password;
        do {
            password = generateRandomString(8, 20, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%^&*(){}[]<>!?:;.,-_+/=~|");
        } while (!(password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*[0-9].*") &&
                password.matches(".*[@#$%^&*(){}\\[\\]<>!?:;.,\\-_+/=~|].*") &&
                password.matches(".*[@#$%^&*].*")));
        return password;
    }
}