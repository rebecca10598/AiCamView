package com.example.translate_objecttext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import android.util.Pair;

public class ErrorMessageTest {

    @Test
    public void testGetErrorMessage() {
        String errorString = "Unable to resolve host";

        // Call the method
        Pair<String, String> result = GetErrorMessage(errorString);

        // Assert the expected error message pair
        Pair<String, String> expected = new Pair<>("Unable to Connect!", "Check your Internet Connection,Unable to connect the Server");
        assertEquals(expected, result);
    }



    // Add more test cases for other error conditions if needed

    public static Pair<String, String> GetErrorMessage(String errorString) {
        Pair<String, String> pair = new Pair<>("Something went Wrong", "Can't find anything for you");

        if (errorString.contains("Unable to resolve host")) {
            pair = new Pair<>("Unable to Connect!", "Check your Internet Connection,Unable to connect the Server");
        } else if (errorString.contains("Failed to connect")) {
            pair = new Pair<>("Connection timed out", "Check your Internet Connection");
        } else if (errorString.contains("Network is unreachable")) {
            pair = new Pair<>("Network unreachable", "Could not connect to Internet, Check your mobile/wifi Connection");
        } else if (errorString.contains("Software caused connection abort")) {
            pair = new Pair<>("Connection Aborted", "Connection was aborted by server, without any response");
        } else if (errorString.contains("Connection timed out")) {
            pair = new Pair<>("Connection timed out", "Could not connect server, check internet connection");
        } else if (errorString.contains("No address associated with hostname")) {
            pair = new Pair<>("Unable to Connect!", "Check your Internet Connection,Unable to connect the Server");
        }
        return pair;
    }
}
