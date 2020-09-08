package com.ashomok.lullabies.ui.main_activity;

import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.ashomok.lullabies.R;
import com.ashomok.lullabies.Settings;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.ashomok.lullabies.utils.MatcherHelper.withIndex;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class MusicPlayerActivityTest {
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
    public void testMediaInitialized() throws Exception {
        assertFalse(mMusicPlayerActivityTestRule.getActivity()
                .getMediaId().isEmpty());
        assertTrue(mMusicPlayerActivityTestRule.getActivity()
                .getSupportMediaController().getQueue().size()>0);
        assertTrue(  mMusicPlayerActivityTestRule.getActivity()
                .getSupportMediaController().isSessionReady());
    }

    @Test
    public void testPlayBtn() throws Exception {
        //GIVEN
        assertNotEquals(STATE_PLAYING, mMusicPlayerActivityTestRule.getActivity()
                .getSupportMediaController().getPlaybackState().getState());

        //WHEN
        onView(withIndex(withId(R.id.tap_me_btn), 0)).check(matches(isDisplayed()));
        onView(withIndex(withId(R.id.tap_me_btn), 0)).perform(click());
        Thread.sleep(1000); //it needs time for music to start playing

        //THAN
        assertEquals(STATE_PLAYING, mMusicPlayerActivityTestRule.getActivity()
                .getSupportMediaController().getPlaybackState().getState());
    }

    @Test
    public void testRemoveAdIcon() throws Exception {
        if (Settings.isAdsActive) {
            try {
                String touch_to_cast = InstrumentationRegistry.getInstrumentation().getTargetContext()
                        .getString(R.string.touch_to_cast);
                onView(withText(containsString(touch_to_cast))).check(matches(isDisplayed()));
                onView((withId(R.id.drawer_layout))).perform(click());
            } catch (Exception e) {
                e.printStackTrace();
                //ignore
            }

            onView(withId(R.id.ads_container)).check(matches(isDisplayed()));

            onView(withId(R.id.remove_ads)).check(matches(isDisplayed()));
            onView(withId(R.id.remove_ads)).perform(click());

            String expectedButtonText = InstrumentationRegistry.getInstrumentation().getTargetContext()
                    .getString(R.string.buy_ads_free);
            onView(withText(containsString(expectedButtonText))).check(matches(isDisplayed()));
            onView(withText(containsString(expectedButtonText))).perform(click());
        } else {
            onView(withId(R.id.remove_ads)).check(doesNotExist());
            onView(withId(R.id.ads_container)).check(matches(not(isDisplayed())));
        }
    }


}