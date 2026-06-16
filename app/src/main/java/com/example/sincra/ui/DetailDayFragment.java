package com.example.sincra.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincra.R;
import com.example.sincra.adapter.CatalogoAdapter;
import com.example.sincra.database.repositorio.CicloRepository;
import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.model.Registrazione;
import com.example.sincra.utils.SwipeDueDitaHelper;
import com.example.sincra.viewModel.DetailDayViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetailDayFragment extends Fragment {

    private CatalogoAdapter moodAdapter;
    private CatalogoAdapter symptomAdapter;
    private DetailDayViewModel viewModel;
    private String currentDate = "";
    private Registrazione registrazione;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());


    private List<ElementoCatalogo> currentMoodsFromDb = new ArrayList<>();
    private List<ElementoCatalogo> currentSymptomsFromDb = new ArrayList<>();


    public DetailDayFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_detail_day, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        // inizializziamo la data passata per argomenti e creiamo una registrazione con essa
        if (getArguments() != null) {
            currentDate = getArguments().getString("date");
        }

        // La data ora è strettamente troncata alle 00:00:00
        registrazione = new Registrazione(stringToDate(currentDate));


        TextView textDate = view.findViewById(R.id.todayBar);
        RecyclerView moodRecycler = view.findViewById(R.id.moodRecycler);
        RecyclerView symptomRecycler = view.findViewById(R.id.symptomRecycler);
        Button btnSalva = view.findViewById(R.id.btnGuardar);
        Button btnAddMood = view.findViewById(R.id.btnAddMood);
        Button btnAddSintomi = view.findViewById(R.id.btnAddSymptom);

        textDate.setText(currentDate);

        // configura la disposizione visuale in griglie (3 colonne)
        moodRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
        symptomRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));

        moodAdapter = new CatalogoAdapter(new ArrayList<>());
        symptomAdapter = new CatalogoAdapter(new ArrayList<>());
        moodRecycler.setAdapter(moodAdapter);
        symptomRecycler.setAdapter(symptomAdapter);

        // configuriamo i gesti con due dita
        SwipeDueDitaHelper swipeDueDitaHelper =
                new SwipeDueDitaHelper(
                        requireContext(),
                        new SwipeDueDitaHelper.OnDuaDitaSwipeListener() {
                            @Override
                            public void onSwipeLeft() {
                                openNewDate(-1);
                            }

                            @Override
                            public void onSwipeRight() {
                                openNewDate(1);
                            }
                        }
                );

        swipeDueDitaHelper.configuraSwipeDueDita(view);
        swipeDueDitaHelper.configuraSwipeDueDita(moodRecycler);
        swipeDueDitaHelper.configuraSwipeDueDita(symptomRecycler);


        viewModel = new ViewModelProvider(this).get(DetailDayViewModel.class);

        // osservare tutti gli stati d'animo disponibili per l'utente
        viewModel.getAllMoods().observe(getViewLifecycleOwner(), moods -> {
            if (moods != null) {
                currentMoodsFromDb = moods;
                sincronizzaElementiSelezionati();
            }
        });

        // osservare tutti i sintomi disponibili per l'utente
        viewModel.getAllSymptoms().observe(getViewLifecycleOwner(), symptoms -> {
            if (symptoms != null) {
                currentSymptomsFromDb = symptoms;
                sincronizzaElementiSelezionati();
            }
        });

        // controlliamo se il giorno aveva già dei record selezionati
        viewModel.getRegistro().observe(getViewLifecycleOwner(), registroConElementi -> {
            if (registroConElementi != null) {
                // Al caricamento dal DB, porta già il suo cicloId associato (se presente) o 0 se orfano
                registrazione = registroConElementi.registrazione;
                if (registroConElementi.elementiCatalogo != null) {
                    marcaElementiComeSelezionati(registroConElementi.elementiCatalogo);
                }
            }
            sincronizzaElementiSelezionati();
        });

        if (getArguments() != null) {
            currentDate = getArguments().getString("date");
            if (currentDate != null) {
                viewModel.setDate(currentDate);
            }
        }

        btnSalva.setOnClickListener(v -> salvaRegistroDelGiorno());
        btnAddMood.setOnClickListener(v -> {
            CatalogoEditableFragment fragment = new CatalogoEditableFragment();
            Bundle argsMood = new Bundle();
            argsMood.putString("tipo", "mood");
            fragment.setArguments(argsMood);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        btnAddSintomi.setOnClickListener(v -> {
            CatalogoEditableFragment fragment = new CatalogoEditableFragment();
            Bundle argsMood = new Bundle();
            argsMood.putString("tipo", "symptom");
            fragment.setArguments(argsMood);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

    }


    private void marcaElementiComeSelezionati(List<ElementoCatalogo> salvati) {
        for (ElementoCatalogo g : salvati) {
            for (ElementoCatalogo m : currentMoodsFromDb) {
                if (m.getElementoId() == g.getElementoId()) {
                    m.setSelected(true);
                }
            }
            for (ElementoCatalogo s : currentSymptomsFromDb) {
                if (s.getElementoId() == g.getElementoId()) {
                    s.setSelected(true);
                }
            }
        }
    }

    private void sincronizzaElementiSelezionati() {
        moodAdapter.updateList(new ArrayList<>(currentMoodsFromDb));
        symptomAdapter.updateList(new ArrayList<>(currentSymptomsFromDb));
    }

    private void salvaRegistroDelGiorno() {
        List<ElementoCatalogo> selezionati = new ArrayList<>();

        selezionati.addAll(moodAdapter.getSelezionati());
        selezionati.addAll(symptomAdapter.getSelezionati());

        viewModel.save(registrazione, selezionati);

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    public Date stringToDate(String dateString){
        Date dateToSave;
        if (dateString != null) {
            try {
                dateToSave = dateFormat.parse(dateString);
            } catch (ParseException e) {
                dateToSave = CicloRepository.truncarFecha(new Date());
            }
        } else {
            dateToSave = CicloRepository.truncarFecha(new Date());
        }

        return dateToSave;
    }


    private void openNewDate(int dias) {
        Date fechaActual = stringToDate(currentDate);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaActual);
        calendar.add(Calendar.DAY_OF_MONTH, dias);

        String nuevaFecha = dateFormat.format(calendar.getTime());
        DetailDayFragment fragment = new DetailDayFragment();

        Bundle args = new Bundle();
        args.putString("date", nuevaFecha);
        fragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }


}