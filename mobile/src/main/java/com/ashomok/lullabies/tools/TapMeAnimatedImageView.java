package com.ashomok.lullabies.tools;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;

import com.ashomok.lullabies.R;


public class TapMeAnimatedImageView extends androidx.appcompat.widget.AppCompatImageView {
    public TapMeAnimatedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setImageDrawable(ContextCompat.getDrawable(
                context, R.drawable.ic_tap_animated));

        Drawable circle = ContextCompat.getDrawable(context, R.drawable.circle);
        setBackground(circle);
    }

    @Override
    protected void onAttachedToWindow() {
        // It's important to note that the start() method called on
        // the AnimationDrawable cannot be called during the onCreate()
        // method of your Activity, because the AnimationDrawable
        // is not yet fully attached to the window.
        super.onAttachedToWindow();
        AnimationDrawable tapAnimation = (AnimationDrawable) getDrawable();
        tapAnimation.start();
    }
}
