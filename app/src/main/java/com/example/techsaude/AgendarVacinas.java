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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

public class AgendarVacinas extends AppCompatActivity {

    private EditText editDate, editPreco;
    private ImageView Voltar;
    private AutoCompleteTextView autoCompleteTime;
    private Button ConfirmarVacina;
    private AutoCompleteTextView autocompleteVacinas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendar_vacinas);

        editPreco = findViewById(R.id.editPreco);
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

        autocompleteVacinas.setOnItemClickListener((parent, view, position, id) -> {
            editPreco.setText("89.90");
        });

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


            // --- Salvando no SharedPreferences ---
            SharedPreferences prefs = getSharedPreferences("user_prefs_agendamentos", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.clear(); // limpa dados anteriores
            editor.putString("vacina", vacinaSelecionada);
            editor.putString("data_vacina", dataSelecionada);
            editor.putString("hora_vacina", horaSelecionada);
            editor.putString("valor", editPreco.getText().toString());
            editor.putString("status_vacina", "Agendado");
            editor.apply();

            // Vai para a tela de pagamento
            Intent it = new Intent(AgendarVacinas.this, FormaPagamento.class);
            startActivity(it);
        });
    }

    private void carregarHorarios(String data) {

        String url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/get_horarios_disponiveis_vacina.php?data=" + data;

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);

                        if (json.getBoolean("success")) {
                            JSONArray horariosArray = json.getJSONArray("horarios");

                            String[] horarios = new String[horariosArray.length()];

                            for (int i = 0; i < horariosArray.length(); i++) {
                                horarios[i] = horariosArray.getString(i);
                            }

                            // Atualiza o AutoCompleteTextView
                            ArrayAdapter<String> adapterTime = new ArrayAdapter<>(
                                    AgendarVacinas.this,
                                    android.R.layout.simple_dropdown_item_1line,
                                    horarios
                            );

                            autoCompleteTime.setAdapter(adapterTime);
                            autoCompleteTime.showDropDown(); // mostra automaticamente

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    error.printStackTrace();
                });

        queue.add(request);
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {

                    String selectedDate = String.format("%04d-%02d-%02d", year1, (month1 + 1), dayOfMonth);
                    editDate.setText(selectedDate);

                    carregarHorarios(selectedDate);

                }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
}
