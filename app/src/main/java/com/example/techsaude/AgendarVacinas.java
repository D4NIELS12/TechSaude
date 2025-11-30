package com.example.techsaude;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;

public class AgendarVacinas extends AppCompatActivity {

    private EditText editDate;
    private ImageView Voltar;
    private AutoCompleteTextView autoCompleteTime;
    private Button ConfirmarVacina;
    private AutoCompleteTextView autocompleteVacinas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendar_vacinas);

        editDate = findViewById(R.id.editTextDate);
        Voltar = findViewById(R.id.btnVoltarConsulta);
        autocompleteVacinas = findViewById(R.id.auto_complete_vacina);
        autoCompleteTime = findViewById(R.id.auto_complete_time);
        ConfirmarVacina = findViewById(R.id.btnConfirmarConsulta);

        // --- Botão de voltar ---
        Voltar.setOnClickListener(v -> finish());

        // --- Lista de vacinas ---
        String[] Vacinas = new String[]{
                "Covid-19",
                "Influenza (Gripe)",
                "Hepatite B",
                "Tétano",
                "Febre Amarela",
                "HPV",
                "Meningite",
                "Sarampo",
                "Raiva"
        };

        ArrayAdapter<String> adapterVacina = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                Vacinas
        );
        autocompleteVacinas.setAdapter(adapterVacina);

        // --- Lista de horários ---
        String[] availableTimes = {
                "08:00", "09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00"
        };

        ArrayAdapter<String> adapterTime = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, availableTimes) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextSize(20);
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                textView.setTextSize(20);
                return textView;
            }
        };
        autoCompleteTime.setAdapter(adapterTime);
        autoCompleteTime.setOnClickListener(v -> autoCompleteTime.showDropDown());

        // --- Calendário ---
        editDate.setOnClickListener(v -> showDatePicker());

        // --- Botão confirmar ---
        ConfirmarVacina.setOnClickListener(v -> {
            String vacinaSelecionada = autocompleteVacinas.getText().toString().trim();
            String dataSelecionada = editDate.getText().toString().trim();
            String horaSelecionada = autoCompleteTime.getText().toString().trim();

            if (vacinaSelecionada.isEmpty() || dataSelecionada.isEmpty() || horaSelecionada.isEmpty()) {
                // Campos obrigatórios
                autocompleteVacinas.setError("Selecione a vacina");
                editDate.setError("Escolha a data");
                autoCompleteTime.setError("Escolha o horário");
                return;
            }

            String valor = "120.00";

            // --- Salvando no SharedPreferences ---
            SharedPreferences prefs = getSharedPreferences("user_prefs_agendamentos", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.clear(); // limpa dados anteriores
            editor.putString("vacina", vacinaSelecionada);
            editor.putString("data_vacina", dataSelecionada);
            editor.putString("hora_vacina", horaSelecionada);
            editor.putString("valor_vacina", valor);
            editor.putString("status_vacina", "Agendado");
            editor.apply();

            // Vai para a tela de pagamento
            Intent it = new Intent(AgendarVacinas.this, FormaPagamento.class);
            startActivity(it);
        });
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (DatePicker view, int year1, int month1, int dayOfMonth) -> {
            String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, (month1 + 1), year1);
            editDate.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }


}
