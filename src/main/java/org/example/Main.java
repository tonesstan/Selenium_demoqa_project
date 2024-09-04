package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.net.HttpURLConnection;


public class Main {
//    public static void main(String[] args) {
//        // 1. Указать путь к скаченному chromedriver: можно через PATH или Java Properties
//        System.setProperty("WebDriver.chrome.driver", "D:\\Soft\\chromedriver-win64\\chromedriver.exe");
//
//        // 2. Создание объекта драйвер, который будет общаться с chromedriver
//        WebDriver driver = new ChromeDriver();
//
//        // 3. Выполнение команд
//        driver.get("https://demoqa.com/books");
//
//        // находим строки таблицы с информацией по книгам (включая строку с названиями столбцов - table header);
//        // исключаем из них пустые строки
//        List<WebElement> tableRowsIncludingHeader = driver.findElements(By.className("rt-tr"));
//        List<WebElement> filteredTableRows = tableRowsIncludingHeader.stream()
//                .filter(row -> !row.getAttribute("class").contains("-padRow"))
//                .toList();
//
//        // выводим на экран содержание строк таблицы;
//        // начнём с table header
//        final String infoDelimiter = " - ";
//
//        String headerValues = filteredTableRows.get(0)
//                .findElements(By.className("rt-resizable-header-content"))
//                .stream()
//                .map(WebElement::getText)
//                .collect(Collectors.joining(infoDelimiter));
//
//        System.out.println(headerValues);
//
//        // перейдём к информации по книгам
//        filteredTableRows.stream().skip(1)
//                .map(tableRow -> {
//                    String imgSrc = tableRow.findElement(By.tagName("img")).getAttribute("src");
//
//                    WebElement bookCell = tableRow.findElement(By.tagName("a"));
//                    String bookLink = bookCell.getAttribute("href");
//                    String bookTitle = bookCell.getText();
//
//                    // ячейки с автором и издателем мы можем идентифицировать только по имени тега и индексу
//                    final int authorCellIndex = 2;
//                    String bookAuthor = tableRow.findElements(By.className("rt-td")).get(authorCellIndex).getText();
//
//                    final int publisherCellIndex = 3;
//
//                    // более хитрый способ найти по индексу; важно указывать КОНТЕКСТ!
//                    String publisherCellXpath = String.format("(.//div)[%d]", publisherCellIndex + 1);
//                    String bookPublisher = tableRow.findElement(By.xpath(publisherCellXpath)).getText();
//
//                    // соединяем всю полученную информацию в одну строку
//                    String tableRowInfo = String.join(infoDelimiter, imgSrc, "(" + bookLink, bookTitle + ")", bookAuthor, bookPublisher);
//                    return tableRowInfo;
//                })
//                .forEach(System.out::println);
//
//        // 4. Окончание сессии
//        driver.quit();
//        //driver.close() - закрывает только текущее окно; если оно единственно открытое, то заканчивается и сессия
//    }

    // тут идёт вся логика вашего скрипта; закомментируйте скрипт рассмотренный на уроке перед тем, как начнёте разработку;
    // сама логика основного скрипта уже описана, вам же нужно только реализовать сами методы
    public static void main(String[] args) {
        WebDriver driver = configureAndCreateDriver();
        printAllFullBookInfoToTerminal(driver);
        tearDownDriver(driver);
    }

    public static WebDriver configureAndCreateDriver() {
        // Опишите тут логику настройки драйвера и его инициализации

        // верните ссылку на созданный объект

        ChromeOptions options = (ChromeOptions) new ChromeOptions().setPageLoadStrategy(PageLoadStrategy.EAGER).addArguments("--headless", "--window-size=1920,1080", "--disable-notifications", "--disable-gpu", "--disable-dev-tools");
        return new ChromeDriver(options);
    }

    public static void tearDownDriver(WebDriver driver) {
        // Опишите логику корректного закрытия драйвера (так, как мы делали во время урока)
        driver.quit();
    }

    public static void printAllFullBookInfoToTerminal(WebDriver driver) {
        // 1. Переходим на страницу с данными обо всех книгах и извлекаем из неё информацию
        JsonArray BooksJSON = getBooksJSON().getAsJsonArray("books");

        // 2. Используем метод getAllBookISBN, чтобы получить список ссылок всех книг
        // 3. Для каждого элемента полученного листа используем метод printFullBookInfoToTerminal, чтобы вывести информацию по всем книгам на экран
        for (String bookISBN : getAllBooksISBN(driver)) {
            printFullBookInfoToTerminal(driver, bookISBN, BooksJSON);
        }
    }

    public static List<String> getAllBooksISBN(WebDriver driver) {
        // 1. Перейти на страницу с перечнем всех книг
        driver.get("https://demoqa.com/books");

        // 2. Извлечём url каждой книги и достанем из них ISBN
        // Например, если у книги в таблице url равен "/books?book=9781449325862", то её ISBN будет 9781449325862
        List<WebElement> bookUrls = driver.findElements(By.xpath("//*[@class='action-buttons']/span/a"));

        // 3. Вернуть ISBN каждой книги
        return bookUrls.stream().map(bookUrl -> Objects.requireNonNull(bookUrl.getAttribute("href")).replaceAll("[^0-9]", "")).toList();
    }

    public static void printFullBookInfoToTerminal(WebDriver driver, String bookISBN, JsonArray BooksJSON) {
        for (JsonElement bookJSON : BooksJSON) {
            if (bookJSON.getAsJsonObject().get("isbn").getAsString().equals(bookISBN)) {
                printBookInfoToTerminal(driver, bookJSON.getAsJsonObject());
                break;
            }
        }
    }

    private static void printBookInfoToTerminal(WebDriver driver, JsonObject bookJSON) {
        // Используем метод printBookInfoToTerminal, чтобы вывести информацию по конкретной книге на экран
        Locale locale = new Locale.Builder().setLanguage("ru").setRegion("RU").build();
        System.out.println("\n");
        System.out.println("ISBN книги: " + bookJSON.get("isbn").getAsString());
        System.out.println("Название книги: " + translate(driver, bookJSON.get("title").getAsString()));
        System.out.println("Подзаголовок книги: " + translate(driver, bookJSON.get("subTitle").getAsString()));
        System.out.println("Автор книги: " + translate(driver, bookJSON.get("author").getAsString()));
        System.out.println("Дата публикации: " + LocalDate.parse(bookJSON.get("publish_date").getAsString().substring(0, 10)).format(DateTimeFormatter.ofPattern("d MMMM yyyy 'года'", locale)));
        System.out.println("Издательство: " + bookJSON.get("publisher").getAsString());
        System.out.println("Количество страниц: " + bookJSON.get("pages").getAsInt());
        System.out.println("Описание книги: " + translate(driver, bookJSON.get("description").getAsString()));
        System.out.println("URL книги: " + bookJSON.get("website").getAsString());
    }

    private static String translate(WebDriver driver, String text) {

        driver.get("https://www.deepl.com/ru/translator#en/ru/" + text);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//d-textarea[@name='target']//child::span")));

        List<WebElement> translations = driver.findElements(By.xpath("//d-textarea[@name='target']//child::span"));
        StringBuilder result = new StringBuilder();
        for (WebElement element : translations) {
            result.append(element.getAttribute("innerText"));
        }
        return result.toString();
    }

    private static JsonObject getBooksJSON() {
        String url = "https://demoqa.com/BookStore/v1/Books";
        StringBuilder response = new StringBuilder();

        try {
            URL obj = new URI(url).toURL();
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            } else {System.out.println("GET request not worked");}
            con.disconnect();
        } catch (IOException | URISyntaxException e) {throw new RuntimeException(e);}

        return JsonParser.parseString(response.toString()).getAsJsonObject();
    }
}