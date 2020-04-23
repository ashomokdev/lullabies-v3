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
package com.ashomok.lullabies.ui.about_activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.ashomok.lullabies.BuildConfig;
import com.ashomok.lullabies.R;
import com.ashomok.lullabies.ui.BaseActivity;
import com.ashomok.lullabies.utils.LogHelper;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class AboutActivity extends BaseActivity {
    private static final String TAG = LogHelper.makeLogTag(AboutActivity.class);

    @Inject
    AboutPresenter mPresenter;

    private TextView mTextView_email1;
    private TextView mTextView_email2;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initializeToolbar();

        //base app data
        TextView mTextView_appName = findViewById(R.id.appName);
        mTextView_appName.setText(R.string.app_name);

        TextView mTextView_developer = findViewById(R.id.developer);
        mTextView_developer.setText(R.string.author);

        mTextView_email1 = findViewById(R.id.email);
        mTextView_email1.setText(
                Html.fromHtml("<u>" + getString(R.string.my_email) + "</u>"));
        mTextView_email1.setOnClickListener(
                view -> copyTextToClipboard(mTextView_email1.getText()));

        mTextView_email2 = findViewById(R.id.email2);
        mTextView_email2.setText(
                Html.fromHtml("<u>" + getString(R.string.my_email) + "</u>"));
        mTextView_email2.setOnClickListener(
                view -> copyTextToClipboard(mTextView_email2.getText()));

        TextView mTextView_version = findViewById(R.id.version);
        String version = getString(R.string.version) + " " + BuildConfig.VERSION_NAME;
        mTextView_version.setText(version);

        TextView privacy_policy_link = findViewById(R.id.privacy_policy_link);
        privacy_policy_link.setText(
                Html.fromHtml("<u>" + getString(R.string.privacy_policy_agreement) + "</u>"));

        privacy_policy_link.setOnClickListener(view -> openPrivacyPolicy());
    }

    @Override
    protected void onMediaControllerConnected() {
        //nothing
    }

    private void copyTextToClipboard(CharSequence text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.my_email), text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
        }
        Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 300 milliseconds
        if (v != null) {
            v.vibrate(300);
        }
    }

    private void openPrivacyPolicy() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.PRIVACY_POLICY_LINK)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
