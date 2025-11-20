package com.example.techsaude;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

public class Configuracoes extends Fragment {

    Switch switchTema, switchNotificacoes;
    Spinner spinnerIdioma;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    public Configuracoes() {
        super(R.layout.activity_configuracoes);
    }

    /*@Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switchTema = view.findViewById(R.id.switchTema);
        switchNotificacoes = view.findViewById(R.id.switchNotificacoes);
        spinnerIdioma = view.findViewById(R.id.spinnerIdioma);

        prefs = requireActivity().getSharedPreferences("configuracoes", getContext().MODE_PRIVATE);
        editor = prefs.edit();

        boolean modoEscuroAtivo = prefs.getBoolean("modoEscuro", false);
        boolean notificacoesAtivas = prefs.getBoolean("notificacoes", true);
        int idiomaSelecionado = prefs.getInt("idioma", 0);

        switchTema.setChecked(modoEscuroAtivo);
        switchNotificacoes.setChecked(notificacoesAtivas);
        spinnerIdioma.setSelection(idiomaSelecionado);

        // 🌙 Modo escuro
        switchTema.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("modoEscuro", isChecked);
            editor.apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Toast.makeText(getContext(), "Modo escuro ativado", Toast.LENGTH_SHORT).show();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Toast.makeText(getContext(), "Modo claro ativado", Toast.LENGTH_SHORT).show();
            }
        });

        // 🔔 Notificações
        switchNotificacoes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("notificacoes", isChecked);
            editor.apply();
            Toast.makeText(getContext(), isChecked ? "Notificações ativadas" : "Notificações desativadas", Toast.LENGTH_SHORT).show();
        });

        // 🌐 Idioma
        spinnerIdioma.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                editor.putInt("idioma", position);
                editor.apply();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }*/
}
