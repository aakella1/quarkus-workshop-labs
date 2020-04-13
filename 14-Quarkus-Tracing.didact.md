Tracing Quarkus Apps
This exercise shows how your Quarkus application can utilize Eclipse MicroProfile OpenTracing to provide distributed tracing for interactive web applications.

In a distributed cloud-native application, multiple microservices are collaborating to deliver the expected functionality. If you have hundreds of services, how do you debug an individual request as it travels through a distributed system? For Java enterprise developers, the Eclipse MicroProfile OpenTracing specification makes it easier. Let’s find out how.

Install Jaeger
Jaeger is a distributed tracing system originally created by Uber (the ride sharing company). It is used for monitoring and troubleshooting microservices-based distributed systems, including:

Distributed context propagation

Distributed transaction monitoring

Root cause analysis

Service dependency analysis

Performance / latency optimization

Jaeger exposes a collector through which apps (like our People app) report tracing details. Jaeger stores and reports on this tracing activity.

What are Traces and Spans?

At the highest level, a trace tells the story of a transaction or workflow as it propagates through a (potentially distributed) system. A trace is a directed acyclic graph (DAG) of spans: named, timed operations representing a contiguous segment of work in that trace.

Each component (microservice) in a distributed trace will contribute its own span or spans.

Jaeger is installed and managed through its Kubernetes Operator (just like AMQ Streams and Kafka that you used in previous exercises).

First, on the https://console-openshift-console.apps.cluster-alpha-eeb8.alpha-eeb8.sandbox811.opentlc.com/topology/ns/PLEASE ENTER USERID AT TOP OF PAGE-project[Topology View^], click +Add and, then From Catalog:

names
Type in jaeger in the keyword filter box:

kafkacatalog
Click on Jaeger, and then click on Create. This will open a yaml file for you to configure the Jaeger service before it’s installed.

Change the metadata > name value to jaeger as shown, and click Create:

kafkacatalog
This will create a new Jaeger Kubernetes object in your namespace, triggering the Operator to deploy Jaeger. In the https://console-openshift-console.apps.cluster-alpha-eeb8.alpha-eeb8.sandbox811.opentlc.com/topology/ns/PLEASE ENTER USERID AT TOP OF PAGE-project[Topology View^] you’ll see Jaeger spin up:

spin
Jaeger exposes its collector at different ports for different protocols. Most use the HTTP collector at jaeger-collector:14268 but other protocols like gRPC are also supported on different ports. You can see them by clicking on the Jaeger circle and clicking the Resources tab:

spin
The endpoint on port 14268 is the one we’ll use for our app.

Add Tracing to Quarkus
With Jaeger installed, let’s turn back to our Quarkus app. Like other exercises, we’ll need another extension to enable tracing in our app. Install it with:

mvn quarkus:add-extension -Dextensions="opentracing, rest-client" -f $CHE_PROJECTS_ROOT/quarkus-workshop-labs
This will add the necessary entries in your pom.xml to bring in the OpenTracing capability, and an HTTP REST Client we’ll use pater.

Configure Quarkus
Next, open the application.properties file (in the src/main/resources directory). Add the following lines to it to configure the default Jaeger tracer in Quarkus:

quarkus.jaeger.service-name=people
quarkus.jaeger.sampler-type=const
quarkus.jaeger.sampler-param=1
quarkus.jaeger.endpoint=http://jaeger-collector:14268/api/traces
The name of our service from the perspective of Jaeger (useful when multiple apps report to the same Jaeger instance)
How Jaeger samples traces. Other options exist to tune the performance.
This is the default HTTP-based collector exposed by Jaeger
Test it out
Like many other Quarkus frameworks, sensible defaults and out of the box functionality means you can get immediate value out of Quarkus without changing any code. By default, all JAX-RS endpoints (like our /hello and others) are automatically traced. Let’s see that in action by re-deploying our traced app.

First, re-build the app using the Package for OpenShift.

create

Once that’s done, run the following command to re-deploy:

oc start-build people --from-file $CHE_PROJECTS_ROOT/quarkus-workshop-labs/target/*-runner.jar --follow
Confirm deployment
Run and wait for the app to complete its rollout:

oc rollout status -w dc/people
Trigger traces
You’ll need to trigger some HTTP endpoints to generate traces. Access the http://people-PLEASE ENTER USERID AT TOP OF PAGE-project.apps.cluster-alpha-eeb8.alpha-eeb8.sandbox811.opentlc.com/datatable.html[graphical person browser powered by the DataTables library we created earlier^].

Exercise the table a bit by paging through the entries and using various search terms to force several RESTful calls back to our app:

paging
Inspect traces
Open the https://jaeger-PLEASE ENTER USERID AT TOP OF PAGE-project.apps.cluster-alpha-eeb8.alpha-eeb8.sandbox811.opentlc.com[Jaeger Query UI^]. By default Jaeger uses the same login details as OpenShift, so click the Login with OpenShift button, enter your credentials:

Username: PLEASE ENTER USERID AT TOP OF PAGE

Password: openshift

Then, click Allow Selected Permissions to allow Jaeger to access your account details. You’ll end up on the Jaeger query page:

jaeger
Using the menu on the left, select the people Service, and click Find Traces. Jaeger will show the collected traces on the right:

If you do not see people in the Service drop-down, ensure your changes to application.properties to add Jaeger configuration is correct, and reload the Jaeger UI.

jaeger
Click on one of the traces from "a few seconds ago" to show the individual spans of each trace:

jaeger
You can see that this trace (along with the others) shows the incoming HTTP GET operation to the /datatable endpoint we created earlier, along with the time it took, and other ancillary info about the request. Not terribly interesting as it’s a single call, but you can imagine with a real world app and multiple microservices working together, that traces could reveal a lot of detail.

Service Mesh technologies like Istio can provide even more tracing prowess as the calls across different services are traced at the network level, not requiring any frameworks or developer instrumentation to be enabled for tracing.

Tracing external calls
This exercise showa how to use the MicroProfile REST Client with Quarkus in order to trace external, outbound requests with very little effort.

We will use the publicly available Star Wars API to fetch some characters from the Star Wars universe. Our first order of business is to setup the model we will be using, in the form of a StarWarsPerson POJO.

Create model
Create a new class file in the org.acme.people.model package called StarWarsPerson.java with the following content:

package org.acme.people.model;

public class StarWarsPerson {

    private String name;
    private String mass;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMass() {
        return mass;
    }

    public void setMass(String mass) {
        this.mass = mass;
    }
}
This contains a subset of the full Star Wars model, just enough to demonstrate tracing.

Create interface
Using the MicroProfile REST Client is as simple as creating an interface using the proper JAX-RS and MicroProfile annotations. Create a new Java class file in the org.acme.people.service package called StarWarsService.java with the following content:

package org.acme.people.service;

import org.acme.people.model.StarWarsPerson;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@RegisterRestClient 
@Path("/api") 
public interface StarWarsService {

    @GET
    @Path("/people/{id}/") 
    @Produces("application/json") 
    @ClientHeaderParam(name="User-Agent", value="QuarkusLab") 
    StarWarsPerson getPerson(@PathParam("id") int id); 
}
@RegisterRestClient allows Quarkus to know that this interface is meant to be available for CDI injection as a REST Client
@Path, @GET and @PathParam are the standard JAX-RS annotations used to define how to access the service
While @Consumes and @Produces are optional as auto-negotiation is supported, it is heavily recommended to annotate your endpoints with them to define precisely the expected content types. It will also allow to narrow down the number of JAX-RS providers (which can be seen as converters) included in the native executable.
The Star Wars API requires a User-Agent header, so with Quarkus we add that with @ClientHeaderParam. Other parameters can be added here as needed.
The getPerson method gives our code the ability to query the Star Wars API by id. The client will handle all the networking and marshalling leaving our code clean of such technical details.
Configure endpoint
In order to determine the base URL to which REST calls will be made, the REST Client uses configuration from application.properties. To configure it, add this to your application.properties (in src/main/resources):

org.acme.people.service.StarWarsService/mp-rest/url=https://swapi.co
Having this configuration means that all requests performed using our code will use https://swapi.co as the base URL.

Note that org.acme.people.service.StarWarsService must match the fully qualified name of the StarWarsService interface we created in the previous section.

Using the configuration above, calling the getPerson(int) method of StarWarsService with a value of 1 would result in an HTTP GET request being made to https://swapi.co/api/people/1/. Confirm you can access the Star Wars API using curl:

curl -s https://swapi.co/api/people/1/ | jq
You should get Luke Skywalker back:

{
  "name": "Luke Skywalker",
  "height": "172",
  "mass": "77",
  "hair_color": "blond",
  "skin_color": "fair",
  "eye_color": "blue",
  "birth_year": "19BBY",
  "gender": "male",
  "homeworld": "https://swapi.co/api/planets/1/",
  ....<more here>....
}
Final step: add endpoint
We need to @Inject an instance of our new StarWarsService and call it. Open the existing PersonResource class and add the following injected field and method:

@Inject
@RestClient
StarWarsService swService; 

@GET
@Path("/swpeople")
@Produces(MediaType.APPLICATION_JSON)
public List<StarWarsPerson> getCharacters() {
    return IntStream.range(1, 6) 
        .mapToObj(swService::getPerson)  
        .collect(Collectors.toList());  
}
Our injected service
Generate a stream of 5 integers that we will use as IDs to pass to the service
For each of the integers, call the StarWarsService::getPerson method
Collect the results into a list and return it
You’ll need to add a few imports at the top of the file:

import org.acme.people.model.StarWarsPerson;
import org.acme.people.service.StarWarsService;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import java.util.stream.IntStream;
Test it out
First, re-build the app using the Package App for OpenShift command:

create

Once that’s done, run the following command to re-deploy:

oc start-build people --from-file $CHE_PROJECTS_ROOT/quarkus-workshop-labs/target/*-runner.jar --follow
Confirm deployment
Run and wait for the app to complete its rollout:

oc rollout status -w dc/people
Trigger traces
Access the endpoint by running the following command:

curl -s http://$(oc get route people -o=go-template --template='{{ .spec.host }}')/person/swpeople | jq
You should see:

[
  {
    "mass": "77",
    "name": "Luke Skywalker"
  },
  {
    "mass": "75",
    "name": "C-3PO"
  },
  {
    "mass": "32",
    "name": "R2-D2"
  },
  {
    "mass": "136",
    "name": "Darth Vader"
  },
  {
    "mass": "49",
    "name": "Leia Organa"
  }
]
Inspect traces
Reload the https://jaeger-PLEASE ENTER USERID AT TOP OF PAGE-project.apps.cluster-alpha-eeb8.alpha-eeb8.sandbox811.opentlc.com[Jaeger Query UI^], and click Find Traces. The new trace should appear the top with multiple spans. Click on it to display details:

swpeople
You can see that this trace (along with the others) shows multiple spans: the incoming HTTP GET operation to the /swperson endpoint we created earlier, and the external calls to the Star Wars API. Expand the traces to show the detail:

swpeopleext
Extra credit: Explicit method tracing
An annotation is provided to define explicit Span creation. This works on top of the "no-action" setup we did in the previous steps.

The @Traced annotation, applies to a class or a method. When applied to a class, the @Traced annotation is applied to all methods of the class. If the annotation is applied to a class and method then the annotation applied to the method takes precedence. The annotation starts a Span at the beginning of the method, and finishes the Span at the end of the method.

If you have time after this workshop, add a @Traced annotation to some of the other methods and test them out.

Congratulations!
You’ve seen how to enable automatic tracing for JAX-RS methods as well as create custom tracers for non-JAX-RS methods and external services by using MicroProfile OpenTracing. This specification makes it easy for Quarkus developers to instrument services with distributed tracing for learning, debugging, performance tuning, and general analysis of behavior.