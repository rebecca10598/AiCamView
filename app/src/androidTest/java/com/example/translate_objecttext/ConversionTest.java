package com.example.translate_objecttext;
import org.junit.Test;

import java.util.Collections;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.TextAnnotation;

public class ConversionTest {

    @Test
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
}
