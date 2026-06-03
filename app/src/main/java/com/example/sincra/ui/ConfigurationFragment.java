package com.example.sincra.ui;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.sincra.R;
import com.example.sincra.notifications.DailyReminderScheduler;
import com.example.sincra.steps.StepCounterScheduler;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ConfigurationFragment extends Fragment {

    private static final String PREFS_NAME = "settings_prefs";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_STEPS_ENABLED = "steps_enabled";

    private SwitchMaterial notificationSwitch;
    private SwitchMaterial stepsSwitch;

    private SharedPreferences prefs;

    private ActivityResultLauncher<String> notificationPermissionLauncher;
    private ActivityResultLauncher<String> activityRecognitionPermissionLauncher;

    public ConfigurationFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        notificationPermissionLauncher = registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            enableNotifications();
                        } else {
                            notificationSwitch.setChecked(false);
                            prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, false).apply();
                        }
                    }
                );

        activityRecognitionPermissionLauncher = registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            enableSteps();
                        } else {
                            stepsSwitch.setChecked(false);
                            prefs.edit().putBoolean(KEY_STEPS_ENABLED, false).apply();
                        }
                    }
                );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configuration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        notificationSwitch = view.findViewById(R.id.notificationSwitch);
        stepsSwitch = view.findViewById(R.id.stepsSwitch);

        boolean notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
        boolean stepsEnabled = prefs.getBoolean(KEY_STEPS_ENABLED, true);

        notificationSwitch.setChecked(notificationsEnabled);
        stepsSwitch.setChecked(stepsEnabled);

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkNotificationPermission();
            } else {
                disableNotifications();
            }
        });

        stepsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkActivityRecognitionPermission();
            } else {
                disableSteps();
            }
        });
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            enableNotifications();
            return;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            enableNotifications();
        } else {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void checkActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            enableSteps();
            return;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            enableSteps();
        } else {
            activityRecognitionPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION);
        }
    }

    private void enableNotifications() {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, true).apply();
        DailyReminderScheduler.startDailyReminder(requireContext());
        Toast.makeText(requireContext(), "Notifiche attivate", Toast.LENGTH_SHORT).show();
    }

    private void disableNotifications() {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, false).apply();
        DailyReminderScheduler.cancelDailyReminder(requireContext());
        Toast.makeText(requireContext(), "Notifiche disattivate", Toast.LENGTH_SHORT).show();
    }

    private void enableSteps() {
        prefs.edit().putBoolean(KEY_STEPS_ENABLED, true).apply();
        StepCounterScheduler.startStepCounter(requireContext());
        Toast.makeText(requireContext(), "Contatore passi attivato", Toast.LENGTH_SHORT).show();
    }

    private void disableSteps() {
        prefs.edit().putBoolean(KEY_STEPS_ENABLED, false).apply();
        StepCounterScheduler.cancelStepCounter(requireContext());
        Toast.makeText(requireContext(), "Contatore passi disattivato", Toast.LENGTH_SHORT).show();
    }
}