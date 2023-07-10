package com.example.translate_objecttext;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.intent.Intents.intended;

@RunWith(AndroidJUnit4.class)
public class fragment_text_crvTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.CAMERA);

    @Test
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

    public static class TestActivity extends FragmentActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_object_text_curved);
        }
    }

}