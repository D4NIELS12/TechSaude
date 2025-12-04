package com.example.techsaude;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AgendamentosPaciente extends Fragment {

    private MaterialCalendarView calendarView;
    private TextView txtDetalhes;
    private Button btnAlterar;

    // Mapa com data -> lista de descrições
    private final HashMap<String, List<String>> agendamentos = new HashMap<>();

    // Data selecionada no calendário
    private String dataSelecionada = "";

    public AgendamentosPaciente() {
        super(R.layout.activity_agendamentos);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarView = view.findViewById(R.id.calendarView);
        txtDetalhes = view.findViewById(R.id.txtDetalhes);
        btnAlterar = view.findViewById(R.id.btnAlterarAgendamento); // Adicione no XML

        calendarView.setCurrentDate(CalendarDay.today());
        calendarView.addDecorator(new PastDaysDecorator());

        carregarAgendamentosDoBanco();
        marcarAgendamentosNoCalendario();

        // Botão começa invisível
        btnAlterar.setVisibility(View.GONE);

        // Quando o usuário clicar em um dia
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            dataSelecionada = String.format("%04d-%02d-%02d",
                    date.getYear(),
                    date.getMonth() + 1,
                    date.getDay());

            if (agendamentos.containsKey(dataSelecionada)) {
                StringBuilder detalhes = new StringBuilder("📅 Agendamentos em " + dataSelecionada + ":\n");
                for (String desc : agendamentos.get(dataSelecionada)) {
                    detalhes.append("\n• ").append(desc);
                }
                txtDetalhes.setText(detalhes.toString());
                btnAlterar.setVisibility(View.VISIBLE);
            } else {
                txtDetalhes.setText("Nenhum agendamento neste dia.");
                btnAlterar.setVisibility(View.GONE);
            }
        });

        // Quando clicar em "Alterar"
        btnAlterar.setOnClickListener(v -> mostrarDialogAlteracao());
    }


    private void carregarAgendamentosDoBanco() {

        agendamentos.clear();

        int idUsuario = requireActivity()
                .getSharedPreferences("loginUsuario_prefs", getContext().MODE_PRIVATE)
                .getInt("idUsuario", 0);

        String URL_LISTAR_CONSULTAS = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/lista_consulta.php?idUsuario=" + idUsuario;
        String URL_LISTAR_VACINAS   = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/lista_vacina.php?idUsuario=" + idUsuario;
        String URL_LISTAR_EXAMES = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/lista_exame.php?idUsuario=" + idUsuario;
        com.android.volley.toolbox.JsonObjectRequest request =
                new com.android.volley.toolbox.JsonObjectRequest(
                        com.android.volley.Request.Method.GET,
                        URL_LISTAR_CONSULTAS,
                        null,
                        response -> {
                            try {
                                boolean success = response.getBoolean("success");
                                if (!success) {
                                    txtDetalhes.setText("Nenhum agendamento encontrado.");
                                    return;
                                }

                                org.json.JSONArray array = response.getJSONArray("consultas");

                                for (int i = 0; i < array.length(); i++) {

                                    org.json.JSONObject obj = array.getJSONObject(i);

                                    String data = obj.getString("dataConsulta");      // yyyy-MM-dd
                                    String horario = obj.getString("horarioConsulta"); // HH:mm:ss
                                    String especialidade = obj.getString("especialidadeConsulta");
                                    String medico = obj.getString("nome_completoMedico");

                                    // Ajustar horário (remover segundos)
                                    if (horario.length() == 8) {
                                        horario = horario.substring(0,5);
                                    }

                                    String descricao =
                                            "Médico: " + medico +
                                                    " | Especialidade: " + especialidade +
                                                    " | Horário: " + horario;

                                    adicionarAgendamento(data, descricao);
                                }

                                marcarAgendamentosNoCalendario();

                            } catch (Exception e) {
                                e.printStackTrace();
                                txtDetalhes.setText("Erro ao carregar consultas.");
                            }
                        },
                        error -> {
                            error.printStackTrace();
                            txtDetalhes.setText("Falha ao conectar ao servidor.");
                        }
                );

        com.android.volley.toolbox.JsonObjectRequest request1 =
                new com.android.volley.toolbox.JsonObjectRequest(
                        com.android.volley.Request.Method.GET,
                        URL_LISTAR_VACINAS,
                        null,
                        response -> {
                            try {
                                boolean success = response.getBoolean("success");
                                if (!success) {
                                    txtDetalhes.setText("Nenhum agendamento encontrado.");
                                    return;
                                }

                                org.json.JSONArray array = response.getJSONArray("vacinas");

                                for (int i = 0; i < array.length(); i++) {

                                    org.json.JSONObject obj = array.getJSONObject(i);

                                    String data = obj.getString("dataVacina");      // yyyy-MM-dd
                                    String horario = obj.getString("horarioVacina"); // HH:mm:ss
                                    String vacina = obj.getString("tipoVacina");

                                    // Ajustar horário (remover segundos)
                                    if (horario.length() == 8) {
                                        horario = horario.substring(0,5);
                                    }

                                    String descricao =
                                                    "Vacina: " + vacina +
                                                    " | Horário: " + horario;

                                    adicionarAgendamento(data, descricao);
                                }

                                marcarAgendamentosNoCalendario();

                            } catch (Exception e) {
                                e.printStackTrace();
                                txtDetalhes.setText("Erro ao carregar vacinas.");
                            }
                        },
                        error -> {
                            error.printStackTrace();
                            txtDetalhes.setText("Falha ao conectar ao servidor.");
                        }
                );

        com.android.volley.toolbox.JsonObjectRequest req =
                new com.android.volley.toolbox.JsonObjectRequest(
                        com.android.volley.Request.Method.GET,
                        URL_LISTAR_EXAMES,
                        null,
                        response -> {
                            try {
                                boolean success = response.getBoolean("success");
                                if (!success) {
                                    txtDetalhes.setText("Nenhum agendamento encontrado.");
                                    return;
                                }

                                org.json.JSONArray array = response.getJSONArray("exames");

                                for (int i = 0; i < array.length(); i++) {

                                    org.json.JSONObject obj = array.getJSONObject(i);

                                    String data = obj.getString("dataExame");      // yyyy-MM-dd
                                    String horario = obj.getString("horarioExame"); // HH:mm:ss
                                    String exame = obj.getString("tipoExame");
                                    String medico = obj.getString("nome_completoMedico");

                                    // Ajustar horário (remover segundos)
                                    if (horario.length() == 8) {
                                        horario = horario.substring(0,5);
                                    }

                                    String descricao =
                                            "Médico: " + medico +
                                                    " | Tipo de exame: " + exame +
                                                    " | Horário: " + horario;

                                    adicionarAgendamento(data, descricao);
                                }

                                marcarAgendamentosNoCalendario();

                            } catch (Exception e) {
                                e.printStackTrace();
                                txtDetalhes.setText("Erro ao carregar exames.");
                            }
                        },
                        error -> {
                            error.printStackTrace();
                            txtDetalhes.setText("Falha ao conectar ao servidor.");
                        }
                );

        com.android.volley.toolbox.Volley.newRequestQueue(requireContext()).add(request1);
        com.android.volley.toolbox.Volley.newRequestQueue(requireContext()).add(request);
        com.android.volley.toolbox.Volley.newRequestQueue(requireContext()).add(req);
    }


    private void marcarAgendamentosNoCalendario() {
        List<CalendarDay> diasComAgendamento = new ArrayList<>();
        for (String data : agendamentos.keySet()) {
            try {
                String[] partes = data.split("-");
                int ano = Integer.parseInt(partes[0]);
                int mes = Integer.parseInt(partes[1]) - 1;
                int dia = Integer.parseInt(partes[2]);
                diasComAgendamento.add(CalendarDay.from(ano, mes, dia));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        calendarView.addDecorator(new ConsultaDecorator(diasComAgendamento));
    }

    private void adicionarAgendamento(String data, String descricao) {
        if (!agendamentos.containsKey(data)) {
            agendamentos.put(data, new ArrayList<>());
        }
        agendamentos.get(data).add(descricao);
    }

    private String normalizarData(String data) {
        if (data.contains("/")) {
            try {
                String[] partes = data.split("/");
                return String.format("%04d-%02d-%02d",
                        Integer.parseInt(partes[2]),
                        Integer.parseInt(partes[1]),
                        Integer.parseInt(partes[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    private void mostrarDialogAlteracao() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Alterar Agendamento")
                .setMessage("Deseja alterar o agendamento do dia " + dataSelecionada + "?")
                .setPositiveButton("Sim", (dialog, which) -> {

                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
