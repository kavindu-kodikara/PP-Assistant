package com.kv.viewstool;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.accessibility.AccessibilityEvent;

/**
 * Service that performs automatic swipe gestures on the screen.
 * Requires Accessibility Permission.
 */
public class AutoScrollService extends AccessibilityService {

    private static AutoScrollService instance;

    public static AutoScrollService getInstance() {
        return instance;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    /**
     * Performs a swipe gesture based on direction.
     * @param forward If true, swipe UP (next). If false, swipe DOWN (previous).
     */
    public void performSwipe(boolean forward) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        Path path = new Path();
        int midX = width / 2;
        
        if (forward) {
            // Swipe UP (Scroll Down to next video)
            path.moveTo(midX, height * 0.9f);
            path.lineTo(midX, height * 0.1f);
        } else {
            // Swipe DOWN (Scroll Up to previous video)
            path.moveTo(midX, height * 0.1f);
            path.lineTo(midX, height * 0.9f);
        }

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        // Duration increased to 400ms for TikTok flicker reliability
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 0, 400));
        
        dispatchGesture(gestureBuilder.build(), null, null);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {}

    @Override
    public boolean onUnbind(android.content.Intent intent) {
        instance = null;
        return super.onUnbind(intent);
    }
}
