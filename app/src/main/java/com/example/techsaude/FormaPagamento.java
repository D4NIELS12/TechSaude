package com.example.techsaude;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.sql.Time;
import java.time.LocalTime;

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
        String valor = prefsAgendamento.getString("valor", "0.00");
        txtValor.setText(valor);


        // Confirmar pagamento
        btnConfirmar.setOnClickListener(v -> {

            boolean eConsulta = prefsAgendamento.contains("especialidadeConsulta");
            boolean eExame = prefsAgendamento.contains("exame");
            boolean eVacina = prefsAgendamento.contains("vacina");

            if (eConsulta) {
                confirmarESalvarConsulta();
            } else if (eExame) {
                confirmarESalvarExame();
            }else if (eVacina) {
                confirmarESalvarVacina();
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

    private void mostrarVacinaDialog(String tipo, String data, String horario, String valor) {

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_vacina, null);

        TextView txtDialogTipo = dialogView.findViewById(R.id.txtTipo);
        TextView txtDialogData = dialogView.findViewById(R.id.txtData);
        TextView txtDialogHorario = dialogView.findViewById(R.id.txtHorario);
        TextView txtDialogValor = dialogView.findViewById(R.id.txtValor);
        Button btnConfirmar = dialogView.findViewById(R.id.btnConfirmar);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);

        txtDialogTipo.setText("Tipo: " + tipo);
        txtDialogData.setText("Data: " + data);
        txtDialogHorario.setText("Horário: " + horario);
        txtDialogValor.setText("Valor: R$" + valor);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnConfirmar.setOnClickListener(v -> {
            dialog.dismiss();


            salvarVacinaNoServidor(tipo, horario, valor,"Agendado");
            Intent it = new Intent(FormaPagamento.this, PacienteLogado.class);
            startActivity(it);
            finish();
        });

        dialog.show();
    }
    private void confirmarESalvarVacina() {
        String vacina = prefsAgendamento.getString("vacina", "");
        String data_vacina = prefsAgendamento.getString("data_vacina", "");
        String hora_vacina = prefsAgendamento.getString("hora_vacina","");
        String valor_vacina = prefsAgendamento.getString("valor","");
        String dataBR = converterDataBR(data_vacina);
        mostrarVacinaDialog(vacina, dataBR, hora_vacina, valor_vacina);
    }

    private void mostrarExameDialog(String tipo, String medico, String data, String horario, String valor) {

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_agendamento, null);

        TextView txtDialogTipo = dialogView.findViewById(R.id.txtTipo);
        TextView txtDialogMedico = dialogView.findViewById(R.id.txtMedico);
        TextView txtDialogData = dialogView.findViewById(R.id.txtData);
        TextView txtDialogHorario = dialogView.findViewById(R.id.txtHorario);
        TextView txtDialogValor = dialogView.findViewById(R.id.txtValor);
        Button btnConfirmar = dialogView.findViewById(R.id.btnConfirmar);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);

        txtDialogTipo.setText("Exame: " + tipo);
        txtDialogMedico.setText("Médico: " + medico);
        txtDialogData.setText("Data: " + data);
        txtDialogHorario.setText("Horário: " + horario);
        txtDialogValor.setText("Valor: R$" + valor);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnConfirmar.setOnClickListener(v -> {
            dialog.dismiss();

            buscarIdMedico(medico, idMedico -> {
                salvarExameNoServidor(tipo, idMedico, data, horario, "Agendado", valor);
            });

            Intent it = new Intent(FormaPagamento.this, PacienteLogado.class);
            startActivity(it);
            finish();
        });

        dialog.show();
    }

    private void confirmarESalvarExame() {
        String exame = prefsAgendamento.getString("exame", "");
        String medico = prefsAgendamento.getString("medicoExame", "");
        String dia = prefsAgendamento.getString("dataExame", "");
        String hora = prefsAgendamento.getString("horarioExame", "");
        String valor = prefsAgendamento.getString("valor", "");
        String dataBR = converterDataBR(dia);
        mostrarExameDialog(exame, medico, dataBR, hora, valor);
    }
    private void mostrarConsultaDialog(String tipo, String medico, String data, String horario, String valor) {

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_agendamento, null);

        TextView txtDialogTipo = dialogView.findViewById(R.id.txtTipo);
        TextView txtDialogMedico = dialogView.findViewById(R.id.txtMedico);
        TextView txtDialogData = dialogView.findViewById(R.id.txtData);
        TextView txtDialogHorario = dialogView.findViewById(R.id.txtHorario);
        TextView txtDialogValor = dialogView.findViewById(R.id.txtValor);
        Button btnConfirmar = dialogView.findViewById(R.id.btnConfirmar);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);

        txtDialogTipo.setText("Especialidade: " + tipo);
        txtDialogMedico.setText("Médico: " + medico);
        txtDialogData.setText("Data: " + data);
        txtDialogHorario.setText("Horário: " + horario);
        txtDialogValor.setText("Valor: R$" + valor);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnConfirmar.setOnClickListener(v -> {
            dialog.dismiss();

            buscarIdMedico(medico, idMedico -> {
                salvarConsultaNoServidor(tipo, idMedico, horario, "Agendada", valor);
            });

            Intent it = new Intent(FormaPagamento.this, PacienteLogado.class);
            startActivity(it);
            finish();
        });

        dialog.show();
    }


    private void confirmarESalvarConsulta() {
        String especialidade = prefsAgendamento.getString("especialidadeConsulta", "");
        String medico = prefsAgendamento.getString("medicoConsulta", "");
        String dia = prefsAgendamento.getString("dataConsulta", "");
        String hora = prefsAgendamento.getString("horarioConsulta", "");
        String valor = prefsAgendamento.getString("valor", "120.00");
        String dataBR = converterDataBR(dia);
        mostrarConsultaDialog(especialidade, medico, dataBR, hora, valor);
    }

    private String converterDataBR(String dataMYSQL) {
        try {
            String[] partes = dataMYSQL.split("-");
            String dia = partes[2];
            String mes = partes[1];
            String ano = partes[0];
            return dia + "/" + mes + "/" + ano;
        } catch (Exception e) {
            e.printStackTrace();
            return dataMYSQL;
        }
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

    private void salvarVacinaNoServidor(String vacina, String hora_vacina, String valor_vacina, String status_vacina) {

        int idUsuario = prefsUsuario.getInt("idUsuario", 0);
        String dataBR = prefsAgendamento.getString("data_vacina","");
        String data = converterDataParaMysql(dataBR);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("idUsuario", idUsuario);
            jsonBody.put("vacina", vacina);
            jsonBody.put("data_vacina", data);
            jsonBody.put("hora_vacina", hora_vacina);
            jsonBody.put("valor_vacina", valor_vacina);
            jsonBody.put("status_vacina", status_vacina);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String URL = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/salvar_vacina.php";

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
                error -> {                    if (error.networkResponse != null && error.networkResponse.data != null) {
                    String resposta = new String(error.networkResponse.data);
                    Log.e("ERRO_SERVIDOR", "Resposta: " + resposta);
                } else {
                    Log.e("ERRO_SERVIDOR", "Erro sem resposta do servidor: " + error.toString());
                }

                    Toast.makeText(this, "Erro no servidor", Toast.LENGTH_SHORT).show();}

        );
        Volley.newRequestQueue(this).add(request);

    }

    private void salvarConsultaAgendaAPI(int idMedico, String inicio) {

        String dataBR = prefsAgendamento.getString("dataConsulta", "");
        String dataMysql = converterDataParaMysql(dataBR);

        String fimAgenda = somar30Minutos(inicio);  // <-- usa função correta

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("idMedico", idMedico);
            jsonBody.put("dataAgenda", dataMysql);
            jsonBody.put("inicioAgenda", inicio + ":00");  // garante formato MySQL
            jsonBody.put("fimAgenda", fimAgenda + ":00");
            jsonBody.put("statusAgenda", "Agendado");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String URL = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/salvar_agenda.php";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                jsonBody,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String resposta = new String(error.networkResponse.data);
                        Log.e("ERRO_SERVIDOR", "Resposta: " + resposta);
                    } else {
                        Log.e("ERRO_SERVIDOR", "Erro sem resposta do servidor: " + error.toString());
                    }
                    Toast.makeText(this, "Erro ao salvar agenda", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private String somar30Minutos(String horario) {
        // horario no formato "HH:mm"
        LocalTime time = LocalTime.parse(horario);
        LocalTime fim = time.plusMinutes(30);
        return fim.toString(); // retorna "HH:mm"
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

                        if (success) {finish(); salvarConsultaAgendaAPI(idMedico, horario);}
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

    private void salvarExameAgendaAPI(int idMedico, String inicio) {

        String dataBR = prefsAgendamento.getString("dataExame", "");
        String dataMysql = converterDataParaMysql(dataBR);

        String fimAgenda = somar30Minutos(inicio);  // <-- usa função correta

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("idMedico", idMedico);
            jsonBody.put("dataAgenda", dataMysql);
            jsonBody.put("inicioAgenda", inicio + ":00");  // garante formato MySQL
            jsonBody.put("fimAgenda", fimAgenda + ":00");
            jsonBody.put("statusAgenda", "Agendado");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String URL = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/salvar_agenda.php";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                jsonBody,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String resposta = new String(error.networkResponse.data);
                        Log.e("ERRO_SERVIDOR", "Resposta: " + resposta);
                    } else {
                        Log.e("ERRO_SERVIDOR", "Erro sem resposta do servidor: " + error.toString());
                    }
                    Toast.makeText(this, "Erro ao salvar agenda", Toast.LENGTH_SHORT).show();
                }
        );

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

            Log.d("DEBUG_JSON", jsonBody.toString());

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

                        if (success) {salvarExameAgendaAPI(idMedico, hora);  finish(); }

                    } catch (Exception e) { e.printStackTrace();
                    }
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String resposta = new String(error.networkResponse.data);
                        Log.e("ERRO_SERVIDOR", "Resposta: " + resposta);
                        Log.e("ERRO", "Resposta: " + error);
                    } else {
                        Log.e("ERRO_SERVIDOR", "Erro sem resposta do servidor: " + error.toString());
                        Toast.makeText(this, "Erro no servidor", Toast.LENGTH_SHORT).show();
                    }

                }

        );

        Volley.newRequestQueue(this).add(request);
    }

}
