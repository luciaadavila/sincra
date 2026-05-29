package com.example.sincra;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.example.sincra.database.AppDatabase;
import com.example.sincra.ui.EstadisticaFragment;
import com.example.sincra.ui.HistorialFragment;
import com.example.sincra.ui.HomeFragment;
import com.example.sincra.ui.InfoFragment;
import com.example.sincra.ui.PredictFragment;
import com.example.sincra.ui.RegistroFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.sincra.ui.DetailDayFragment;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        replaceFragment(new HomeFragment());

        bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                replaceFragment(new HomeFragment());
            }

            else if (item.getItemId() == R.id.nav_historial) {
                replaceFragment(new HistorialFragment());
            }

            else if (item.getItemId() == R.id.nav_prediction){
                replaceFragment(new PredictFragment());
            }

            else if (item.getItemId() == R.id.nav_registro) {
                replaceFragment(new RegistroFragment());
            }

            else if (item.getItemId() == R.id.nav_stats) {
                replaceFragment(new EstadisticaFragment());
            }

            else if (item.getItemId() == R.id.nav_info) {
                replaceFragment(new InfoFragment());
            }
            return true;
        });


    }


    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }
}

