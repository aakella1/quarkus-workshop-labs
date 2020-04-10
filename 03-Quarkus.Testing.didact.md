
# Welcome to Quarkus Testing

In this step we’ll show you how to effectively write functional and unit tests for your Quarkus Apps.

## Before you begin

Make sure you check-out this repository from git and open it with [VSCode](https://code.visualstudio.com/).

Instructions are based on [VSCode Didact](https://github.com/redhat-developer/vscode-didact), so make sure it's installed
from the VSCode extensions marketplace.

From the VSCode UI, right-click on the `02-Quarkus.CDI.didact.md` file and select "Didact: Start Didact tutorial from File". A new Didact tab will be opened in VS Code.

[Make sure you've checked all the requirements](./requirements.didact.md) before jumping into the tutorial section.

## Checking requirements

<a href='didact://?commandId=vscode.didact.validateAllRequirements' title='Validate all requirements!'><button>Validate all Requirements at Once!</button></a>

**Quarkus Tools for Visual Studio Code Extension Pack**

The Quarkus Tools for Visual Studio Code Extension Pack by Red Hat provides a collection of useful tools for Quarkus developers, such as code completion and integrated lifecycle management.

You can install it from the VS Code Extensions marketplace.

[Check if the Quarkus Tools for Visual Studio Code Extension Pack by Red Hat is installed](didact://?commandId=vscode.didact.extensionRequirementCheck&text=extension-requirement-status$$redhat.vscode-quarkus&completion=Quarkus%20extension%20pack%20is%20available%20on%20this%20system. "Checks the VS Code workspace to make sure the extension pack is installed"){.didact}

*Status: unknown*{#extension-requirement-status}

**OpenShift CLI ("oc")**

The OpenShift CLI tool ("oc") will be used to interact with the OpenShift cluster.

[Check if the OpenShift CLI ("oc") is installed](didact://?commandId=vscode.didact.cliCommandSuccessful&text=oc-requirements-status$$oc%20help&completion=Checked%20oc%20tool%20availability "Tests to see if `oc help` returns a 0 return code"){.didact}

*Status: unknown*{#oc-requirements-status}


**Connection to an OpenShift cluster**

You need to connect to an OpenShift cluster in order to run the examples.

[Check if you're connected to an OpenShift cluster](didact://?commandId=vscode.didact.requirementCheck&text=cluster-requirements-status$$oc%20get%20project$$NAME&completion=OpenShift%20is%20connected. "Tests to see if `kamel version` returns a result"){.didact}

*Status: unknown*{#cluster-requirements-status}

## 1. Quick Peek - Quarkus Testing

You should already have a completed test written for you, including the correct `pom.xml`([open](didact://?commandId=vscode.openFolder&projectFilePath=pom.xml&completion=Opened%20the%20pom.xml%20file "Opens the pom.xml file"){.didact})setup where you should see 2 test dependencies:

```
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-junit5</artifactId>
    <version>${quarkus.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
</dependency>
```
`quarkus-junit5` is required for testing, as it provides the `@QuarkusTest` annotation that controls the testing framework. `rest-assured` is not required but is a convenient way to test HTTP endpoints, we also provide integration that automatically sets the correct URL so no configuration is required.

The basic test is in the `GreetingResourceTest` ([open](didact://?commandId=vscode.openFolder&projectFilePath=src/test/java/org/acme/people/GreetingResourceTest.java&completion=Opened%20the%20GreetingResourceTest.java%20file "Opens the GreetingResourceTest.java file"){.didact}). For example:
```
@QuarkusTest
public class GreetingResourceTest {
    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/hello")
          .then()
             .statusCode(200)
             .body(is("hello"));
    }
}
```

Also, notice the second test case for testing the uri `/hello/greeting/{name}`
```
    @Test
    public void testGreetingEndpoint() {
        String uuid = UUID.randomUUID().toString();
        given()
          .pathParam("name", uuid)
          .when().get("/hello/greeting/{name}")
          .then()
            .statusCode(200)
            .body(startsWith("hello " + uuid));
    }
```


## 2. Test the app 

You can use the `mvn` (Maven) command below to test Quarkus apps.

```
mvnw clean compile test
```

([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=QuarkusTerm$$mvn%20clean%20compile%20test&completion=mvn%20quarkus%20test. "Opens a new terminal and sends the command above"){.didact})

The tests will run, and eventually complete. Did you get any errors? You probably got:
```
[ERROR] testHelloEndpoint  Time elapsed: 1.112 s  <<< FAILURE!
java.lang.AssertionError:
1 expectation failed.
Response body doesn't match expectation.
Expected: is "hello"
  Actual: hola
  ```

This is because you changed the `greeting` in an earlier step. In `GreetingResource` ([open](didact://?commandId=vscode.openFolder&projectFilePath=src/main/java/org/acme/people/rest/GreetingResource.java&completion=Opened%20the%20GreetingResource.java%20file "Opens the GreetingResource.java file"){.didact}), change `hola` back to `hello` and re-run the test and confirm it passes with BUILD SUCCESS using the same command:

## 3. Controlling the test port

While Quarkus will listen on port `8080` by default, when running tests it defaults to `8081`. This allows you to run tests while having the application running in parallel (which you just did - your app is still running from the previous exercises).

You can configure the port used by tests by configuring `quarkus.http.test-port` in your `application.properties` ([open](didact://?commandId=vscode.openFolder&projectFilePath=src/main/resources/application.properties&completion=Opened%20the%20Application%20Properties%20file "Opens the Application Properties file"){.didact}). 
Open that file and make sure you see the new line at the end:

`quarkus.http.test-port=8083`

Now re-run the tests
```
mvnw clean compile test
```

([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=QuarkusTerm$$mvn%20clean%20compile%20test&completion=mvn%20quarkus%20test. "Opens a new terminal and sends the command above"){.didact})

>look for
>INFO  Quarkus x.x.x.x started in x.xxxs. Listening on: `http://[...]:8083`
>Notice the port `8083`.

## 5. Injecting a URI in Tests

It is also possible to directly inject the URL into the test which can make is easy to use a different client. This is done via the `@TestHTTPResource` annotation.

Check the content in `test.html` file ([open](didact://?commandId=vscode.openFolder&projectFilePath=src/main/resources/META-INF/resources/test.html&completion=Opened%20the%20test.html%20file "Opens the test.html file"){.didact}).

Our test will verify that the `<title>` tags contain the right content.

Now, let us check the content of StaticContentTest.java ([open](didact://?commandId=vscode.openFolder&projectFilePath=src/test/java/org/acme/people/StaticContentTest.java&completion=Opened%20the%20StaticContentTest.java%20file "Opens the StaticContentTest.java file"){.didact}) which does the testing

>The `@TestHTTPResource` annotation allows you to directly inject the URL of the Quarkus instance, the value of the annotation will be the path component of the URL. For now `@TestHTTPResource` allows you to inject URI, URL and String representations of the URL.

Re-run the tests
```
mvnw clean compile test
```

([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=QuarkusTerm$$mvn%20clean%20compile%20test&completion=mvn%20quarkus%20test. "Opens a new terminal and sends the command above"){.didact})

## 6. Injection into tests
So far we have only covered integration style tests that test the app via HTTP endpoints, but what if we want to do unit testing and test our beans directly?

Quarkus supports this by allowing you to inject CDI beans into your tests via the `@Inject` annotation (in fact, tests in Quarkus are full CDI beans, so you can use all CDI functionality). Let’s look into a simple test that tests the greeting service directly without using HTTP.

Check test class file in `src/test` in the `org.acme.people` package called `GreetingServiceTest.java` ([open](didact://?commandId=vscode.openFolder&projectFilePath=src/test/java/org/acme/people/GreetingServiceTest.java&completion=Opened%20the%20GreetingServiceTest.java%20file "Opens the GreetingServiceTest.java file"){.didact}). 

Here we are injecting our `GreetingService` and calling it, just as our RESTful resource endpoint does in the production code.
Run the tests again to verify the new test passes.
```
mvnw clean compile test
```

([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=QuarkusTerm$$mvn%20clean%20compile%20test&completion=mvn%20quarkus%20test. "Opens a new terminal and sends the command above"){.didact})

>As mentioned above Quarkus tests are actually full CDI beans, and as such you can apply CDI interceptors as you would normally. As an example, if you want a test method to run within the context of a transaction you can simply apply the `@Transactional` annotation to the method and the transaction interceptor will handle it.
>
>In addition to this you can also create your own test *stereotypes*. Stereotypes can be particularly useful in large applications where you have a number of beans that perform similar functions, as it allows you to do something akin to multiple inheritance (multiple annotations) without having to repeat yourself over and over.
>
>For example we could create a `@TransactionalQuarkusTest` if we needed to write a large number of tests that required transactional support with particular configuration. It would look like 
>```
>@QuarkusTest
>@Stereotype
>@Transactional
>@Retention(RetentionPolicy.RUNTIME)
>@Target(ElementType.TYPE)
>public @interface TransactionalQuarkusTest {
>}
>```
>If we then apply this annotation to a test class it will act as if we had applied both the `@QuarkusTest` and `@Transactional` annotations, e.g.:
>```
>@TransactionalQuarkusTest
>public class TestStereotypeTestCase {
>
>    @Inject
>    UserTransaction userTransaction;
>
>    @Test
>    public void testUserTransaction() throws Exception {
>        Assertions.assertEquals(Status.STATUS_ACTIVE, userTransaction.getStatus());
>    }
>
>}
>```

## 8. Mock Support

Quarkus supports the use of mock objects using the CDI `@Alternative` mechanism. To use this simply override the bean you wish to mock with a class in the `src/test/java` directory, and put the `@Alternative` and `@Priority(1)` annotations on the bean. Alternatively, a convenient `io.quarkus.test.Mock` stereotype annotation could be used. This built-in stereotype declares `@Alternative`, `@Priority(1)` and `@Dependent`.

Let’s mock our existing `GreetingService`. Although our existing service is pretty simple, in the real world the service might have too many dependencies on external systems to be feasible to call directly.

Check the class file in `src/test/java` in the `org.acme.people` package called `MockGreetingService.java`([open](didact://?commandId=vscode.openFolder&projectFilePath=src/test/java/org/acme/people/MockGreetingService.java&completion=Opened%20the%20MockGreetingService.java%20file "Opens the MockGreetingService.java file"){.didact}).


Now check the `MockGreetingServiceTest` class ([open](didact://?commandId=vscode.openFolder&projectFilePath=src/test/java/org/acme/people/MockGreetingServiceTest.java&completion=Opened%20the%20MockGreetingServiceTest.java%20file "Opens the MockGreetingServiceTest.java file"){.didact}). to find a log statement showing the value retrieved during the test. Modify the `testGreetingService` method to look like:

```
    @Test
    public void testGreetingService() {
        LOGGER.info("greeting: " + service.greeting("Quarkus"));
        Assertions.assertTrue(service.greeting("Quarkus").startsWith("hello Quarkus"));
    }
```    
Basically we’ve added a new `LOGGER.info` line.

Now run the tests again (with Run Tests) and watch the output closely 
```
mvnw clean compile test
```

([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=QuarkusTerm$$mvn%20clean%20compile%20test&completion=mvn%20quarkus%20test. "Opens a new terminal and sends the command above"){.didact})

>you will see:
>
>INFO  [GreetingServiceTest] (main) greeting: hello Quarkus <<<<<<<<<< from mock greeting >>>>>>>>>>

This confirms that our `MockGreetingService` is being used as a separate test along with the original `GreetingService`.

## 9. Congratulations!

In this section we covered basic testing of Quarkus Apps using the `@QuarkusTest` and supporting annotations. This is an important part of any software engineering project and with Quarkus, testing has never been easier. For more information on testing with Quarkus, be sure to review the Quarkus Testing Guide.

In the next section we’ll talk about how to effectively debug Quarkus applications. On with the show!