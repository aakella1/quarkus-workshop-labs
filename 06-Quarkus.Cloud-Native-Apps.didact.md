
# Welcome to Quarkus - Cloud Native Apps


In this step we will package the application as a Linux Container image, and deploy it to Kubernetes, and add a few features common to cloud native apps that you as a developer will need to handle. We’ll use OpenShift 4 as our deployment target, which is a distribution of Kubernetes from Red Hat.

## 1.Health Probes

Quarkus application developers can utilize the MicroProfile Health specification to write HTTP health probes for their applications. These endpoints by default provide basic data about the service however they all provide a way to customize the health data and add more meaningful information (e.g. database connection health, backoffice system availability, etc).

>There are of course a category of issues that can’t be resolved by restarting the container. In those scenarios, the container never recovers and traffic will no longer be sent to it (which can have cascading effects on the rest of the system, possibly requiring human intervention, which is why monitoring is crucial to availability).

## 2.Add Extension

Let’s build a simple REST application endpoint exposes `MicroProfile` Health checks at the `/health` endpoint according to the specification. It will also provide several other REST endpoints to allow us to dynamically query the health of our Quarkus application.

We’ll need to add a `Quarkus Extension` to enable this feature in our app. Fortunately, adding a Quarkus extension is super easy. We’ll cover extensions in more depth in other sections of this workshop but for now, open a Terminal and execute the following command to add the extension to our project’s `pom.xml`:
```
mvn quarkus:add-extension -Dextensions="health" -f .
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=QuarkusTerm$$mvn%20quarkus:add-extension%20-Dextensions="health"%20-f%20.&completion=maven%20Quarkus%20add%20health%20extensions. "Opens a new terminal and sends the command above"){.didact})

You should get:
```
✅ Adding extension io.quarkus:quarkus-smallrye-health
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

This will add the extension below to your `pom.xml`:
```
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-health</artifactId>
</dependency>
```
## 3. Running in Dev Mode - Quarkus Cloud native app 

**Live Coding** (also referred to as dev mode) allows us to run the app and make changes on the fly. Quarkus will automatically re-compile and reload the app when changes are made. This is a powerful and efficient style of developing that you will use throughout the lab.

You can use the `mvn` (Maven) command below to run Quarkus apps in dev mode.

```
mvn compile quarkus:dev
```

([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=QuarkusTerm$$mvn%20compile%20quarkus:dev&completion=Run%20live%20coding. "Opens a new terminal and sends the command above"){.didact})

With no code, Quarkus still provides a default health check which may be enough for you if all you need is to know the app started. Try to access the `/health/ready` endpoint on the Terminal:

Now, invoke the health check endpoint using the following curl command:

open [http://localhost:8080/health/ready](http://localhost:8080/health/ready) in your browser or you can also do a curl on a separate terminal

```
curl http://localhost:8080/health/ready
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=curlTerm$$curl%20http://localhost:8080/health/ready%20;%20echo%20''&completion=Run%20curl%20command. "Opens a new terminal and sends the command above"){.didact})

You’ll see:
```
{
    "status": "UP",
    "checks": [
    ]
}
```
This default health check will return success as long as the app is running - if it crashes, the health check will of course fail.

## 4. Add a probe

We can now implement a better Health Check using the MicroProfile APIs. We created a new Java class - `org.acme.people.health.SimpleHealthCheck`. Open the Health check program in `SimpleHealthCheck.java`; in the editor([open](didact://?commandId=vscode.openFolder&projectFilePath=src/main/java/org/acme/people/health/SimpleHealthCheck.java&completion=Opened%20the%20SimpleHealthCheck.java%20file "Opens the SimpleHealthCheck.java file"){.didact}).In this file, you will find the implementation for the health check:

```
package org.acme.people.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Readiness
public class SimpleHealthCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("Simple health check").up().build();
    }
}
```
As you can see health check procedures are defined as implementations of the `HealthCheck` interface which are defined as CDI beans with the either the `@Readiness` or `@Liveness` annotation. `HealthCheck` is a functional interface whose single method `call` returns a `HealthCheckResponse` object which can be easily constructed by the fluent builder API shown above. This simple example will serve as our *Readiness* probe.

>There are two types of probes in Quarkus apps (and Kubernetes):
>
>**Liveness Probe** - Many applications running for long periods of time eventually transition to broken states, and cannot recover except by being restarted. Kubernetes provides liveness probes to detect and remedy such situations. Restarting a container in such a state can help to make the application more available despite bugs.
>
>**Readiness Probe** - Sometimes, applications are temporarily unable to serve traffic. For example, an application might need to load large data or configuration files during startup, or depend on external services after startup. In such cases, you don’t want to kill the application, but you don’t want to send it requests either. Kubernetes provides readiness probes to detect and mitigate these situations. A pod with containers reporting that they are not ready does not receive traffic through Kubernetes Services.
>
>Readiness and liveness probes can be used in parallel for the same container. Using both can ensure that traffic does not reach a container that is not ready for it, and that containers are restarted when they fail. There are various Configuration Paramters you can set, such as the timeout period, frequency, and other parameters that can be tuned to expected application behavior.
>

Thanks to Live Coding mode, simply open a Terminal window and run:


```
curl http://localhost:8080/health/ready
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=curlTerm$$curl%20http://localhost:8080/health/ready%20;%20echo%20''&completion=Run%20curl%20command. "Opens a new terminal and sends the command above"){.didact})

The new health check procedure is now present in the checks array:
```
{
    "status": "UP",
    "checks": [
        {
            "name": "Simple health check",
            "status": "UP"
        }
    ]
}
```

Congratulations! You’ve created your first Quarkus health check procedure. Let’s continue by exploring what else can be done with the MicroProfile Health specification.

## 5. Custom data in health checks
In the previous step we created a simple health check with only the minimal attributes, namely, the health check name and its state (`UP` or `DOWN`). However, MicroProfile also provides a way for the applications to supply arbitrary data in the form of key/value pairs sent in the health check response. This can be done by using the `withData(key, value)` method of the health check response builder API. This is useful to provide additional info about passing or failing health checks, to give some indication of the problem when failures are investigated.

Let’s create our second health check procedure, a *Liveness* probe. Create another Java class file in the `org.acme.people.health` package named `DataHealthCheck.java` with the following code:

in the editor([open](didact://?commandId=vscode.openFolder&projectFilePath=src/main/java/org/acme/people/health/DataHealthCheck.java&completion=Opened%20the%20DataHealthCheck.java%20file "Opens the DataHealthCheck.java file"){.didact}).In this file, you will find the implementation for the health check:
```
package org.acme.people.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Liveness
public class DataHealthCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("Health check with data")
        .up()
        .withData("foo", "fooValue")
        .withData("bar", "barValue")
        .build();

    }
}
```

Access the liveness health checks:
```
curl http://localhost:8080/health/live
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=curlTerm$$curl%20http://localhost:8080/health/live%20;%20echo%20''&completion=Run%20curl%20command. "Opens a new terminal and sends the command above"){.didact})

You can see that the new health check with data is present in the `checks` array. This check contains a new attribute called `data` which is a JSON object consisting of the properties (e.g. `bar=barValue`) we have defined in our health check procedure above:
```
{
    "status": "UP",
    "checks": [
        {
            "name": "Health check with data",
            "status": "UP",
            "data": {
                "bar": "barValue",
                "foo": "fooValue"
            }
        }
    ]
}
```
## 6. Negative Health Checks

In this section we create another health check which simulates a connection to an external service provider such as a database. For simplicity reasons, we’ll use an `application.properties` ([open](didact://?commandId=vscode.openFolder&projectFilePath=src/main/resources/application.properties&completion=Opened%20the%20application.properties%20file "Opens the application.properties file"){.didact}) setting to toggle the health check from `DOWN` to `UP`.

We created another Java class in the same package called `DatabaseConnectionHealthCheck.java`([open](didact://?commandId=vscode.openFolder&projectFilePath=src/main/java/org/acme/people/health/DatabaseConnectionHealthCheck.java&completion=Opened%20the%20DatabaseConnectionHealthCheck.java%20file "Opens the DatabaseConnectionHealthCheck.java file"){.didact}) with the following code:
```
package org.acme.people.health;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Liveness
public class DatabaseConnectionHealthCheck implements HealthCheck {

    @ConfigProperty(name = "database.up", defaultValue = "false")
    public boolean databaseUp;

    @Override
    public HealthCheckResponse call() {

        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("Database connection health check");

        try {
            simulateDatabaseConnectionVerification();
            responseBuilder.up();
        } catch (IllegalStateException e) {
            // cannot access the database
            responseBuilder.down()
                    .withData("error", e.getMessage());
        }

        return responseBuilder.build();
    }

    private void simulateDatabaseConnectionVerification() {
        if (!databaseUp) {
            throw new IllegalStateException("Cannot contact database");
        }
    }
}
```
Re-run the health check test:
```
curl -i http://localhost:8080/health/live
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=curlTerm$$curl%20http://localhost:8080/health/live%20;%20echo%20''&completion=Run%20curl%20command. "Opens a new terminal and sends the command above"){.didact})

You should see at the beginning the HTTP response:
```
HTTP/1.1 503 Service Unavailable
```

And the returned content should begin with `"status": "DOWN"` and you should see in the `checks` array the newly added Database connection health check which is down and the error message explaining why it failed:
```
        {
            "name": "Database connection health check",
            "status": "DOWN",
            "data": {
                "error": "Cannot contact database"
            }
        },
```

## 7. Fix Health Check

We shouldn’t leave this application with a health check in DOWN state. Because we are running Quarkus dev mode, add this to to the end of the `src/main/resources/application.properties` file ([open](didact://?commandId=vscode.openFolder&projectFilePath=src/main/resources/application.properties&completion=Opened%20the%20application.properties%20file "Opens the application.properties file"){.didact}), uncomment the line below and save the properties file:
```
database.up=true
```

And access again using the same `curl` command — it should be `UP`!

```
curl -i http://localhost:8080/health/live
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=curlTerm$$curl%20http://localhost:8080/health/live%20;%20echo%20''&completion=Run%20curl%20command. "Opens a new terminal and sends the command above"){.didact})


## 8. Accessing liveness and readiness separately

Quarkus apps can access the two different types using two different endpoints (`/health/live` and `/health/ready`). This is useful when configuring Kubernetes with probes which we’ll do later, as it can access each separately (and configure each with different timeouts, periods, failure thresholds, etc). For example, You may want your Readiness probe to wait 30 seconds before starting, but Liveness should wait 2 minutes and only wait 10 seconds between retries.

Access the two endpoints. Each endpoint will only report on its specific type of probe:
```
curl http://localhost:8080/health/live
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=curlTerm$$curl%20http://localhost:8080/health/live%20;%20echo%20''&completion=Run%20curl%20command. "Opens a new terminal and sends the command above"){.didact})

You should only see the two Liveness probes.
```
curl http://localhost:8080/health/ready
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=curlTerm$$curl%20http://localhost:8080/health/ready%20;%20echo%20''&completion=Run%20curl%20command. "Opens a new terminal and sends the command above"){.didact})

You should only see our single readiness probes.

Later, when we deploy this to our Kubernetes cluster, we’ll configure it to use these endpoints.

## 9. Externalized Configuration

Hardcoded values in your code are a no-no (even if we all did it at some point ;-)). In this step, we learn how to configure your application to externalize configuration.

Quarkus uses `MicroProfile Config` to inject the configuration into the application. The injection uses the `@ConfigProperty` annotation, for example:

```
@ConfigProperty(name = "greeting.message")
String message;
```

> When injecting a configured value, you can use `@Inject` `@ConfigProperty` or just `@ConfigProperty`. The `@Inject` annotation is not necessary for members annotated with `@ConfigProperty`, a behavior which differs from `MicroProfile` Config.

## 10. Add some external config

To make this exercise easy, we made a copy of GreetingResource.java class, and called it GreetingResourceExternalized.java.

In the `org.acme.people.rest.GreetingResourceExternalized` class ([open](didact://?commandId=vscode.openFolder&projectFilePath=src/main/java/org/acme/people/rest/GreetingResourceExternalized.java&completion=Opened%20the%20GreetingResourceExternalized.java%20file "Opens the GreetingResourceExternalized.java file"){.didact}), notice the following fields to the class definition below the existing `@Inject GreetingService` service; line:

```
@ConfigProperty(name = "greeting.message")
String message;

@ConfigProperty(name = "greeting.suffix", defaultValue="!")
String suffix;

@ConfigProperty(name = "greeting.name")
Optional<String> name;
```

> MicroProfile config annotations include a `name =` (required) and a `defaultValue =` (optional). You can also later access these values directly if declared as a String or other primitive type, or declare them with `<Optional>` type to safely access them using the Optional API in case they are not defined.

Now, notice the modifications made to the `helloext()` method to use the injected properties:
```
@GET
@Produces(MediaType.TEXT_PLAIN)
public String helloext() {
    return message + " " + name.orElse("world") + suffix; 
}
```
Here we use the Optional API to safely access the value using `name.orElse()` and provide a default `world` value in case the value for `name` is not defined in `application.properties`.

> Note that when you do the coding, you’ll get red squiggly errors underneath `@ConfigProperty`. Hover the cursor over them and select Quick Fix:
> and select `Import 'ConfigProperty' (org.eclipse.microprofile.config.inject)` to fix the issue.
> You may have to do the same thing for `java.util.Optional` type to eliminate the errors.

## 11. Create the configuration

By default, Quarkus reads `application.properties` ([open](didact://?commandId=vscode.openFolder&projectFilePath=src/main/resources/application.properties&completion=Opened%20the%20application.properties%20file "Opens the application.properties file"){.didact}). Make sure the following properties are uncommented in the `src/main/resources/application.properties` file:
```
greeting.message = hello
greeting.name = quarkus
```

Open up a Terminal window and run a `curl` command to test the changes in live mode:
```
curl http://localhost:8080/helloext
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=curlTerm$$curl%20http://localhost:8080/helloext%20;%20echo%20''&completion=Run%20curl%20command. "Opens a new terminal and sends the command above"){.didact})

You should get `hello quarkus!`.

> If the application requires configuration values and these values are not set, an error is thrown. So you can quickly know when your configuration is complete.

## 12. Update the test

We also need to create a new functional test for the new endpoint. Notice the changes in the `src/test/java/org/acme/people/GreetingResourceExternalizedTest.java` file ([open](didact://?commandId=vscode.openFolder&projectFilePath=src/test/java/org/acme/people/GreetingResourceExternalizedTest.java&completion=Opened%20the%20GreetingResourceExternalizedTest.java%20file "Opens the GreetingResourceExternalizedTest.java file"){.didact}) and change the content of the testhelloExtEndpoint method to:
```
    @Test
    public void testHelloExtEndpoint() {
        given()
          .when().get("/helloext")
          .then()
             .statusCode(200)
             .body(is("hello quarkus!")); // Modified line
    }
```

Since our application is still running from before, thanks to Quarkus Live Reload we should immediately see changes. Update `application.properties` ([open](didact://?commandId=vscode.openFolder&projectFilePath=src/main/resources/application.properties&completion=Opened%20the%20application.properties%20file "Opens the application.properties file"){.didact}), by removing the comments (#) for `greeting.message`, `greeting.name`, or adding `greeting.suffix` and running the same `curl http://localhost:8080/helloext` after each change.
```
curl http://localhost:8080/helloext
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=curlTerm$$curl%20http://localhost:8080/helloext%20;%20echo%20''&completion=Run%20curl%20command. "Opens a new terminal and sends the command above"){.didact})

## 13. Quarkus Configuration options

Quarkus itself is configured via the same mechanism as your application. Quarkus reserves the `quarkus`. namespace in `application.properties` for its own configuration.

It is also possible to generate an example `application.properties` with *all known* configuration properties, to make it easy to see what Quarkus configuration options are available depending on which extensions you’ve enabled. To do this, open a Terminal and run:
```
mvn quarkus:generate-config -f .
```

([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=QNativeTerm$$mvn%20quarkus:generate-config%20-f%20.&completion=maven%20Quarkus%20generate-config. "Opens a new terminal and sends the command above"){.didact})

This will create a `src/main/resources/application.properties.example` ([open](didact://?commandId=vscode.openFolder&projectFilePath=src/main/resources/application.properties.example&completion=Opened%20the%20application.properties.example%20file "Opens the application.properties.example file"){.didact}) file that contains all the config options exposed via the extensions you currently have installed. These options are commented out, and have their default value when applicable.

>### Overriding properties at runtime
>
>As you have seen, in dev mode, properties can be changed at will and reflected in the running app, however once you are ready to package your app for deployment, you’ll not be running in dev mode anymore, but rather building and packaging (e.g. into fat JAR or native executable.) Quarkus will do much of its configuration and bootstrap at build time. Most properties will then be read and set during the build time step. To change them, you have to stop the application, re-package it, and restart.
>
>Extensions do define some properties as overridable at runtime. A canonical example is the database URL, username and password which is only known specifically in your target environment. **This is a tradeoff** as the more runtime properties are available, the less build time pre-work Quarkus can do. The list of runtime properties is therefore lean.
>
>You can override these runtime properties with the following mechanisms (in decreasing priority):
>
> - using system properties:
>
>   - for a runner jar: `java -Dquarkus.datasource.password=youshallnotpass -jar target/myapp-runner.jar`
>
>   - for a native executable: ``./target/myapp-runner -Dquarkus.datasource.password=youshallnotpass`
>
> - using environment variables:
>
>   - for a runner jar: `QUARKUS_DATASOURCE_PASSWORD=youshallnotpass java -jar target/myapp-runner.jar`
>
>   - for a native executable: `QUARKUS_DATASOURCE_PASSWORD=youshallnotpass ./target/myapp-runner`
>
> Environment variables names are following the conversion rules of `Eclipse MicroProfile Config sources`

## 14. Configuration Profiles

Quarkus supports the notion of configuration profiles. These allow you to have multiple configuration values in `application.properties` and select between then via a profile name.

The syntax for this is `%{profile}.config.key=value`. For example if I have the following: (do not copy this code!):
```
quarkus.http.port=9090
%dev.quarkus.http.port=8181
```
The Quarkus HTTP port will be `9090`, unless the `dev` profile is active, in which case it will be `8181`.

By default Quarkus has three profiles, although it is possible to use as many as you like (just use your custom profile names in `application.properties` and when running the app, and things will match up). The default profiles are:

- `dev` - Activated when in development mode (i.e. `mvn quarkus:dev`)

- `test` - Activated when running tests (i.e. `mvn verify`)

- `prod` - The default profile when not running in `dev` or `test` mode

## 15. Exercise Configuration Profile

Let’s give this a go. In your `application.properties`, add a different `message.prefix` for the `prod` profile. To do this, change the content of the `greeting`. properties in `application.properties` ([open](didact://?commandId=vscode.openFolder&projectFilePath=src/main/resources/application.properties&completion=Opened%20the%20application.properties%20file "Opens the application.properties file"){.didact})to be:
```
greeting.message = hello
greeting.name = quarkus
%prod.greeting.name = production quarkus
```

Verify that in *dev* mode (which you’re currently running in) that:
```
curl http://localhost:8080/helloext
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=curlTerm$$curl%20http://localhost:8080/helloext%20;%20echo%20''&completion=Run%20curl%20command. "Opens a new terminal and sends the command above"){.didact})

produces `hello quarkus!`.

Next, let’s re-build the app as an executable JAR (which will run with the `prod` profile active).

Build an executable JAR using the **Package app for OpenShift** command to build an Uber-JAR:
```
mvn package -DuberJar=true -DskipTests -f .
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=QNativeTerm$$mvn%20package%20-Duberjar=true%20-DskipTests%20-f%20.&completion=maven%20Quarkus%20package%20for%20OpenShift. "Opens a new terminal and sends the command above"){.didact})

Next, run the the rebuilt app in a Terminal:
```
java -Dquarkus.http.port=8081 -jar target/*-runner.jar
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=QNativeTerm$$java%20-Dquarkus.http.port=8081%20-jar%20target/*-runner.jar&completion=java%20-jar%20OpenShift%20uberjar. "Opens a new terminal and sends the command above"){.didact})

Notice we did not specify any Quarkus profile. When not running in dev mode (`mvn quarkus:dev`), and not running in test mode (`mvn verify`), then the default profile is `prod`.

> Notice that the **dev mode** is still running on port 8080, and returns `"hello quarkus!"` as response. We are running **production mode** on port 8081, and expecting a different response.

While the app is running, open a separate Terminal window and test it by running:
```
curl http://localhost:8081/helloext
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=curlTerm$$curl%20http://localhost:8081/helloext%20;%20echo%20''&completion=Run%20curl%20command. "Opens a new terminal and sends the command above"){.didact})

What did you get? You should get `hello production quarkus!` indicating that the `prod` profile was active by default. In other sections in this workshop we’ll use this feature to overrride important variables like database credentials.

>In this example we read configuration properties from `application.properties`. You can also introduce custom configuration sources in the standard MicroProfile Config manner. This would be useful, for example, to read directly from Kubernetes ConfigMaps.

## 16. Cleanup
Stop the app that you ran with `java -jar` by pressing CTRL+C in the terminal in which the app runs. Make sure to leave the original Live Coding app running!

[**Click here to exit the current command**](didact://?commandId=vscode.didact.sendNamedTerminalCtrlC&text=QNativeTerm&completion=Quarkus%20Cloud-native%20interrupted. "Interrupt the current operation on the terminal"){.didact},
or hit `ctrl+c` on the terminal window.


## 17. Congratulations
Cloud native encompasses much more than health probes and externalized config. With Quarkus' *container and Kubernetes-first philosophy*, excellent performance, support for many cloud native frameworks, it’s a great place to build your next cloud native app.



