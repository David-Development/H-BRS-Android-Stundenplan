package de.luhmer.stundenplanh_brsimporter.app.View;

import android.content.Context;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by David on 15.08.2014.
 */
public class InterceptedSlidingPaneLayout extends SlidingPaneLayout {

    int mWidth = 100;
    boolean mStayOpen = false;

    public void setViewWidth(int width) {
        this.mWidth = width;
    }

    public void setStayOpen(boolean stayOpen) {
        this.mStayOpen = stayOpen;
    }

    public InterceptedSlidingPaneLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mStayOpen && (ev.getX() < mWidth / 6 || isOpen())) {
            return super.onInterceptTouchEvent(ev);
        }
        return false;
    }

    /*
    @Override
    public boolean onTouchEvent(MotionEvent event){
        if (event.getX() < mWidth / 6) {
            return super.onTouchEvent(event);// here it works as a normal SlidingPaneLayout
        }
        return false; // here it returns false so that another event's listener should be called, in your case the MapFragment listener
    }
    */
}
