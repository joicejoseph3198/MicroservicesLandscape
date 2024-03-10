# Documentation

Status: In progress

Overview of the project: 

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled.png)

I’m aware that the set of supporting services that I’ve applied throughtout my project are rather overwhelmingly complex for such few microservices and somewhat over engineered and at some places not fully implemented. However the purpose from the beginning was, to be able to learn about services that can support a much larger microservices landscape.

Tech stack/Technologies used:

Java 17, SpringBoot 3.X, Docker, Kubernetes, Helm, MySQL, MongoDB, Kafka

Stage 1: Creating cooperating microservices

Created a small set of cooperating microservice with minimalistic functionality, to which I’ve added features as I’ve made progress with my project. 

Product Service - manages and stores information about product. (Product focused towards a single typle of product — mechanical keyboards)

Review Service - manages reviews about the products

Product Composite Service -  composite service aggregates information from the two core services and presents information about a product

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%201.png)

1. Created barebones microservices with minimal functionality
2. Didn’t have a discovery service used [localhost](http://localhost) and hardcoded port numbers for each microservice. Used the RestTemplate in composite service to perform HTTP requests to APIs that are exposed by the core services
3. Created a util project that can hold some helper classes that are shared by our
microservices. Added that as a dependency to my core services by packaging it as jar
4. Created global exception controller to create custom exception for error handling

Stage 2: Dockerizing services

1. Dockerized my microservices
2. Created docker images for each of the microservices and added a Docker-specific spring profile to each of them.
3. Created and configured a docker compose file so I could easily bring all my services up.

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%202.png)

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%203.png)

Stage 3: Adding Persistence

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%204.png)

1. Added persistence to my services. Tried to follow a database per microservice approach.Used mongodb for my product service and mysql for review service. 
2. Wrote integration tests using TestConatiners
3. Introduced the use of MapStruct for data mapping
4. Used DataFaker to generate dummy data 
5. Configured swagger for composite microservice

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%205.png)

[Grunt developer’s guide to test containers for integration testing](https://levelup.gitconnected.com/grunt-developers-guide-to-test-containers-for-integration-testing-55ccef00db26)

Stage 4: event driven microservices

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%206.png)

1. Used Spring Cloud Stream to develop event-driven asynchronous services that work with Kafka as messaging systems.
2. Created an API for composite service to generate delete events on each core service topic and then return an OK response back to the caller without waiting for processing to take place in the core services.

A bit about Spring Cloud Stream and how I configured it: 

Spring Cloud Stream abstracts away the complexities associated with messaging middleware, enabling developers to concentrate on crafting business logic rather than dealing with middleware specific code. 

The core concepts in Spring Cloud Stream are as follows:
• Message: A data structure that’s used to describe data sent to and received from a messaging
system.
• Publisher: Sends messages to the messaging system, also known as a Supplier.
• Subscriber: Receives messages from the messaging system, also known as a Consumer.
• Destination: Used to communicate with the messaging system. Publishers use output destina-
tions and subscribers use input destinations. Destinations are mapped by the specific binders
to queues and topics in the underlying messaging system.
• Binder: implementation responsible for integration with specific message broker is called binder. By default, Spring Cloud Stream provides binder implementations for Kafka and RabbitMQ. It is able to automatically detect and use a binder found on the classpath. Any middleware-specific settings can be overridden through external configuration properties in the form supported by Spring Boot, such as application arguments, environment variables, or just the application.yml file.

We can also specify the number of retries until a message is moved to another storage
for fault analysis and correction. A failing message is typically moved to a dedicated queue called a
dead-letter queue. To avoid overloading the infrastructure during temporary failure, for example, a
network error, it must be possible to configure how often retries are performed, preferably with an
increasing length of time between each retry.

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%207.png)

![producer configurations](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%208.png)

producer configurations

![consumer configurations](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%209.png)

consumer configurations

A helper class StreamBridge is used to trigger the processing. It will publish a message on a topic. A function that consumes events from a topic (not creating new events) can be defined by implementing the functional interface java.util.function.Consumer as:

![using stream bridge to publish event](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2010.png)

using stream bridge to publish event

![consumer bean](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2011.png)

consumer bean

1. Configured actuator into microservices for monitoring health

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2012.png)

Stage 5: Discovery Service

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2013.png)

Used Netflix Eureka for service discovery

With Spring Cloud, it is easier to set up a Netflix Eureka server and adapt Spring Boot-based microservices, both so that they can register themselves to Eureka during startup and, when acting as a client to other microservices, to keep track of available microservice instances.

![discovery server configuration](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2014.png)

discovery server configuration

![discovery client configuration](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2015.png)

discovery client configuration

Used a single instance of discovery server, which is okay in from the project perspective. But would probably need multi instance in a production environment for high availabilty.

Stage 6: Edge Server/Gateway Service

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2016.png)

Introduced an edge server to the landscape, it can be used to secure a microservice landscape,
which involves hiding private services from external usage and protecting public services when they’re used by external clients. All incoming requests will now be routed through the edge server

- Gateway uses Netflix Eureka to find the microservices it will route traffic to, so configured it as a Eureka client.
- Added routing rules

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2017.png)

Stage 7: Authorization Server

1. Secured access to the discovery service using HTTP Basic authentication.
2. Adding a local authorization server to the system landscape
3. Authenticated and authorized API access using OAuth 2.0 and OpenID Connect
4. configured gateway to accept only HTTPS-based external trafficusing the certificate.

The gateway will be configured to accept any access token it can validate using the digital
signature provided by the authorization server. The product-composite service will also require the
access token to contain valid OAuth 2.0 scopes:

• The product:read scope will be required for accessing the read-only APIs.
• The product:write scope will be required for accessing the create and delete APIs.

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2018.png)

Stage 8: Config Service

Introduced Spring Cloud Config, which provides the centralized management of configuration files for all the microservices 

configuration files for all our microservices are stored in  central configuration repository, which will make it much easier to handle them. All microservices were updated to retrieve their configuration from the configuration server at startup.

For the project I’ve resorted to using local file system as the repository but I plan to change this with a Github repository or something like Hashicorp Vault.

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2019.png)

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2020.png)

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2021.png)

Structure of the local config repository looks something like this:

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2022.png)

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2023.png)

Future enchancement: Try to use Spring Cloud Bus to add support to detect changes in configuration and pushing notification to the affected microservice

Stage 9 : Resiliency using Circuit Breaker mechanism

Used resilience4j to implement a circuit breaker and retry mechanism between product-composite and product service.

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2024.png)

How circuit breakers work:

Sometimes synchronous dependencies to other services can become unresponsive or even crash if these services stop responding as expected, especially under a high load. These types of error scenarios can be avoided by using a circuit breaker, which applies fail-fast logic and calls fallback methods when it is open.

- If a circuit breaker detects too many faults, it will open its circuit, that is, not allow new calls.
- When the circuit is open, a circuit breaker will perform fail-fast logic. This means that it doesn’t
wait for a new fault, for example, a timeout, to happen on subsequent calls. Instead, it directly
redirects the call to a fallback method. The fallback method can apply various business logic
to produce a best-effort response. For example, a fallback method can return data from a
local cache or simply return an immediate error message. This will prevent a microservice
from becoming unresponsive if the services it depends on stop responding normally. This is
specifically useful under high load.
- After a while, the circuit breaker will be half-open, allowing new calls to see whether the is-
sue that caused the failures is gone. If new failures are detected by the circuit breaker, it will
open the circuit again and go back to the fail-fast logic. Otherwise, it will close the circuit and
go back to normal operation. This makes a microservice resilient to faults, or self-healing, a
capability that is indispensable in a system landscape of microservices that communicate
synchronously with each other.

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2025.png)

![product composite service configurations](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2026.png)

product composite service configurations

![simulating a scenario where product service would fail](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2027.png)

simulating a scenario where product service would fail

Stage 10: Observability

Set up distributed tracing for observability.

Observability in microservices architecture involves understanding and monitoring the system’s internal state to ensure it functions correctly. Micrometer is a metrics collection library, while Zipkin is a distributed tracing system. Integrating Micrometer with Zipkin in a Spring Boot application can provide metrics and tracing capabilities.

- Tracing helps you understand what happened in your application. This includes things like remote calls made, web requests processed, and database accesses.
- Metrics help you understand how long it took for things to happen. Metrics provide information about things like the number of requests, response times, and error rates.

This can be handy when you have a lot of components in your system. Observability is crucial because it lets you know what exactly happened, when, how and why.

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2028.png)

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2029.png)

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2030.png)

Stage 11: local Kubernetes cluster

Deployed my entire microservices landscape to a local kubernetes cluster using k8s distribution called kind ( kubernetes in docker) similar to minikube, or k3s etc.

1. Replaced discovery service, Netflix Eureka. Kubernetes comes with a built-in discovery service based on Kubernetes Service objects and the kube-proxy runtime component. This makes it unnecessary to deploy a separate discovery service.

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2031.png)

1. Used Springboot’s built in implementations of liveness and readiness probes are essential for Kubernetes to be able to manage our Pods. A liveness probe tells Kubernetes if a Pod needs to be replaced, and a readiness probe tells Kubernetes if its Pod is ready to accept requests.
2. 
- For each microservice, one Deployment object and one Service object will be created. For all components, except for the gateway, the Service object will be of type ClusterIP. For the gateway, the Service object will be of type NodePort, accepting external HTTPS requests on port 30433.
- The config server will use a ConfigMap, containing the configuration files in the config-repo.
- To hold credentials for the config server and its clients, two Secrets will be created: one for
the config server and one for its clients.
1. Using Helm charts to create manifest files, which is a package manager for Kuberentes. 

With Helm you can use its templating language that can be used to extract settings specific to a microservice or an environment from generic definitions of the various Kubernetes objects used. 

A package is known as a chart in Helm. A chart contains templates, default values for the templates, and optional dependencies on definitions in other charts. Each component that needs to be deployed, meaning the microservices and the resources related to it, will have its own chart describing how to deploy it. 

To extract boilerplate definitions from the components’ charts, a special type of chart, a library chart,will be used. A library chart doesn’t contain any deployable definitions but only templates expected to be used by other charts for Kubernetes manifests – in our case, for Deployment, Service, ConfigMap, and Secret objects.

I have also applied the concept of parent charts and subcharts. I have defined an environment called dev-env, which will be implemented as a parent chart that depends on different sets of subcharts, for example, the microservice charts. 

The dev-env environment chart will also provide environment-specific default values, for the requested number of Pods, Docker image versions, credentials, etc etc.

So essentially we will have one reusable library chart, named common; a set of microservices and resource manager-specific charts, placed in the components folder; and one environment-specific parent chart, placed in the environments folder.

![overview of how I’ve structer my helm charts](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2032.png)

overview of how I’ve structer my helm charts

![configuration for my kind cluster](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2033.png)

configuration for my kind cluster

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled.png)

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2034.png)

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2035.png)

![Untitled](Documentation%205b7ef6eb41dc41e3b3f7c7650ffa9594/Untitled%2036.png)

Future Improvements: