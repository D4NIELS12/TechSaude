package com.example.techsaude;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class InicioMedico extends Fragment {

    public InicioMedico() {
        // Construtor padrão
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflando o layout do fragmento
        View view = inflater.inflate(R.layout.activity_inicio_medico, container, false);

        // Referência ao CardView do XML
        View cardAgenda = view.findViewById(R.id.cardCalendario);

        // Clique para abrir a Agenda Médica
        cardAgenda.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent it = new Intent(getActivity(), AgendaMedica.class);
                startActivity(it);
            }
        });

        return view;
    }
}
