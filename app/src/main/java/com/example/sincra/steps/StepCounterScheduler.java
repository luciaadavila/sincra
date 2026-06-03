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

        PeriodicWorkRequest stepRequest =
                new PeriodicWorkRequest.Builder(StepCounterWorker.class, 4, TimeUnit.HOURS).setConstraints(constraints).build();

        // CON ENEQUEUE hacemos que solo se cree un worker
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                stepRequest
        );
    }

    public static void cancelStepCounter(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
    }
}