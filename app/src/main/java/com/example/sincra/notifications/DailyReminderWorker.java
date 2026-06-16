package com.example.sincra.notifications;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

// la notifica è più flessibile (non orario preciso)
public class DailyReminderWorker extends Worker {

    public static final String PREFS_NAME = "daily_reminder_prefs";
    public static final String KEY_LAST_OPEN_DAY = "last_open_day";


    public DailyReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    // WorkManager esegue automaticamente questo metodo quando decide di avviare il lavoro
    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        // non restituiamo errore perché non avere le notifiche attivate non è un problema
        if (!ReminderNotificationHelper.notificheDisponibili(context)) return Result.success();

        // mostriamo la notifica (chiamata al helper)
        ReminderNotificationHelper.showFlexibleReminder(context);
        return Result.success();
    }

}