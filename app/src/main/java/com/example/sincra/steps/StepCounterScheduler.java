package com.example.sincra.steps;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class StepCounterScheduler {

    private static final String WORK_NAME = "sincra_step_counter_work";

    public static void startStepCounter(Context context) {

        Constraints constraints = new Constraints.Builder().setRequiresStorageNotLow(true).build();

        // 1 volta ogni 4 ore
        PeriodicWorkRequest stepRequest = new PeriodicWorkRequest.Builder(StepCounterWorker.class, 4, TimeUnit.HOURS, 1, TimeUnit.HOURS).setConstraints(constraints).build();

        // CON ENQUEUE facciamo sì che venga creato solo un worker
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                stepRequest
        );
    }

    public static void cancelStepCounter(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
    }
}