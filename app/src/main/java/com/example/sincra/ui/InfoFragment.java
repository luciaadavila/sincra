package com.example.sincra.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recycler = view.findViewById(R.id.infoRecycler);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        List<InfoOption> options = new ArrayList<>();
        options.add(new InfoOption(getString(R.string.profilo)));
        options.add(new InfoOption(getString(R.string.stati_d_animo)));
        options.add(new InfoOption(getString(R.string.sintomi)));
        options.add(new InfoOption(getString(R.string.configurazione)));

        InfoAdapter adapter = new InfoAdapter(options, (option, position) -> {
            Fragment destino = null;

            switch (position) {
                case 0: // Profilo
                    destino = new ProfiloFragment();
                    break;
                case 1: // Stato d'animo
                    destino = new CatalogoEditableFragment();
                    Bundle argsMood = new Bundle();
                    argsMood.putString("tipo", "mood");
                    destino.setArguments(argsMood);
                    break;
                case 2: // Sintomi
                    destino = new CatalogoEditableFragment();
                    Bundle argsSintomi = new Bundle();
                    argsSintomi.putString("tipo", "symptom");
                    destino.setArguments(argsSintomi);
                    break;
                case 3: // Configurazione
                    destino = new ConfigurationFragment();
                    break;
            }

            if (destino != null) {
                getParentFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.fragment_container, destino)
                        .addToBackStack(null)
                        .commit();
            }
        });
        recycler.setAdapter(adapter);
    }
}
