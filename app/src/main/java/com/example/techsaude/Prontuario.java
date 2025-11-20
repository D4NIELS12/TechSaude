package com.example.techsaude;

import static android.content.Context.MODE_PRIVATE;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Prontuario extends Fragment {

    EditText editPeso, editAltura, editSintomas, editAlergias, editObservacoes, editAlertas;
    CheckBox checkDiabetes, checkHipertensao, checkAsma, checkColesterol, checkObesidade, checkNenhuma;
    MaterialButton btnSalvarProntuario;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflar o layout
        View view = inflater.inflate(R.layout.activity_prontuario, container, false);

        // Inicializar os campos usando a View
        editPeso = view.findViewById(R.id.editPeso);
        editAltura = view.findViewById(R.id.editAltura);
        editSintomas = view.findViewById(R.id.editSintomas);
        editAlergias = view.findViewById(R.id.editAlergias);
        editObservacoes = view.findViewById(R.id.editObservacoes);
        editAlertas = view.findViewById(R.id.editAlertas);

        checkDiabetes = view.findViewById(R.id.checkDiabetes);
        checkHipertensao = view.findViewById(R.id.checkHipertensao);
        checkAsma = view.findViewById(R.id.checkAsma);
        checkColesterol = view.findViewById(R.id.checkColesterol);
        checkObesidade = view.findViewById(R.id.checkObesidade);
        checkNenhuma = view.findViewById(R.id.checkNenhuma);

        btnSalvarProntuario = view.findViewById(R.id.btnSalvarProntuario);

        checkNenhuma.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkDiabetes.setChecked(false);
                checkHipertensao.setChecked(false);
                checkAsma.setChecked(false);
                checkColesterol.setChecked(false);
                checkObesidade.setChecked(false);
            }
        });

        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            if (isChecked) {
                checkNenhuma.setChecked(false);
            }
        };

        checkDiabetes.setOnCheckedChangeListener(listener);
        checkHipertensao.setOnCheckedChangeListener(listener);
        checkAsma.setOnCheckedChangeListener(listener);
        checkColesterol.setOnCheckedChangeListener(listener);
        checkObesidade.setOnCheckedChangeListener(listener);


        // Ação do botão salvar
        btnSalvarProntuario.setOnClickListener(v -> inserirOuAtualizarProntuario());

        return view;
    }

    private void inserirOuAtualizarProntuario() {
        // Captura os valores digitados
        String alturaStr = editAltura.getText().toString().trim();
        String pesoStr = editPeso.getText().toString().trim();
        String sintomas = editSintomas.getText().toString().trim();
        String alergias = editAlergias.getText().toString().trim();
        String observacoes = editObservacoes.getText().toString().trim();
        String alertas = editAlertas.getText().toString().trim();
        String condicoes = getCondicoesSelecionadas();

        // Validação básica
        if (alturaStr.isEmpty() || pesoStr.isEmpty() || sintomas.isEmpty() || condicoes.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha os campos obrigatórios: altura, peso, Condições e sintomas.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double altura = Double.parseDouble(alturaStr);
            double peso = Double.parseDouble(pesoStr);

            // Recupera o CPF do usuário logado
            SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String cpfUsuario = prefs.getString("cpfUsuario", "");

            if (cpfUsuario.isEmpty()) {
                Toast.makeText(requireContext(), "Erro: CPF do usuário não encontrado.", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
            String dataRegistro = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

            // ✅ Verifica se o prontuário já existe
            if (dbHelper.existeProntuario(cpfUsuario)) {
                // Atualiza o prontuário existente
                boolean sucesso = dbHelper.atualizarProntuario(
                        cpfUsuario,
                        peso,
                        altura,
                        sintomas,
                        alergias,
                        condicoes,
                        observacoes,
                        alertas
                );

                Toast.makeText(requireContext(),
                        sucesso ? "Prontuário atualizado com sucesso!" : "Erro ao atualizar prontuário.",
                        Toast.LENGTH_SHORT).show();

            } else {
                // Insere um novo prontuário
                boolean sucesso = dbHelper.inserirProntuario(
                        cpfUsuario,
                        dataRegistro,
                        peso,
                        altura,
                        sintomas,
                        alergias,
                        condicoes,
                        observacoes,
                        alertas
                );

                Toast.makeText(requireContext(),
                        sucesso ? "Prontuário criado com sucesso!" : "Erro ao criar prontuário.",
                        Toast.LENGTH_SHORT).show();

                if (sucesso) limparCampos();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Verifique os valores de peso e altura.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Erro inesperado: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }



    private String getCondicoesSelecionadas() {
        StringBuilder condicoes = new StringBuilder();

        if (checkNenhuma.isChecked()) {
            return "Nenhuma das opções";
        }

        if (checkDiabetes.isChecked()) condicoes.append("Diabetes, ");
        if (checkHipertensao.isChecked()) condicoes.append("Hipertensão, ");
        if (checkAsma.isChecked()) condicoes.append("Asma, ");
        if (checkColesterol.isChecked()) condicoes.append("Colesterol Alto, ");
        if (checkObesidade.isChecked()) condicoes.append("Obesidade, ");

        // Remove a última vírgula e espaço, se houver
        if (condicoes.length() > 0)
            condicoes.setLength(condicoes.length() - 2);

        return condicoes.toString();
    }


    private void limparCampos() {
        editPeso.setText("");
        editAltura.setText("");
        editSintomas.setText("");
        editAlergias.setText("");
        editObservacoes.setText("");
        editAlertas.setText("");

        checkDiabetes.setChecked(false);
        checkHipertensao.setChecked(false);
        checkAsma.setChecked(false);
        checkColesterol.setChecked(false);
        checkObesidade.setChecked(false);
        checkNenhuma.setChecked(false);
    }
}
