package com.ashomok.lullabies;

/**
 * Created by iuliia on 12/12/16.
 */
public class Settings {

    public static boolean isAdsActive = false; //will be set in MainActivity

    public static final String PRIVACY_POLICY_LINK = BuildConfig.PRIVACY_POLICY_LINK;

    /* BASE_64_ENCODED_PUBLIC_KEY should be YOUR APPLICATION'S PUBLIC KEY
     * (that you got from the Google Play developer console). This is not your
     * developer public key, it's the *app-specific* public key.
     *
     * Instead of just storing the entire literal string here embedded in the
     * program,  construct the key at runtime from pieces or
     * use bit manipulation (for example, XOR with some other string) to hide
     * the actual key.  The key itself is not secret information, but we don't
     * want to make it easy for an attacker to replace the public key with one
     * of their own and then fake messages from the server.
     */
    public static final String BASE_64_ENCODED_PUBLIC_KEY = BuildConfig.BASE_64_ENCODED_PUBLIC_KEY;

    public static final String CATALOG_URL = BuildConfig.CATALOG_URL;

}
