package com.example.translate_objecttext;

import static org.junit.Assert.*;

import org.junit.Test;
import android.util.Pair;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HelperTest {

    @Test
    public void checkConnection() {
        if (Helper.checkConnection("unable to resolve host")
                || Helper.checkConnection("failed to connect")
                || Helper.checkConnection("software caused connection abort")
                || Helper.checkConnection("connection timed out")
                || Helper.checkConnection("No address associated with hostname")
        ) {
            assertTrue(true);
        } else {
            assertTrue(false);
        }
    }
    @Test
    public void testGetErrorMessage() {
        String errorString = "Unable to resolve host";

        Pair<String, String> expectedPair = new Pair<>("Unable to Connect!", "Check your Internet Connection,Unable to connect the Server");

        Pair<String, String> resultPair = Helper.GetErrorMessage(errorString);

        assertNotNull(resultPair);
        assertEquals(expectedPair.first, resultPair.first);
        assertEquals(expectedPair.second, resultPair.second);
    }


}