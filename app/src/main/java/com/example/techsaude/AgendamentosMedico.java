package com.example.techsaude;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AgendamentosMedico extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private TextView txtDataPaciente;

    ImageView VoltarConsulta;

    // data -> lista de consultas
    private HashMap<String, List<String>> consultas = new HashMap<>();

    // data selecionada no calendário
    private String dataSelecionada = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendamentos_medico);


        calendarView = findViewById(R.id.calendarView);
        txtDataPaciente = findViewById(R.id.txtDataPaciente);

        calendarView.setCurrentDate(CalendarDay.today());
        calendarView.addDecorator(new PastDaysDecorator());

        VoltarConsulta = findViewById(R.id.imageView10);

        // carrega consultas do médico
        carregarConsultas();

        // quando clicar em um dia
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            dataSelecionada = String.format("%04d-%02d-%02d",
                    date.getYear(),
                    date.getMonth() + 1,
                    date.getDay());

            if (consultas.containsKey(dataSelecionada)) {
                StringBuilder detalhes = new StringBuilder("📅 Consultas em " + dataSelecionada + ":\n");

                for (String desc : consultas.get(dataSelecionada)) {
                    detalhes.append("\n• ").append(desc);
                }

                txtDataPaciente.setText(detalhes.toString());

            } else {
                txtDataPaciente.setText("Nenhuma consulta para este dia.");
            }
        });

        VoltarConsulta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void carregarConsultas() {

        consultas.clear();

        SharedPreferences prefs = getSharedPreferences("loginMedico_prefs", MODE_PRIVATE);
        int idMedico = prefs.getInt("idMedico", 0);

        String URL = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/lista_consulta_medico.php?idMedico=" + idMedico;

        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.GET,
                URL,
                null,
                response -> {
                    try {
                        if (!response.getBoolean("success")) {
                            txtDataPaciente.setText("Nenhuma consulta encontrada.");
                            return;
                        }

                        JSONArray array = response.getJSONArray("consultas");

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);

                            String data = obj.getString("dataConsulta");
                            String horario = obj.getString("horarioConsulta");
                            String nomePaciente = obj.getString("nomePaciente");
                            String telefone = obj.getString("telefonePaciente");
                            String email = obj.getString("emailPaciente");

                            if (horario.length() == 8) {
                                horario = horario.substring(0, 5);
                            }

                            String descricao =
                                    "Paciente: " + nomePaciente +
                                            " | Horário: " + horario +
                                            " | Telefone: " + telefone +
                                            " | Email: " + email;

                            adicionarConsulta(data, descricao);
                        }

                        marcarDiasComConsultas();

                    } catch (Exception e) {
                        txtDataPaciente.setText("Erro ao carregar consultas.");
                    }
                },
                error -> txtDataPaciente.setText("Falha ao conectar ao servidor.")
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void adicionarConsulta(String data, String descricao) {
        if (!consultas.containsKey(data)) {
            consultas.put(data, new ArrayList<>());
        }
        consultas.get(data).add(descricao);
    }

    private void marcarDiasComConsultas() {
        List<CalendarDay> dias = new ArrayList<>();

        for (String data : consultas.keySet()) {
            try {
                String[] partes = data.split("-");
                int ano = Integer.parseInt(partes[0]);
                int mes = Integer.parseInt(partes[1]) - 1;
                int dia = Integer.parseInt(partes[2]);

                dias.add(CalendarDay.from(ano, mes, dia));

            } catch (Exception ignored) {}
        }

        calendarView.addDecorator(new ConsultaDecorator(dias));
    }

    private void mostrarDialogAlteracao() {
        new AlertDialog.Builder(this)
                .setTitle("Alterar Consulta")
                .setMessage("Deseja alterar as consultas do dia " + dataSelecionada + "?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    // abrir tela de alteração depois
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
