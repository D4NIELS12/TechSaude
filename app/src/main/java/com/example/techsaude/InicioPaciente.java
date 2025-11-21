package com.example.techsaude;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class InicioPaciente extends Fragment {

    public InicioPaciente() {
        super(R.layout.activity_inicio_paciente);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CardView Card_Vacinas = view.findViewById(R.id.cardVacinas);
        CardView Card_Consultas = view.findViewById(R.id.cardConsultas);
        CardView Card_Exame = view.findViewById(R.id.cardExames);

        Card_Consultas.setOnClickListener(v -> {
            Intent it = new Intent(getActivity(), AgendarConsultas.class);
            startActivity(it);
            SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs_agendamentos", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
        });
        Card_Exame.setOnClickListener(v -> {
            Intent it = new Intent(getActivity(), AgendarExames.class);
            startActivity(it);
            SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs_agendamentos", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
        });
        Card_Vacinas.setOnClickListener(v -> {
            Intent it = new Intent(getActivity(), AgendarVacinas.class);
            startActivity(it);
            SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs_agendamentos", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
        });
    }
}


