package com.example.translate_objecttext;

import static androidx.test.espresso.intent.Intents.intended;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.util.Pair;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Pair;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.TextAnnotation;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@RunWith(AndroidJUnit4.class)
public class UnitTest {
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.CAMERA);


    public static class TestActivity extends FragmentActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_object_text_curved);
        }
    }

    @org.junit.Test
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

    @org.junit.Test
    public void testConvertResponseToString() {
        // Create a mock BatchAnnotateImagesResponse
        BatchAnnotateImagesResponse response = new BatchAnnotateImagesResponse();
        AnnotateImageResponse annotateImageResponse = new AnnotateImageResponse();
        TextAnnotation textAnnotation = new TextAnnotation();
        textAnnotation.setText("Hello, world!");
        annotateImageResponse.setFullTextAnnotation(textAnnotation);
        response.setResponses(Collections.singletonList(annotateImageResponse));

        // Create an instance of the class under test
        fragment_text_crv fragment_text_crv = new fragment_text_crv();

        // Call the method being tested
        String result = fragment_text_crv.convertResponseToString(response);

        // Verify the result
        String expected = "Hello, world!\n";
        assertEquals(expected, result);
    }

    @org.junit.Test
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
    @org.junit.Test
    public void testCameraPermission() {
        Context context = ApplicationProvider.getApplicationContext();

        int cameraPermissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        boolean hasCameraPermission = cameraPermissionStatus == PackageManager.PERMISSION_GRANTED;

        Assert.assertTrue("Camera permission not granted", hasCameraPermission);
    }

    @org.junit.Test
    public void testStoragePermission() {
        Context context = ApplicationProvider.getApplicationContext();

        int storagePermissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        boolean hasStoragePermission = storagePermissionStatus == PackageManager.PERMISSION_GRANTED;

        Assert.assertTrue("Storage permission not granted", hasStoragePermission);
    }
    @org.junit.Test
    public void testStartCamera() {
        ActivityScenario.launch(ObjectTextCurved.class).onActivity(activity -> {
            Fragment fragment = new fragment_text_crv();
            activity.getSupportFragmentManager().beginTransaction().add(fragment, null).commit();

            Intents.init();
            Espresso.onView(ViewMatchers.withId(R.id.object_fbutton)).perform(ViewActions.click());
            intended(IntentMatchers.hasAction(MediaStore.ACTION_IMAGE_CAPTURE));
            Intents.release();
        });
    }
    @org.junit.Test
    public void testScaleBitmapDown() {
        // Arrange
        Bitmap testBitmap = createTestBitmap(600, 800); // Set the test bitmap dimensions
        int maxDimension = 100;

        // Act
        Bitmap scaledBitmap =  ObjectTextCurved.scaleBitmapDown(testBitmap, maxDimension);
        System.out.println(scaledBitmap.getWidth());
        System.out.println(scaledBitmap.getHeight());
        // Assert
        assertEquals(true,maxDimension>= scaledBitmap.getWidth());
        assertEquals(true,maxDimension>=  scaledBitmap.getHeight());
    }
    private Bitmap createTestBitmap(int width, int height) {
        int[] pixels = new int[width * height];
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    }


    @Test
    public void testGetAvailableLanguages() {
        // Call the method
        List<LanguageTest.Language> result = getAvailableLanguages();

        // Assert the expected number of languages
        int expectedSize = LanguageTest.TranslateLanguage.getAllLanguages().size();
        assertEquals(expectedSize, result.size());

        // Assert that each language in the result is a valid Language object
        for (LanguageTest.Language language : result) {
            // Assert any additional conditions or validations on the Language object
            // For example, you can check that the language name is not empty or null
            assertNotNull(language.getName());
            assertFalse(language.getName().isEmpty());
        }
    }

    public List<LanguageTest.Language> getAvailableLanguages() {
        List<LanguageTest.Language> languages = new ArrayList<>();
        List<String> languageIds = LanguageTest.TranslateLanguage.getAllLanguages();
        for (String languageId : languageIds) {
            languages.add(new LanguageTest.Language(LanguageTest.TranslateLanguage.fromLanguageTag(languageId)));
        }
        return languages;
    }

    // Placeholder for the Language class, update with your actual implementation
    public static class Language {
        private String name;

        public Language(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    // Placeholder for the TranslateLanguage class, update with your actual implementation
    public static class TranslateLanguage {
        public static List<String> getAllLanguages() {
            // Implement the logic to fetch the available language IDs
            return new ArrayList<>();
        }

        public static String fromLanguageTag(String languageTag) {
            // Implement the logic to convert a language tag to a language name
            return "";
        }
    }

}
