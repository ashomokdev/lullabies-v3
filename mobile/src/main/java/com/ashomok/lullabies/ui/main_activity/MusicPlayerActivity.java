/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ashomok.lullabies.ui.main_activity;

import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.annimon.stream.Stream;
import com.ashomok.lullabies.R;
import com.ashomok.lullabies.Settings;
import com.ashomok.lullabies.ad.AdMobAd;
import com.ashomok.lullabies.billing_kotlin.localdb.AugmentedSkuDetails;
import com.ashomok.lullabies.ui.PlaybackControlActivity;
import com.ashomok.lullabies.ui.ExitDialogFragment;
import com.ashomok.lullabies.ui.about_activity.AboutActivity;
import com.ashomok.lullabies.ui.full_screen_player_activity.FullScreenPlayerActivity;
import com.ashomok.lullabies.utils.FirebaseAnalyticsHelper;
import com.ashomok.lullabies.utils.InfoSnackbarUtil;
import com.ashomok.lullabies.utils.LogHelper;
import com.ashomok.lullabies.utils.MediaIDHelper;
import com.ashomok.lullabies.utils.Result;
import com.ashomok.lullabies.utils.favourite_music.FavouriteMusicDAO;
import com.google.android.material.navigation.NavigationView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import kotlin.Unit;

import static android.view.Menu.NONE;
import static com.ashomok.lullabies.utils.MediaIDHelper.MEDIA_ID_FAVOURITES;
import static com.ashomok.lullabies.utils.MediaIDHelper.MEDIA_ID_MUSICS_BY_CATEGORY;

/**
 * Main activity for the music player.
 * This class hold the MediaBrowser and the MediaController instances. It will create a MediaBrowser
 * when it is created and connect/disconnect on start/stop. Thus, a MediaBrowser will be always
 * connected while this activity is running.
 */
//todo chech sharedpreferances to get info about favourites!!!
//todo it is not very good to know about favourites using custom action. Custom action may be used to add|remove track from favourites. Write and use another code here instead.
//todo this activity MUST know about favourites changes.
// It must check favourites size before every menu opening and hide/show My Favourites row.
//MediaBrowserFragment must be customized for favourites collection.
// I must receive favourites media from  MusicPlayerActivity insted
// of loading it again inside customized MediaBrowserFragment

public class MusicPlayerActivity extends PlaybackControlActivity implements MediaFragmentListener,
        MusicPlayerContract.View {

    private static final String TAG = LogHelper.makeLogTag(MusicPlayerActivity.class);
    public static final String SAVED_MEDIA_ID = "com.ashomok.lullabies.MEDIA_ID";
    private static final String FRAGMENT_TAG = "lullabies_list_container";
    private static final String INIT_MEDIA_ID_VALUE_ROOT = MediaIDHelper.MEDIA_ID_MUSICS_BY_CATEGORY;
    public static final String EXTRA_START_FULLSCREEN = "com.ashomok.lullabies.EXTRA_START_FULLSCREEN";

    /**
     * Optionally used with {@link #EXTRA_START_FULLSCREEN} to carry a MediaDescription to
     * the {@link FullScreenPlayerActivity}, speeding up the screen rendering
     * while the {@link android.support.v4.media.session.MediaControllerCompat} is connecting.
     */
    public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION =
            "com.ashomok.lullabies.CURRENT_MEDIA_DESCRIPTION";

    /**
     * Optionally used with {@link #EXTRA_START_FULLSCREEN} to carry a MediaId (category) to
     * update the view.
     */
    public static final String EXTRA_CURRENT_MEDIA_ID_FROM_NOTIFICATION =
            "com.ashomok.lullabies.CURRENT_MEDIA_ID_FROM_NOTIFICATION";


    private Bundle mVoiceSearchParams;

    @Inject
    MusicPlayerPresenter mPresenter;

    @Inject
    AdMobAd adMobAd;

    @Inject
    FirebaseAnalyticsHelper firebaseAnalyticsHelper;

    @Inject
    FavouriteMusicDAO favouriteMusicDAO;

    private View emptyResultView;
    private TextView mErrorMessage;

    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;

    private final Set<String> mediaIdMenuRoots = new HashSet<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.UAmpAppTheme);
        super.onCreate(savedInstanceState);
        LogHelper.d(TAG, "Activity onCreate");
        setContentView(R.layout.activity_main);

        emptyResultView = findViewById(R.id.empty_result_layout);
        mErrorMessage = emptyResultView.findViewById(R.id.error_message);

        initializeToolbar();
        initializeNavigationDrawer();
        initializeFromParams(savedInstanceState, getIntent());

        // Only check if a full screen player is needed on the first time:
        if (savedInstanceState == null) {
            startFullScreenActivityIfNeeded(getIntent());
        }
        adMobAd.initAd(findViewById(R.id.ads_container));
        mPresenter.takeView(this);
    }

    @Nullable
    private Unit addMenuItems(
            Result<? extends List<? extends MediaBrowserCompat.MediaItem>> result) {

        if (result instanceof Result.Success) {
            List<String> mediaIds = getMediaIds((Result.Success<List<MediaBrowserCompat.MediaItem>>) result);

            if (mediaIds.isEmpty()) {
                LogHelper.e(TAG, "Unexpected empty result from loading media");
                checkForUserVisibleErrors(true);
            } else {
                addMenuItems(mediaIds);
            }

        } else if (result instanceof Result.Error) {
            LogHelper.e(TAG, ((Result.Error) result).getException(),
                    "Error from loading media");
            checkForUserVisibleErrors(true);
        } else {
            LogHelper.e(TAG, "Unknown error, unexpected result.");
        }
        return null;
    }

    private void navigateToDefault(String currentMediaId, List<String> mediaIds) {
        //if currently on root and categories obtained
        if ((currentMediaId.equals(INIT_MEDIA_ID_VALUE_ROOT) || currentMediaId.equals(getMediaBrowser().getRoot()))
                && mediaIds.get(0).contains(MEDIA_ID_MUSICS_BY_CATEGORY)
        ) {
            //browse first category as default
            currentMediaId = mediaIds.get(0);
        }

        if (!currentMediaId.equals(INIT_MEDIA_ID_VALUE_ROOT)) {
            LogHelper.d(TAG, "browse mediaId " + currentMediaId);
            navigateToBrowser(currentMediaId);
        }
    }

    private List<String> getMediaIds(Result.Success<List<MediaBrowserCompat.MediaItem>> result) {
        List<MediaBrowserCompat.MediaItem> mediaItems = result.getData();
        return Stream.of(mediaItems)
                .map(mediaItem -> mediaItem.getDescription().getMediaId()).toList();
    }

    @Override
    protected void initializeToolbar() {
        super.initializeToolbar();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        }
    }

    private void initializeNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        setupDrawerContent();

        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                Menu navigationMenu = navigationView.getMenu();
                updateMyFavouritesMenuItem(navigationMenu);
                super.onDrawerOpened(drawerView);
            }
        });
    }

    private void setupDrawerContent() {
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    Bundle bundle = ActivityOptions.makeCustomAnimation(
                            this, R.anim.fade_in, R.anim.fade_out).toBundle();
                    String mediaId = null;
                    Class activityClass = null;

                    switch (menuItem.getItemId()) {
                        case R.id.navigation_rate_app:
                            rateApp();
                            break;
                        case R.id.navigation_about:
                            activityClass = AboutActivity.class;
                            break;
                        case R.id.navigation_exit:
                            exit();
                            break;
                        case R.id.navigation_my_favorites:
                            activityClass = MusicPlayerActivity.class;
                            mediaId = MediaIDHelper.getFavouritesHierarchyAwareMediaID(getResources());
                            break;
                        default:
                            break;
                    }
                    if (mediaIdMenuRoots != null) {
                        for (String categoryMediaId : mediaIdMenuRoots) {
                            if (categoryMediaId.contains(menuItem.getTitle())) {
                                activityClass = MusicPlayerActivity.class;
                                mediaId = categoryMediaId;
                            }
                        }
                    }

                    if (activityClass != null) {
                        Intent intent = new Intent(this, activityClass);
                        if (mediaId != null) {
                            intent.putExtra(MusicPlayerActivity.SAVED_MEDIA_ID, mediaId);
                        }
                        startActivity(intent, bundle);
                    }
                    // Close the navigation drawer_actions when an item is selected.
                    menuItem.setChecked(true);
                    mDrawerLayout.closeDrawers();
                    return true;
                });
    }

    private void exit() {
        ExitDialogFragment exitDialogFragment = ExitDialogFragment.newInstance(
                R.string.exit_dialog_title);
        exitDialogFragment.show(getFragmentManager(), "dialog");
    }

    private void rateApp() {
        mPresenter.rateApp();
    }

    private void updateRateAppMenuItem(Menu navigationMenu) {
        MenuItem rateAppMenuItem = navigationMenu.findItem(R.id.navigation_rate_app);
        CharSequence menuItemText = rateAppMenuItem.getTitle();
        SpannableString spannableString = new SpannableString(menuItemText);
        spannableString.setSpan(
                new ForegroundColorSpan(getResources().getColor(R.color.orange_600)),
                0,
                spannableString.length(),
                0);
        rateAppMenuItem.setTitle(spannableString);
    }

    private void updateMyFavouritesMenuItem(Menu navigationMenu) {
        navigationMenu.findItem(R.id.navigation_my_favorites)
                .setVisible(favouriteMusicDAO.getFavoriteMusicIds().size() > 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Open the navigation drawer_actions when the home icon is selected from the toolabr_menu.
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.remove_ads:
                mPresenter.proposeRemoveAds();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        String mediaId = getMediaId(null, getIntent());
        outState.putString(SAVED_MEDIA_ID, mediaId);
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        LogHelper.d(TAG, "onSaveInstanceState with media id " + mediaId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onMediaItemSelected(MediaBrowserCompat.MediaItem item) {
        LogHelper.d(TAG, "onMediaItemSelected, mediaId=" + item.getMediaId());
        firebaseAnalyticsHelper.trackContentSelected(item);

        if (item.isPlayable()) {
            MediaControllerCompat.getMediaController(MusicPlayerActivity.this).getTransportControls()
                    .playFromMediaId(item.getMediaId(), null);
        } else if (item.isBrowsable()) {
            navigateToBrowser(item.getMediaId());
        } else {
            LogHelper.w(TAG, "Ignoring MediaItem that is neither browsable nor playable: ",
                    "mediaId=", item.getMediaId());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);//must store the new intent unless getIntent() will return the old one
        LogHelper.d(TAG, "onNewIntent, intent=" + intent);
        initializeFromParams(null, intent);
        startFullScreenActivityIfNeeded(intent);
        super.onNewIntent(intent);
    }

    private void startFullScreenActivityIfNeeded(Intent intent) {
        if (intent != null && intent.getBooleanExtra(EXTRA_START_FULLSCREEN, false)) {
            MediaDescriptionCompat description =
                    intent.getParcelableExtra(EXTRA_CURRENT_MEDIA_DESCRIPTION);

            Intent fullScreenIntent = new Intent(this, FullScreenPlayerActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .putExtra(EXTRA_CURRENT_MEDIA_DESCRIPTION, description);
            startActivity(fullScreenIntent);
        }
    }

    protected void initializeFromParams(Bundle savedInstanceState, Intent intent) {
        String mediaId = getMediaId(savedInstanceState, intent);

        MediaBrowserLoader.loadChildrenMediaItems(
                getMediaBrowser(),
                INIT_MEDIA_ID_VALUE_ROOT,
                result -> {
                    if (result instanceof Result.Success) {
                        List<String> mediaIds = getMediaIds((Result.Success<List<MediaBrowserCompat.MediaItem>>) result);
                        navigateToDefault(mediaId, mediaIds);
                    }
                    return addMenuItems(result);
                });
    }

    @NonNull
    String getMediaId(@Nullable Bundle savedInstanceState, @NonNull Intent intent) {
        String mediaId = INIT_MEDIA_ID_VALUE_ROOT;

        // check if we were started from a "Play XYZ" voice search. If so, we save the extras
        // (which contain the query details) in a parameter, so we can reuse it later, when the
        // MediaSession is connected.
        if (intent.getAction() != null
                && intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
            mVoiceSearchParams = intent.getExtras();
            LogHelper.d(TAG, "Starting from voice search query=",
                    mVoiceSearchParams.getString(SearchManager.QUERY));
        } else if (intent.getStringExtra(SAVED_MEDIA_ID) != null) { //called from menu or from internet broadcast receiver
            mediaId = intent.getStringExtra(SAVED_MEDIA_ID);
            LogHelper.d(TAG, "getMediaId with mediaId from String extra, " +
                    "mediaId = " + mediaId);
        } else if (intent.getStringExtra(EXTRA_CURRENT_MEDIA_ID_FROM_NOTIFICATION) != null) { //returned from notification manager
            mediaId = intent.getStringExtra(EXTRA_CURRENT_MEDIA_ID_FROM_NOTIFICATION);
            LogHelper.d(TAG, "getMediaId with mediaId from notification manager, " +
                    "mediaId = " + mediaId);
        } else if (savedInstanceState != null) { //screen rotated
            // If there is a saved media ID, use it
            String savedMediaId = savedInstanceState.getString(SAVED_MEDIA_ID);
            if (savedMediaId != null) {
                mediaId = savedMediaId;
                LogHelper.d(TAG, "getMediaId from savedInstanceState, " +
                        "mediaId = " + mediaId);
            }
        }
        return mediaId;
    }

    private void navigateToBrowser(@NonNull String mediaId) {
        LogHelper.d(TAG, "navigateToBrowser, mediaId=" + mediaId);
        MediaBrowserFragment fragment = getBrowseFragment();
        if (fragment == null || !TextUtils.equals(fragment.getMediaId(), mediaId)) {
            fragment = new MediaBrowserFragment();
            fragment.setMediaId(mediaId);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(
                    R.animator.slide_in_from_right, R.animator.slide_out_to_left,
                    R.animator.slide_in_from_left, R.animator.slide_out_to_right);
            transaction.replace(R.id.media_browser_container, fragment, FRAGMENT_TAG);
            transaction.commit();
        }

    }

    private MediaBrowserFragment getBrowseFragment() {
        return (MediaBrowserFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    }

    @Override
    protected void onMediaControllerConnected() {
        LogHelper.d(TAG, "onMediaControllerConnected");
        if (mVoiceSearchParams != null) {
            // If there is a bootstrap parameter to start from a search query, we
            // send it to the media session and set it to null, so it won't play again
            // when the activity is stopped/started or recreated:
            String query = mVoiceSearchParams.getString(SearchManager.QUERY);
            MediaControllerCompat.getMediaController(MusicPlayerActivity.this).getTransportControls()
                    .playFromSearch(query, mVoiceSearchParams);
            mVoiceSearchParams = null;
        }
        if (getBrowseFragment() != null) {
            getBrowseFragment().onConnected();
        }
    }

    @Override
    public void showError(int errorMessageRes) {
        InfoSnackbarUtil.showError(errorMessageRes, mRootView);
    }

    @Override
    public void showInfo(int infoMessageRes) {
        InfoSnackbarUtil.showInfo(infoMessageRes, mRootView);
    }

    @Override
    public void showInfo(String message) {
        InfoSnackbarUtil.showInfo(message, mRootView);
    }

    @Override
    public void updateViewForAd(boolean isAdsActive) {
        adMobAd.showAd(isAdsActive);
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        LogHelper.d(TAG, "onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);

        Menu navigationMenu = navigationView.getMenu();
        updateRateAppMenuItem(navigationMenu);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        LogHelper.d(TAG, "onPrepareOptionsMenu");
        menu.findItem(R.id.remove_ads).setVisible(Settings.isAdsActive);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void showRemoveAdDialog(AugmentedSkuDetails removeAdsSkuRow) {
        RemoveAdDialogFragment removeAdDialogFragment =
                RemoveAdDialogFragment.newInstance(removeAdsSkuRow.getPrice());

        removeAdDialogFragment.show(getFragmentManager(), "dialog");
    }

    @Override
    public AppCompatActivity getActivity() {
        return this;
    }

    @Override
    public void addMenuItems(List<String> mediaIds) {
        if (navigationView == null || mediaIds == null) {
            LogHelper.e(TAG, "unexpected error when add menu items");
        } else {
            for (int i = 0; i < mediaIds.size(); i++) {

                String mediaId = mediaIds.get(i);

                if (!mediaIdMenuRoots.contains(mediaId)) {
                    mediaIdMenuRoots.add(mediaId);
                    String categoryTitle =
                            MediaIDHelper.extractBrowseCategoryValueFromMediaID(mediaId);
                    MenuItem menuItem =
                            navigationView.getMenu().add(R.id.group_1, i, 2, categoryTitle);
                    menuItem.setIcon(R.drawable.ic_library_music_black_24dp);
                }
            }
            LogHelper.d(TAG, "addMenuItems called with size " + mediaIds.size()
                    + ", menu size updated to " + mediaIdMenuRoots.size());
        }
    }

    @Override
    public void onBackPressed() {
        LogHelper.d(TAG, "onBackPressed()" + getFragmentManager().getBackStackEntryCount());

        MediaBrowserFragment fragment = getBrowseFragment();
        if (fragment != null
                && fragment.getViewPager() != null
                && fragment.getViewPager().getCurrentItem() != 0) {
            ViewPager viewPager = fragment.getViewPager();
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
        } else {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                super.onBackPressed();
            } else {
                ExitDialogFragment exitDialogFragment = ExitDialogFragment.newInstance(
                        R.string.exit_dialog_title);

                exitDialogFragment.show(getFragmentManager(), "dialog");
            }
        }
    }

    @Override
    public void checkForUserVisibleErrors(boolean forceError) {
        boolean showError = forceError;
        // if state is ERROR and metadata!=null, use playback state error message:
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null
                && controller.getMetadata() != null
                && controller.getPlaybackState() != null
                && controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_ERROR
                && controller.getPlaybackState().getErrorMessage() != null) {
            mErrorMessage.setText(controller.getPlaybackState().getErrorMessage());
            showError = true;
        } else if (forceError) {
            // Finally, if the caller requested to show error, show a generic message:
            mErrorMessage.setText(R.string.error_loading_media);
            showError = true;
        }

        emptyResultView.setVisibility(showError ? View.VISIBLE : View.INVISIBLE);
        LogHelper.d(TAG, "checkForUserVisibleErrors. forceError=", forceError,
                " showError=", showError);
    }
}
