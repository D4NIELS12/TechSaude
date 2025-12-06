package com.example.techsaude;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AgendaMedica extends AppCompatActivity {

    private TableLayout tableAgendamentos;
    ImageView btnVoltar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda_medica);

        btnVoltar = (ImageView) findViewById(R.id.btnVoltar);
        tableAgendamentos = findViewById(R.id.tableAgendamentos);

        removerLinhaExemplo();

        carregarAgendamentos();

        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void removerLinhaExemplo() {
        if (tableAgendamentos.getChildCount() > 1) {
            tableAgendamentos.removeViewAt(1);
        }
    }
    private void carregarAgendamentos() {
        SharedPreferences prefs = getSharedPreferences("user_prefs_medico", MODE_PRIVATE);
        int idMedico = prefs.getInt("idMedico", 0);

        String url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/mostrar_proximas_consultas.php";

        JSONObject json = new JSONObject();
        try {
            json.put("idMedico", idMedico);
        } catch (Exception e) {}

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                json,
                response -> { // SUCESSO
                    try {
                        if (!response.getBoolean("success")) {
                            Toast.makeText(this, "Nenhum agendamento encontrado.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONArray agenda = response.getJSONArray("agenda");

                        for (int i = 0; i < agenda.length(); i++) {
                            JSONObject item = agenda.getJSONObject(i);

                            String paciente = item.getString("paciente");
                            String dia = item.getString("dia");
                            String hora = item.getString("horario");

                            adicionarLinhaTabela(paciente, dia, hora);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> { // ERRO
                    Toast.makeText(this, "Erro ao conectar ao servidor.", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
        );

        // Enviar requisição
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void adicionarLinhaTabela(String paciente, String data, String hora) {
        TableRow row = new TableRow(this);
        row.setPadding(20,20, 20, 20);

        TextView txtPaciente = new TextView(this);
        txtPaciente.setText(paciente);
        txtPaciente.setGravity(Gravity.CENTER);
        txtPaciente.setTextSize(15);
        txtPaciente.setTextColor(getResources().getColor(R.color.preto));

        TextView txtData = new TextView(this);
        txtData.setText(formatarData(data));
        txtData.setGravity(Gravity.CENTER);
        txtData.setTextSize(15);
        txtData.setTextColor(getResources().getColor(R.color.preto));

        TextView txtHora = new TextView(this);
        txtHora.setText(hora.substring(0, 5)); // 14:00
        txtHora.setGravity(Gravity.CENTER);
        txtHora.setTextSize(15);
        txtHora.setTextColor(getResources().getColor(R.color.preto));

        row.addView(txtPaciente);
        row.addView(txtData);
        row.addView(txtHora);

        tableAgendamentos.addView(row);
    }

    private String formatarData(String dataISO) {
        String[] partes = dataISO.split("-");
        return partes[2] + "/" + partes[1] + "/" + partes[0];
    }
}