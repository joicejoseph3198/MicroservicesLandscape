package com.example.reviewservice;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class MySqlTestBase {
    public static final MySQLContainer database =
            new MySQLContainer(DockerImageName.parse("mysql:8.0.32"));

    static {
        database.start();
    }

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.driverClassName", database::getDriverClassName);
        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto",() -> "create");
    }
}
