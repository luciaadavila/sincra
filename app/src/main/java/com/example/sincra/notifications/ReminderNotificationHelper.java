package com.example.sincra.notifications;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.sincra.MainActivity;
import com.example.sincra.R;

// crea el canal de notificaciones
// comprueba si se pueden mostrar notificaciones
// construye la notificación
// muestra la notificación
public class ReminderNotificationHelper {
    public static final String CHANNEL_ID = "sincra_daily_reminder_channel";
    private static final String CHANNEL_NAME = "Promemoria Sincra";
    private static final String CHANNEL_DESCRIPTION = "Notifiche per ricordare all'utente di registrare i dati";

    private static final String SETTINGS_PREFS_NAME = "settings_prefs";
    private static final String KEY_NOTIFICHE_ABILITATE = "notifications_enabled";

    // para diferenciar las notificaciones
    private static final int NOTIFICATION_ID_DAILY = 300;
    private static final int NOTIFICATION_ID_FLEXIBLE = 301;

    private static final int REQUEST_CODE_APRI_APP = 200;


    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // creiamo il canale e lo configuriamo
            NotificationChannel canale = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            canale.setDescription(CHANNEL_DESCRIPTION);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            // registriamo il canale con il notification manager
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(canale);
            }
        }
    }

    @SuppressLint("MissingPermission")
    public static void showDailyReminder(Context context) {
        // si no tiene permiso salimos
        if (!notificheDisponibili(context)) {
            return;
        }

        createNotificationChannel(context);

        NotificationCompat.Builder builder = creaBuilderBase(context)
                .setContentTitle("Sincra")
                .setContentText("Non dimenticare di registrare i dati di oggi!");

        // creamos la notificación
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_DAILY, builder.build());
    }

    @SuppressLint("MissingPermission")
    public static void showFlexibleReminder(Context context) {
        // si no tiene permiso salimos
        if (!notificheDisponibili(context)) {
            return;
        }

        createNotificationChannel(context);

        NotificationCompat.Builder builder = creaBuilderBase(context)
                .setContentTitle("Sincra")
                .setContentText("Apri Sincra e controlla come sta andando il tuo monitoraggio.");

        // creamos la notificación
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_FLEXIBLE, builder.build());
    }

    // metodo comun para crear la base de la notificación
    private static NotificationCompat.Builder creaBuilderBase(Context context) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_APRI_APP,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
    }


    public static boolean notificheDisponibili(Context context) {

        if (!notificheAbilitateDaImpostazioni(context)) {
            return false;
        }

        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    public static boolean notificheAbilitateDaImpostazioni(Context context) {

        SharedPreferences settingsPrefs = context.getSharedPreferences(
                SETTINGS_PREFS_NAME,
                Context.MODE_PRIVATE
        );

        return settingsPrefs.getBoolean(KEY_NOTIFICHE_ABILITATE, true);
    }
}