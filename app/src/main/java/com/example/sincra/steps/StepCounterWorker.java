package com.example.sincra.steps;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

// sottoclasse de worker
public class StepCounterWorker extends Worker implements SensorEventListener {
    private CountDownLatch latch;
    private Integer pasosSensorActuales = null;

    private static final String PREFS_NAME = "step_counter_prefs";
    private static final String KEY_DIA = "giorno";
    private static final String KEY_PASOS_BASE = "pasos_base";
    private static final String KEY_PASOS_HOY = "passi_oggi";

    public StepCounterWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters){
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork(){
        Context context = getApplicationContext();
        if (!hasActivityRecognitionPermission(context)) return Result.success();

        SharedPreferences settingsPrefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);
        boolean notificationsEnabled = settingsPrefs.getBoolean("steps_enabled", true);
        if (!notificationsEnabled) return Result.success();

        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) return Result.failure();

        Sensor stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepCounterSensor == null) return Result.failure();

        latch = new CountDownLatch(1);
        HandlerThread handlerThread = new HandlerThread("StepCounterWorkerThread");
        handlerThread.start();

        Handler handler = new Handler(handlerThread.getLooper());
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL, handler);

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            sensorManager.unregisterListener(this);
            handlerThread.quitSafely();
            return Result.retry();
        }

        sensorManager.unregisterListener(this);
        handlerThread.quitSafely();

        if (pasosSensorActuales == null) {
            return Result.retry();
        }

        guardarPasos(context, pasosSensorActuales);

        return Result.success();
    }

    private boolean hasActivityRecognitionPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true;
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED;
    }

    private void guardarPasos(Context context, int pasosSensor) {

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        String diaGuardado = prefs.getString(KEY_DIA, null);
        int pasosBase = prefs.getInt(KEY_PASOS_BASE, -1);

        if (!hoy.equals(diaGuardado) || pasosBase < 0 || pasosSensor < pasosBase) {
            pasosBase = pasosSensor;
            prefs.edit().putString(KEY_DIA, hoy).putInt(KEY_PASOS_BASE, pasosBase).putInt(KEY_PASOS_HOY, 0).apply();
            return;
        }

        int pasosHoy = pasosSensor - pasosBase;
        if (pasosHoy < 0) pasosHoy = 0;

        prefs.edit().putString(KEY_DIA, hoy).putInt(KEY_PASOS_HOY, pasosHoy).apply();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_STEP_COUNTER) return;

        pasosSensorActuales = Math.round(event.values[0]);
        if (latch != null) latch.countDown();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
