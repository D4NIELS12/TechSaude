package com.example.techsaude;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

    private static final String URL_SALVAR_EXAME =
            "http://tcc3edsmodetecgr3.hospedagemdesites.ws/salvar_exame.php";

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
        btnConfirmar.setOnClickListener(v -> {

            boolean eConsulta = prefsAgendamento.contains("especialidadeConsulta");
            boolean eExame = prefsAgendamento.contains("exame");

            if (eConsulta) {
                confirmarESalvarConsulta();
            } else if (eExame) {
                confirmarESalvarExame();
            } else {
                Toast.makeText(this, "Nenhuma informação encontrada", Toast.LENGTH_SHORT).show();
            }

        });
    }


    private void buscarIdMedico(String nomeMedico, Callback callback) {

        String url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/buscar_id_medico.php?nome=" + nomeMedico.replace(" ", "%20");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            int idMedico = response.getInt("idMedico");
                            callback.onSuccess(idMedico);
                        } else {
                            Toast.makeText(this, "Médico não encontrado", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Erro ao buscar médico", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    interface Callback {
        void onSuccess(int idMedico);
    }


    private void confirmarESalvarExame() {

        String exame = prefsAgendamento.getString("exame", "");
        String medico = prefsAgendamento.getString("medicoExame", "");
        String dia = prefsAgendamento.getString("dataExame", "");
        String hora = prefsAgendamento.getString("horarioExame", "");
        String valor = prefsAgendamento.getString("valorExame", "");
        String status = prefsAgendamento.getString("statusExame", "");

        String mensagem = "Exame: " + exame +
                "\nMédico: " + medico +
                "\nData: " + dia +
                "\nHorário: " + hora +
                "\nValor: R$ " + valor;

        new AlertDialog.Builder(this)
                .setTitle("Confirmar Exame")
                .setMessage(mensagem)
                .setPositiveButton("Confirmar", (dialog, which) -> {

                    // Buscar ID do médico antes de salvar
                    buscarIdMedico(medico, idMedico -> {
                        salvarExameNoServidor(exame, idMedico, dia, hora, status, valor);
                    });

                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void confirmarESalvarConsulta() {
        // Pegando dados do SharedPreferences
        String especialidade = prefsAgendamento.getString("especialidadeConsulta", "");
        String medico = prefsAgendamento.getString("medicoConsulta", "");
        String dia = prefsAgendamento.getString("dataConsulta", "");
        String hora = prefsAgendamento.getString("horarioConsulta", "");
        String status = prefsAgendamento.getString("statusConsulta", "");
        String valor = prefsAgendamento.getString("valorConsulta", "120.00");

        // Montar mensagem de confirmação
        String mensagem = "Especialidade: " + especialidade +
                "\nMédico: " + medico +
                "\nData: " + dia +
                "\nHorário: " + hora +
                "\nValor: R$ " + valor;

        Log.d("DEBUG_MEDICO", "Nome enviado para buscar id: " + medico);

        // AlertDialog
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Consulta")
                .setMessage(mensagem)
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    buscarIdMedico(medico, idMedico -> {
                        salvarConsultaNoServidor(especialidade, idMedico, hora, status, valor);
                    });
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private String converterDataParaMysql(String dataBR) {
        try {
            String[] partes = dataBR.split("/");
            String dia = partes[0];
            String mes = partes[1];
            String ano = partes[2];
            return ano + "-" + mes + "-" + dia;
        } catch (Exception e) {
            e.printStackTrace();
            return dataBR; // evita crash
        }
    }

    private void salvarConsultaNoServidor(String especialidade, int idMedico, String horario, String status, String valor) {

        int idUsuario = prefsUsuario.getInt("idUsuario", 0);
        String dataBR = prefsAgendamento.getString("dataConsulta", "");
        String data = converterDataParaMysql(dataBR);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("idUsuario", idUsuario);
            jsonBody.put("idMedico", idMedico);
            jsonBody.put("especialidadeConsulta", especialidade);
            jsonBody.put("dataConsulta", data);
            jsonBody.put("horarioConsulta", horario);
            jsonBody.put("statusConsulta", status);
            jsonBody.put("valorConsulta", valor);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String URL = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/salvar_consulta.php";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                jsonBody,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                        if (success) finish();
                    } catch (Exception e) { e.printStackTrace(); }
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String resposta = new String(error.networkResponse.data);
                        Log.e("ERRO_SERVIDOR", "Resposta: " + resposta);
                    } else {
                        Log.e("ERRO_SERVIDOR", "Erro sem resposta do servidor: " + error.toString());
                    }

                    Toast.makeText(this, "Erro no servidor", Toast.LENGTH_SHORT).show();
                }        );

        Volley.newRequestQueue(this).add(request);
    }

    private void salvarExameNoServidor(String exame, int idMedico, String dia, String hora, String status, String valor) {

        int idUsuario = prefsUsuario.getInt("idUsuario", 0);
        String data = converterDataParaMysql(dia);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("idUsuario", idUsuario);
            jsonBody.put("idMedico", idMedico);
            jsonBody.put("tipoExame", exame);
            jsonBody.put("dataExame", data);
            jsonBody.put("horarioExame", hora);
            jsonBody.put("statusExame", status);
            jsonBody.put("valorExame", valor);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL_SALVAR_EXAME,
                jsonBody,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                        if (success) finish();

                    } catch (Exception e) { e.printStackTrace();
                    }
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String resposta = new String(error.networkResponse.data);
                        Log.e("ERRO_SERVIDOR", "Resposta: " + resposta);
                    } else {
                        Log.e("ERRO_SERVIDOR", "Erro sem resposta do servidor: " + error.toString());
                    }

                    Toast.makeText(this, "Erro no servidor", Toast.LENGTH_SHORT).show();
                }

        );

        Volley.newRequestQueue(this).add(request);
    }

}
