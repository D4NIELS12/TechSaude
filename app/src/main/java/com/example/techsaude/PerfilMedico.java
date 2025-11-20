package com.example.techsaude;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PerfilMedico extends Fragment {

    public PerfilMedico() {
        super(R.layout.activity_perfil_medico); // usa o layout que você criou
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvNomeMedicoValor = view.findViewById(R.id.tvMedicoValor);
        TextView tvCpfMedicoValor = view.findViewById(R.id.tvCpfMedicoValor);
        TextView tvEmailMedicoValor = view.findViewById(R.id.tvEmailMedicoValor);
        TextView tvNascimentoMedicoValor = view.findViewById(R.id.tvNascimentoMedicoValor);
        TextView tvCRMValor = view.findViewById(R.id.tvCRMValor);
        TextView tvTelefoneMedicoValor = view.findViewById(R.id.tvTelefoneMedicoValor);
        TextView tvSexoMedicoValor = view.findViewById(R.id.tvSexoMedicoValor);
        TextView tvEspecalidadeMedicaValor = view.findViewById(R.id.tvEspecialidadeMedicaValor);

        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs_medico", requireContext().MODE_PRIVATE);

        String medico = prefs.getString("nome", "Não informado");
        String cpf = prefs.getString("cpf", "Não informado");
        String email = prefs.getString("email", "Não informado");
        String nascimento = prefs.getString("data_nasc", "Não informado");
        String crm = prefs.getString("crm", "Não informado");
        String especialidade = prefs.getString("especialidade", "Não informado");
        String telefone = prefs.getString("telefone", "Não informado");
        String sexo = prefs.getString("sexo", "Não informado");

        String sexoEntenso = "";

        if (sexo.equals("M")) {
            sexoEntenso = "Masculino";
        } else if (sexo.equals("F")) {
            sexoEntenso = "Feminino";
        } else {
            sexoEntenso = " sexo não informado";
        }

        tvNomeMedicoValor.setText(medico);
        tvCpfMedicoValor.setText(cpf);
        tvEmailMedicoValor.setText(email);
        tvNascimentoMedicoValor.setText(nascimento);
        tvCRMValor.setText(crm);
        tvTelefoneMedicoValor.setText(telefone);
        tvSexoMedicoValor.setText(sexoEntenso);
        tvEspecalidadeMedicaValor.setText(especialidade);



    }
}