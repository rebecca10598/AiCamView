package com.example.translate_objecttext;
import org.junit.Test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import static org.junit.Assert.assertEquals;

public class TranslationURLTest {

    @Test
    public void testTranslationURL() throws IOException {
        String url = "https://translation.googleapis.com/language/translate/v2?key=AIzaSyB346GIwwEl_CPMXo53TSltDjsBEyRZ0AM";
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL requestUrl = new URL(url);
            connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod("GET");

            // Verify the HTTP response code
            int responseCode = connection.getResponseCode();
            assertEquals(400, responseCode);

            // Read the response body
//            InputStream inputStream = connection.getInputStream();
//            reader = new BufferedReader(new InputStreamReader(inputStream));
//            StringBuilder responseBody = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                responseBody.append(line);
//            }

            // Further assertions on the response body or other details can be added here
            // For example, you can check if the response body contains specific data or follows a certain structure
        } finally {
            // Clean up resources
            if (reader != null) {
                reader.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
