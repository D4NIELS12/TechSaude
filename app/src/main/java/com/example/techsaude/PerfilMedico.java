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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

        String telefoneFormatado = formatarTelefone(telefone);
        try {
            // Formatadores
            SimpleDateFormat formatoEUA = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat formatoBR = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));

            // Converter
            Date data = formatoEUA.parse(nascimento);
            String dataBrasileira = formatoBR.format(data);
            tvNascimentoMedicoValor.setText("Nascimento: " + dataBrasileira);


        } catch (Exception e) {
            e.printStackTrace();
        }

        tvNomeMedicoValor.setText("Nome: " + medico);
        tvCpfMedicoValor.setText("CPF: " + cpf);
        tvEmailMedicoValor.setText("Email: " + email);
        tvCRMValor.setText("CRM: " + crm);
        tvTelefoneMedicoValor.setText("Telefone: " + telefoneFormatado);
        tvSexoMedicoValor.setText("Sexo: " + sexoEntenso);
        tvEspecalidadeMedicaValor.setText("Especialidade: " + especialidade);
    }
    public static String formatarTelefone(String telefone) {

        if (telefone.length() == 11) {
            // Celular
            return String.format("(%s) %s-%s",
                    telefone.substring(0, 2),        // DDD
                    telefone.substring(2, 7),        // 5 primeiros
                    telefone.substring(7, 11));      // últimos 4
        } else {
            return telefone; // Número fora do padrão
        }
    }
}