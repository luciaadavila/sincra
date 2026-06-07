package com.example.sincra.notifications;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

// programma il FlexibleReminderWorker
// indica ad Android che la classe deve essere eseguita periodicamente
public class FlexibleReminderScheduler {
    private static final String WORK_NAME = "sincra_flexible_reminder_work";

    // attiva il recordatorio flessibile (chiamato da mainActivity in onCreate)
    public static void startFlexibleReminder(Context context) {

        PeriodicWorkRequest richiesta = new PeriodicWorkRequest.Builder(
                DailyReminderWorker.class, // classe che se deve eseguire (doWork())
                1,
                TimeUnit.DAYS
        ).build();

        // registriamo il worker in WorkManager
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                richiesta
        );
    }

    // per cancellarlo
    public static void cancelFlexibleReminder(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
    }
}