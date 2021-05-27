package com.ashomok.lullabies.ui.main_activity.media_browser

import android.content.Context
import android.preference.PreferenceManager
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatImageButton
import androidx.leanback.widget.Presenter
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.ashomok.lullabies.R
import com.ashomok.lullabies.ui.main_activity.MusicPlayerActivity
import com.ashomok.lullabies.utils.favourite_music.FavouriteMusicDAO
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaBrowserFragmentFavouritesTest {

    @Before
    fun emptyFavouritesListBefore() {
        val targetContext = getInstrumentation().targetContext
        FavouriteMusicDAO.getInstance(
                targetContext.getSharedPreferences(
                        targetContext.getString(R.string.preferences),
                        Context.MODE_PRIVATE)
        ).cleanFavouriteMusicList()
    }

    @After
    fun emptyFavouritesListAfter() {
        val targetContext = getInstrumentation().targetContext
        FavouriteMusicDAO.getInstance(
                targetContext.getSharedPreferences(
                        targetContext.getString(R.string.preferences),
                        Context.MODE_PRIVATE)
        ).cleanFavouriteMusicList()
    }

    @Test
    //https://github.com/ashomokdev/lullabies-v3/issues/91
    fun testEvent91_NoEmptyViewWhenExpected() {
        //GIVEN
        val scenario = launchActivity<MusicPlayerActivity>()
        scenario.moveToState(Lifecycle.State.RESUMED)
        Thread.sleep(200)

        //WHEN
        //set 1st music as favourite
        onView(allOf(isDisplayed(), withId(R.id.tap_me_img))).perform(click())
        Thread.sleep(200)
        scenario.onActivity { activity ->
            Assert.assertTrue(activity.mediaBrowser.isConnected)
            Assert.assertTrue(activity.mediaController.playbackState?.state == 3
                    || activity.mediaController.playbackState?.state == 6)
        }

        onView(withId(R.id.fragment_playback_controls)).perform(click())
        onView(withId(R.id.favourite_icon)).perform(click())

        onView(allOf(
                instanceOf(AppCompatImageButton::class.java), withParent(withId(R.id.toolbar))
        )).perform(click())

        onView(withId(R.id.pager)).perform(swipeLeft())

        //set 2nd music as favourite
        Thread.sleep(300)
        onView(allOf(isDisplayed(), withId(R.id.tap_me_img))).perform(click())


        scenario.onActivity { activity ->
            Assert.assertTrue(activity.mediaController.playbackState?.state == 3
                    || activity.mediaController.playbackState?.state == 6)
        }

        onView(withId(R.id.fragment_playback_controls)).perform(click())
        onView(withId(R.id.favourite_icon)).perform(click())

        onView(allOf(
                instanceOf(AppCompatImageButton::class.java), withParent(withId(R.id.toolbar))
        )).perform(click())

        //open my favourites
        onView(allOf(
                instanceOf(AppCompatImageButton::class.java), withParent(withId(R.id.toolbar))
        )).perform(click())
        onView(withId(R.id.navigation_my_favorites)).perform(click())

        //switch to 1st music
        onView(allOf(isDisplayed(), withId(R.id.tap_me_img))).perform(click())

        scenario.onActivity { activity ->
            Assert.assertTrue(activity.mediaController.playbackState?.state == 3
                    || activity.mediaController.playbackState?.state == 6)
        }

        //uncheck favourite for 1st music
        onView(withId(R.id.fragment_playback_controls)).perform(click())
        onView(withId(R.id.favourite_icon)).perform(click())
        onView(allOf(
                instanceOf(AppCompatImageButton::class.java), withParent(withId(R.id.toolbar))
        )).perform(click())
        Thread.sleep(300)

        //play music
        onView(allOf(isDisplayed(), withId(R.id.tap_me_img))).perform(click())

        //THEN KOALA
        scenario.onActivity { activity ->
            Assert.assertTrue(activity.mediaController.metadata?.description?.title.toString() == "Коала")
        }

        //CLEAN UP (uncheck all favourites)
        onView(withId(R.id.fragment_playback_controls)).perform(click())
        onView(withId(R.id.favourite_icon)).perform(click())
        onView(allOf(
                instanceOf(AppCompatImageButton::class.java), withParent(withId(R.id.toolbar))
        )).perform(click())

        onView(allOf(isDisplayed(), withId(R.id.empty_result_layout))).check(matches(isDisplayed()))

        onView(withId(R.id.play_pause)).perform(click())

        onView(allOf(isDisplayed(), withId(R.id.empty_result_layout))).check(matches(isDisplayed()))
    }
}
