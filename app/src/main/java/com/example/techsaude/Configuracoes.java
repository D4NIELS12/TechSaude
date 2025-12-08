package com.example.techsaude;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

public class Configuracoes extends Fragment {

    TextView txtAlterarSenha, txtAlteraDadosPessoais;

    public Configuracoes() {
        super(R.layout.activity_configuracoes);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtAlterarSenha = view.findViewById(R.id.txtAlterarSenha);
        txtAlteraDadosPessoais = view.findViewById(R.id.txtAlterarDadosPessoais);

        txtAlterarSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getActivity(), AlterarSenhaConfiguracoes.class);
                startActivity(it);
            }
        });
        txtAlteraDadosPessoais.setOnClickListener(v -> {
            Intent it = new Intent(getActivity(), AlterarDadosPessoaisPaciente.class);
            startActivity(it);
        });

    }
}
