package com.example.sincra.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public class DailyReminderScheduler {
    // questo identificarò il pending intent della allarma
    private static final int REQUEST_CODE_PROMEMORIA = 400;

    // metodo programma la allarma diaria (li chiamamo da mainActivity)
    public static void startDailyReminder(Context context) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        // creiamo il pending intent -> azione che viene eseguita quando si attiva l'allarme
        PendingIntent pendingIntent = creaPendingIntent(context);

        Calendar calendario = Calendar.getInstance();
        calendario.set(Calendar.HOUR_OF_DAY, 21);
        calendario.set(Calendar.MINUTE, 0);
        calendario.set(Calendar.SECOND, 0);
        calendario.set(Calendar.MILLISECOND, 0);

        // evitiamo una allarma passata
        if (calendario.getTimeInMillis() <= System.currentTimeMillis()) {
            calendario.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendario.getTimeInMillis(),
                pendingIntent
        );
    }

    // l'allarme viene annullato se l'utente disattiva le notifiche
    public static void cancelDailyReminder(Context context) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        alarmManager.cancel(creaPendingIntent(context));
    }

    // creiamo il pending intent della allarma
    private static PendingIntent creaPendingIntent(Context context) {

        Intent intent = new Intent(context, DailyReminderReceiver.class);
        // il pending intent sta pensato per lanzare un service (broadcast)
        return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_PROMEMORIA,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}