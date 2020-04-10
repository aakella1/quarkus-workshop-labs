
# Welcome to Quarkus on Didact!

In this step, you will create a straightforward application serving a hello endpoint. To demonstrate dependency injection this endpoint uses a greeting bean.

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

## 1. Quick Peek - Quarkus Hello World

The Hello World program is in `GreetingResource.java` ([open](didact://?commandId=vscode.openFolder&projectFilePath=src/main/java/org/acme/people/rest/GreetingResource.java&completion=Opened%20the%20GreetingResource.java%20file "Opens the GreetingResource.java file"){.didact}).

> **Note:** Compared to vanilla JAX-RS, with Quarkus there is no need to create an `Application` class. It’s supported but not required. In addition, only one instance of the resource is created and not one per request. You can configure this using the different `*Scoped` annotations (`ApplicationScoped`, `RequestScoped`, etc).

## 2. Running in Dev Mode - Quarkus Hello World 

**Live Coding** (also referred to as dev mode) allows us to run the app and make changes on the fly. Quarkus will automatically re-compile and reload the app when changes are made. This is a powerful and efficient style of developing that you will use throughout the lab.

You can use the `mvn` (Maven) command below to run Quarkus apps in dev mode.

```
mvn compile quarkus:dev
```

([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=QuarkusTerm$$mvn%20compile%20quarkus:dev&completion=Run%20live%20coding. "Opens a new terminal and sends the command above"){.didact})

open [localhost:8080](http://localhost:8080) in your browser. 

Now, invoke the hello endpoint using the following curl command:

open [localhost:8080/hello](http://localhost:8080/hello) in your browser or you can also do a curl on a separate terminal

```
curl http://localhost:8080/hello
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=curlTerm$$curl%20http://localhost:8080/hello%20;%20echo%20''&completion=Run%20curl%20command. "Opens a new terminal and sends the command above"){.didact})


Open the Hello World program in `GreetingResource.java` and change `return "hello";` to `return "hola";` in the editor([open](didact://?commandId=vscode.openFolder&projectFilePath=src/main/java/org/acme/people/rest/GreetingResource.java&completion=Opened%20the%20GreetingResource.java%20file "Opens the GreetingResource.java file"){.didact}).

Invoke the hello endpoint again using the following curl command:

open [localhost:8080/hello](http://localhost:8080/hello) in your browser or you can also do a curl on a separate terminal

```
curl http://localhost:8080/hello
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=curlTerm$$curl%20http://localhost:8080/hello%20;%20echo%20''&completion=Run%20curl%20command. "Opens a new terminal and sends the command above"){.didact})

> **Note:** This will also listen for a debugger on port `5005`. If you want to wait for the debugger to attach before running you can pass `-Ddebug` on the command line. If you don’t want the debugger at all you can use `-Ddebug=false`. We’ll use this later.

## 3. Stop Live Coding

[**Click here to exit the current command**](didact://?commandId=vscode.didact.sendNamedTerminalCtrlC&text=QuarkusTerm&completion=Quarkus%20K%20Hello%20World%20interrupted. "Interrupt the current operation on the terminal"){.didact},
or hit `ctrl+c` on the terminal window.

Open the Hello World program in `GreetingResource.java` and change `return "hola";` to `return "hello";` again in the editor([open](didact://?commandId=vscode.openFolder&projectFilePath=src/main/java/org/acme/people/rest/GreetingResource.java&completion=Opened%20the%20GreetingResource.java%20file "Opens the GreetingResource.java file"){.didact}).

## 4. Package the app

Quarkus apps can be packaged as an executable JAR file or a native binary. We’ll cover native binaries later, so for now, let’s package as an executable JAR.

```
./mvnw -DskipTests clean package
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=QuarkusTerm$$mvn%20-DskipTests%20clean%20package&completion=maven%20clean%20package. "Opens a new terminal and sends the command above"){.didact})

## 5. Run the executable JAR

Run the packaged application. In a Terminal, run the following command:

```
java -Dquarkus.http.port=8081 -jar target/*-runner.jar
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=QuarkusTerm$$java%20-Dquarkus.http.port=8081%20-jar%20target/*-runner.jar&completion=java%20-jar%20*.jar. "Opens a new terminal and sends the command above"){.didact})

> **Note** We use -Dquarkus.http.port=8081 to avoid conflicting with port 8080 used for Live Coding mode

Invoke the hello endpoint using the following curl command:

open [localhost:8081/hello](http://localhost:8081/hello) in your browser or you can also do a curl on a separate terminal

```
curl http://localhost:8081/hello
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=curlTerm$$curl%20http://localhost:8081/hello%20;%20echo%20''&completion=Run%20curl%20command. "Opens a new terminal and sends the command above"){.didact})

## 5. Cleanup

[**Click here to exit the current command**](didact://?commandId=vscode.didact.sendNamedTerminalCtrlC&text=QuarkusTerm&completion=Quarkus%20K%20Hello%20World%20interrupted. "Interrupt the current operation on the terminal"){.didact},
or hit `ctrl+c` on the terminal window.

You should see a "hello" as the output.

## 6. Congratulations!

You’ve seen how to build a basic app, package it as an executable JAR and start it up very quickly. The JAR file can be used like any other executable JAR file (e.g. running it as-is, packaging as a Linux container, etc.)

In the next step we’ll inject a custom bean to showcase Quarkus' CDI capabilities.