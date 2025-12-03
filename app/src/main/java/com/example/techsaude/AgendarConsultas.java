package com.example.techsaude;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AgendarConsultas extends AppCompatActivity {

    AutoCompleteTextView autoEspecialidade, autoMedico, autoHorario;
    EditText editTextDate;
    Button btnConfirmarConsulta;
    ImageView btnVoltar;

    RequestQueue queue;

    String BASE_URL = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/";

    String URL_LISTA_ESPECIALIDADES = BASE_URL + "lista_especialidade.php";
    String URL_LISTA_MEDICOS = BASE_URL + "lista_medico.php?especialidade=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendar_consultas);

        queue = Volley.newRequestQueue(this);

        btnVoltar = findViewById(R.id.btnVoltarConsulta);
        autoEspecialidade = findViewById(R.id.auto_complete_especialidade);
        autoMedico = findViewById(R.id.auto_complete_medico);
        autoHorario = findViewById(R.id.auto_complete_time);
        editTextDate = findViewById(R.id.editTextDate);
        btnConfirmarConsulta = findViewById(R.id.btnConfirmarConsulta);


        // Carrega lista de especialidades ao abrir o app
        carregarEspecialidades();

        btnVoltar.setOnClickListener(v -> finish());

        // Quando o usuário escolhe uma especialidade → carrega os médicos
        autoEspecialidade.setOnItemClickListener((parent, view, position, id) -> {
            String esp = parent.getItemAtPosition(position).toString();
            carregarMedicos(esp);
        });

        editTextDate.setOnClickListener(v -> showDatePicker());

        btnConfirmarConsulta.setOnClickListener(v -> salvarConsultaLocal());


    }

    // CARREGAR ESPECIALIDADES
    private void carregarEspecialidades() {

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                URL_LISTA_ESPECIALIDADES,
                null,
                response -> {

                    ArrayList<String> lista = new ArrayList<>();

                    try {
                        JSONArray array = response.getJSONArray("data");

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            lista.add(obj.getString("especialidadeMedico"));
                        }

                    } catch (Exception e) { e.printStackTrace(); }

                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<>(this,
                                    android.R.layout.simple_list_item_1,
                                    lista);

                    autoEspecialidade.setThreshold(1);
                    autoEspecialidade.setAdapter(adapter);
                },
                error -> {
                    Log.e("VOLLEY_ERROR", error.toString());
                }
        );

        queue.add(request);
    }


    // CARREGAR MÉDICOS PELA ESPECIALIDADE
    private void carregarMedicos(String especialidade) {

        // Encode da especialidade (IMPORTANTÍSSIMO)
        String especialidadeEncoded = Uri.encode(especialidade);

        String url = URL_LISTA_MEDICOS + especialidadeEncoded;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    ArrayList<String> medicos = new ArrayList<>();

                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            medicos.add("Dr. " + obj.getString("nome_completoMedico"));
                        }
                    } catch (Exception e) { e.printStackTrace(); }

                    autoMedico.setAdapter(
                            new ArrayAdapter<>(this,
                                    android.R.layout.simple_dropdown_item_1line,
                                    medicos)
                    );

                    autoMedico.setText("");
                },
                error -> error.printStackTrace()
        );

        queue.add(request);
    }

    // ENVIAR CONSULTA PARA O SERVIDOR

    private void salvarConsultaLocal() {

        String especialidade = autoEspecialidade.getText().toString();
        String medico = autoMedico.getText().toString();
        String data = editTextDate.getText().toString();
        String horario = autoHorario.getText().toString();
        String valor = "120.00"; // Ou puxar dinamicamente


        if (especialidade.isEmpty() || medico.isEmpty() || data.isEmpty() || horario.isEmpty()) {
            // Campos obrigatórios
            autoEspecialidade.setError("Selecione a especialidade");
            autoMedico.setError("Selecione o médico");
            editTextDate.setError("Selecione o dia");
            autoHorario.setError("Selecione o horário ");
            return;
        }
        SharedPreferences prefs = getSharedPreferences("user_prefs_agendamentos", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String medicoFormatado = medico.replace("Dr. ", "").trim();

        editor.putString("especialidadeConsulta", especialidade);
        editor.putString("medicoConsulta", medicoFormatado);
        editor.putString("dataConsulta", data);
        editor.putString("horarioConsulta", horario);
        editor.putString("valorConsulta", valor);
        editor.putString("statusConsulta", "Agendada");

        editor.apply();

        // Depois de salvar, vai para a tela de pagamento
        Intent it = new Intent(AgendarConsultas.this, FormaPagamento.class);
        startActivity(it);
    }

    // DATE PICKER + HORÁRIOS
    private void showDatePicker() {

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view, int year1, int month1, int dayOfMonth) -> {

                    String selectedDate = String.format("%02d/%02d/%04d",
                            dayOfMonth, (month1 + 1), year1);

                    editTextDate.setText(selectedDate);
                },
                year, month, day
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();

        // Lista de horários
        String[] horas = {
                "08:00","09:00","10:00","11:00",
                "13:00","14:00","15:00","16:00","17:00"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                horas
        ) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextSize(20);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextSize(20);
                return tv;
            }
        };

        autoHorario.setAdapter(adapter);
        autoHorario.setOnClickListener(v -> autoHorario.showDropDown());
    }
}
