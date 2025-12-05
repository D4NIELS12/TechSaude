package com.example.techsaude;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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
    private ImageView VoltarConsulta;

    // DATA → LISTA DE CONSULTAS OU EXAMES
    private HashMap<String, List<String>> consultas = new HashMap<>();
    private HashMap<String, List<String>> exames = new HashMap<>();

    private String dataSelecionada = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendamentos_medico);

        calendarView = findViewById(R.id.calendarView);
        txtDataPaciente = findViewById(R.id.txtDataPaciente);
        VoltarConsulta = findViewById(R.id.imageView10);

        calendarView.setCurrentDate(CalendarDay.today());
        calendarView.addDecorator(new PastDaysDecorator());

        // Carrega ambos
        carregarConsultas();
        carregarExames();

        // Quando clicar em uma data
        calendarView.setOnDateChangedListener((widget, date, selected) -> {

            dataSelecionada = String.format("%04d-%02d-%02d",
                    date.getYear(),
                    date.getMonth() + 1,
                    date.getDay());

            StringBuilder detalhes = new StringBuilder("📅 Agendamentos em " + dataSelecionada + ":\n");

            boolean temAlgo = false;

            if (consultas.containsKey(dataSelecionada)) {
                temAlgo = true;
                for (String c : consultas.get(dataSelecionada)) {
                    detalhes.append("\n• ").append(c);
                }
            }

            if (exames.containsKey(dataSelecionada)) {
                temAlgo = true;
                for (String e : exames.get(dataSelecionada)) {
                    detalhes.append("\n• ").append(e);
                }
            }

            if (temAlgo) {
                txtDataPaciente.setText(detalhes.toString());
            } else {
                txtDataPaciente.setText("Nenhum agendamento para este dia.");
            }
        });

        VoltarConsulta.setOnClickListener(v -> finish());
    }


    // ==============================
    // CARREGAR EXAMES
    // ==============================
    private void carregarExames() {

        exames.clear();

        SharedPreferences prefs = getSharedPreferences("user_prefs_medico", MODE_PRIVATE);
        int idMedico = prefs.getInt("idMedico", 0);

        String URL =
                "http://tcc3edsmodetecgr3.hospedagemdesites.ws/lista_exame_medico.php?idMedico=" + idMedico;

        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.GET,
                URL,
                null,
                response -> {
                    try {

                        if (!response.getBoolean("success")) return;

                        JSONArray array = response.getJSONArray("exames");

                        for (int i = 0; i < array.length(); i++) {

                            JSONObject obj = array.getJSONObject(i);

                            String data = obj.getString("dataExame");
                            String horario = obj.getString("horarioExame");

                            if (horario.length() >= 5)
                                horario = horario.substring(0, 5);

                            String nomePaciente = obj.getString("nomePaciente"); // CORRIGIDO
                            String tipoExame = obj.getString("tipoExame");
                            String telefone = obj.getString("telefonePaciente");
                            String email = obj.getString("emailPaciente");

                            String descricao =
                                    "[EXAME] Paciente: " + nomePaciente +
                                            " | Hora: " + horario +
                                            " | Telefone: " + telefone +
                                            " | Email: " + email +
                                            " | Tipo: " + tipoExame + "\n";

                            adicionarExame(data, descricao);
                        }

                        marcarDiasComEventos();

                    } catch (Exception ignored) {}
                },
                error -> txtDataPaciente.setText("Falha ao conectar ao servidor.")
        );

        Volley.newRequestQueue(this).add(request);
    }


    // ==============================
    // CARREGAR CONSULTAS
    // ==============================
    private void carregarConsultas() {

        consultas.clear();

        SharedPreferences prefs = getSharedPreferences("user_prefs_medico", MODE_PRIVATE);
        int idMedico = prefs.getInt("idMedico", 0);

        String URL =
                "http://tcc3edsmodetecgr3.hospedagemdesites.ws/lista_consulta_medico.php?idMedico=" + idMedico;

        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.GET,
                URL,
                null,
                response -> {
                    try {

                        if (!response.getBoolean("success")) return;

                        JSONArray array = response.getJSONArray("consultas");

                        for (int i = 0; i < array.length(); i++) {

                            JSONObject obj = array.getJSONObject(i);

                            String data = obj.getString("dataConsulta");
                            String horario = obj.getString("horarioConsulta");

                            if (horario.length() >= 5)
                                horario = horario.substring(0, 5);

                            String nomePaciente = obj.getString("nomePaciente");
                            String especialidade = obj.getString("especialidade");
                            String telefone = obj.getString("telefonePaciente");
                            String email = obj.getString("emailPaciente");

                            String descricao =
                                    "[CONSULTA] Paciente: " + nomePaciente +
                                            " | Hora: " + horario +
                                            " | Tel: " + telefone +
                                            " | Email: " + email +
                                            " | Especialidade: " + especialidade + "\n";

                            adicionarConsulta(data, descricao);
                        }

                        marcarDiasComEventos();

                    } catch (Exception ignored) {}
                },
                error -> txtDataPaciente.setText("Erro ao conectar ao servidor.")
        );

        Volley.newRequestQueue(this).add(request);
    }


    // ==============================
    // ADICIONAR CONSULTA
    // ==============================
    private void adicionarConsulta(String data, String descricao) {
        consultas.computeIfAbsent(data, k -> new ArrayList<>()).add(descricao);
    }

    // ==============================
    // ADICIONAR EXAME
    // ==============================
    private void adicionarExame(String data, String descricao) {
        exames.computeIfAbsent(data, k -> new ArrayList<>()).add(descricao);
    }

    // ==============================
    // MARCAR DATAS NO CALENDÁRIO
    // ==============================
    private void marcarDiasComEventos() {

        List<CalendarDay> dias = new ArrayList<>();

        for (String data : consultas.keySet()) {
            dias.add(toCalendarDay(data));
        }

        for (String data : exames.keySet()) {
            dias.add(toCalendarDay(data));
        }

        calendarView.addDecorator(new ConsultaDecorator(dias));
    }

    private CalendarDay toCalendarDay(String data) {
        try {
            String[] p = data.split("-");
            return CalendarDay.from(
                    Integer.parseInt(p[0]),
                    Integer.parseInt(p[1]) - 1,
                    Integer.parseInt(p[2])
            );
        } catch (Exception e) {
            return null;
        }
    }


    private void mostrarDialogAlteracao() {
        new AlertDialog.Builder(this)
                .setTitle("Alterar Agendamento")
                .setMessage("Deseja alterar os agendamentos de " + dataSelecionada + "?")
                .setPositiveButton("Sim", (d, w) -> {})
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
