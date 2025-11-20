package com.example.techsaude;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

public class Agendamentos extends Fragment {

    private MaterialCalendarView calendarView;
    private TextView txtDetalhes;
    private Button btnAlterar;
    private DatabaseHelper dbHelper;

    // Mapa com data -> lista de descrições
    private HashMap<String, List<String>> agendamentos = new HashMap<>();

    // Data selecionada no calendário
    private String dataSelecionada = "";

    public Agendamentos() {
        super(R.layout.activity_agendamentos);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarView = view.findViewById(R.id.calendarView);
        txtDetalhes = view.findViewById(R.id.txtDetalhes);
        btnAlterar = view.findViewById(R.id.btnAlterarAgendamento); // Adicione no XML
        dbHelper = new DatabaseHelper(requireContext());

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
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // CONSULTAS
        Cursor c1 = db.rawQuery("SELECT especialidadeConsulta, medicoConsulta, dataConsulta, horarioConsulta FROM TB_Consulta", null);
        if (c1.moveToFirst()) {
            do {
                String especialidade = c1.getString(0);
                String medico = c1.getString(1);
                String data = normalizarData(c1.getString(2));
                String hora = c1.getString(3);
                adicionarAgendamento(data, "Consulta (" + especialidade + ") com " + medico + " às " + hora);
            } while (c1.moveToNext());
        }
        c1.close();

        // EXAMES
        Cursor c2 = db.rawQuery("SELECT tipoExame, medicoExame, dataExame, horarioExame FROM TB_Exame", null);
        if (c2.moveToFirst()) {
            do {
                String tipo = c2.getString(0);
                String medico = c2.getString(1);
                String data = normalizarData(c2.getString(2));
                String hora = c2.getString(3);
                adicionarAgendamento(data, "Exame (" + tipo + ") com " + medico + " às " + hora);
            } while (c2.moveToNext());
        }
        c2.close();

        // VACINAS
        Cursor c3 = db.rawQuery("SELECT tipoVacina, dataVacina FROM TB_Vacina", null);
        if (c3.moveToFirst()) {
            do {
                String tipo = c3.getString(0);
                String data = normalizarData(c3.getString(1));
                adicionarAgendamento(data, "Vacina: " + tipo);
            } while (c3.moveToNext());
        }
        c3.close();

        db.close();
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
