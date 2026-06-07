package com.example.sincra;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.sincra.notifications.DailyReminderScheduler;
import com.example.sincra.notifications.FlexibleReminderScheduler;
import com.example.sincra.notifications.DailyReminderWorker;
import com.example.sincra.notifications.ReminderNotificationHelper;
import com.example.sincra.steps.StepCounterScheduler;
import com.example.sincra.ui.StatisticheFragment;
import com.example.sincra.ui.HistorialFragment;
import com.example.sincra.ui.HomeFragment;
import com.example.sincra.ui.InfoFragment;
import com.example.sincra.ui.PredictFragment;
import com.example.sincra.ui.RegistroFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;
    private ActivityResultLauncher<String> activityRecognitionPermissionLauncher;
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        activityRecognitionPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) {
                                Toast.makeText(this, "Permesso passi concesso", Toast.LENGTH_SHORT).show();

                                // Aquí arrancas el contador en segundo plano
                                StepCounterScheduler.startStepCounter(this);

                            } else {
                                Toast.makeText(this, "Permesso passi non concesso", Toast.LENGTH_SHORT).show();
                            }
                        }
                );

        requestActivityRecognitionPermission();

        notificationPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) {
                                Toast.makeText(this, "Permesso notifiche concesso", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Permesso notifiche non concesso", Toast.LENGTH_SHORT).show();
                            }
                        }
                );

        ReminderNotificationHelper.createNotificationChannel(this);
        FlexibleReminderScheduler.startFlexibleReminder(this);
        DailyReminderScheduler.startDailyReminder(this);
        requestNotificationPermission();

        bottomNavigation = findViewById(R.id.bottom_navigation);
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
        }
        bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                replaceFragment(new HomeFragment());
            }

            else if (item.getItemId() == R.id.nav_historial) {
                replaceFragment(new HistorialFragment());
            }

            else if (item.getItemId() == R.id.nav_prediction){
                replaceFragment(new PredictFragment());
            }

            else if (item.getItemId() == R.id.nav_registro) {
                replaceFragment(new RegistroFragment());
            }

            else if (item.getItemId() == R.id.nav_stats) {
                replaceFragment(new StatisticheFragment());
            }

            else if (item.getItemId() == R.id.nav_info) {
                replaceFragment(new InfoFragment());
            }
            return true;
        });
    }

    private void requestActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            StepCounterScheduler.startStepCounter(this);
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            // Ya tiene permiso, pero NO mostramos Toast
            StepCounterScheduler.startStepCounter(this);

        } else {
            activityRecognitionPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION);
        }
    }


    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        saveAppOpenedToday();
    }

    private void saveAppOpenedToday() {
        String oggi = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        SharedPreferences prefs = getSharedPreferences(DailyReminderWorker.PREFS_NAME, Context.MODE_PRIVATE);

        prefs.edit().putString(DailyReminderWorker.KEY_LAST_OPEN_DAY, oggi).apply();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }
}

