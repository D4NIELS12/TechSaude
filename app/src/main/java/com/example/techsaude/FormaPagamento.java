package com.example.techsaude;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class FormaPagamento extends AppCompatActivity {

    private RadioGroup rgOpcoes;
    private Button btnConfirmar;
    private TextView txtValor;
    private ImageView imgVoltar;

    private SharedPreferences prefsAgendamento;
    private SharedPreferences prefsUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forma_pagamento);

        // Inicializar componentes
        btnConfirmar = findViewById(R.id.btnConfirmar);
        rgOpcoes = findViewById(R.id.rgOpcoes);
        txtValor = findViewById(R.id.txtValorPagamento);
        imgVoltar = findViewById(R.id.imgVoltar);

        // Inicializar SharedPreferences **dentro do onCreate**
        prefsAgendamento = getSharedPreferences("user_prefs_agendamentos", Context.MODE_PRIVATE);
        prefsUsuario = getSharedPreferences("loginUsuario_prefs", Context.MODE_PRIVATE);

        // Voltar
        imgVoltar.setOnClickListener(v -> finish());

        // Mostrar valor da consulta
        String valor = prefsAgendamento.getString("valorConsulta", "0.00");
        txtValor.setText("R$: " + valor);

        // Confirmar pagamento
        btnConfirmar.setOnClickListener(v -> confirmarESalvarConsulta());
    }

    private void confirmarESalvarConsulta() {
        // Pegando dados do SharedPreferences
        String especialidade = prefsAgendamento.getString("especialidadeConsulta", "");
        String medico = prefsAgendamento.getString("medicoConsulta", "");
        String data = prefsAgendamento.getString("dataConsulta", "");
        String hora = prefsAgendamento.getString("horarioConsulta", "");
        String status = prefsAgendamento.getString("statusConsulta", "PENDENTE");
        String valor = prefsAgendamento.getString("valorConsulta", "120.00");

        // Montar mensagem de confirmação
        String mensagem = "Especialidade: " + especialidade +
                "\nMédico: " + medico +
                "\nData: " + data +
                "\nHorário: " + hora +
                "\nValor: R$ " + valor;

        // AlertDialog
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Consulta")
                .setMessage(mensagem)
                .setPositiveButton("Confirmar", (dialog, which) -> salvarConsultaNoServidor(especialidade, medico, data, hora, status, valor))
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void salvarConsultaNoServidor(String especialidade, String medico, String data, String horario, String status, String valor) {

        int idUsuario = prefsUsuario.getInt("idUsuario", 0);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("idUsuario", idUsuario);
            jsonBody.put("especialidadeConsulta", especialidade);
            jsonBody.put("medicoConsulta", medico);
            jsonBody.put("dataConsulta", data);
            jsonBody.put("horarioConsulta", horario);
            jsonBody.put("statusConsulta", status);
            jsonBody.put("valorConsulta", valor);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String URL_SALVAR_CONSULTA = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/salvar_consulta.php";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL_SALVAR_CONSULTA,
                jsonBody,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");

                        Toast.makeText(FormaPagamento.this, message, Toast.LENGTH_LONG).show();

                        if(success){
                            // Vai para outra tela, por exemplo MainActivity ou Tela de Agendamentos
                            Intent it = new Intent(FormaPagamento.this, PacienteLogado.class);
                            startActivity(it);
                            finish(); // fecha a tela atual
                        }

                    } catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(FormaPagamento.this, "Erro ao processar resposta.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(FormaPagamento.this, "Erro na requisição ao servidor.", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }
}
