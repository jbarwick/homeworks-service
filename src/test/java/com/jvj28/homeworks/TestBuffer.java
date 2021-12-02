package com.jvj28.homeworks;

import com.google.gson.Gson;
import com.jvj28.homeworks.command.Login;
import com.jvj28.homeworks.command.OSRevision;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestBuffer {

    @Test
    void testParser() {

        Gson gson = new Gson();
        Login s = gson.fromJson("{username: abc, password: pwd}", Login.class);
        assertEquals("abc", s.getUsername());
        assertEquals( "pwd", s.getPassword());

        OSRevision gos = new OSRevision();
        gos.parseLine("Processor 01 O/S Rev : 01.64.00 T13");
        assertEquals("01", gos.getProcessorId());
        assertEquals("T13", gos.getModel());
        assertEquals("01.64.00", gos.getRevision());
    }
}
