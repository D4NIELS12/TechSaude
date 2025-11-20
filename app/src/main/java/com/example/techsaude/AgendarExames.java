package com.example.techsaude;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import java.util.ArrayList;
import java.util.Calendar;

public class AgendarExames extends AppCompatActivity {

    private EditText editDate;
    private AutoCompleteTextView autoCompleteTime;
    ImageView Voltar;
    DatabaseHelper dbHelper;
    Button ConfirmarExame;
    AutoCompleteTextView autoCompleteExame, autoCompleteMedico;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendar_exames);

        dbHelper = new DatabaseHelper(this);

        autoCompleteExame = (AutoCompleteTextView) findViewById(R.id.auto_complete_exame);
        autoCompleteMedico = (AutoCompleteTextView) findViewById(R.id.auto_complete_medico);
        editDate = (EditText) findViewById(R.id.editTextDate);
        Voltar = (ImageView) findViewById(R.id.btnVoltarConsulta);
        autoCompleteTime = (AutoCompleteTextView) findViewById(R.id.auto_complete_time);
        ConfirmarExame = (Button) findViewById(R.id.ConfirmarExame);
        Voltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        carregarExame();

        Voltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        autoCompleteExame.setOnItemClickListener((parent, view, position, id) -> {
            String exameSelecionada = parent.getItemAtPosition(position).toString();
            carregarMedicos(exameSelecionada);
        });


        editDate.setOnClickListener(v -> showDatePicker());

        ConfirmarExame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(AgendarExames.this, FormaPagamento.class);
                startActivity(it);

                SharedPreferences prefs = getSharedPreferences("user_prefs_agendamentos", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

// Exemplo salvando informações do usuário
                editor.putString("exame", String.valueOf(autoCompleteExame.getText()));
                editor.putString("medico_exame", String.valueOf(autoCompleteMedico.getText()));
                editor.putString("dia_exame", editDate.getText().toString());
                editor.putString("hora_exame", String.valueOf(autoCompleteTime.getText()));

// Confirma o salvamento
                editor.apply(); // ou editor.commit();

            }
        });
    }
    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,(DatePicker view, int year1, int month1, int dayOfMonth) -> {
            String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, (month1 + 1), year1);
            editDate.setText(selectedDate);
        },
                year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        datePickerDialog.show();

        String[] availableTimes = {
                "08:00", "09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, availableTimes) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextSize(20); // aumenta o texto do item selecionado
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                textView.setTextSize(20); // aumenta o texto da lista suspensa
                return textView;
            }
        };
        autoCompleteTime.setAdapter(adapter);

        autoCompleteTime.setOnClickListener(v -> autoCompleteTime.showDropDown());
    }
    private void carregarExame() {
        ArrayList<String> exames = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT DISTINCT especialidadeMedico FROM TB_Medico", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                exames.add(cursor.getString(0));
            }
            cursor.close();
        }
        db.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, exames);

        autoCompleteExame.setAdapter(adapter);
    }

    private void carregarMedicos(String especialidade) {
        ArrayList<String> medicos = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT nome_completoMedico FROM TB_Medico WHERE especialidadeMedico = ?",
                new String[]{especialidade});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String nomeMedico = cursor.getString(0).trim();

                // Evita duplicar "Dr."
                if (!nomeMedico.toLowerCase().startsWith("dr.")) {
                    nomeMedico = "Dr. " + nomeMedico;
                }

                medicos.add(nomeMedico);
            }
            cursor.close();
        }
        db.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, medicos);

        autoCompleteMedico.setAdapter(adapter);
        autoCompleteMedico.setText(""); // limpa o campo
    }

}