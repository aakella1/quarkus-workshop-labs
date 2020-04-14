# Streaming Data with Quarkus and Kafka

In this exercise, you will use the Quarkus Kafka extension to build a streaming application using MicroProfile Reactive Streams Messaging and `Apache Kafka`, a distributed streaming platform. You will also use `Strimzi`, which provides an easy way to run an Apache Kafka cluster on Kubernetes using `Operators`.

## 1. What is Apache Kafka?

Apache Kafka is a distributed streaming platform. A streaming platform has three key capabilities:

- Publish and subscribe to streams of records, similar to a message queue or enterprise messaging system.

- Store streams of records in a fault-tolerant durable way.

- Process streams of records as they occur.

- Kafka is generally used for two broad classes of applications:

- Building real-time streaming data pipelines that reliably get data between systems or applications

- Building real-time streaming applications that transform or react to the streams of data

## 2. What is Strimzi?

Strimzi provides a way to run an Apache Kafka cluster on Kubernetes in various deployment configurations.

Strimzi is based on Apache Kafka, and makes it easy to run Apache Kafka on OpenShift or Kubernetes.

Strimzi provides three operators:

- **Cluster Operator** - Responsible for deploying and managing Apache Kafka clusters within an OpenShift or Kubernetes cluster.

- **Topic Operator** - Responsible for managing Kafka topics within a Kafka cluster running within an OpenShift or Kubernetes cluster.

- **User Operator** - Responsible for managing Kafka users within a Kafka cluster running within an OpenShift or Kubernetes cluster.

## 3. The Goal

In this exercise, we are going to generate (random) names in one component. These names are written in a Kafka topic (`names`). A second component reads from the `names` Kafka topic and applies some magic conversion to the name (adding an honorific). The result is sent to an in-memory stream consumed by a JAX-RS resource. The data is sent to a browser using `server-sent events` and displayed in the browser. It will look like this:

## 4. Create Kafka Cluster

The Strimzi operator installs and manages Kafka clusters on Kubernetes. It’s been pre-installed for you, so all you have to do is create a Kafka cluster inside your namespace.

First, on the https://console-openshift-console.apps.cluster-alpha-eeb8.alpha-eeb8.sandbox811.opentlc.com/topology/ns/PLEASE ENTER USERID AT TOP OF PAGE-project[Topology View^], click **+Add** and, then From Catalog:

![Diagram](docs/31-qstreaming-fromcat.png)

Type in `kafka` in the keyword filter box:

![Diagram](docs/32-qstreaming-kafkacatalog.png)

These are all of the Kafka cluster elements you can install. Click on **Kafka**, and then click on **Create**. This will open a yaml file for you to configure the cluster before it’s installed. Change the name of the cluster from `my-cluster` to `names-cluster` (under the metadata section of the YAML file). Leave all other values as-is, and click **Create**:

![Diagram](docs/33-qstreaming-createkafka.png)

This will create a new Kafka Kubernetes object in your namespace, triggering the Operator to deploy Kafka.

## 5. Create Kafka Topic

Follow the same process to create a Kafka Topic:

Click **+Add** on the left again, select **From Catalog**, and enter `topic` into the search box. Click on the *Kafka Topic* box, then click **Create**:

![Diagram](docs/34-qstreaming-createkafkatopic.png)

We’ll need to create a topic for our application to stream to and from, so in the YAML:

- Change the metadata > name value from `my-topic` to `names`.

- Change the vale of the `strimzi.io/cluster` label from `my-cluster` to `names-cluster`

Then click **Create**.

![Diagram](docs/35-qstreaming-topiccreate.png)

This will cause the Operator to provision a new Topic in the Kafka cluster.

Back on the https://console-openshift-console.apps.cluster-alpha-eeb8.alpha-eeb8.sandbox811.opentlc.com/topology/ns/PLEASE ENTER USERID AT TOP OF PAGE-project[Topology View^], make sure all the Kafka and Zookeeper pods are up and running (with dark blue circles):

![Diagram](docs/36-qstreaming-kafkaup.png)

It may take a few minutes for all of the pods to appear spin up. You can continue to the next step while the Kafka cluster and topics are created.

## 6. Add Quarkus Kafka Extension

With Kafka installing, turn your attention back to the app. Like other exercises, we’ll need another extension to integrate with Kafka. Install it with:

```
mvn quarkus:add-extension -Dextensions="kafka" -f $CHE_PROJECTS_ROOT/quarkus-workshop-labs
```
([^ execute](didact://?commandId=vscode.didact.sendNamedTerminalAString&text=curlTerm$$mvn%20quarkus:add-extension%20-Dextensions="kafka"%20-f%20.&completion=Run%20Quarkus%20add-extension%20command. "Opens a new terminal and sends the command above"){.didact})

This will add the necessary entries in your `pom.xml` to bring in the Kafka extension.

## 7. The Application You Will Build

The app consists of 3 components that pass messages via Kafka and an in-memory stream, then uses SSE to push messages to the browser. It looks like:

![Diagram](docs/37-qstreaming-kafkaarch.png)

## 8. Create name generator
To start building the app, create a new Java class file in the `org.acme.people.stream` called `NameGenerator.java`. This class will generate random names and publish them to our Kafka topic for further processing. Use this code:

```
package org.acme.people.stream;

import io.reactivex.Flowable;
import javax.enterprise.context.ApplicationScoped;
import org.acme.people.utils.CuteNameGenerator;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class NameGenerator {

    @Outgoing("generated-name")           
    public Flowable<String> generate() {  
        return Flowable.interval(5, TimeUnit.SECONDS)
                .map(tick -> CuteNameGenerator.generate());
    }

}
```

- Instruct Reactive Messaging to dispatch the items from returned stream to generated-name
- The method returns a RX Java 2 stream (Flowable) emitting a random name every 5 seconds

The method returns a Reactive Stream. The generated items are sent to the stream named `generated-name`. This stream is mapped to Kafka using the `application.properties` file that we will create soon.

## 9. Add honorifics

The name converter reads the names from Kafka, and transforms them, adding a random (English) honorific to the beginning of the name.

Create a new Java class file in the same package called `NameConverter.java`. Use this code:

```
package org.acme.people.stream;

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import io.smallrye.reactive.messaging.annotations.Broadcast;

@ApplicationScoped
public class NameConverter {

    private static final String[] honorifics = {"Mr.", "Mrs.", "Sir", "Madam", "Lord", "Lady", "Dr.", "Professor", "Vice-Chancellor", "Regent", "Provost", "Prefect"};

    @Incoming("names")               
    @Outgoing("my-data-stream")      
    @Broadcast                       
    public String process(String name) {
        String honorific = honorifics[(int)Math.floor(Math.random() * honorifics.length)];
        return honorific + " " + name;
    }
}
```

- Indicates that the method consumes the items from the `names`` topic
- Indicates that the objects returned by the method are sent to the `my-data-stream` stream
- Indicates that the item are dispatched to all *subscribers*

The process method is called for every *Kafka* record from the `names` topic (configured in the application configuration). Every result is sent to the my-data-stream in-memory stream.

## 10. Expose to front end

Finally, let’s bind our stream to a JAX-RS resource. Create a new Java class in the same package called `NameResource.java`. Use this code:

```
package org.acme.people.stream;

import io.smallrye.reactive.messaging.annotations.Channel;
import org.reactivestreams.Publisher;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.jboss.resteasy.annotations.SseElementType;

/**
 * A simple resource retrieving the in-memory "my-data-stream" and sending the items as server-sent events.
 */
@Path("/names")
public class NameResource {

    @Inject
    @Channel("my-data-stream") Publisher<String> names; 

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType("text/plain") 
    public Publisher<String> stream() { 
        return names;
    }
}
```

- Injects the `my-data-stream` stream using the `@Channel` qualifier
- Indicates that the content is sent using *Server Sent Events*
- Indicates that the data contained within the server sent events is of type `text/plain`
- Returns the stream (Reactive Stream)

>There is a pre-created `names.html` page for you to use (in the `src/main/resources/META-INF/resources` directory) which will make a request to this `/names/stream` endpoint using standard JavaScript running in the browser and draw the resulting names using the D3.js library. The JavaScript that makes this call looks like this (do not copy this into anything!):
>```
>var source = new EventSource("/names/stream"); 
>
>source.onmessage = function (event) { 
>
>    console.log("received new name: " + event.data);
>    // process new name in event.data
>    // ...
>
>    // update the display with the new name
>    update(); 
>};
>```

- Uses your browser’s support for the `EventSource` API (part of the W3C SSE standard) to call the endpoint
- Each time a message is received via SSE, react to it by running this function
- Refresh the display using the D3.js library

## 11. Configure application

We need to configure the Kafka connector. This is done in the `application.properties` file (in the `src/main/resources` directory). The keys are structured as follows:

`mp.messaging.[outgoing|incoming].{channel-name}.property=value`

The `channel-name` segment must match the value set in the `@Incoming` and `@Outgoing` annotation:

- `generated-name` → sink to which we write the names

- `names` → source from which we read the names

Add the following values to the app’s `src/main/resources/application.properties`:

```
# Configure the Kafka sink (we write to it)
%prod.mp.messaging.outgoing.generated-name.bootstrap.servers=names-cluster-kafka-bootstrap:9092
%prod.mp.messaging.outgoing.generated-name.connector=smallrye-kafka
%prod.mp.messaging.outgoing.generated-name.topic=names
%prod.mp.messaging.outgoing.generated-name.value.serializer=org.apache.kafka.common.serialization.StringSerializer

# Configure the Kafka source (we read from it)
%prod.mp.messaging.incoming.names.bootstrap.servers=names-cluster-kafka-bootstrap:9092
%prod.mp.messaging.incoming.names.connector=smallrye-kafka
%prod.mp.messaging.incoming.names.value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

> The hostnames you see here will only make sense (be resolvable via DNS) when this app is run in the same Kubernetes namespace as the Kafka cluster you created earlier. So you’ll see this and other config values above prefixed with `%prod` which will not try to initialize Kafka when in `dev` mode.

More details about this configuration is available on the Producer configuration and Consumer configuration section from the Kafka documentation.

> What about `my-data-stream`? This is an in-memory stream, not connected to a message broker.

## 12. Rebuild Executable JAR

Using the commands on the right, select **Package App for OpenShift**.



You should see a bunch of log output that ends with a `SUCCESS` message.

## 13. Deploy to OpenShift

And now start the build using our executable JAR:

```
oc start-build people --from-file target/*-runner.jar --follow
```

The build should take a minute or two to complete.

## 14. Test

Our application should be up and running in a few seconds after the build completes and generating names. To see if it’s working, access the http://people-PLEASE ENTER USERID AT TOP OF PAGE-project.apps.cluster-alpha-eeb8.alpha-eeb8.sandbox811.opentlc.com/names.html[graphical name cloud powered by Quarkus, MicroProfile and Kafka^]. You should see a cloud of names updating every 5 seconds (it may take a few seconds for it to start!):

> It takes a few seconds to establish the connection to Kafka. If you don’t see new names generated every 5 seconds, reload the browser page to re-initialize the SSE stream.

These are the original names streamed through Kafka, altered to add a random honorific like "Sir" or "Madam", and displayed in a "word cloud" for you to enjoy!

## 15. Congratulations!

This guide has shown how you can interact with Kafka using Quarkus. It utilizes MicroProfile Reactive Messaging to build data streaming applications.

If you want to go further check the documentation of SmallRye Reactive Messaging, the implementation used in Quarkus.