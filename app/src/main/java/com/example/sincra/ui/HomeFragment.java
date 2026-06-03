package com.example.sincra.ui;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sincra.R;
import com.example.sincra.adapter.CalendarioHorizontalAdapter;
import com.example.sincra.utils.FaseCiclo;
import com.example.sincra.utils.StatisticheCalculator;
import com.example.sincra.viewModel.HomeViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements SensorEventListener {

    private CalendarioHorizontalAdapter adapter;
    private Button dayButton;
    private String fechaSeleccionadaFormateada;
    private HomeViewModel viewModel;
    private RecyclerView dayRecycler;
    private Date fechaSeleccionada;

    private LinearLayout homeSintomiContainer;
    private LinearLayout homeMoodContainer;

    // contador de pasos
    private TextView stepsTextView;
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;

    private SharedPreferences stepPrefs;
    private int ultimiPassiOggi = 0;
    private boolean passiLettiDalSensore = false;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        dayButton = view.findViewById(R.id.dayButton);
        dayRecycler = view.findViewById(R.id.calendarRecyclerView);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        homeSintomiContainer = view.findViewById(R.id.homeSintomiContainer);
        homeMoodContainer = view.findViewById(R.id.homeMoodContainer);

        stepsTextView = view.findViewById(R.id.stepsTextView);
        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }


        stepPrefs = requireContext().getSharedPreferences("step_counter_prefs", Context.MODE_PRIVATE);
        int pasosGuardados = stepPrefs.getInt("passi_oggi", 0);
        if (stepCounterSensor == null) {
            stepsTextView.setText("Sensore passi non disponibile");
        } else {
            stepsTextView.setText("Passi oggi: " + pasosGuardados);
        }

        fechaSeleccionada = new Date();
        viewModel.updateSelectedDate(fechaSeleccionada);

        adapter = new CalendarioHorizontalAdapter(new ArrayList<>(), new CalendarioHorizontalAdapter.OnDateClickListener() {
            @Override
            public void onDateClick(Date fechaSelec) {
                fechaSeleccionada = fechaSelec;
                viewModel.updateSelectedDate(fechaSelec);
                updateTextoBoton(null);
            }

            @Override
            public void onDateDoubleClick(Date fechaSeleccionada) {
                viewModel.addOrDeletePeriodDay(fechaSeleccionada);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        dayRecycler.setLayoutManager(layoutManager);
        dayRecycler.setAdapter(adapter);

        viewModel.getCicloActual().observe(getViewLifecycleOwner(), cicloConReg -> {
            if (cicloConReg != null) {
                viewModel.calcoloPredict(cicloConReg.getCiclo().getDataInizio());
                viewModel.updateSelectedDate(fechaSeleccionada);
            }
        });

        viewModel.getListaFechas().observe(getViewLifecycleOwner(), fechas -> {
            if (fechas != null) {
                adapter.setListaFechas(fechas);
                int todayIndex = -1;
                SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                String todayStr = fmt.format(new Date());
                for (int i = 0; i < fechas.size(); i++) {
                    if (fmt.format(fechas.get(i)).equals(todayStr)) {
                        todayIndex = i;
                        break;
                    }
                }
                if (todayIndex != -1) {
                    adapter.setPosicionSeleccionada(todayIndex);
                    dayRecycler.scrollToPosition(todayIndex);
                }
            }
        });

        viewModel.getDiasDeRegla().observe(getViewLifecycleOwner(), diasDeRegla -> {
            if (diasDeRegla != null) adapter.setFechasConPeriodo(diasDeRegla);
        });

        viewModel.getDiasProbables().observe(getViewLifecycleOwner(), diasProbables -> {
            if (diasProbables != null) adapter.setDiasProbables(diasProbables);
        });

        viewModel.getFaseSeleccionada().observe(getViewLifecycleOwner(), this::updateTextoBoton);

        viewModel.getStatisticheFaseSelezionata().observe(getViewLifecycleOwner(), stats -> {
            mostraStatisticheFase(stats);
        });

        dayButton.setOnClickListener(v -> {
            DetailDayFragment detailFragment = new DetailDayFragment();
            Bundle args = new Bundle();
            args.putString("date", fechaSeleccionadaFormateada);
            detailFragment.setArguments(args);
            getParentFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void updateTextoBoton(FaseCiclo faseSeleccionadaPorViewModel){
        if (fechaSeleccionada == null) return;
        fechaSeleccionadaFormateada = viewModel.formatDate(fechaSeleccionada);

        if (faseSeleccionadaPorViewModel == null){
            dayButton.setText(fechaSeleccionadaFormateada);
        } else {
            dayButton.setText(fechaSeleccionadaFormateada + "\n" + faseSeleccionadaPorViewModel.getLabel());
        }
    }

    private void mostraStatisticheFase(StatisticheCalculator.StatisticheFase stats) {
        homeSintomiContainer.removeAllViews();
        homeMoodContainer.removeAllViews();

        if (stats == null) {
            addTextRow(homeSintomiContainer, "Sintomi: nessun dato");
            addTextRow(homeMoodContainer, "Stati d'animo: nessun dato");
            return;
        }

        addSectionTitle(homeSintomiContainer, "Sintomi più comuni");
        addElementiSemplici(homeSintomiContainer, stats.getTopSintomi(2));

        addSectionTitle(homeMoodContainer, "Stati d'animo più comuni");
        addElementiSemplici(homeMoodContainer, stats.getTopMood(2));
    }

    private void addElementiSemplici(
            LinearLayout container,
            List<StatisticheCalculator.ElementoStat> elementi
    ) {
        if (elementi == null || elementi.isEmpty()) {
            addTextRow(container, "Nessun dato");
            return;
        }

        for (StatisticheCalculator.ElementoStat elemento : elementi) {
            addTextRow(container, "• " + elemento.getNome() + " (" + elemento.getCount() + ")");
        }
    }

    private void addSectionTitle(LinearLayout container, String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextSize(14);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setTextColor(0xFF333333);
        tv.setPadding(0, 8, 0, 4);
        container.addView(tv);
    }

    private void addTextRow(LinearLayout container, String text) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextSize(14);
        tv.setTextColor(0xFF555555);
        tv.setPadding(0, 2, 0, 2);
        container.addView(tv);
    }

    /////////// PARA EL CONTADOR DE PASOS

    // empezamos a escuchar el sensor
    private void registerStepCounter(){
        if (sensorManager != null && stepCounterSensor != null){
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onResume(){ //cuanto la pantalla está activa, se comprueba el permiso
        super.onResume();
        if (hasActivityRecognitionPermission()) registerStepCounter();
    }

    private boolean hasActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return true;
        }

        return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onPause(){ // dejamos de escuchar al salir de la pantalla -> ahorrar batería
        super.onPause();
        if (sensorManager != null) sensorManager.unregisterListener(this);
        if (passiLettiDalSensore) viewModel.savePassiOggi(ultimiPassiOggi);
    }

    @Override
    public void onSensorChanged(@NonNull SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_STEP_COUNTER) return;

        int pasosSensor = Math.round(event.values[0]); // valor que manda el sensor
        String oggi = dateFormat.format(new Date()); // obtiene el día actual
        // leemos lo que teníamos guardado en las share preferences
        String diaGuardado = stepPrefs.getString("giorno", null);
        int pasosBase = stepPrefs.getInt("pasos_base", -1);

        if (!oggi.equals(diaGuardado) || pasosBase < 0 || pasosSensor < pasosBase) {
            pasosBase = pasosSensor;
            stepPrefs.edit().putString("giorno", oggi).putInt("pasos_base", pasosBase).putInt("passi_oggi", 0).apply();
        }

        int pasosOggi = pasosSensor - pasosBase;
        if (pasosOggi < 0) pasosOggi = 0;

        ultimiPassiOggi = pasosOggi;
        passiLettiDalSensore = true;
        stepsTextView.setText("Passi oggi: " + pasosOggi);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Per STEP_COUNTER non ci serve gestire l'accuracy.
    }


}
