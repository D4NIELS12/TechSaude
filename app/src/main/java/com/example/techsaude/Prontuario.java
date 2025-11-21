package com.example.techsaude;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Prontuario extends Fragment {

    EditText editPeso, editAltura, editSintomas, editAlergias, editObservacoes, editAlertas;
    CheckBox checkDiabetes, checkHipertensao, checkAsma, checkColesterol, checkObesidade, checkNenhuma;
    MaterialButton btnSalvarProntuario;

    String URL_INSERIR   = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/inserir_prontuario.php";
    String URL_ATUALIZAR = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/atualizar_prontuario.php";
    String URL_VERIFICAR = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/verificar_prontuario.php?cpfUsuario=";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_prontuario, container, false);

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

        // Regras checkbox
        checkNenhuma.setOnCheckedChangeListener((b, checked) -> {
            if (checked) {
                checkDiabetes.setChecked(false);
                checkHipertensao.setChecked(false);
                checkAsma.setChecked(false);
                checkColesterol.setChecked(false);
                checkObesidade.setChecked(false);
            }
        });

        View.OnClickListener limparNenhuma = v -> checkNenhuma.setChecked(false);
        checkDiabetes.setOnClickListener(limparNenhuma);
        checkHipertensao.setOnClickListener(limparNenhuma);
        checkAsma.setOnClickListener(limparNenhuma);
        checkColesterol.setOnClickListener(limparNenhuma);
        checkObesidade.setOnClickListener(limparNenhuma);

        btnSalvarProntuario.setOnClickListener(v -> salvarProntuario());

        return view;
    }

    // =============================================
    // SALVAR PRONTUÁRIO (INSERT OU UPDATE)
    // =============================================
    private void salvarProntuario() {

        String pesoStr = editPeso.getText().toString().trim();
        String alturaStr = editAltura.getText().toString().trim();
        String sintomas = editSintomas.getText().toString().trim();
        String alergias = editAlergias.getText().toString().trim();
        String observacoes = editObservacoes.getText().toString().trim();
        String alertas = editAlertas.getText().toString().trim();
        String condicoes = getCondicoesSelecionadas();

        if (pesoStr.isEmpty() || alturaStr.isEmpty() || sintomas.isEmpty() || condicoes.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show();
            return;
        }

        double peso = Double.parseDouble(pesoStr);
        double altura = Double.parseDouble(alturaStr);

        SharedPreferences prefs = requireContext().getSharedPreferences("loginUsuario_prefs", getContext().MODE_PRIVATE);
        String cpfUsuario = prefs.getString("cpf", "");


        String data = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        try {
            JSONObject dados = new JSONObject();
            dados.put("cpfUsuario", cpfUsuario);
            dados.put("data_registro", data);
            dados.put("peso", peso);
            dados.put("altura", altura);
            dados.put("sintomas", sintomas);
            dados.put("alergias", alergias);
            dados.put("condicoes", condicoes);
            dados.put("observacoes", observacoes);
            dados.put("alertas", alertas);

            verificarProntuario(cpfUsuario,
                    existe -> {
                        if (existe) atualizarProntuario(dados);
                        else inserirProntuario(dados);
                    });

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // =============================================
    // VERIFICAR SE EXISTE PRONTUÁRIO
    // =============================================
    private void verificarProntuario(String idUsuario, Callback callback) {

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                URL_VERIFICAR + idUsuario,
                null,
                response -> {
                    try {
                        callback.onResult(response.getBoolean("existe"));
                    } catch (Exception e) {
                        callback.onResult(false);
                    }
                },
                error -> callback.onResult(false)
        );

        Volley.newRequestQueue(requireContext()).add(req);
    }

    interface Callback {
        void onResult(boolean existe);
    }

    // =============================================
    // INSERIR PRONTUÁRIO
    // =============================================
    private void inserirProntuario(JSONObject dados) {

        Limpar();
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                URL_INSERIR,
                dados,
                response -> Toast.makeText(requireContext(), "Prontuário criado com sucesso!", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(requireContext(), "Erro ao criar prontuário.", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(requireContext()).add(req);
    }

    // =============================================
    // ATUALIZAR PRONTUÁRIO
    // =============================================
    private void atualizarProntuario(JSONObject dados) {

        Limpar();
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                URL_ATUALIZAR,
                dados,
                response -> Toast.makeText(requireContext(), "Prontuário atualizado!", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(requireContext(), "Erro ao atualizar prontuário.", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(requireContext()).add(req);
    }

    // =============================================
    // MONTAR TEXTO DAS CONDIÇÕES
    // =============================================
    private String getCondicoesSelecionadas() {
        StringBuilder c = new StringBuilder();

        if (checkNenhuma.isChecked()) return "Nenhuma";

        if (checkDiabetes.isChecked())     c.append("Diabetes, ");
        if (checkHipertensao.isChecked())  c.append("Hipertensão, ");
        if (checkAsma.isChecked())         c.append("Asma, ");
        if (checkColesterol.isChecked())   c.append("Colesterol Alto, ");
        if (checkObesidade.isChecked())    c.append("Obesidade, ");

        if (c.length() > 0)
            c.setLength(c.length() - 2);

        return c.toString();
    }
    private void Limpar() {
        checkAsma.setChecked(false);
        checkHipertensao.setChecked(false);
        checkObesidade.setChecked(false);
        checkDiabetes.setChecked(false);
        checkColesterol.setChecked(false);
        checkNenhuma.setChecked(false);
        editPeso.setText(null);
        editAltura.setText(null);
        editSintomas.setText(null);
        editAlergias.setText(null);
        editObservacoes.setText(null);
        editAlertas.setText(null);
    }
}
