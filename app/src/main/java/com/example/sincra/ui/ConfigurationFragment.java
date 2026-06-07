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
import com.example.sincra.notifications.FlexibleReminderScheduler;
import com.example.sincra.steps.StepCounterScheduler;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ConfigurationFragment extends Fragment {

    private static final String PREFS_NAME = "settings_prefs";

    private static final String KEY_DAILY_NOTIFICATIONS_ENABLED = "daily_notifications_enabled";
    private static final String KEY_FLEXIBLE_NOTIFICATIONS_ENABLED = "flexible_notifications_enabled";
    private static final String KEY_STEPS_ENABLED = "steps_enabled";

    private static final int NOTIFICA_GIORNALIERA = 1;
    private static final int NOTIFICA_FLESSIBILE = 2;
    private static final int NESSUNA_NOTIFICA = 0;

    private SwitchMaterial dailyNotificationSwitch;
    private SwitchMaterial flexibleNotificationSwitch;
    private SwitchMaterial stepsSwitch;

    private SharedPreferences prefs;

    private ActivityResultLauncher<String> notificationPermissionLauncher;
    private ActivityResultLauncher<String> activityRecognitionPermissionLauncher;

    private int richiestaNotificaInAttesa = NESSUNA_NOTIFICA;

    public ConfigurationFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                permessoConcesso -> {
                    if (permessoConcesso) {
                        if (richiestaNotificaInAttesa == NOTIFICA_GIORNALIERA) {
                            enableDailyNotifications();
                        } else if (richiestaNotificaInAttesa == NOTIFICA_FLESSIBILE) {
                            enableFlexibleNotifications();
                        }
                    } else {
                        if (richiestaNotificaInAttesa == NOTIFICA_GIORNALIERA) {
                            dailyNotificationSwitch.setChecked(false);
                            prefs.edit()
                                    .putBoolean(KEY_DAILY_NOTIFICATIONS_ENABLED, false)
                                    .apply();
                        } else if (richiestaNotificaInAttesa == NOTIFICA_FLESSIBILE) {
                            flexibleNotificationSwitch.setChecked(false);
                            prefs.edit()
                                    .putBoolean(KEY_FLEXIBLE_NOTIFICATIONS_ENABLED, false)
                                    .apply();
                        }

                        Toast.makeText(
                                requireContext(),
                                "Permesso notifiche non concesso",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    richiestaNotificaInAttesa = NESSUNA_NOTIFICA;
                }
        );

        activityRecognitionPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                permessoConcesso -> {
                    if (permessoConcesso) {
                        enableSteps();
                    } else {
                        stepsSwitch.setChecked(false);

                        prefs.edit()
                                .putBoolean(KEY_STEPS_ENABLED, false)
                                .apply();

                        Toast.makeText(
                                requireContext(),
                                "Permesso attività fisica non concesso",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_configuration, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        dailyNotificationSwitch = view.findViewById(R.id.dailyNotificationSwitch);
        flexibleNotificationSwitch = view.findViewById(R.id.flexibleNotificationSwitch);
        stepsSwitch = view.findViewById(R.id.stepsSwitch);

        boolean notificheGiornaliereAttive =
                prefs.getBoolean(KEY_DAILY_NOTIFICATIONS_ENABLED, false);

        boolean notificheFlessibiliAttive =
                prefs.getBoolean(KEY_FLEXIBLE_NOTIFICATIONS_ENABLED, false);

        boolean passiAttivi =
                prefs.getBoolean(KEY_STEPS_ENABLED, true);

        dailyNotificationSwitch.setChecked(notificheGiornaliereAttive);
        flexibleNotificationSwitch.setChecked(notificheFlessibiliAttive);
        stepsSwitch.setChecked(passiAttivi);

        dailyNotificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkDailyNotificationPermission();
            } else {
                disableDailyNotifications();
            }
        });

        flexibleNotificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkFlexibleNotificationPermission();
            } else {
                disableFlexibleNotifications();
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

    private void checkDailyNotificationPermission() {
        richiestaNotificaInAttesa = NOTIFICA_GIORNALIERA;
        checkNotificationPermission();
    }

    private void checkFlexibleNotificationPermission() {
        richiestaNotificaInAttesa = NOTIFICA_FLESSIBILE;
        checkNotificationPermission();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (richiestaNotificaInAttesa == NOTIFICA_GIORNALIERA) {
                enableDailyNotifications();
            } else if (richiestaNotificaInAttesa == NOTIFICA_FLESSIBILE) {
                enableFlexibleNotifications();
            }

            richiestaNotificaInAttesa = NESSUNA_NOTIFICA;
            return;
        }

        boolean permessoConcesso =
                ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED;

        if (permessoConcesso) {
            if (richiestaNotificaInAttesa == NOTIFICA_GIORNALIERA) {
                enableDailyNotifications();
            } else if (richiestaNotificaInAttesa == NOTIFICA_FLESSIBILE) {
                enableFlexibleNotifications();
            }

            richiestaNotificaInAttesa = NESSUNA_NOTIFICA;
        } else {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void checkActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            enableSteps();
            return;
        }

        boolean permessoConcesso =
                ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED;

        if (permessoConcesso) {
            enableSteps();
        } else {
            activityRecognitionPermissionLauncher.launch(
                    Manifest.permission.ACTIVITY_RECOGNITION
            );
        }
    }

    private void enableDailyNotifications() {
        prefs.edit()
                .putBoolean(KEY_DAILY_NOTIFICATIONS_ENABLED, true)
                .apply();

        DailyReminderScheduler.startDailyReminder(requireContext());

        Toast.makeText(
                requireContext(),
                "Notifiche giornaliere attivate",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void disableDailyNotifications() {
        prefs.edit()
                .putBoolean(KEY_DAILY_NOTIFICATIONS_ENABLED, false)
                .apply();

        DailyReminderScheduler.cancelDailyReminder(requireContext());

        Toast.makeText(
                requireContext(),
                "Notifiche giornaliere disattivate",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void enableFlexibleNotifications() {
        prefs.edit()
                .putBoolean(KEY_FLEXIBLE_NOTIFICATIONS_ENABLED, true)
                .apply();

        FlexibleReminderScheduler.startFlexibleReminder(requireContext());

        Toast.makeText(
                requireContext(),
                "Notifiche flessibili attivate",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void disableFlexibleNotifications() {
        prefs.edit()
                .putBoolean(KEY_FLEXIBLE_NOTIFICATIONS_ENABLED, false)
                .apply();

        FlexibleReminderScheduler.cancelFlexibleReminder(requireContext());

        Toast.makeText(
                requireContext(),
                "Notifiche flessibili disattivate",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void enableSteps() {
        prefs.edit()
                .putBoolean(KEY_STEPS_ENABLED, true)
                .apply();

        StepCounterScheduler.startStepCounter(requireContext());

        Toast.makeText(
                requireContext(),
                "Contatore passi attivato",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void disableSteps() {
        prefs.edit()
                .putBoolean(KEY_STEPS_ENABLED, false)
                .apply();

        StepCounterScheduler.cancelStepCounter(requireContext());

        Toast.makeText(
                requireContext(),
                "Contatore passi disattivato",
                Toast.LENGTH_SHORT
        ).show();
    }
}