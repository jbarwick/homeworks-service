package com.jvj28.homeworks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvj28.homeworks.command.Login;
import com.jvj28.homeworks.command.OSRevision;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class TestBuffer {

    @Test
    void testParser() {

        try {
            Login s = new ObjectMapper().readValue("{username: abc, password: pwd}", Login.class);
            assertEquals("abc", s.getUsername());
            assertEquals("pwd", s.getPassword());

            OSRevision gos = new OSRevision();
            gos.parseLine("Processor 01 O/S Rev : 01.64.00 T13");
            assertEquals("01", gos.getProcessorId());
            assertEquals("T13", gos.getModel());
            assertEquals("01.64.00", gos.getRevision());

        } catch (Exception e) {
            fail(e);
        }
    }
}
