package com.ashomok.lullabies.ui.main_activity.media_browser

import android.content.Context
import android.content.Intent
import androidx.appcompat.widget.AppCompatImageButton
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.ashomok.lullabies.R
import com.ashomok.lullabies.ui.main_activity.MusicPlayerActivity
import com.ashomok.lullabies.utils.favourite_music.FavouriteMusicDAO
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Automator test
 * doc: https://developer.android.com/training/testing/ui-testing/uiautomator-testing
 */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
class FavouritesBehaviorUIAuthomatorTest {

    private lateinit var device: UiDevice

    @Before
    fun startMainActivityFromHomeScreen() {
        emptyFavouritesListBefore()

        // Initialize UiDevice instance
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Start from the home screen
        device.pressHome()
    }

    fun emptyFavouritesListBefore() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        FavouriteMusicDAO.getInstance(
                targetContext.getSharedPreferences(
                        targetContext.getString(R.string.preferences),
                        Context.MODE_PRIVATE)
        ).cleanFavouriteMusicList()
    }

    @After
    fun emptyFavouritesListAfter() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        FavouriteMusicDAO.getInstance(
                targetContext.getSharedPreferences(
                        targetContext.getString(R.string.preferences),
                        Context.MODE_PRIVATE)
        ).cleanFavouriteMusicList()
    }

    @Test
    //https://github.com/ashomokdev/lullabies-v3/issues/92
    fun testEvent92_CommonCollectionOpenedInsteadOfMyFavouritesWhenComeBack() {
        //GIVEN
        val scenario = launchActivity<MusicPlayerActivity>()
        scenario.moveToState(Lifecycle.State.RESUMED)

        //WHEN
        //set 1st music as favourite
        Espresso.onView(Matchers.allOf(ViewMatchers.isDisplayed(), ViewMatchers.withId(R.id.tap_me_img))).perform(ViewActions.click())
        Thread.sleep(200)
        scenario.onActivity { activity ->
            Assert.assertTrue(activity.mediaController.playbackState?.state == 3
                    || activity.mediaController.playbackState?.state == 6)
        }

        Espresso.onView(
            ViewMatchers.withId(R.id.fragment_playback_controls)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.favourite_icon)).perform(ViewActions.click())

        device.pressBack()

        Espresso.onView(ViewMatchers.withId(R.id.pager)).perform(ViewActions.swipeLeft())

        //set 2nd music as favourite
        Thread.sleep(200)
        Espresso.onView(
            Matchers.allOf(ViewMatchers.isDisplayed(), ViewMatchers.withId(R.id.tap_me_img)))
            .perform(ViewActions.click())

        scenario.onActivity { activity ->
            Assert.assertTrue(activity.mediaController.playbackState?.state == 3
                    || activity.mediaController.playbackState?.state == 6)
        }

        Espresso.onView(
            ViewMatchers.withId(R.id.fragment_playback_controls)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.favourite_icon)).perform(ViewActions.click())

        //open my favourites
        Thread.sleep(200)
        Espresso.onView(ViewMatchers.withId(R.id.my_favorites_btn)).perform(ViewActions.click())

        //Play My favourites collection
        Thread.sleep(200)
        Espresso.onView(
            Matchers.allOf(ViewMatchers.isDisplayed(), ViewMatchers.withId(R.id.tap_me_img)))
            .perform(ViewActions.click())

        scenario.onActivity { activity ->
            Assert.assertTrue(activity.mediaController.playbackState?.state == 3
                    || activity.mediaController.playbackState?.state == 6)
        }

        // Start from the home screen
        device.pressHome()

        // Go to notification bar
        device.swipe(300, 0, 300, 1000, 100)

        // Open app from notification bar
        device.findObject(
                UiSelector().resourceId("com.android.systemui:id/sec_title_artist_container")
        ).click()

        device.waitForIdle(500)
        // Go back from toolbar
        device.pressBack()

        // Common collection opened
        val wrongToolbarTitle: UiObject = device.findObject(
                UiSelector().text("Колыбельные - Главная Коллекция")
                        .className("android.widget.TextView")
        )

        if (wrongToolbarTitle.exists() && wrongToolbarTitle.isEnabled) {
            Assert.fail("Common collection opened instead of My favourites when come back " +
                    "from notification tab - Bug")
        }

        val correctToolbarTitle: UiObject = device.findObject(
                UiSelector().text("Колыбельные - Моё Избранное")
                        .className("android.widget.TextView")
        )

        Assert.assertTrue(correctToolbarTitle.exists() && correctToolbarTitle.isEnabled)
    }
}