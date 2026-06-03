package com.example.sincra.notifications;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class DailyReminderScheduler {
    private static final String WORK_NAME = "sincra_daily_reminder_work";

    public static void startDailyReminder(Context context) {
        Constraints constraints = new Constraints.Builder().setRequiresBatteryNotLow(true).build();

        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(DailyReminderWorker.class, 1, TimeUnit.DAYS).setConstraints(constraints).build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request);
    }

    public static void cancelDailyReminder(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
    }
}