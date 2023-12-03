package com.jvj28.homeworks;

import com.jvj28.homeworks.model.Model;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class TestDataSource {

    @Autowired
    private Model model;

    @Autowired
    private DataSource dataSource;

    @Test
    void givenTomcatConnectionPoolInstance_whenCheckedPoolClassName_thenCorrect() {
        String name = dataSource.getClass().getName();
        assertEquals("org.apache.tomcat.jdbc.pool.DataSource", name);
    }
}
