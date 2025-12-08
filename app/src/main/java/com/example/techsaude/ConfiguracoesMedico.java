package com.example.techsaude;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ConfiguracoesMedico extends Fragment {

    TextView txtAlterarSenhaMedico;

    public ConfiguracoesMedico() {
        super(R.layout.activity_configuracoes_medico);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtAlterarSenhaMedico = view.findViewById(R.id.txtAlterarSenhaMedico);

        txtAlterarSenhaMedico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getActivity(), AlterarSenhaConfiguracoesMedico.class);
                startActivity(it);
            }
        });

    }
}