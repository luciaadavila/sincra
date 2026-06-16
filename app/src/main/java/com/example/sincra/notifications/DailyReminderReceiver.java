package com.example.sincra.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// broadcastReceiver => riceve l'evento che avvia l'allarma
public class DailyReminderReceiver extends BroadcastReceiver {

    // metodo che viene eseguito quando scatta l'allarme
    @Override
    public void onReceive(Context context, Intent intent) {
        // notifiche attivata? ha aperto la app oggi?
        if (ReminderNotificationHelper.notificheDisponibili(context) && !appApertaOggi(context)) {
            ReminderNotificationHelper.showDailyReminder(context);
        }

        // impostiamo la allarma per il giorno dopo
        DailyReminderScheduler.startDailyReminder(context);
    }

    private boolean appApertaOggi(Context context) {
        String oggi = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        SharedPreferences prefs = context.getSharedPreferences(
                DailyReminderWorker.PREFS_NAME,
                Context.MODE_PRIVATE
        );

        String ultimoGiornoAperto = prefs.getString(
                DailyReminderWorker.KEY_LAST_OPEN_DAY,
                null
        );

        return oggi.equals(ultimoGiornoAperto);
    }
}