package com.example.techsaude;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;



public class PacienteLogado extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private MaterialToolbar toolbar;
    TextView nav_sair, txtNomePacienteNav, txtNomeDaTela;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paciente_logado);

        drawerLayout = findViewById(R.id.drawerLayout);
        navView = findViewById(R.id.navView);
        toolbar = findViewById(R.id.toolbar);
        nav_sair = (TextView) findViewById(R.id.nav_sair);
        txtNomeDaTela = (TextView) findViewById(R.id.txtNomeDaTela);

        // ⚠️ IMPORTANTE: acessar o header APÓS inicializar o navView
        View headerView = navView.getHeaderView(0);
        txtNomePacienteNav = headerView.findViewById(R.id.txtNomeNav);

        // 🔹 Recuperar o nome do usuário do SharedPreferences
        SharedPreferences prefs = getSharedPreferences("loginUsuario_prefs", MODE_PRIVATE);
        String nome = prefs.getString("nome", "Usuário");
        if (nome != null && !nome.isEmpty()) {
            String primeiroNome = nome.split(" ")[0]; // pega o que vem antes do primeiro espaço
            txtNomePacienteNav.setText(primeiroNome);
        } else {
            txtNomePacienteNav.setText("Usuário");
        }

        nav_sair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sair(view);
            }
        });

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_inicio) {
                txtNomeDaTela.setText("Ínicio");
                  replaceFragment(new InicioPaciente(), "Ínicio");
            } else if (id == R.id.nav_Perfil){
                txtNomeDaTela.setText("Perfil");
                replaceFragment(new Perfil(), "Perfil");
            } else if (id == R.id.nav_Area_Paciente) {
                txtNomeDaTela.setText("Área do paciente");
                replaceFragment(new AreaPaciente(), "Área do paciente");
            } else if (id == R.id.nav_Prontuario) {
                txtNomeDaTela.setText("Prontuário");
                replaceFragment(new Prontuario(), "Prontuário");
            }else if (id == R.id.nav_Agendamentos) {
                txtNomeDaTela.setText("Agendamentos");
                replaceFragment(new Agendamentos(), "Agendamentos");
            }else if (id == R.id.nav_Configurações) {
                txtNomeDaTela.setText("Configurações");
                replaceFragment(new Configuracoes(), "Configurações");
            }


            drawerLayout.closeDrawers();
            return true;
        });



        // Primeira tela
        if (savedInstanceState == null) {
            navView.setCheckedItem(R.id.nav_inicio);
            txtNomeDaTela.setText("Ínicio");
            replaceFragment(new InicioPaciente(), "Ínicio");
        }
    }

    private void sair(View view) {
        logout(PacienteLogado.this);
    }
    private void logout(PacienteLogado pacienteLogado) {
        AlertDialog.Builder builder = new AlertDialog.Builder(pacienteLogado);
        builder.setTitle("Sair");
        builder.setMessage("Certeza que deseja sair ?");
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Apaga todos os dados salvos no SharedPreferences
                SharedPreferences prefs = getSharedPreferences("loginUsuario_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear(); // limpa tudo
                editor.apply();
                Intent it = new Intent(PacienteLogado.this, LoginPaciente.class);
                startActivity(it);
                finish();
            }
        });
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();
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
