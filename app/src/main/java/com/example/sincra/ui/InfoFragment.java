package com.example.sincra.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sincra.R;
import com.example.sincra.adapter.InfoAdapter;
import com.example.sincra.model.InfoOption;

import java.util.ArrayList;
import java.util.List;


public class InfoFragment extends Fragment {

    public InfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        RecyclerView recycler = view.findViewById(R.id.infoRecycler);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        List<InfoOption> options = new ArrayList<>();
        options.add(new InfoOption("Profilo"));
        options.add(new InfoOption("Stato d'animo"));
        options.add(new InfoOption("Sintomi"));
        options.add(new InfoOption("Configurazione"));

        InfoAdapter adapter = new InfoAdapter(options, (option, position) -> {
            Fragment destino = null;

            switch (position) {
                case 0: // Perfil
                    destino = new ProfiloFragment();
                    break;
                case 1: // Stato d'animo
                    destino = new CatalogoEditableFragment();
                    Bundle argsMood = new Bundle();
                    argsMood.putString("tipo", "mood"); // Indicamos que es para estados de ánimo
                    destino.setArguments(argsMood);
                    break;
                case 2: // Sintomi
                    destino = new CatalogoEditableFragment();
                    Bundle argsSintomi = new Bundle();
                    argsSintomi.putString("tipo", "symptom"); // O el string exacto que use tu base de datos
                    destino.setArguments(argsSintomi);
                    break;
                case 3: // Configuración
                    // destino = new ConfigurationFragment();
                    break;
            }

            // Si definiste un fragmento de destino, navegamos de forma segura
            if (destino != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, destino)
                        .addToBackStack(null)
                        .commit();
            }
        });
        recycler.setAdapter(adapter);

        return view;
    }
}
