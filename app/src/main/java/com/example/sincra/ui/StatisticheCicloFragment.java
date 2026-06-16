package com.example.sincra.ui;

import android.graphics.Typeface;
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
    private CicloConRegistrazioni ultimoCicloConRegistrazioni;
    private List<RegistrazioneConElementi> ultimeRegistrazioniConElementi = new ArrayList<>();
    private static final int LABEL_WIDTH_DP = 90;
    private static final int CELL_WIDTH_DP = 52;
    private static final int CELL_HEIGHT_DP = 38;


    public StatisticheCicloFragment() {
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

        StatisticheCicloViewModel viewModel = new ViewModelProvider(this).get(StatisticheCicloViewModel.class);
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

    private void generateTable(CicloConRegistrazioni datiCiclo, List<RegistrazioneConElementi> registrazioniConElementi) {
        Ciclo ciclo = datiCiclo.getCiclo();
        if (ciclo == null) return;

        int totalDays = ciclo.getDurataTotale();

        List<Registrazione> registrazioni = datiCiclo.getRegistrazioni() != null
                ? datiCiclo.getRegistrazioni()
                : new ArrayList<>();

        Map<String, Registrazione> registriPerData = creaMappaRegistriPerData(registrazioni);

        addRow(getString(R.string.giorno), totalDays, i -> String.valueOf(i + 1), true);

        addRow(getString(R.string.fase), totalDays, i -> {
            int giornoCiclo = i + 1;

            FaseCiclo fase = FaseCicloUtils.calcoloFase(
                    giornoCiclo,
                    ciclo.getDurataPeriodo(),
                    ciclo.getDurataTotale()
            );

            return getSiglaFase(fase);
        }, false);

        addRow(getString(R.string.periodo), totalDays, i -> {
            java.util.Date dataColonna = CicloRepository.xDay(ciclo.getDataInizio(), i);
            String key = formattaChiaveData(dataColonna);
            Registrazione reg = registriPerData.get(key);
            return (reg != null && reg.isPeriodo()) ? "🔴" : "";
        }, false);

        addRow(getString(R.string.passi) , totalDays, i -> {
            java.util.Date dataColonna = CicloRepository.xDay(ciclo.getDataInizio(), i);
            String key = formattaChiaveData(dataColonna);
            Registrazione reg = registriPerData.get(key);

            if (reg != null && reg.getPassi() > 0) {
                return String.valueOf(reg.getPassi());
            }
            return "";
        }, false);

        List<ElementoCatalogo> elementiUnici = ottieniElementiUnici(registrazioniConElementi);

        for (ElementoCatalogo elemento : elementiUnici) {
            addRow(elemento.getNome(), totalDays, i -> {
                java.util.Date dataColonna = CicloRepository.xDay(ciclo.getDataInizio(), i);

                boolean appare = esisteElementoNelGiorno(
                        registrazioniConElementi,
                        elemento.getElementoId(),
                        dataColonna
                );

                return appare ? "✕" : "";
            }, false);
        }
    }

    private Map<String, Registrazione> creaMappaRegistriPerData(List<Registrazione> registrazioni) {
        Map<String, Registrazione> mappa = new LinkedHashMap<>();
        for (Registrazione reg : registrazioni) {
            mappa.put(formattaChiaveData(reg.getData()), reg);
        }
        return mappa;
    }

    private String formattaChiaveData(java.util.Date data) {
        if (data == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault());
        return sdf.format(data);
    }

    private boolean esisteElementoNelGiorno(
            List<RegistrazioneConElementi> registri,
            int elementoId,
            java.util.Date data
    ) {
        if (registri == null) return false;

        java.util.Date dataTroncata = CicloRepository.truncarFecha(data);

        for (RegistrazioneConElementi rce : registri) {
            if (rce.registrazione == null) continue;

            java.util.Date regData = CicloRepository.truncarFecha(rce.registrazione.getData());
            if (!regData.equals(dataTroncata)) {
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

        return switch (fase) {
            case MESTRUALE -> "M";
            case FOLLICOLARE -> "F";
            case OVULATORIA -> "O";
            case LUTEALE -> "L";
        };
    }


    private List<ElementoCatalogo> ottieniElementiUnici(List<RegistrazioneConElementi> registri) {
        Map<Integer, ElementoCatalogo> mappa = new LinkedHashMap<>();

        if (registri == null) {
            return new ArrayList<>();
        }

        for (RegistrazioneConElementi rce : registri) {
            if (rce.elementiCatalogo == null) continue;

            for (ElementoCatalogo elemento : rce.elementiCatalogo) {
                mappa.put(elemento.getElementoId(), elemento);
            }
        }

        return new ArrayList<>(mappa.values());
    }

    private void addRow(String label, int totalDays, Function<Integer, String> cellProvider, boolean isBoldRow) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        // nome della riga
        TextView labelView = new TextView(getContext());
        labelView.setText(label);
        labelView.setGravity(Gravity.CENTER_VERTICAL);
        labelView.setTextSize(13);
        labelView.setSingleLine(true);
        labelView.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                dp(LABEL_WIDTH_DP),
                dp(CELL_HEIGHT_DP)
        );
        labelView.setLayoutParams(labelParams);
        row.addView(labelView);

        // celle dinamiche
        for (int i = 0; i < totalDays; i++) {
            TextView cell = new TextView(getContext());
            cell.setText(cellProvider.apply(i));
            cell.setGravity(Gravity.CENTER);
            cell.setTextSize(13);
            cell.setSingleLine(true);

            if (isBoldRow) {
                cell.setTypeface(null, Typeface.BOLD);
            }

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
