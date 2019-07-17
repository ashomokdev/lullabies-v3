package com.ashomok.lullabies.tools;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by iuliia on 16.05.16.
 */

public class CircleView extends View {

    private static final String TAG = CircleView.class.getSimpleName();

    private Paint paintBase;
    private Paint paintAccent;
    private int circleDistance;
    private static final int circleRadius = 10;
    private int mOldItem = 0; //default value
    private int mCurrentItem = 0; //default value
    private int mItemCount = 0; //default value
    private int colorBase;
    private int colorAccent;

    public CircleView(Context context, AttributeSet attrs) throws Exception {
        super(context, attrs);

        colorBase = Color.GRAY;
        colorAccent = Color.WHITE;


        init();
    }

    public void setViewPager(ViewPager viewPager) {

        DataSetObserver dataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateView(viewPager);
            }
        };

        viewPager.addOnAdapterChangeListener((viewPager1, oldAdapter, newAdapter) -> {
            updateView(viewPager1);

            if (newAdapter != null){
                newAdapter.registerDataSetObserver( dataSetObserver);
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                setCurrentItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        PagerAdapter adapter = viewPager.getAdapter();
        if (adapter != null){
            adapter.registerDataSetObserver( dataSetObserver);
            updateView(viewPager);
        }
    }

    private void updateView(ViewPager viewPager) {
        int mNewCount = viewPager.getAdapter().getCount();
        mCurrentItem = viewPager.getCurrentItem();

        if (mOldItem != mCurrentItem || mItemCount != mNewCount) {
            mItemCount = viewPager.getAdapter().getCount();
            mOldItem = mCurrentItem;
            invalidate();
            requestLayout();
        }
    }


    /**
     * Set the current item by index. Optionally, scroll the current item into view. This version
     * is for internal use--the scrollIntoView option is always true for external callers.
     *
     * @param currentItem The index of the current item.
     */
    private void setCurrentItem(int currentItem) {
        mCurrentItem = currentItem;

        invalidate();
        requestLayout();
    }


    /**
     * Setting color of all circles except accent one {@link #setColorAccent(int)}. )
     *
     * @param color
     */
    public void setColorBase(int color) {
        colorBase = color;
        paintBase.setColor(colorBase);

        invalidate();
        requestLayout();
    }


    /**
     * Setting color of accent circle. For another circles use {@link #setColorBase(int)}.
     *
     * @param color
     */
    public void setColorAccent(int color) {
        colorAccent = color;
        paintAccent.setColor(colorAccent);

        invalidate();
        requestLayout();
    }


    private void init() throws Exception {
        circleDistance = generateCircleDistance();

        paintBase = new Paint();
        paintBase.setColor(colorBase);
        paintBase.setAntiAlias(true);

        paintAccent = new Paint();
        paintAccent.setColor(colorAccent);
        paintAccent.setAntiAlias(true);
    }

    /**
     * Generate distance between circles. Return value depends on screen width.
     *
     * @return distance between circles.
     */
    private int generateCircleDistance() throws Exception {

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int screenWidth = display.getWidth();

        int distance = circleRadius * 2;
        if (screenWidth < distance * mItemCount) {
            distance = (int) (circleRadius * 1.5);
            if (screenWidth < distance * mItemCount) {
                throw new Exception("ERROR: You have too many child in ViewPager. It is not possible to show CircleView widget in one line. Try to reorganize your ViewPager.");
            }
        }

        return distance;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int cx = circleRadius;
        for (int i = 0; i < mItemCount; i++) {

            if (i == mCurrentItem) //should have more light color
            {
                canvas.drawCircle(cx, circleRadius, circleRadius, paintAccent);
            } else {
                canvas.drawCircle(cx, circleRadius, circleRadius, paintBase);
            }

            cx = cx + circleRadius * 2 + circleDistance;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int height = circleRadius * 2;
        final int width = ((circleRadius * 2 + circleDistance) * mItemCount) - circleDistance;

        setMeasuredDimension(width, height);
    }
}
