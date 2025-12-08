package com.example.techsaude;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class AgendamentosMedico extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private LinearLayout containerAgendamentos;
    private ImageView imgVoltar;

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
        calendarView.addDecorator(new PastDaysDecorator());

        imgVoltar.setOnClickListener(v -> finish());

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

    // Confirmar cancelamento
    private void mostrarConfirmacaoCancelar(Agendamento a) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirmar cancelamento")
                .setMessage("Deseja realmente cancelar este agendamento?")
                .setPositiveButton("Sim", (dialog, which) -> cancelarAgendamento(a))
                .setNegativeButton("Não", null)
                .show();
    }

    // Confirmar realização
    private void mostrarConfirmacaoRealizado(Agendamento a) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirmar conclusão")
                .setMessage("Deseja marcar este agendamento como realizado?")
                .setPositiveButton("Sim", (dialog, which) -> concluirAgendamento(a))
                .setNegativeButton("Não", null)
                .show();
    }


    // --------------------------------------------------------------------
    // MODELO DE AGENDAMENTO
    // --------------------------------------------------------------------
    private static class Agendamento {
        int id;
        String tipo;
        String texto;
        String data;

        Agendamento(int id, String tipo, String texto, String data) {
            this.id = id;
            this.tipo = tipo;
            this.texto = texto;
            this.data = data;
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

    // --------------------------------------------------------------------
    // ADICIONAR CARD
    // --------------------------------------------------------------------
    private void adicionarCard(Agendamento agendamento) {

        int layout = agendamento.tipo.equals("consulta")
                ? R.layout.item_agendamento_consulta
                : R.layout.item_agendamento_exame;

        View card = getLayoutInflater().inflate(layout, containerAgendamentos, false);

        TextView txtInfo = card.findViewById(R.id.txtInfo);
        txtInfo.setText(agendamento.texto);

        Button btnCancelar = card.findViewById(R.id.btnCancelar);
        Button btnRealizado = card.findViewById(R.id.btnRealizado);

        btnCancelar.setOnClickListener(v -> mostrarConfirmacaoCancelar(agendamento));
        btnRealizado.setOnClickListener(v -> mostrarConfirmacaoRealizado(agendamento));


        containerAgendamentos.addView(card);
    }

    // --------------------------------------------------------------------
    // MARCAR COMO REALIZADO
    // --------------------------------------------------------------------
    private void concluirAgendamento(Agendamento a) {

        String url;
        JSONObject json = new JSONObject();

        try {
            if (a.tipo.equals("consulta")) {
                url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/realizado_consulta.php";
                json.put("idConsulta", a.id);
            } else {
                url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/realizado_exame.php";
                json.put("idExame", a.id);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, json,
                response -> {
                    Toast.makeText(this, "Marcado como realizado!", Toast.LENGTH_SHORT).show();

                    if (a.tipo.equals("consulta")) {
                        consultas.get(a.data).remove(a);
                    } else {
                        exames.get(a.data).remove(a);
                    }

                    mostrarAgendamentos(a.data);
                    carregarConsultas();
                    carregarExames();
                },
                error -> Toast.makeText(this, "Erro ao concluir!", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    // --------------------------------------------------------------------
    // CANCELAR AGENDAMENTO
    // --------------------------------------------------------------------
    private void cancelarAgendamento(Agendamento a) {

        String url;
        JSONObject json = new JSONObject();

        try {
            if (a.tipo.equals("consulta")) {
                url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/cancelar_consulta.php";
                json.put("idConsulta", a.id);
            } else {
                url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/cancelar_exame.php";
                json.put("idExame", a.id);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, json,
                response -> {
                    Toast.makeText(this, "Cancelado!", Toast.LENGTH_SHORT).show();

                    if (a.tipo.equals("consulta")) {
                        consultas.get(a.data).remove(a);
                    } else {
                        exames.get(a.data).remove(a);
                    }

                    mostrarAgendamentos(a.data);
                    carregarConsultas();
                    carregarExames();
                },
                error -> Toast.makeText(this, "Erro ao cancelar!", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
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
                                    .add(new Agendamento(id, "consulta", texto, data));
                        }

                        marcarDiasComEventos();

                    } catch (Exception ignored) {}
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
                                    .add(new Agendamento(id, "exame", texto, data));
                        }

                        marcarDiasComEventos();

                    } catch (Exception ignored) {}
                },
                error -> {}
        );

        Volley.newRequestQueue(this).add(request);
    }

    // --------------------------------------------------------------------
    // MARCAR DIAS NO CALENDÁRIO
    // --------------------------------------------------------------------
    private void marcarDiasComEventos() {

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
                    Integer.parseInt(p[1]) - 1,
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
