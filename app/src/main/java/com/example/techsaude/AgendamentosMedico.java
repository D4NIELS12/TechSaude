package com.example.techsaude;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class AgendamentosMedico extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private LinearLayout containerAgendamentos;
    ImageView imgVoltar;
    private HashMap<String, List<Agendamento>> consultas = new HashMap<>();
    private HashMap<String, List<Agendamento>> exames = new HashMap<>();

    private String dataSelecionada = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendamentos_medico);

        calendarView = findViewById(R.id.calendarView);
        containerAgendamentos = findViewById(R.id.containerAgendamentos);
        imgVoltar = findViewById(R.id.imageView10);
        calendarView.setCurrentDate(CalendarDay.today());

        imgVoltar.setOnClickListener(v -> finish());
        // Evita crash ao selecionar dias anteriores
        calendarView.addDecorator(new PastDaysDecorator());


        carregarConsultas();
        carregarExames();

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            dataSelecionada = String.format("%04d-%02d-%02d",
                    date.getYear(),
                    date.getMonth() + 1,
                    date.getDay());

            mostrarAgendamentos(dataSelecionada);
        });
    }

    // --------------------------------------------------------------------
    // MODELO
    // --------------------------------------------------------------------
    private static class Agendamento {
        int id;
        String tipo; // "consulta" ou "exame"
        String texto;

        Agendamento(int id, String tipo, String texto) {
            this.id = id;
            this.tipo = tipo;
            this.texto = texto;
        }
    }

    // --------------------------------------------------------------------
    // MOSTRAR AGENDAMENTOS
    // --------------------------------------------------------------------
    private void mostrarAgendamentos(String data) {
        containerAgendamentos.removeAllViews();

        boolean temAlgo = false;

        if (consultas.containsKey(data)) {
            temAlgo = true;
            for (Agendamento a : consultas.get(data)) {
                adicionarCard(a);
            }
        }

        if (exames.containsKey(data)) {
            temAlgo = true;
            for (Agendamento a : exames.get(data)) {
                adicionarCard(a);
            }
        }

        if (!temAlgo) {
            TextView vazio = new TextView(this);
            vazio.setText("Nenhum agendamento para este dia.");
            vazio.setTextSize(16);
            containerAgendamentos.addView(vazio);
        }
    }

    private void adicionarCard(Agendamento agendamento) {
        View card = getLayoutInflater().inflate(R.layout.item_agendamento, containerAgendamentos, false);

        TextView txtInfo = card.findViewById(R.id.txtInfo);
        txtInfo.setText(agendamento.texto);

        containerAgendamentos.addView(card);
    }

    // --------------------------------------------------------------------
    // CARREGAR CONSULTAS
    // --------------------------------------------------------------------
    private void carregarConsultas() {
        consultas.clear();

        SharedPreferences prefs = getSharedPreferences("user_prefs_medico", MODE_PRIVATE);
        int idMedico = prefs.getInt("idMedico", 0);

        if (idMedico == 0) return;

        String url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/lista_consulta_medico.php?idMedico=" + idMedico;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (!response.getBoolean("success")) return;

                        JSONArray array = response.getJSONArray("consultas");

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);

                            int id = obj.getInt("idConsulta");
                            String data = obj.getString("dataConsulta");
                            String horario = obj.getString("horarioConsulta");

                            if (horario.length() >= 5)
                                horario = horario.substring(0, 5);

                            String texto =
                                    "[CONSULTA]\nPaciente: " + obj.getString("nomePaciente") +
                                            "\nHora: " + horario +
                                            "\nTelefone: " + formatarTelefone(obj.getString("telefonePaciente")) +
                                            "\nEmail: " + obj.getString("emailPaciente") +
                                            "\nEspecialidade: " + obj.getString("especialidade") +
                                            "\nStatus: " + obj.getString("statusConsulta");

                            consultas.computeIfAbsent(data, k -> new ArrayList<>())
                                    .add(new Agendamento(id, "consulta", texto));
                        }

                        marcarDiasComEventos();

                    } catch (Exception ignored) {
                    }
                },
                error -> {}
        );

        Volley.newRequestQueue(this).add(request);
    }

    // --------------------------------------------------------------------
    // CARREGAR EXAMES
    // --------------------------------------------------------------------
    private void carregarExames() {
        exames.clear();

        SharedPreferences prefs = getSharedPreferences("user_prefs_medico", MODE_PRIVATE);
        int idMedico = prefs.getInt("idMedico", 0);

        if (idMedico == 0) return;

        String url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/lista_exame_medico.php?idMedico=" + idMedico;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (!response.getBoolean("success")) return;

                        JSONArray array = response.getJSONArray("exames");

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);

                            int id = obj.getInt("idExame");
                            String data = obj.getString("dataExame");
                            String horario = obj.getString("horarioExame");

                            if (horario.length() >= 5)
                                horario = horario.substring(0, 5);

                            String texto =
                                    "[EXAME]\nPaciente: " + obj.getString("nomePaciente") +
                                            "\nHora: " + horario +
                                            "\nTelefone: " + formatarTelefone(obj.getString("telefonePaciente")) +
                                            "\nEmail: " + obj.getString("emailPaciente") +
                                            "\nTipo de exame: " + obj.getString("tipoExame") +
                                            "\nStatus: " + obj.getString("statusExame");

                            exames.computeIfAbsent(data, k -> new ArrayList<>())
                                    .add(new Agendamento(id, "exame", texto));
                        }

                        marcarDiasComEventos();

                    } catch (Exception ignored) {
                    }
                },
                error -> {}
        );

        Volley.newRequestQueue(this).add(request);
    }

    // --------------------------------------------------------------------
    // MARCAR DIAS COM EVENTOS
    // --------------------------------------------------------------------
    private void marcarDiasComEventos() {

        // evita duplicar decoradores no calendário
        calendarView.removeDecorators();

        HashSet<CalendarDay> dias = new HashSet<>();

        for (String data : consultas.keySet()) {
            CalendarDay day = toCalendarDay(data);
            if (day != null) dias.add(day);
        }

        for (String data : exames.keySet()) {
            CalendarDay day = toCalendarDay(data);
            if (day != null) dias.add(day);
        }

        calendarView.addDecorator(new ConsultaDecorator(new ArrayList<>(dias)));
        calendarView.addDecorator(new PastDaysDecorator());
    }

    private CalendarDay toCalendarDay(String data) {
        try {
            String[] p = data.split("-");
            return CalendarDay.from(
                    Integer.parseInt(p[0]),
                    Integer.parseInt(p[1]) - 1, // CalendarDay usa month 0–11
                    Integer.parseInt(p[2])
            );
        } catch (Exception e) {
            return null;
        }
    }

    public static String formatarTelefone(String telefone) {
        if (telefone != null && telefone.length() == 11) {
            return String.format("(%s) %s-%s",
                    telefone.substring(0, 2),
                    telefone.substring(2, 7),
                    telefone.substring(7));
        }
        return telefone;
    }
}
