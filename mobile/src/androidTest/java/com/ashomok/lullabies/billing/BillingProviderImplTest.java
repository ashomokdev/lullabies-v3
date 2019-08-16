package com.ashomok.lullabies.billing;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.ashomok.lullabies.R;
import com.ashomok.lullabies.Settings;
import com.ashomok.lullabies.ui.main_activity.MusicPlayerActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
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
            onView(withId(R.id.remove_ads)).check(matches(isDisplayed()));
            onView(withId(R.id.remove_ads)).perform(click());
            onView(withId(R.id.ads_container)).check(matches(isDisplayed()));

            String expectedButtonText = InstrumentationRegistry.getTargetContext()
                    .getString(R.string.buy_ads_free);
            onView(withText(containsString(expectedButtonText))).check(matches(isDisplayed()));
            onView(withText(containsString(expectedButtonText))).perform(click());
        } else {
            onView(withId(R.id.remove_ads)).check(matches(not(isDisplayed())));
            onView(withId(R.id.ads_container)).check(matches(not(isDisplayed())));
        }
    }
}