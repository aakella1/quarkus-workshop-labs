
# Welcome to Quarkus and CDI

In this step, we’ll add a custom bean using dependency injection (DI). Quarkus DI solution is based on the Contexts and Dependency Injection for Java 2.0 specification.

## 1. Quick Peek - Quarkus CDI

For CDI, we work on an injectable bean that implements a `greeting()` method returning a string `hello <hostname>` (where `<hostname>` is the Linux hostname of the machine on which the code runs).

The program is in `GreetingService.java` ([open](didact://?commandId=vscode.openFolder&projectFilePath=src/main/java/org/acme/people/service/GreetingService.java&completion=Opened%20the%20GreetingService.java%20file "Opens the GreetingService.java file"){.didact}).

Next, open the existing `GreetingResource.java`([open](didact://?commandId=vscode.openFolder&projectFilePath=src/main/java/org/acme/people/rest/GreetingResource.java&completion=Opened%20the%20GreetingResource.java%20file "Opens the GreetingResource.java file"){.didact}) file (in the `org.acme.people.rest` package) and notice the `greeting` method.

>```    
>    @Inject
>    GreetingService service;
>
>    @GET
>    @Produces(MediaType.TEXT_PLAIN)
>    @Path("/greeting/{name}")
>    public String greeting(@PathParam("name") String name) {
>        return service.greeting(name);
>    }
>```

This will cause our new `GreetingResource` class to be instantiated and injected as the `service` field, and then the method `greeting` accesses this service to return the name.

>Notice the necessary imports below the existing import statements near the top of the `GreetingResource.java` file:
>```
>import javax.inject.Inject;
>import org.acme.people.service.GreetingService;
>import javax.ws.rs.PathParam;
>```

## 2. Running in Dev Mode - Quarkus Hello World 

**Live Coding** (also referred to as dev mode) allows us to run the app and make changes on the fly. Quarkus will automatically re-compile and reload the app when changes are made. This is a powerful and efficient style of developing that you will use throughout the lab.

You can use the `mvn` (Maven) command below to run Quarkus apps in dev mode.

```
mvn compile quarkus:dev
```

([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=QuarkusTerm$$mvn%20compile%20quarkus:dev&completion=Run%20live%20coding. "Opens a new terminal and sends the command above"){.didact})

open [localhost:8080/hello/greeting/quarkus](http://localhost:8080/hello/greeting/quarkus) in your browser or you can also do a curl on a separate terminal

```
curl http://localhost:8080/hello/greeting/quarkus
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=curlTerm$$curl%20http://localhost:8080/hello/greeting/quarkus%20;%20echo%20''&completion=Run%20curl%20command. "Opens a new terminal and sends the command above"){.didact})


## 3. Cleanup

[**Click here to exit the current command**](didact://?commandId=vscode.didact.sendNamedTerminalCtrlC&text=QuarkusTerm&completion=Quarkus%20K%20Hello%20World%20interrupted. "Interrupt the current operation on the terminal"){.didact},
or hit `ctrl+c` on the terminal window.


## 4. Congratulations!

It’s a familiar CDI-based environment for you Enterprise Java developers out there, with powerful mechanisms to reload your code as you type (or very close to realtime). 

In the next step, we’ll create some tests for our app, which should also be familiar to all developers.