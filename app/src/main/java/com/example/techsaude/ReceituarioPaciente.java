package com.example.techsaude;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class ReceituarioPaciente extends Fragment {

    CardView cardVerReceituario;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_receituario_paciente, container, false);

        cardVerReceituario = view.findViewById(R.id.cardVerReceituario);

        cardVerReceituario.setOnClickListener(v -> {
            Intent it = new Intent(requireActivity(), ReceituarioRealizados.class);
            startActivity(it);
        });

        return view;
    }
}
