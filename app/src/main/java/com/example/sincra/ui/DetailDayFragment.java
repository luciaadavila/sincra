package com.example.sincra.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
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
import com.example.sincra.database.repositorio.CicloRepository; // IMPORTANTE: Añadido para truncar
import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.model.Registrazione;
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

    // swipe con dos dedos
    private float startTwoFingerX;
    private float startTwoFingerY;
    private boolean twoFingerGestureActive = false;
    private boolean twoFingerGestureDetected = false;

    public DetailDayFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_detail_day, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        // inicializamos la fecha pasada por argumentos y creamos una registrazione con ella
        if (getArguments() != null) {
            currentDate = getArguments().getString("date");
        }

        // La fecha ahora está estrictamente truncada a las 00:00:00
        registrazione = new Registrazione(stringToDate(currentDate));


        TextView textDate = view.findViewById(R.id.todayBar);
        RecyclerView moodRecycler = view.findViewById(R.id.moodRecycler);
        RecyclerView symptomRecycler = view.findViewById(R.id.symptomRecycler);
        Button btnGuardar = view.findViewById(R.id.btnGuardar);
        Button btnAddMood = view.findViewById(R.id.btnAddMood);
        Button btnAddSintomi = view.findViewById(R.id.btnAddSymptom);

        textDate.setText(currentDate);

        // 2. Configurar la disposición visual en cuadrículas (3 columnas)
        moodRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
        symptomRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));

        moodAdapter = new CatalogoAdapter(new ArrayList<>());
        symptomAdapter = new CatalogoAdapter(new ArrayList<>());
        moodRecycler.setAdapter(moodAdapter);
        symptomRecycler.setAdapter(symptomAdapter);

        // configuramos gestos con dos dedos
        configurarSwipeDosDedos(view);
        configurarSwipeDosDedos(moodRecycler);
        configurarSwipeDosDedos(symptomRecycler);


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
                // Al cargar de la DB, ya trae su cicloId asociado (si tiene) o 0 si estaba huérfano
                registrazione = registroConElementi.registrazione;
                if (registroConElementi.elementiCatalogo != null) {
                    marcarElementosComoSeleccionados(registroConElementi.elementiCatalogo);
                }
            }
            sincronizarElementosSeleccionados();
        });

        if (getArguments() != null) {
            currentDate = getArguments().getString("date");
            if (currentDate != null) {
                viewModel.setDate(currentDate);
            }
        }

        btnGuardar.setOnClickListener(v -> guardarRegistroDelDia());
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
        List<ElementoCatalogo> seleccionados = new ArrayList<>();

        // Extraemos de forma limpia lo seleccionado en la UI
        seleccionados.addAll(moodAdapter.getSeleccionados());
        seleccionados.addAll(symptomAdapter.getSeleccionados());

        // Envío asíncrono al repositorio.
        // Si registrazione se creó nueva arriba, su cicloId será 0 (huérfano).
        viewModel.save(registrazione, seleccionados);

        // Finalizar y remover el fragment de la pila de navegación
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    // APLICAMOS EL TRUNCADO ESTRICTO AQUÍ
    public Date stringToDate(String dateString){
        Date dateToSave;
        if (dateString != null) {
            try {
                // parse() ya trunca horas porque el formato solo tiene año-mes-día
                dateToSave = dateFormat.parse(dateString);
            } catch (ParseException e) {
                // Si falla, truncamos el Date actual
                dateToSave = CicloRepository.truncarFecha(new Date());
            }
        } else {
            // Si llega nulo, truncamos el Date actual
            dateToSave = CicloRepository.truncarFecha(new Date());
        }

        return dateToSave;
    }

    // target view es la vista sobre la que queremos detectar gesto
    @SuppressLint("ClickableViewAccessibility")
    private void configurarSwipeDosDedos(View targetView) {
        // definimos la distancia mínima para considerarlo swipe
        int minSwipeDistance = ViewConfiguration.get(requireContext()).getScaledTouchSlop() * 8;

        // cuando el usuario toca la vista, obtenemos información de MotionEvent
        targetView.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                // se apoya el segundo dedo
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (event.getPointerCount() == 2) {
                        // guardamos posición inicial media de los dedos
                        startTwoFingerX = getMediaX(event);
                        startTwoFingerY = getMediaY(event);

                        // indicamos que se inicia gesto con dos dedos pero sin detectar gesto
                        twoFingerGestureActive = true;
                        twoFingerGestureDetected = false;
                        return true;
                    }
                    break;
                // los dedos se mueven
                case MotionEvent.ACTION_MOVE:
                    // si había dos dedos y siguen los dos dedos y todavía no se había detectado gesto
                    if (twoFingerGestureActive && event.getPointerCount() == 2 && !twoFingerGestureDetected) {
                        // calculamos la nueva posición de los dedos
                        float currentX = getMediaX(event);
                        float currentY = getMediaY(event);

                        // calculamos la diferencia entre la posición actual y la anterior
                        float diffX = currentX - startTwoFingerX;
                        float diffY = currentY - startTwoFingerY;

                        // comprobamos que el movimiento haya sido más horizontal que vertical
                        boolean movimientoHorizontal = Math.abs(diffX) > Math.abs(diffY);
                        // comprobamos que la distancia haya sido suficinete
                        boolean distanciaSuficiente = Math.abs(diffX) > minSwipeDistance;

                        // si se dan los dos casos => comprobamos si fue hacia izquierda o derecha
                        if (movimientoHorizontal && distanciaSuficiente) {
                            twoFingerGestureDetected = true;
                            if (diffX < 0) {
                                openNewDate(-1);
                            } else {
                                openNewDate(1);
                            }
                            return true;
                        }
                    }
                    break;

                case MotionEvent.ACTION_POINTER_UP: // se levanta un dedo
                case MotionEvent.ACTION_UP: // se levanta el segundo dedo
                case MotionEvent.ACTION_CANCEL: // android cancela el gesto
                    twoFingerGestureActive = false;
                    twoFingerGestureDetected = false;
                    break;
            }

            return false;
        });
    }

    private float getMediaX(MotionEvent event) {
        float suma = 0;
        for (int i = 0; i < event.getPointerCount(); i++) {
            suma += event.getX(i);
        }
        return suma / event.getPointerCount();
    }

    private float getMediaY(MotionEvent event) {
        float suma = 0;
        for (int i = 0; i < event.getPointerCount(); i++) {
            suma += event.getY(i);
        }
        return suma / event.getPointerCount();
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