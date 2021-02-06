package APITest;

import APITestData.APITestData;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.json.simple.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class BookAPI extends APITestData {

    String author_name = "";
    String book_title = "";
    int book_id;

    public static String getAuthor(){
        String random_number = RandomStringUtils.randomNumeric(1);
        return ("Author_" + random_number);
    }
    public static String getTitle(){
        String random_number = RandomStringUtils.randomNumeric(1);
        return ("Title_" + random_number);
    }

    public static HashMap map = new HashMap();

    @BeforeClass
    public void defineTestData()
    {
        api_endpoint = "/api/books";

        baseURI= API_ROOT + api_endpoint;

        map.put("author", getAuthor());
        map.put("title", getTitle());

    }

    @Test(priority = 1)
    public void testNoBooks()
    {

        given().
            header("Content-Type","Application/json").
            get().
        then().
            statusCode(200).
            body("", Matchers.hasSize(0)).
            log().all();
    }
    @Test(priority = 2)
    public void testAuthorNotEmpty()
    {
        author_name = "";
        book_title = (String) map.get("title");

        JSONObject request = new JSONObject();

        request.put("author", author_name);
        request.put("title", book_title);

        System.out.println("The request will be sent as: "+ request.toJSONString());

        given().
            header("Content-Type", "application/json").
            contentType(ContentType.JSON).
            accept(ContentType.JSON).
            body(request.toJSONString()).
        when().
            put().
        then().
            statusCode(400).
            body("error", equalTo("Field 'author' cannot be empty.")).
            log().all();
    }

    @Test(priority = 3)
    public void testTitleNotEmpty()
    {

        author_name = (String) map.get("author");
        book_title = "";

        JSONObject request = new JSONObject();

        request.put("author", author_name);
        request.put("title", book_title);

        System.out.println("The request will be sent as: "+ request.toJSONString());

        given().
            header("Content-Type", "application/json").
            contentType(ContentType.JSON).
            accept(ContentType.JSON).
            body(request.toJSONString()).
        when().
            put().
        then().
            statusCode(400).
            body("error", equalTo("Field 'title' cannot be empty.")).
            log().all();
    }

    @Test(priority = 4)
    public void testAuthorRequired()
    {

        book_title = (String) map.get("title");

        JSONObject request = new JSONObject();

        request.put("title", book_title);

        System.out.println("The request will be sent as: "+ request.toJSONString());

        given().
            header("Content-Type", "application/json").
            contentType(ContentType.JSON).
            accept(ContentType.JSON).
            body(request.toJSONString()).
        when().
            put().
        then().
            statusCode(400).
            body("error", equalTo("Field 'author' is required.")).
            log().all();
    }

    @Test(priority = 5)
    public void testBookTitleRequired()
    {

        author_name = (String) map.get("author");

        JSONObject request = new JSONObject();

        request.put("author", author_name);

        System.out.println("The request will be sent as: "+ request.toJSONString());

        given().
            header("Content-Type", "application/json").
            contentType(ContentType.JSON).
            accept(ContentType.JSON).
            body(request.toJSONString()).
        when().
            put().
        then().
            statusCode(400).
            body("error", equalTo("Field 'title' is required.")).
            log().all();
    }

    @Test(priority = 6)
    public void testBookIdReadOnly()
    {
        map.put("author", getAuthor());
        map.put("title", getTitle());

        author_name = (String) map.get("author");
        book_title = (String) map.get("title");
        book_id = 1001;
        map.put("id", book_id);

        int created_book_id = given().
            header("Content-Type", "application/json").
            contentType(ContentType.JSON).
            accept(ContentType.JSON).
            body(map).
        when().
            put().
        then().
            statusCode(200).
            body("author", equalTo(author_name),"title", equalTo(book_title),"id", not(book_id)).
            extract().path("id");

        // query book with given book_id. It should throw 404 error
        given().
            header("Content-Type","Application/json").
            get("/"+ book_id).
        then().
            statusCode(404).
            log().all();

        System.out.println("ID of the book cannot be set to "+ book_id + ". " + "Created Book's book_id is: " + created_book_id);

    }

    @Test(priority = 7)
    public void testCreateBook()
    {
        map.put("author", getAuthor());
        map.put("title", getTitle());
        // define author name and book title from the randomly generated map value
        author_name = (String) map.get("author");
        book_title = (String) map.get("title");

        System.out.println("A book will be created with Author Name: "+ author_name + " and " +"Book Title: " + book_title);

        // create book with PUT request and get the book_id from the response message
        int book_id = given().
            header("Content-Type", "application/json").
            contentType(ContentType.JSON).
            accept(ContentType.JSON).
            body(map).
        when().
            put().
        then().
            statusCode(200).
            body("author", equalTo(author_name),"title", equalTo(book_title)).
            extract().path("id");

        System.out.println("Created Book's book_id is: " + book_id);

        // query the created book with the book_id
        given().
            header("Content-Type","Application/json").
            get("/"+ book_id).
        then().
            statusCode(200).
            body("author", equalTo(author_name),"title", equalTo(book_title)).
            log().all();
    }

    @Test(priority = 8)
    public void testDuplicateBook()
    {
        map.put("author", getAuthor());
        map.put("title", getTitle());

        // Note from the Case Study: The testing API is reset before each of your test cases.
        JSONObject request = new JSONObject();

        String author_name_static = "John Smith";
        String title_name_static = "Reliability of late night deployments";

        request.put("author", author_name_static);
        request.put("title", title_name_static);

        given().header("Content-Type", "application/json").
            contentType(ContentType.JSON).
            accept(ContentType.JSON).
            body(request.toJSONString()).
        when().
            put().
        then().
            statusCode(200).
            body("author", equalTo(author_name_static),"title", equalTo(title_name_static));

        given().header("Content-Type", "application/json").
            contentType(ContentType.JSON).
            accept(ContentType.JSON).
            body(request.toJSONString()).
        when().
            put().
        then().
            statusCode(400).
            body("error", equalTo("Another book with similar title and author already exists."));
    }
}
