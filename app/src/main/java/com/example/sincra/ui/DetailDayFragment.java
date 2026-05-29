package com.example.sincra.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sincra.R;
import com.example.sincra.adapter.CatalogoAdapter;
import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.model.Registrazione;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;
import com.example.sincra.viewModel.DetailDayViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.widget.Toast;

public class DetailDayFragment extends Fragment {

    private CatalogoAdapter moodAdapter;
    private CatalogoAdapter symptomAdapter;
    private DetailDayViewModel viewModel;
    private Button btnGuardar;
    private String currentDate;
    private int cicloIdActivo = -1;
    private int registroIdExistente = 0;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


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

        RecyclerView moodRecycler = view.findViewById(R.id.moodRecycler);
        RecyclerView symptomRecycler = view.findViewById(R.id.symptomRecycler);
        btnGuardar = view.findViewById(R.id.btnGuardar);

        // 2. Configurar la disposición visual en cuadrículas (3 columnas)
        moodRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
        symptomRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));

        moodAdapter = new CatalogoAdapter(new ArrayList<>());
        symptomAdapter = new CatalogoAdapter(new ArrayList<>());
        moodRecycler.setAdapter(moodAdapter);
        symptomRecycler.setAdapter(symptomAdapter);

        viewModel = new ViewModelProvider(this).get(DetailDayViewModel.class);
        // 5. Observar todos los estados de ánimo disponibles para el usuario
        viewModel.getAllMoods().observe(getViewLifecycleOwner(), moods -> {
            if (moods != null) {
                currentMoodsFromDb = moods;
                sincronizarElementosSeleccionados();
            }
        });

        // 6. Observar todos los síntomas disponibles para el usuario
        viewModel.getAllSymptoms().observe(getViewLifecycleOwner(), symptoms -> {
            if (symptoms != null) {
                currentSymptomsFromDb = symptoms;
                sincronizarElementosSeleccionados();
            }
        });

        // miramos si el dia ya contaba con registros seleccionados
        viewModel.getRegistro().observe(getViewLifecycleOwner(), registroConElementi -> {
            if (registroConElementi != null) {
                registroIdExistente = registroConElementi.registrazione.getRegistroId();
                if (registroConElementi.elementiCatalogo != null) {
                    marcarElementosComoSeleccionados(registroConElementi.elementiCatalogo);
                }
            }
            sincronizarElementosSeleccionados();
        });

        viewModel.getCicloActual().observe(getViewLifecycleOwner(), ciclo -> {
            if (ciclo != null) {
                cicloIdActivo = ciclo.getCiclo().getCicloId();
            }
        });

        if (getArguments() != null) {
            currentDate = getArguments().getString("date");
            if (currentDate != null) {
                viewModel.setDate(currentDate);
            }
        }

        btnGuardar.setOnClickListener(v -> guardarRegistroDelDia());
    }


    private void marcarElementosComoSeleccionados(List<ElementoCatalogo> guardados) {
        for (ElementoCatalogo g : guardados) {
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

    private void sincronizarElementosSeleccionados() {
        moodAdapter.updateList(new ArrayList<>(currentMoodsFromDb));
        symptomAdapter.updateList(new ArrayList<>(currentSymptomsFromDb));
    }

    private void guardarRegistroDelDia() {
        if (cicloIdActivo == -1) {
            Toast.makeText(getContext(), "No hay un ciclo activo para guardar el registro", Toast.LENGTH_SHORT).show();
            return;
        }

        List<ElementoCatalogo> seleccionados = new ArrayList<>();

        // Gracias a tu método público getSeleccionados(), extraemos de forma limpia lo seleccionado en la UI
        seleccionados.addAll(moodAdapter.getSeleccionados());
        seleccionados.addAll(symptomAdapter.getSeleccionados());

        Date dateToSave;
        if (currentDate != null) {
            try {
                dateToSave = dateFormat.parse(currentDate);
            } catch (ParseException e) {
                dateToSave = new Date();
            }
        } else {
            dateToSave = new Date();
        }

        Registrazione r = new Registrazione(dateToSave, false, false, 1, "", cicloIdActivo, 0);
        if (registroIdExistente != 0) {
            r.setRegistroId(registroIdExistente);
        }

        // Envío asíncrono al repositorio
        viewModel.save(r, seleccionados);

        // Finalizar y remover el fragment de la pila de navegación
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

}