package com.example.techsaude;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

public class MedicoLogado extends AppCompatActivity
{
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private MaterialToolbar toolbar;
    TextView nav_sair, txtNomeMedicoNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medico_logado);

        drawerLayout = findViewById(R.id.drawerLayout);
        navView = findViewById(R.id.navView);
        toolbar = findViewById(R.id.toolbar);
        nav_sair = (TextView) findViewById(R.id.nav_sair);

        View headerView = navView.getHeaderView(0);
        txtNomeMedicoNav = headerView.findViewById(R.id.txtNomeNav);

        // 🔹 Recuperar o nome do usuário do SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs_medico", MODE_PRIVATE);
        String nome = prefs.getString("nome", "Médico");
        if (nome != null && !nome.isEmpty()) {
            String primeiroNome = nome.split(" ")[0]; // pega o que vem antes do primeiro espaço
            txtNomeMedicoNav.setText("Dr. " + primeiroNome);
        } else {
            txtNomeMedicoNav.setText("Médico");
        }

        nav_sair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sair(view);
            }
        });
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_inicio_medico) {
                replaceFragment(new InicioMedico(), "Ínicio");
            } else if (id == R.id.nav_Perfil_medico){
                replaceFragment(new PerfilMedico(), "Perfil");
            }else if (id == R.id.nav_Configurações_medico) {
                replaceFragment(new ConfiguracoesMedico(), "Configurações");
            }


            drawerLayout.closeDrawers();
            return true;
        });


        // Primeira tela
        if (savedInstanceState == null) {
            navView.setCheckedItem(R.id.nav_inicio_medico);
            replaceFragment(new InicioMedico(), "Ínicio");
        }
    }

    private void sair(View view) {
        logout();
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogStyle);

        View layout = getLayoutInflater().inflate(R.layout.dialog_logout, null);
        builder.setView(layout);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        layout.findViewById(R.id.btnCancelar).setOnClickListener(v -> dialog.dismiss());

        layout.findViewById(R.id.btnSair).setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("user_prefs_medico", MODE_PRIVATE);
            prefs.edit().clear().apply();

            startActivity(new Intent(MedicoLogado.this, LoginPaciente.class));
            finish();
        });

        dialog.show();
    }


    private void replaceFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment, tag)
                .commit();
    }


    public  void ClickMenu(View view) {opeDrawer(drawerLayout);}

    private void opeDrawer(DrawerLayout drawerLayout){
        drawerLayout.openDrawer(GravityCompat.START);
    }
}