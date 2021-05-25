package com.ashomok.lullabies.ui.main_activity.media_browser;

import com.ashomok.lullabies.ui.MediaBrowserProvider;

//todo refactore - remove thi sinterface is the best option
public interface MediaFragmentListener extends MediaBrowserProvider {
    void setToolbarTitle(CharSequence title);
}