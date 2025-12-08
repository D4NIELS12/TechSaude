package com.example.techsaude;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Iterator;
import java.util.List;

public class AgendamentosPaciente extends Fragment {

    private MaterialCalendarView calendarView;
    private TextView txtDetalhes;
    private LinearLayout containerAgendamentos;

    private int idUsuario;

    // Mapa com data → lista de objetos (agendamento completo)
    private final HashMap<String, List<AgendamentoItem>> agendamentos = new HashMap<>();

    private String dataSelecionada = "";

    // contador de requisições pendentes (consultas, vacinas, exames)
    private static final int TOTAL_REQUESTS = 3;
    private int pendingRequests = 0;

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
        pendingRequests = TOTAL_REQUESTS; // reset contador

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
                        if (!response.getBoolean("success")) {
                            decrementAndMaybeFinish();
                            return;
                        }

                        JSONArray array = response.getJSONArray("consultas");

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);

                            String id = obj.optString("idConsulta", "");
                            String data = obj.optString("dataConsulta", "");
                            String horarioRaw = obj.optString("horarioConsulta", "");
                            String hora = horarioRaw != null && horarioRaw.length() >= 5 ? horarioRaw.substring(0, 5) : horarioRaw;
                            String esp = obj.optString("especialidadeConsulta", obj.optString("especialidade", ""));
                            String medico = obj.optString("nome_completoMedico", obj.optString("nomePaciente", ""));
                            String status = obj.optString("statusConsulta", "");

                            String desc = "Consulta com " + medico +
                                    "\nEspecialidade: " + esp +
                                    "\nHorário: " + hora +
                                    "\nStatus: " + status;

                            adicionarAgendamento(data, desc, id, "consulta");
                        }

                    } catch (Exception e) {
                        Log.e("AGEND_PAC", "Erro parseando consultas", e);
                    } finally {
                        decrementAndMaybeFinish();
                    }
                },
                error -> {
                    Log.e("ERRO:", "Erro ao carregar consultas:" + error);
                    txtDetalhes.setText("Erro ao carregar consultas.");
                    decrementAndMaybeFinish();
                }
        );

        // ------ VACINAS ------
        JsonObjectRequest reqVacinas = new JsonObjectRequest(
                Request.Method.GET,
                URL_LISTAR_VACINAS,
                null,
                response -> {
                    try {
                        if (!response.getBoolean("success")) {
                            decrementAndMaybeFinish();
                            return;
                        }

                        JSONArray array = response.getJSONArray("vacinas");

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);

                            String id = obj.optString("idVacina", "");
                            String data = obj.optString("dataVacina", "");
                            String horarioRaw = obj.optString("horarioVacina", "");
                            String hora = horarioRaw != null && horarioRaw.length() >= 5 ? horarioRaw.substring(0,5) : horarioRaw;
                            String tipo = obj.optString("tipoVacina", "");
                            String status = obj.optString("statusVacina", "");

                            String desc = "Vacina: " + tipo +
                                    "\nHorário: " + hora +
                                    "\nStatus: " + status;

                            adicionarAgendamento(data, desc, id, "vacina");
                        }

                    } catch (Exception e) {
                        Log.e("AGEND_PAC", "Erro parseando vacinas", e);
                    } finally {
                        decrementAndMaybeFinish();
                    }
                },
                error -> {
                    txtDetalhes.setText("Erro ao carregar vacinas.");
                    decrementAndMaybeFinish();
                }
        );

        // ------ EXAMES ------
        JsonObjectRequest reqExames = new JsonObjectRequest(
                Request.Method.GET,
                URL_LISTAR_EXAMES,
                null,
                response -> {
                    try {
                        if (!response.getBoolean("success")) {
                            decrementAndMaybeFinish();
                            return;
                        }

                        JSONArray array = response.getJSONArray("exames");

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);

                            String id = obj.optString("idExame", "");
                            String data = obj.optString("dataExame", "");
                            String horarioRaw = obj.optString("horarioExame", "");
                            String hora = horarioRaw != null && horarioRaw.length() >= 5 ? horarioRaw.substring(0,5) : horarioRaw;
                            String tipo = obj.optString("tipoExame", "");
                            String medico = obj.optString("nome_completoMedico", "");
                            String status = obj.optString("statusExame", "");

                            String desc = "Exame: " + tipo +
                                    "\nMédico: " + medico +
                                    "\nHorário: " + hora +
                                    "\nStatus: " + status;

                            adicionarAgendamento(data, desc, id, "exame");
                        }

                    } catch (Exception e) {
                        Log.e("AGEND_PAC", "Erro parseando exames", e);
                    } finally {
                        decrementAndMaybeFinish();
                    }
                },
                error -> {
                    txtDetalhes.setText("Erro ao carregar exames.");
                    decrementAndMaybeFinish();
                }
        );

        Volley.newRequestQueue(requireContext()).add(reqConsultas);
        Volley.newRequestQueue(requireContext()).add(reqVacinas);
        Volley.newRequestQueue(requireContext()).add(reqExames);
    }

    // decrementa contador e, se zero, finaliza (marca calendário e mostra data selecionada)
    private void decrementAndMaybeFinish() {
        pendingRequests--;
        if (pendingRequests <= 0) {
            // garantir execução na thread UI
            requireActivity().runOnUiThread(() -> {
                marcarAgendamentosNoCalendario();
                if (!dataSelecionada.isEmpty()) {
                    mostrarAgendamentos(dataSelecionada);
                }
            });
        }
    }

    // -------------------------------------------------------------
    // ARMAZENAR AGENDAMENTO
    // -------------------------------------------------------------
    private void adicionarAgendamento(String data, String desc, String id, String tipo) {
        if (data == null || data.trim().isEmpty()) return;
        if (!agendamentos.containsKey(data))
            agendamentos.put(data, new ArrayList<>());

        agendamentos.get(data).add(new AgendamentoItem(id, desc, tipo));
    }

    // -------------------------------------------------------------
    // MARCAR DIAS NO CALENDÁRIO
    // -------------------------------------------------------------
    private void marcarAgendamentosNoCalendario() {
        // remove decoradores antigos pra não duplicar
        calendarView.removeDecorators();
        calendarView.addDecorator(new PastDaysDecorator());

        List<CalendarDay> dias = new ArrayList<>();

        for (String data : agendamentos.keySet()) {
            try {
                String[] p = data.split("-");
                CalendarDay day = CalendarDay.from(
                        Integer.parseInt(p[0]),
                        Integer.parseInt(p[1]) - 1,
                        Integer.parseInt(p[2])
                );
                dias.add(day);
            } catch (Exception e) {
                // ignora datas inválidas
            }
        }

        if (!dias.isEmpty()) {
            calendarView.addDecorator(new ConsultaDecorator(dias));
        }
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

        btnCancelar.setOnClickListener(v -> cancelarAgendamentosDialog(item, card));

        containerAgendamentos.addView(card);
    }

    // abre diálogo de confirmação antes de cancelar
    private void cancelarAgendamentosDialog(AgendamentoItem item, View card) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar cancelamento")
                .setMessage("Deseja realmente cancelar este agendamento?")
                .setPositiveButton("Sim", (dialog, which) -> cancelarAgendamentos(item, card))
                .setNegativeButton("Não", null)
                .show();
    }

    // -------------------------------------------------------------
    // CANCELAR AGENDAMENTO
    // -------------------------------------------------------------
    private void cancelarAgendamentos(AgendamentoItem item, View card) {

        String url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/cancelar_agendamento_paciente.php";

        JSONObject json = new JSONObject();
        try {
            json.put("idUsuario", idUsuario);
            json.put("idAgendamento", item.id);
            json.put("tipo", item.tipo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                url,
                json,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(requireContext(), "Agendamento cancelado!", Toast.LENGTH_SHORT).show();

                            // remove view do card
                            containerAgendamentos.removeView(card);

                            // remove do mapa local
                            String dataRemovida = removeItemFromMapById(item.id);

                            // se o dia removido for o atualmente exibido, atualiza visual
                            if (dataRemovida != null && dataRemovida.equals(dataSelecionada)) {
                                mostrarAgendamentos(dataSelecionada);
                            }

                            // atualiza calendário
                            marcarAgendamentosNoCalendario();

                            // recarrega do servidor para manter sincronizado (opcional, mas recomendado)
                            carregarAgendamentosDoBanco();

                        } else {
                            String msg = response.optString("message", "Falha ao cancelar.");
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e("AGEND_PAC", "Erro no response cancelar", e);
                        Toast.makeText(requireContext(), "Resposta inválida do servidor.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("AGEND_PAC", "Erro ao cancelar", error);
                    Toast.makeText(requireContext(),
                            "Falha ao conectar ao servidor.",
                            Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(requireContext()).add(req);
    }

    // procura e remove item do mapa usando o id; retorna a data onde foi removido ou null
    private String removeItemFromMapById(String id) {
        Iterator<String> it = agendamentos.keySet().iterator();
        while (it.hasNext()) {
            String data = it.next();
            List<AgendamentoItem> lista = agendamentos.get(data);
            if (lista != null) {
                for (int i = 0; i < lista.size(); i++) {
                    if (id.equals(lista.get(i).id)) {
                        lista.remove(i);
                        if (lista.isEmpty()) {
                            it.remove();
                        }
                        return data;
                    }
                }
            }
        }
        return null;
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
