package com.kv.viewstool;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import java.util.Random;

/**
 * Floating Overlay Service that manages the tool UI and looping logic.
 * Fixed ultra-compact design to prevent gesture interference.
 */
public class FloatingOverlayService extends Service {

    private WindowManager windowManager;
    private View overlayView;
    private WindowManager.LayoutParams params;

    private PingPongSequencer sequencer;
    private int minDelay, maxDelay;
    
    private TextView tvTimer, tvDescription;
    private ImageButton btnStartPause;
    
    private Handler timerHandler = new Handler();
    private boolean isRunning = false;
    private int secondsRemaining = 0;
    
    private PowerManager.WakeLock wakeLock;
    private Random random = new Random();

    private static final String CHANNEL_ID = "OverlayServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, getNotification("Service Ready"));
        
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "ViewsTool::PingPongWakeLock");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int count = intent.getIntExtra("itemCount", 3);
            minDelay = intent.getIntExtra("minDelay", 5);
            maxDelay = intent.getIntExtra("maxDelay", 30);
            
            sequencer = new PingPongSequencer(count);
            
            if (overlayView == null) {
                initOverlay();
            }
            updateUI();
        }
        return START_STICKY;
    }

    private void initOverlay() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        android.view.ContextThemeWrapper contextThemeWrapper = new android.view.ContextThemeWrapper(this, R.style.Theme_ViewsTool);
        overlayView = LayoutInflater.from(contextThemeWrapper).inflate(R.layout.layout_overlay, null);

        int layoutType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
        );

        // Fixed position at top-left corner to avoid centering swipes
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 40;
        params.y = 80;

        // UI References
        tvTimer = overlayView.findViewById(R.id.tvTimer);
        tvDescription = overlayView.findViewById(R.id.tvDescription);
        btnStartPause = overlayView.findViewById(R.id.btnStartPause);
        ImageButton btnStop = overlayView.findViewById(R.id.btnStop);

        // DRAGGING DISABLED to prevent scroll gesture interference
        overlayView.setOnTouchListener(null);

        btnStartPause.setOnClickListener(v -> toggleLoop());
        btnStop.setOnClickListener(v -> stopSelf());

        windowManager.addView(overlayView, params);
    }

    private void toggleLoop() {
        if (isRunning) {
            pauseLoop();
        } else {
            startLoop();
        }
    }

    private void startLoop() {
        isRunning = true;
        btnStartPause.setImageResource(android.R.drawable.ic_media_pause);
        
        if (!wakeLock.isHeld()) wakeLock.acquire();
        
        if (secondsRemaining <= 0) {
            scheduleNextMove();
        } else {
            runTimer();
        }
        
        updateForegroundNotification("Loop Running");
    }

    private void pauseLoop() {
        isRunning = false;
        btnStartPause.setImageResource(android.R.drawable.ic_media_play);
        
        if (wakeLock.isHeld()) wakeLock.release();
        timerHandler.removeCallbacksAndMessages(null);
        
        updateForegroundNotification("Loop Paused");
    }

    private void scheduleNextMove() {
        secondsRemaining = minDelay + random.nextInt(maxDelay - minDelay + 1);
        runTimer();
    }

    private void runTimer() {
        if (!isRunning) return;

        tvTimer.setText(secondsRemaining + "s");
        
        if (secondsRemaining <= 0) {
            moveToNextItem();
        } else {
            timerHandler.postDelayed(() -> {
                secondsRemaining--;
                runTimer();
            }, 1000);
        }
    }

    private void moveToNextItem() {
        boolean wasForward = sequencer.isForward();
        sequencer.next();
        updateUI();
        
        // Safety: ensure it is not touchable during the gesture itself 
        if (params != null && overlayView != null && windowManager != null) {
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            windowManager.updateViewLayout(overlayView, params);
        }

        AutoScrollService autoScroll = AutoScrollService.getInstance();
        if (autoScroll != null) {
            autoScroll.performSwipe(wasForward);
        }
        
        timerHandler.postDelayed(() -> {
            if (params != null && overlayView != null && windowManager != null) {
                params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                windowManager.updateViewLayout(overlayView, params);
            }
        }, 600);
        
        scheduleNextMove();
    }

    private void updateUI() {
        tvDescription.setText(sequencer.getCurrentIndex() + "/" + sequencer.getTotalCount());
    }

    private void updateForegroundNotification(String status) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = getNotification(status);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private Notification getNotification(String content) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Views Tool")
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Overlay Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null) windowManager.removeView(overlayView);
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        timerHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
