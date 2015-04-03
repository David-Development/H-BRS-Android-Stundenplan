package de.luhmer.stundenplanh_brsimporter.app.View;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.AbsListView;

import de.luhmer.stundenplanh_brsimporter.app.TimetableFragment;

/**
 * Created by David on 27.03.2015.
 */
public class InterceptedSwipeRefreshLayout extends SwipeRefreshLayout {

    private final String TAG = getClass().getCanonicalName();
    TimetableFragment ttFragment;
    private int mTouchSlop;
    private float mPrevX;

    public InterceptedSwipeRefreshLayout(Context context) {
        super(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public InterceptedSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setTimetableFragment(TimetableFragment ttFragment) {
        this.ttFragment = ttFragment;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        AbsListView selectedLv = ttFragment.getSelectedListView();

        if(selectedLv != null) {
            if (selectedLv.getFirstVisiblePosition() == 0 &&
                    (selectedLv.getChildAt(0) == null || selectedLv.getChildAt(0).getTop() == 0)) {
                //Log.v(TAG, "Ok");
            } else {
                //Log.v(TAG, "Intercept");
                return false;
            }
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPrevX = MotionEvent.obtain(event).getX();
                break;

            case MotionEvent.ACTION_MOVE:
                final float eventX = event.getX();
                float xDiff = Math.abs(eventX - mPrevX);

                if (xDiff > mTouchSlop) {
                    return false;
                }
        }

        return super.onInterceptTouchEvent(event);
    }

    /*
    @Override
    public boolean onTouchEvent(MotionEvent ev) {


        Log.v(TAG, "Date: " + ttFragment.getSelectedDate());
        //Log.v(TAG, "Position: " + ttFragment.getSelectedListView().getFirstVisiblePosition());
        //Log.v(TAG, "Top: " + ttFragment.getSelectedListView().getChildAt(0).getTop());

        if(ttFragment.getSelectedListView().getFirstVisiblePosition() == 0 && (ttFragment.getSelectedListView().getCount() == 0 || ttFragment.getSelectedListView().getChildAt(0).getTop() == 0)) {
            Log.v(TAG, "Ok");
            return super.onTouchEvent(ev);
        } else {
            Log.v(TAG, "Intercepting");

            //ttFragment.getSelectedListView().onTouchEvent(ev);
            return false;
        }

    }
    */
}
