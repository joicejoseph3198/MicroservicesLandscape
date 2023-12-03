package com.example.productservice;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Typically while using TestContainers, you use @Container annotation
 * indicating you want your tests to run using a Docker container.
 * A disadvantage of this approach is that each test class will use its own Docker container.
 * Running multiple test classes that use the same type of test container will add latency for each test class.
 * To avoid this extra latency, we can use the 'Single Container Pattern'
 **/

// Base class is used to launch a single Docker container for MongoDB
public abstract class MongoDbTestBase {
    public static final MongoDBContainer database = new MongoDBContainer(DockerImageName.parse("mongo:6.0.4")).withExposedPorts(27017);

    static {
        database.start();
    }

    /**
     * @DynamicPropertySource- makes it easier to set configuration properties from
     * something else that's bootstrapped as part of running an integration test.
     **/
    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", database::getReplicaSetUrl);

    }

}
