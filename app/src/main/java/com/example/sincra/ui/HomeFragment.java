package com.example.sincra.ui;

import android.Manifest;
import android.annotation.SuppressLint;
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
import com.example.sincra.utils.SwipeDueDitaHelper;
import com.example.sincra.viewModel.HomeViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements SensorEventListener {

    private CalendarioHorizontalAdapter adapter;
    private Button dayButton;
    private String dataSelezionataFormattata;
    private HomeViewModel viewModel;
    private RecyclerView dayRecycler;
    private Date dataSelezionata;

    private LinearLayout homeSintomiContainer;
    private LinearLayout homeMoodContainer;

    // contatore passi
    private TextView stepsTextView;
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;

    private SharedPreferences stepPrefs;
    private int ultimiPassiOggi = 0;
    private boolean passiLettiDalSensore = false;

    private boolean scrollInizialeCalendario = false;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        dayButton = view.findViewById(R.id.dayButton);
        dayRecycler = view.findViewById(R.id.calendarRecyclerView);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModel.updateSelectedDate(new Date());

        homeSintomiContainer = view.findViewById(R.id.homeSintomiContainer);
        homeMoodContainer = view.findViewById(R.id.homeMoodContainer);

        stepsTextView = view.findViewById(R.id.stepsTextView);
        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }


        stepPrefs = requireContext().getSharedPreferences("step_counter_prefs", Context.MODE_PRIVATE);
        int passiSalvati = stepPrefs.getInt("passi_oggi", 0);
        if (stepCounterSensor == null) {
            stepsTextView.setText(R.string.sensore_passi_non_disponibile);
        } else {
            stepsTextView.setText(getString(R.string.passi_oggi) + ": " + passiSalvati);
        }

        viewModel.getDataSelezionata().observe(getViewLifecycleOwner(), data -> {
            if (data == null) return;
            dataSelezionata = data;
            aggiornaTestoBottone(null);
            sincronizzaCalendarioConData(data);
        });

        adapter = new CalendarioHorizontalAdapter(new ArrayList<>(), new CalendarioHorizontalAdapter.OnDateClickListener() {
            @Override
            public void onDateClick(Date dataSelez) {
                viewModel.updateSelectedDate(dataSelez);
            }

            @Override
            public void onDateDoubleClick(Date dataSelezionata) {
                viewModel.addOrDeletePeriodDay(dataSelezionata);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        dayRecycler.setLayoutManager(layoutManager);
        dayRecycler.setAdapter(adapter);



        viewModel.getCicloActual().observe(getViewLifecycleOwner(), cicloConReg -> {
            if (cicloConReg != null) {
                viewModel.calcoloPredict(cicloConReg.getCiclo().getDataInizio());
                if (dataSelezionata != null){
                    viewModel.updateSelectedDate(dataSelezionata);
                }
            }
        });

        viewModel.getListaFechas().observe(getViewLifecycleOwner(), fechas -> {
            if (fechas != null) {
                adapter.setListaFechas(fechas);

                Date dataIniziale = dataSelezionata != null ? dataSelezionata : new Date();

                if (!scrollInizialeCalendario) {
                    portaCalendarioAllaData(dataIniziale);
                    scrollInizialeCalendario = true;
                } else {
                    sincronizzaCalendarioConData(dataSelezionata);
                }
            }
        });

        viewModel.getDiasDeRegla().observe(getViewLifecycleOwner(), diasDeRegla -> {
            if (diasDeRegla != null) adapter.setFechasConPeriodo(diasDeRegla);
        });

        viewModel.getDiasProbables().observe(getViewLifecycleOwner(), diasProbables -> {
            if (diasProbables != null) adapter.setDiasProbables(diasProbables);
        });

        viewModel.getFaseSeleccionada().observe(getViewLifecycleOwner(), this::aggiornaTestoBottone);

        viewModel.getStatisticheFaseSelezionata().observe(getViewLifecycleOwner(), this::mostraStatisticheFase);

        dayButton.setOnClickListener(v -> {
            DetailDayFragment detailFragment = new DetailDayFragment();
            Bundle args = new Bundle();
            args.putString("date", dataSelezionataFormattata);
            detailFragment.setArguments(args);
            getParentFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        SwipeDueDitaHelper swipeDueDitaHelper =
                new SwipeDueDitaHelper(
                        requireContext(),
                        new SwipeDueDitaHelper.OnDuaDitaSwipeListener() {
                            @Override
                            public void onSwipeLeft() {
                                cambiaDataSelezionata(-1);
                            }

                            @Override
                            public void onSwipeRight() {
                                cambiaDataSelezionata(1);
                            }
                        }
                );
        swipeDueDitaHelper.configuraSwipeDueDita(view);

    }


    private void aggiornaTestoBottone(FaseCiclo faseSelezionataDalViewModel){
        if (dataSelezionata == null) return;
        dataSelezionataFormattata = viewModel.formatDate(dataSelezionata);

        if (faseSelezionataDalViewModel == null){
            dayButton.setText(dataSelezionataFormattata);
        } else {
            dayButton.setText(dataSelezionataFormattata + "\n" + getString(faseSelezionataDalViewModel.getResId()));
        }
    }

    private void sincronizzaCalendarioConData(Date data){
        if (adapter == null || data == null) return;

        int posizione = adapter.trovaPosizioneData(data);

        if (posizione != RecyclerView.NO_POSITION) {
            adapter.setPosizioneSelezionata(posizione);
        }
    }

    private void portaCalendarioAllaData(Date data) {
        if (adapter == null || data == null) return;

        int posizione = adapter.trovaPosizioneData(data);
        if (posizione == RecyclerView.NO_POSITION) return;

        adapter.setPosizioneSelezionata(posizione);
        RecyclerView.LayoutManager manager = dayRecycler.getLayoutManager();

        if (manager instanceof LinearLayoutManager) {
            dayRecycler.post(() -> {
                ((LinearLayoutManager) manager)
                        .scrollToPositionWithOffset(posizione, 0);
            });
        }
    }


    private void mostraStatisticheFase(StatisticheCalculator.StatisticheFase stats) {
        homeSintomiContainer.removeAllViews();
        homeMoodContainer.removeAllViews();

        if (stats == null) {
            addTextRow(homeSintomiContainer, getString(R.string.sintomi) + ": " + getString(R.string.nessun_dato));
            addTextRow(homeMoodContainer, getString(R.string.umore) + ": " + getString(R.string.nessun_dato));
            return;
        }

        addSectionTitle(homeSintomiContainer, getString(R.string.sintomi_pi_comuni));
        addElementiSemplici(homeSintomiContainer, stats.getTopSintomi(2));

        addSectionTitle(homeMoodContainer, getString(R.string.stati_d_animo_pi_comuni));
        addElementiSemplici(homeMoodContainer, stats.getTopMood(2));
    }

    private void addElementiSemplici(
            LinearLayout container,
            List<StatisticheCalculator.ElementoStat> elementi
    ) {
        if (elementi == null || elementi.isEmpty()) {
            addTextRow(container, getString(R.string.nessun_dato));
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


    private void cambiaDataSelezionata(int giorni) {
        if (dataSelezionata == null) {
            dataSelezionata = new Date();
        }

        java.util.Calendar calendario = java.util.Calendar.getInstance();
        calendario.setTime(dataSelezionata);
        calendario.add(java.util.Calendar.DAY_OF_MONTH, giorni);

        Date nuovaData = calendario.getTime();
        int nuovaPosizione = adapter.trovaPosizioneData(nuovaData);

        if (nuovaPosizione == RecyclerView.NO_POSITION) return;
        viewModel.updateSelectedDate(nuovaData);
    }

    /////////// PER IL CONTATORE PASSI

    // iniziamo ad ascoltare il sensore
    private void registerStepCounter(){
        if (sensorManager != null && stepCounterSensor != null){
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onResume(){ // quando lo schermo è attivo, viene controllato il permesso
        super.onResume();
        passiLettiDalSensore = false;
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
    public void onPause(){ // smettiamo di ascoltare all'uscita dallo schermo -> risparmio batteria
        super.onPause();
        if (sensorManager != null) sensorManager.unregisterListener(this);
        if (passiLettiDalSensore) viewModel.savePassiOggi(ultimiPassiOggi);
    }

    @Override
    public void onSensorChanged(@NonNull SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_STEP_COUNTER) return;

        int passiSensore = Math.round(event.values[0]); // valore inviato dal sensore
        String oggi = dateFormat.format(new Date()); // ottiene il giorno corrente
        // leggiamo ciò che avevamo salvato nelle shared preferences
        String diaGuardado = stepPrefs.getString("giorno", null);
        int passiBase = stepPrefs.getInt("passi_base", -1);

        if (!oggi.equals(diaGuardado) || passiBase < 0 || passiSensore < passiBase) {
            passiBase = passiSensore;
            stepPrefs.edit().putString("giorno", oggi).putInt("passi_base", passiBase).putInt("passi_oggi", 0).apply();
        }

        int passiOggi = passiSensore - passiBase;

        ultimiPassiOggi = passiOggi;
        passiLettiDalSensore = true;
        stepPrefs.edit().putInt("passi_oggi", passiOggi).apply();
        stepsTextView.setText(getString(R.string.passi_oggi) + ": " + passiOggi);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Per STEP_COUNTER non ci serve gestire l'accuracy.
    }


}
