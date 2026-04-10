package com.kv.viewstool;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Setup screen for the Ping-Pong Item Reviewer.
 */
public class MainActivity extends AppCompatActivity {

    private EditText etItemCount, etMinDelay, etMaxDelay;
    private Button btnAccessibility;
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etItemCount = findViewById(R.id.etItemCount);
        etMinDelay = findViewById(R.id.etMinDelay);
        etMaxDelay = findViewById(R.id.etMaxDelay);
        btnAccessibility = findViewById(R.id.btnAccessibility);
        Button btnConfigure = findViewById(R.id.btnConfigure);

        btnConfigure.setOnClickListener(v -> checkPermissionAndStart());
        btnAccessibility.setOnClickListener(v -> openAccessibilitySettings());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAccessibilityButton();
    }

    private void updateAccessibilityButton() {
        if (!isAccessibilityServiceEnabled(this, AutoScrollService.class)) {
            btnAccessibility.setVisibility(View.VISIBLE);
        } else {
            btnAccessibility.setVisibility(View.GONE);
        }
    }

    private void openAccessibilitySettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
        Toast.makeText(this, "Find 'Ping-Pong Reviewer' and turn it ON", Toast.LENGTH_LONG).show();
    }

    private void checkPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
                Toast.makeText(this, "Please grant overlay permission", Toast.LENGTH_LONG).show();
                return;
            }
        }

        if (!isAccessibilityServiceEnabled(this, AutoScrollService.class)) {
            Toast.makeText(this, "Please grant Scroll Permission first", Toast.LENGTH_SHORT).show();
            btnAccessibility.setVisibility(View.VISIBLE);
            return;
        }

        startOverlayService();
    }

    private void startOverlayService() {
        String countStr = etItemCount.getText().toString();
        String minStr = etMinDelay.getText().toString();
        String maxStr = etMaxDelay.getText().toString();

        if (countStr.isEmpty() || minStr.isEmpty() || maxStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int count = Integer.parseInt(countStr);
        int min = Integer.parseInt(minStr);
        int max = Integer.parseInt(maxStr);

        if (count < 1 || min < 1 || max < min) {
            Toast.makeText(this, "Invalid settings", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent serviceIntent = new Intent(this, FloatingOverlayService.class);
        serviceIntent.putExtra("itemCount", count);
        serviceIntent.putExtra("minDelay", min);
        serviceIntent.putExtra("maxDelay", max);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        moveTaskToBack(true);
    }

    public static boolean isAccessibilityServiceEnabled(Context context, Class<?> service) {
        String serviceName = context.getPackageName() + "/" + service.getName();
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                String[] services = settingValue.split(":");
                for (String s : services) {
                    if (s.equalsIgnoreCase(serviceName)) return true;
                }
            }
        }
        return false;
    }
}
