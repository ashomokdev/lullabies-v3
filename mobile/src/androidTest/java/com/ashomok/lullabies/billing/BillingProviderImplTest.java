package com.ashomok.lullabies.billing;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.ashomok.lullabies.R;
import com.ashomok.lullabies.Settings;
import com.ashomok.lullabies.ui.main_activity.MusicPlayerActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class BillingProviderImplTest {
    /**
     * {@link ActivityTestRule} is a JUnit {@link Rule @Rule} to launch your activity under test.
     *
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @Rule
    public ActivityTestRule<MusicPlayerActivity> mMusicPlayerActivityTestRule =
            new ActivityTestRule<>(MusicPlayerActivity.class, true, true);

    @Test
    public void testRemoveAdIcon() throws Exception {
        if (Settings.isAdsActive) {
            onView(withContentDescription(R.string.remove_ads)).check(matches(isDisplayed()));
            onView(withContentDescription(R.string.remove_ads)).perform(click());
            onView(withContentDescription(R.string.ads_container)).check(matches(isDisplayed()));

            String expectedButtonText = InstrumentationRegistry.getTargetContext()
                    .getString(R.string.buy_ads_free);
            onView(withText(containsString(expectedButtonText))).check(matches(isDisplayed()));
            onView(withText(containsString(expectedButtonText))).perform(click());
        } else {
            onView(withContentDescription(R.string.remove_ads)).check(matches(not(isDisplayed())));
            onView(withContentDescription(R.string.ads_container)).check(matches(not(isDisplayed())));
        }
    }
}