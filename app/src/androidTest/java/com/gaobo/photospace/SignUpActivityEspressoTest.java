package com.gaobo.photospace;

/**
 * Created by GaoBo on 5/8/2017.
 * Credit to https://github.com/firebase/friendlychat
 */

import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SignUpActivityEspressoTest {

    @Rule
    public ActivityTestRule<EmailSignInActivity> mActivityRule =
            new ActivityTestRule<>(EmailSignInActivity.class);

    @Test
    public void verifySignUpButtonDisplayed() {
        onView(ViewMatchers.withId(R.id.signupButton)).check(matches(isDisplayed()));
    }

}