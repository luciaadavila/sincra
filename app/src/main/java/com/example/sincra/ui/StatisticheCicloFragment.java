package com.example.sincra.ui;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.sincra.R;
import com.example.sincra.database.repositorio.CicloRepository;
import com.example.sincra.model.Ciclo;
import com.example.sincra.model.ElementoCatalogo;
import com.example.sincra.model.Registrazione;
import com.example.sincra.model.relazioni.CicloConRegistrazioni;
import com.example.sincra.model.relazioni.RegistrazioneConElementi;
import com.example.sincra.utils.FaseCiclo;
import com.example.sincra.utils.FaseCicloUtils;
import com.example.sincra.viewModel.StatisticheCicloViewModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class StatisticheCicloFragment extends Fragment {

    private LinearLayout tableContainer;
    private int cicloId;
    private StatisticheCicloViewModel viewModel;
    private CicloConRegistrazioni ultimoCicloConRegistrazioni;
    private List<RegistrazioneConElementi> ultimeRegistrazioniConElementi = new ArrayList<>();
    private static final int LABEL_WIDTH_DP = 90;
    private static final int CELL_WIDTH_DP = 52;
    private static final int CELL_HEIGHT_DP = 38;


    public StatisticheCicloFragment() {
    }


    @NonNull
    public static StatisticheCicloFragment newInstance(int cicloId) {
        StatisticheCicloFragment fragment = new StatisticheCicloFragment();
        Bundle args = new Bundle();
        args.putInt("cicloId", cicloId);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_statistiche_ciclo, container, false);
        tableContainer = view.findViewById(R.id.tableContainer);

        if (getArguments() != null) {
            cicloId = getArguments().getInt("cicloId");
        }

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(StatisticheCicloViewModel.class);
        viewModel.setCicloId(cicloId);

        viewModel.getCicloConRegistrazioni().observe(getViewLifecycleOwner(), cicloConRegistrazioni -> {
            ultimoCicloConRegistrazioni = cicloConRegistrazioni;
            refreshTable();
        });

        viewModel.getRegistrazioniConElementi().observe(getViewLifecycleOwner(), registrazioniConElementi -> {
            ultimeRegistrazioniConElementi = registrazioniConElementi != null
                    ? registrazioniConElementi
                    : new ArrayList<>();

            refreshTable();
        });
    }


    private void refreshTable() {
        if (ultimoCicloConRegistrazioni == null) return;

        tableContainer.removeAllViews();
        generateTable(ultimoCicloConRegistrazioni, ultimeRegistrazioniConElementi);
    }

    private void generateTable(CicloConRegistrazioni datosCiclo, List<RegistrazioneConElementi> registrazioniConElementi) {
        Ciclo ciclo = datosCiclo.getCiclo();
        int totalDays = ciclo.getDurataTotale();

        List<Registrazione> registrazioni = datosCiclo.getRegistrazioni() != null
                ? datosCiclo.getRegistrazioni()
                : new ArrayList<>();

        Map<String, Registrazione> registrosPorFecha = crearMapaRegistrosPorFecha(registrazioni);

        addRow("Giorno", totalDays, i -> String.valueOf(i + 1));

        addRow("Fase", totalDays, i -> {
            int giornoCiclo = i + 1;

            FaseCiclo fase = FaseCicloUtils.calcoloFase(
                    giornoCiclo,
                    ciclo.getDurataPeriodo(),
                    ciclo.getDurataTotale()
            );

            return getSiglaFase(fase);
        });

        addRow("Periodo", totalDays, i -> {
            java.util.Date fechaColumna = CicloRepository.xDay(ciclo.getDataInizio(), i);
            String key = formatDateKey(fechaColumna);
            Registrazione reg = registrosPorFecha.get(key);
            return (reg != null && reg.isPeriodo()) ? "🔴" : "";
        });

        addRow("Nota", totalDays, i -> {
            java.util.Date fechaColumna = CicloRepository.xDay(ciclo.getDataInizio(), i);
            String key = formatDateKey(fechaColumna);
            Registrazione reg = registrosPorFecha.get(key);

            if (reg != null && reg.getNotas() != null && !reg.getNotas().trim().isEmpty()) {
                return "✕";
            }
            return "";
        });

        addRow("Passi", totalDays, i -> {
            java.util.Date fechaColumna = CicloRepository.xDay(ciclo.getDataInizio(), i);
            String key = formatDateKey(fechaColumna);
            Registrazione reg = registrosPorFecha.get(key);

            if (reg != null && reg.getPasos() > 0) {
                return String.valueOf(reg.getPasos());
            }
            return "";
        });

        List<ElementoCatalogo> elementosUnicos = obtenerElementosUnicos(registrazioniConElementi);

        for (ElementoCatalogo elemento : elementosUnicos) {
            addRow(elemento.getNome(), totalDays, i -> {
                java.util.Date fechaColumna = CicloRepository.xDay(ciclo.getDataInizio(), i);

                boolean aparece = existeElementoEnDia(
                        registrazioniConElementi,
                        elemento.getElementoId(),
                        fechaColumna
                );

                return aparece ? "✕" : "";
            });
        }
    }

    private Registrazione buscarRegistroPorDiaCiclo(List<Registrazione> lista, int numeroDiaCiclo) {
        for (Registrazione r : lista) {
            if (r.getGiornoCiclo() == numeroDiaCiclo) {
                return r;
            }
        }
        return null; // Si el usuario no registró ese día, devolvemos null de forma segura sin romper la app
    }

    private Map<String, Registrazione> crearMapaRegistrosPorFecha(List<Registrazione> registrazioni) {
        Map<String, Registrazione> mapa = new LinkedHashMap<>();
        for (Registrazione reg : registrazioni) {
            mapa.put(formatDateKey(reg.getData()), reg);
        }
        return mapa;
    }

    private String formatDateKey(java.util.Date fecha) {
        if (fecha == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault());
        return sdf.format(fecha);
    }

    private boolean existeElementoEnDia(
            List<RegistrazioneConElementi> registros,
            int elementoId,
            java.util.Date fecha
    ) {
        if (registros == null) return false;

        java.util.Date fechaTruncada = CicloRepository.truncarFecha(fecha);

        for (RegistrazioneConElementi rce : registros) {
            if (rce.registrazione == null) continue;

            java.util.Date regFecha = CicloRepository.truncarFecha(rce.registrazione.getData());
            if (!regFecha.equals(fechaTruncada)) {
                continue;
            }

            if (rce.elementiCatalogo == null) continue;

            for (ElementoCatalogo elemento : rce.elementiCatalogo) {
                if (elemento.getElementoId() == elementoId) {
                    return true;
                }
            }
        }

        return false;
    }

    private String getSiglaFase(FaseCiclo fase) {
        if (fase == null) return "";

        switch (fase) {
            case MESTRUALE:
                return "M";
            case FOLLICOLARE:
                return "F";
            case OVULATORIA:
                return "O";
            case LUTEALE:
                return "L";
            default:
                return "";
        }
    }


    private List<ElementoCatalogo> obtenerElementosUnicos(List<RegistrazioneConElementi> registros) {
        Map<Integer, ElementoCatalogo> mapa = new LinkedHashMap<>();

        if (registros == null) {
            return new ArrayList<>();
        }

        for (RegistrazioneConElementi rce : registros) {
            if (rce.elementiCatalogo == null) continue;

            for (ElementoCatalogo elemento : rce.elementiCatalogo) {
                mapa.put(elemento.getElementoId(), elemento);
            }
        }

        return new ArrayList<>(mapa.values());
    }

    private void addRow(String label, int totalDays, Function<Integer, String> cellProvider) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        // Nombre de la fila
        TextView labelView = new TextView(getContext());
        labelView.setText(label);
        labelView.setGravity(Gravity.CENTER_VERTICAL);
        labelView.setTextSize(13);
        labelView.setSingleLine(true);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                dp(LABEL_WIDTH_DP),
                dp(CELL_HEIGHT_DP)
        );
        labelView.setLayoutParams(labelParams);
        row.addView(labelView);

        // Celdas dinámicas
        for (int i = 0; i < totalDays; i++) {
            TextView cell = new TextView(getContext());
            cell.setText(cellProvider.apply(i));
            cell.setGravity(Gravity.CENTER);
            cell.setTextSize(13);
            cell.setSingleLine(true);

            LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(
                    dp(CELL_WIDTH_DP),
                    dp(CELL_HEIGHT_DP)
            );

            cell.setLayoutParams(cellParams);
            row.addView(cell);
        }

        tableContainer.addView(row);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}