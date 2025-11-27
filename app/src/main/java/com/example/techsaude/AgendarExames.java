package com.example.techsaude;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AgendarExames extends AppCompatActivity {

    private EditText editDate;
    private AutoCompleteTextView autoCompleteTime;
    private ImageView Voltar;
    private Button ConfirmarExame;
    private AutoCompleteTextView autoCompleteExame, autoCompleteMedico;

    // Lista filtrada de especialidades do banco
    private ArrayList<String> especialidadesDisponiveis = new ArrayList<>();

    // Mapeamento exame -> especialidade
    private HashMap<String, String> mapaExameParaEspecialidade = new HashMap<>();

    // Listas de médicos retornados pela API
    private ArrayList<String> listaMedicosNomes = new ArrayList<>();
    private ArrayList<Integer> listaMedicosIds = new ArrayList<>();

    // URLs
    private static final String URL_BUSCAR_ESPECIALIDADES =
            "http://tcc3edsmodetecgr3.hospedagemdesites.ws/lista_especialidade.php";

    private static final String URL_BUSCAR_MEDICOS =
            "http://tcc3edsmodetecgr3.hospedagemdesites.ws/buscar_medicos_por_especialidade.php";

    private static final String URL_SALVAR_EXAME =
            "http://tcc3edsmodetecgr3.hospedagemdesites.ws/salvar_exame.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendar_exames);

        autoCompleteExame = findViewById(R.id.auto_complete_exame);
        autoCompleteMedico = findViewById(R.id.auto_complete_medico);
        editDate = findViewById(R.id.editTextDate);
        Voltar = findViewById(R.id.btnVoltarConsulta);
        autoCompleteTime = findViewById(R.id.auto_complete_time);
        ConfirmarExame = findViewById(R.id.ConfirmarExame);

        Voltar.setOnClickListener(v -> finish());

        prepararHorarios();
        prepararMapasExames();
        configurarListeners();

    }

    private void prepararHorarios() {
        String[] availableTimes = {
                "08:00","09:00","10:00","11:00",
                "13:00","14:00","15:00","16:00"
        };

        ArrayAdapter<String> adapterTimes =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, availableTimes) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        TextView textView = (TextView) super.getView(position, convertView, parent);
                        textView.setTextSize(18);
                        return textView;
                    }
                };

        autoCompleteTime.setAdapter(adapterTimes);
        autoCompleteTime.setOnClickListener(v -> autoCompleteTime.showDropDown());
    }

    private void prepararMapasExames() {

        // Cardiologia
        mapaExameParaEspecialidade.put("Eletrocardiograma (ECG)", "Cardiologia");
        mapaExameParaEspecialidade.put("Ecocardiograma", "Cardiologia");
        mapaExameParaEspecialidade.put("Teste Ergométrico", "Cardiologia");

        // Dermatologia
        mapaExameParaEspecialidade.put("Biópsia de Pele", "Dermatologia");
        mapaExameParaEspecialidade.put("Dermatoscopia", "Dermatologia");

        // Endocrinologia
        mapaExameParaEspecialidade.put("Hemoglobina Glicada (HbA1c)", "Endocrinologia");
        mapaExameParaEspecialidade.put("Glicemia em Jejum", "Endocrinologia");
        mapaExameParaEspecialidade.put("TSH / T4 Livre", "Endocrinologia");

        // Gastroenterologia
        mapaExameParaEspecialidade.put("Endoscopia Digestiva", "Gastroenterologia");
        mapaExameParaEspecialidade.put("Colonoscopia", "Gastroenterologia");
        mapaExameParaEspecialidade.put("Exame de Fezes", "Gastroenterologia");

        // Ginecologia
        mapaExameParaEspecialidade.put("Ultrassom Transvaginal", "Ginecologia");
        mapaExameParaEspecialidade.put("Papanicolau", "Ginecologia");
        mapaExameParaEspecialidade.put("Mamografia", "Ginecologia");

        // etc... (resto igual ao que você tinha)
    }

    private void carregarExamesFiltrados() {
        ArrayList<String> examesValidos = new ArrayList<>();

        for (String exame : mapaExameParaEspecialidade.keySet()) {
            String esp = mapaExameParaEspecialidade.get(exame);

            if (especialidadesDisponiveis.contains(esp)) {
                examesValidos.add(exame);
            }
        }

        java.util.Collections.sort(examesValidos);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, examesValidos);

        autoCompleteExame.setAdapter(adapter);
        autoCompleteExame.setOnClickListener(v -> autoCompleteExame.showDropDown());
    }

    private void configurarListeners() {

        autoCompleteExame.setOnItemClickListener((parent, view, position, id) -> {
            String selecionado = parent.getItemAtPosition(position).toString();

            String especialidade = mapaExameParaEspecialidade.get(selecionado);
            if (especialidade != null) {
                buscarMedicos(especialidade);
            }
        });

        editDate.setOnClickListener(v -> showDatePicker());

        ConfirmarExame.setOnClickListener(v -> confirmarEEnviarExame());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    String selectedDate =
                            String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year);
                    editDate.setText(selectedDate);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void buscarMedicos(String especialidade) {
        try {
            String url = URL_BUSCAR_MEDICOS +
                    "?especialidade=" + URLEncoder.encode(especialidade, "UTF-8");

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    response -> {
                        try {
                            boolean success = response.optBoolean("success", false);
                            if (!success) {
                                Toast.makeText(this, "Nenhum médico encontrado.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            JSONArray array = response.getJSONArray("medicos");

                            listaMedicosIds.clear();
                            listaMedicosNomes.clear();

                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);

                                int id = obj.getInt("idMedico");
                                String nome = obj.getString("nome");

                                if (!nome.toLowerCase().startsWith("dr"))
                                    nome = "Dr. " + nome;

                                listaMedicosIds.add(id);
                                listaMedicosNomes.add(nome);
                            }

                            ArrayAdapter<String> adapter =
                                    new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaMedicosNomes);

                            autoCompleteMedico.setAdapter(adapter);
                            autoCompleteMedico.showDropDown();

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Erro ao processar médicos", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(this, "Erro ao conectar", Toast.LENGTH_SHORT).show()
            );

            Volley.newRequestQueue(this).add(request);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao montar URL", Toast.LENGTH_SHORT).show();
        }
    }

    private int obterIdMedicoSelecionado(String exibicao) {
        for (int i = 0; i < listaMedicosNomes.size(); i++) {
            if (listaMedicosNomes.get(i).equalsIgnoreCase(exibicao))
                return listaMedicosIds.get(i);
        }
        return -1;
    }

    private void confirmarEEnviarExame() {
        String tipo = autoCompleteExame.getText().toString();
        String medico = autoCompleteMedico.getText().toString();
        String dataBR = editDate.getText().toString();
        String hora = autoCompleteTime.getText().toString();

        if (tipo.isEmpty() || medico.isEmpty() || dataBR.isEmpty() || hora.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        int idMedico = obterIdMedicoSelecionado(medico);
        if (idMedico == -1) {
            Toast.makeText(this, "Selecione um médico válido!", Toast.LENGTH_SHORT).show();
            return;
        }

        String dataMysql = converterDataParaMysql(dataBR);
        if (hora.length() == 5) hora += ":00";

        SharedPreferences prefs = getSharedPreferences("loginUsuario_prefs", MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", 0);

        if (idUsuario == 0) {
            Toast.makeText(this, "Usuário não identificado!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("idUsuario", idUsuario);
            json.put("idMedico", idMedico);
            json.put("tipoExame", tipo);
            json.put("dataExame", dataMysql);
            json.put("horarioExame", hora);
            json.put("valorExame", 0);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    URL_SALVAR_EXAME,
                    json,
                    response -> {
                        boolean success = response.optBoolean("success", false);
                        String msg = response.optString("message", success ? "Agendado" : "Erro");
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

                        if (success) {
                            startActivity(new Intent(this, FormaPagamento.class));
                            finish();
                        }
                    },
                    error -> Toast.makeText(this, "Erro ao enviar", Toast.LENGTH_SHORT).show()
            );

            Volley.newRequestQueue(this).add(request);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao criar requisição", Toast.LENGTH_SHORT).show();
        }
    }

    private String converterDataParaMysql(String dataBR) {
        try {
            String[] p = dataBR.split("/");
            return p[2] + "-" + p[1] + "-" + p[0];
        } catch (Exception e) {
            return dataBR;
        }
    }
}
