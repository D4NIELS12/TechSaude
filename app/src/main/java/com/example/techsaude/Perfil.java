package com.example.techsaude;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class Perfil extends Fragment {

    private TextView lblNome, lblCPF, lblEmail, lblNascimento,
            lblEndereco, lblTelefone, lblSexo;

    public Perfil() {
        super(R.layout.activity_perfil);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lblNome = view.findViewById(R.id.lblNome);
        lblCPF = view.findViewById(R.id.lblCPF);
        lblEmail = view.findViewById(R.id.lblEmail);
        lblNascimento = view.findViewById(R.id.lblNascimento);
        lblEndereco = view.findViewById(R.id.lblEndereco);
        lblTelefone = view.findViewById(R.id.lblTelefone);
        lblSexo = view.findViewById(R.id.lblSexo);

        lblNascimento.setFilters(new InputFilter[] { new InputFilter.LengthFilter(10)});

        SharedPreferences prefs = requireActivity().getSharedPreferences("loginUsuario_prefs", Context.MODE_PRIVATE);

        String nome = prefs.getString("nome", "Nome não encontrado");
        String cpf = prefs.getString("cpfUsuario", "CPF não encontrado");
        String email = prefs.getString("email", "Email não encontrado");
        String nascimento = prefs.getString("data_nasc", "Data não encontrada");
        String endereco = prefs.getString("endereco", "Endereço não encontrado");
        String telefone = prefs.getString("telefone", "Telefone não encontrado");
        String sexo = prefs.getString("sexo", "Sexo não informado");

        String sexoExtenso = "";

        if (sexo.equals("M")) {
            sexoExtenso = "Masculino";
        } else if (sexo.equals("F")) {
            sexoExtenso = "Feminino";
        } else {
            sexoExtenso = "Sexo não informado";
        }


        // 🔹 Exibe nos TextViews
        lblNome.setText(nome);
        lblCPF.setText(cpf);
        lblEmail.setText(email);
        lblNascimento.setText(nascimento);
        lblEndereco.setText(endereco);
        lblTelefone.setText(telefone);
        lblSexo.setText(sexoExtenso);
    }
}
