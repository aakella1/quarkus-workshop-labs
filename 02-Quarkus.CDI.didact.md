
# Welcome to Quarkus and CDI

In this step, we’ll add a custom bean using dependency injection (DI). Quarkus DI solution is based on the Contexts and Dependency Injection for Java 2.0 specification.

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