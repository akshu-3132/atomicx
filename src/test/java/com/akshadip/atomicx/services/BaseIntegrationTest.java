package com.akshadip.atomicx.services;

import org.hibernate.dialect.sql.ast.PostgreSQLSqlAstTranslator;
import org.springframework.boot.flyway.autoconfigure.FlywayProperties;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.stereotype.Service;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class BaseIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:14.2");
}
