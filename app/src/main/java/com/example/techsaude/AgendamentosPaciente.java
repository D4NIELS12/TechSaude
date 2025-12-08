package com.example.techsaude;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AgendamentosPaciente extends Fragment {

    private MaterialCalendarView calendarView;
    private TextView txtDetalhes;
    private LinearLayout containerAgendamentos;

    private int idUsuario;

    // Mapa com data → lista de objetos (agendamento completo)
    private final HashMap<String, List<AgendamentoItem>> agendamentos = new HashMap<>();

    private String dataSelecionada = "";

    public AgendamentosPaciente() {
        super(R.layout.activity_agendamentos);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarView = view.findViewById(R.id.calendarView);
        txtDetalhes = view.findViewById(R.id.txtDetalhes);
        containerAgendamentos = view.findViewById(R.id.containerAgendamentos);

        idUsuario = requireActivity()
                .getSharedPreferences("loginUsuario_prefs", getContext().MODE_PRIVATE)
                .getInt("idUsuario", 0);

        calendarView.setCurrentDate(CalendarDay.today());
        calendarView.addDecorator(new PastDaysDecorator());

        carregarAgendamentosDoBanco();


        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            dataSelecionada = String.format("%04d-%02d-%02d",
                    date.getYear(),
                    date.getMonth() + 1,
                    date.getDay()
            );

            mostrarAgendamentos(dataSelecionada);
        });

    }


    // -------------------------------------------------------------
    // CARREGAR DADOS DO BANCO
    // -------------------------------------------------------------
    private void carregarAgendamentosDoBanco() {

        agendamentos.clear();

        String URL_LISTAR_CONSULTAS = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/lista_consulta.php?idUsuario=" + idUsuario;
        String URL_LISTAR_VACINAS   = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/lista_vacina.php?idUsuario=" + idUsuario;
        String URL_LISTAR_EXAMES    = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/lista_exame.php?idUsuario=" + idUsuario;

        // ------ CONSULTAS ------
        JsonObjectRequest reqConsultas = new JsonObjectRequest(
                Request.Method.GET,
                URL_LISTAR_CONSULTAS,
                null,
                response -> {
                    try {
                        if (!response.getBoolean("success")) return;

                        JSONArray array = response.getJSONArray("consultas");

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);

                            String id = obj.getString("idConsulta");
                            String data = obj.getString("dataConsulta");
                            String hora = obj.getString("horarioConsulta").substring(0, 5);
                            String esp = obj.getString("especialidadeConsulta");
                            String medico = obj.getString("nome_completoMedico");
                            String status = obj.getString("statusConsulta");

                            String desc = "Consulta com " + medico +
                                    "\nEspecialidade: " + esp +
                                    "\nHorário: " + hora+
                                    "\nStatus: " + status;

                            adicionarAgendamento(data, desc, id, "consulta");
                        }

                        marcarAgendamentosNoCalendario();

                    } catch (Exception ignored) {}
                },
                error -> txtDetalhes.setText("Erro ao carregar consultas.")
        );

        // ------ VACINAS ------
        JsonObjectRequest reqVacinas = new JsonObjectRequest(
                Request.Method.GET,
                URL_LISTAR_VACINAS,
                null,
                response -> {
                    try {
                        if (!response.getBoolean("success")) return;

                        JSONArray array = response.getJSONArray("vacinas");

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);

                            String id = obj.getString("idVacina");
                            String data = obj.getString("dataVacina");
                            String hora = obj.getString("horarioVacina").substring(0, 5);
                            String tipo = obj.getString("tipoVacina");
                            String status = obj.getString("statusVacina");

                            String desc = "Vacina: " + tipo +
                                    "\nHorário: " + hora+
                                    "\nStatus: " + status;

                            adicionarAgendamento(data, desc, id, "vacina");
                        }

                        marcarAgendamentosNoCalendario();

                    } catch (Exception ignored) {}
                },
                error -> txtDetalhes.setText("Erro ao carregar vacinas.")
        );

        // ------ EXAMES ------
        JsonObjectRequest reqExames = new JsonObjectRequest(
                Request.Method.GET,
                URL_LISTAR_EXAMES,
                null,
                response -> {
                    try {
                        if (!response.getBoolean("success")) return;

                        JSONArray array = response.getJSONArray("exames");

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);

                            String id = obj.getString("idExame");
                            String data = obj.getString("dataExame");
                            String hora = obj.getString("horarioExame").substring(0, 5);
                            String tipo = obj.getString("tipoExame");
                            String medico = obj.getString("nome_completoMedico");
                            String status = obj.getString("statusExame");

                            String desc = "Exame: " + tipo +
                                    "\nMédico: " + medico +
                                    "\nHorário: " + hora+
                                    "\nExame: " + status;

                            adicionarAgendamento(data, desc, id, "exame");
                        }

                        marcarAgendamentosNoCalendario();

                    } catch (Exception ignored) {}
                },
                error -> txtDetalhes.setText("Erro ao carregar exames.")
        );

        Volley.newRequestQueue(requireContext()).add(reqConsultas);
        Volley.newRequestQueue(requireContext()).add(reqVacinas);
        Volley.newRequestQueue(requireContext()).add(reqExames);
    }


    // -------------------------------------------------------------
    // ARMAZENAR AGENDAMENTO
    // -------------------------------------------------------------
    private void adicionarAgendamento(String data, String desc, String id, String tipo) {
        if (!agendamentos.containsKey(data))
            agendamentos.put(data, new ArrayList<>());

        agendamentos.get(data).add(new AgendamentoItem(id, desc, tipo));
    }


    // -------------------------------------------------------------
    // MARCAR DIAS NO CALENDÁRIO
    // -------------------------------------------------------------
    private void marcarAgendamentosNoCalendario() {
        List<CalendarDay> dias = new ArrayList<>();

        for (String data : agendamentos.keySet()) {
            String[] p = data.split("-");

            dias.add(CalendarDay.from(
                    Integer.parseInt(p[0]),
                    Integer.parseInt(p[1]) - 1,
                    Integer.parseInt(p[2])
            ));
        }

        calendarView.addDecorator(new ConsultaDecorator(dias));
    }


    // -------------------------------------------------------------
    // MOSTRAR AGENDAMENTOS DO DIA
    // -------------------------------------------------------------
    private void mostrarAgendamentos(String data) {

        containerAgendamentos.removeAllViews();

        if (!agendamentos.containsKey(data)) {
            txtDetalhes.setText("Nenhum agendamento neste dia.");
            return;
        }

        txtDetalhes.setText("📅 Agendamentos em " + data + ":");

        for (AgendamentoItem item : agendamentos.get(data)) {
            adicionarCard(item);
        }
    }


    // -------------------------------------------------------------
    // CRIAR CARD NA TELA
    // -------------------------------------------------------------
    private void adicionarCard(AgendamentoItem item) {

        View card = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_agendamento_paciente, containerAgendamentos, false);

        TextView txtInfo = card.findViewById(R.id.txtInfo);
        Button btnCancelar = card.findViewById(R.id.btnCancelar);

        txtInfo.setText(item.descricao);

        btnCancelar.setOnClickListener(v -> cancelarAgendamento(item, card));

        containerAgendamentos.addView(card);
    }


    // -------------------------------------------------------------
    // CANCELAR AGENDAMENTO
    // -------------------------------------------------------------
    private void cancelarAgendamento(AgendamentoItem item, View card) {

        String url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/cancelar_agendamento_paciente.php";

        JSONObject json = new JSONObject();
        try {
            json.put("idUsuario", idUsuario);
            json.put("idAgendamento", item.id);
            json.put("tipo", item.tipo);
        } catch (Exception e) { e.printStackTrace(); }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                url,
                json,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(requireContext(), "Agendamento cancelado!", Toast.LENGTH_SHORT).show();
                            containerAgendamentos.removeView(card);
                        } else {
                            Toast.makeText(requireContext(),
                                    response.getString("message"),
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ignored) {}
                },
                error -> Toast.makeText(requireContext(),
                        "Falha ao conectar ao servidor.",
                        Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(requireContext()).add(req);
    }





    // -------------------------------------------------------------
    // CLASSE DE OBJETO DO AGENDAMENTO
    // -------------------------------------------------------------
    private static class AgendamentoItem {
        String id;
        String descricao;
        String tipo;

        AgendamentoItem(String id, String descricao, String tipo) {
            this.id = id;
            this.descricao = descricao;
            this.tipo = tipo;
        }
    }
}
