package com.example.sincra.notifications;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DailyReminderWorker extends Worker {

    public static final String PREFS_NAME = "daily_reminder_prefs";
    public static final String KEY_LAST_OPEN_DAY = "last_open_day";

    public DailyReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        if (!hasNotificationPermission(context)) return Result.success();

        SharedPreferences settingsPrefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);
        boolean notificationsEnabled = settingsPrefs.getBoolean("notifications_enabled", true);
        if (!notificationsEnabled) return Result.success();


        String oggi = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String ultimoGiornoAperto = prefs.getString(KEY_LAST_OPEN_DAY, null);

        if (!oggi.equals(ultimoGiornoAperto)) ReminderNotificationHelper.showDailyReminder(context);

        return Result.success();
    }

    private boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true;
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

}