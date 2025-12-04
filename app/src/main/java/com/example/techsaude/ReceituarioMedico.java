package com.example.techsaude;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ReceituarioMedico extends AppCompatActivity {

    ImageView btnVoltar;

    LinearLayout PerfilReceituario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receituario_medico);

        btnVoltar = findViewById(R.id.btnVoltar);
        PerfilReceituario = findViewById(R.id.PerfilReceituario);

        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        PerfilReceituario.setOnClickListener(v -> {
            Intent it = new Intent(ReceituarioMedico.this, RealizarReceituario.class);
            startActivity(it);
        });


    }
}