package com.example.sincra.notifications;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

// la notifica è più flessible (non ora giusta)
public class DailyReminderWorker extends Worker {

    public static final String PREFS_NAME = "daily_reminder_prefs";
    public static final String KEY_LAST_OPEN_DAY = "last_open_day";

    private static final String SETTINGS_PREFS_NAME = "settings_prefs";
    private static final String KEY_NOTIFICHE_ABILITATE = "notifications_enabled";

    public DailyReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    // WorkManager esegue automaticamente questo metodo quando decide di avviare il lavoro
    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        // non restituiamo errore perchè non avere attivate le notifiche non è un problema
        if (!ReminderNotificationHelper.notificheDisponibili(context)) return Result.success();

        // mostriamo la notifica (chiamata al helper)
        ReminderNotificationHelper.showFlexibleReminder(context);
        return Result.success();
    }

}