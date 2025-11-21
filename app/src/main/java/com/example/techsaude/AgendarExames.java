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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class AgendarExames extends AppCompatActivity {

    private EditText editDate;
    private AutoCompleteTextView autoCompleteTime;
    private ImageView Voltar;
    private Button ConfirmarExame;
    private AutoCompleteTextView autoCompleteExame, autoCompleteMedico;

    // Map para relacionar exame -> especialidade (para buscar médicos)
    private HashMap<String, String> mapaExameParaEspecialidade = new HashMap<>();

    // listas para médicos retornados pela API (nomes e ids)
    private ArrayList<String> listaMedicosNomes = new ArrayList<>();
    private ArrayList<Integer> listaMedicosIds = new ArrayList<>();

    // URL endpoints (ajuste se necessário)
    private static final String URL_BUSCAR_MEDICOS = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/buscar_medicos_especialidade.php";
    private static final String URL_SALVAR_EXAME = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/salvar_exame.php";

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
        carregarExame(); // popula dropdown de exames (com categorias)
        configurarListeners();
    }

    private void prepararHorarios() {
        String[] availableTimes = {
                "08:00", "09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00"
        };

        ArrayAdapter<String> adapterTimes = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, availableTimes) {
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

    // Mapeia cada exame para sua especialidade (usado para buscar médicos)
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

        // Neurologia
        mapaExameParaEspecialidade.put("Ressonância Magnética do Crânio", "Neurologia");
        mapaExameParaEspecialidade.put("Eletroencefalograma (EEG)", "Neurologia");

        // Ortopedia
        mapaExameParaEspecialidade.put("Raio-X", "Ortopedia");
        mapaExameParaEspecialidade.put("Ressonância Magnética", "Ortopedia");
        mapaExameParaEspecialidade.put("Ultrassom Musculoesquelético", "Ortopedia");

        // Pediatria
        mapaExameParaEspecialidade.put("Hemograma Infantil", "Pediatria");
        mapaExameParaEspecialidade.put("Exame de Urina (Infantil)", "Pediatria");

        // Psiquiatria
        mapaExameParaEspecialidade.put("Exames laboratoriais gerais", "Psiquiatria");
        mapaExameParaEspecialidade.put("Avaliação Neuropsicológica", "Psiquiatria");

        // Urologia
        mapaExameParaEspecialidade.put("Ultrassom das Vias Urinárias", "Urologia");
        mapaExameParaEspecialidade.put("PSA", "Urologia");

        // Oftalmologia
        mapaExameParaEspecialidade.put("Acuidade Visual", "Oftalmologia");
        mapaExameParaEspecialidade.put("Mapeamento de Retina", "Oftalmologia");

        // Otorrino
        mapaExameParaEspecialidade.put("Audiometria", "Otorrinolaringologia");
        mapaExameParaEspecialidade.put("Nasofibroscopia", "Otorrinolaringologia");

        // Reumatologia
        mapaExameParaEspecialidade.put("Raio-X das Articulações", "Reumatologia");
        mapaExameParaEspecialidade.put("Fator Reumatoide", "Reumatologia");
        mapaExameParaEspecialidade.put("PCR", "Reumatologia");
    }

    // Popula o AutoCompleteTextView de exames (com categorias visuais)
    private void carregarExame() {
        String[] tiposExames = {
                "— Cardiologia —",
                "Eletrocardiograma (ECG)",
                "Ecocardiograma",
                "Teste Ergométrico",

                "— Dermatologia —",
                "Biópsia de Pele",
                "Dermatoscopia",

                "— Endocrinologia —",
                "Hemoglobina Glicada (HbA1c)",
                "Glicemia em Jejum",
                "TSH / T4 Livre",

                "— Gastroenterologia —",
                "Endoscopia Digestiva",
                "Colonoscopia",
                "Exame de Fezes",

                "— Ginecologia —",
                "Ultrassom Transvaginal",
                "Papanicolau",
                "Mamografia",

                "— Neurologia —",
                "Ressonância Magnética do Crânio",
                "Eletroencefalograma (EEG)",

                "— Ortopedia —",
                "Raio-X",
                "Ressonância Magnética",
                "Ultrassom Musculoesquelético",

                "— Pediatria —",
                "Hemograma Infantil",
                "Exame de Urina (Infantil)",

                "— Psiquiatria —",
                "Exames laboratoriais gerais",
                "Avaliação Neuropsicológica",

                "— Urologia —",
                "Ultrassom das Vias Urinárias",
                "PSA",

                "— Oftalmologia —",
                "Acuidade Visual",
                "Mapeamento de Retina",

                "— Otorrinolaringologia —",
                "Audiometria",
                "Nasofibroscopia",

                "— Reumatologia —",
                "Raio-X das Articulações",
                "Fator Reumatoide",
                "PCR"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tiposExames);
        autoCompleteExame.setAdapter(adapter);
        autoCompleteExame.setOnClickListener(v -> autoCompleteExame.showDropDown());
    }

    private void configurarListeners() {

        // Quando o usuário seleciona um EXAME, decidimos a especialidade e buscamos médicos
        autoCompleteExame.setOnItemClickListener((parent, view, position, id) -> {
            String selecionado = parent.getItemAtPosition(position).toString();

            // Ignorar cabeçalhos (ex.: "— Cardiologia —")
            if (selecionado.startsWith("—")) {
                autoCompleteExame.setText("");
                return;
            }

            String especialidade = mapaExameParaEspecialidade.get(selecionado);
            if (especialidade != null && !especialidade.isEmpty()) {
                buscarMedicos(especialidade);
            } else {
                // se não achou no mapa, limpa médicos
                listaMedicosNomes.clear();
                listaMedicosIds.clear();
                autoCompleteMedico.setAdapter(null);
            }
        });

        // Data picker para o campo de data
        editDate.setOnClickListener(v -> showDatePicker());

        // Ao clicar em confirmar, tenta salvar o exame
        ConfirmarExame.setOnClickListener(v -> confirmarEEnviarExame());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (DatePicker view, int year1, int month1, int dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, (month1 + 1), year1);
            editDate.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    // Chama o endpoint que retorna médicos por especialidade
    private void buscarMedicos(String especialidade) {
        try {
            String url = URL_BUSCAR_MEDICOS + "?especialidade=" + java.net.URLEncoder.encode(especialidade, "UTF-8");
            RequestQueue queue = Volley.newRequestQueue(this);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            boolean success = response.optBoolean("success", false);
                            if (!success) {
                                Toast.makeText(AgendarExames.this, "Nenhum médico encontrado para " + especialidade, Toast.LENGTH_SHORT).show();
                                return;
                            }

                            JSONArray array = response.getJSONArray("medicos");
                            listaMedicosNomes.clear();
                            listaMedicosIds.clear();

                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                int idMed = obj.getInt("idMedico");
                                String nome = obj.getString("nome");

                                // opcional: exibir "Dr." se não tiver
                                String exibicao = nome;
                                if (!exibicao.toLowerCase().startsWith("dr")) {
                                    exibicao = "Dr. " + exibicao;
                                }

                                listaMedicosIds.add(idMed);
                                listaMedicosNomes.add(exibicao);
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaMedicosNomes);
                            autoCompleteMedico.setAdapter(adapter);
                            autoCompleteMedico.showDropDown();

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(AgendarExames.this, "Erro ao processar médicos", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Toast.makeText(AgendarExames.this, "Erro ao conectar com o servidor", Toast.LENGTH_SHORT).show();
                    }
            );

            queue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao montar URL", Toast.LENGTH_SHORT).show();
        }
    }

    // Tenta obter idMedico com base no nome exibido atual no campo (procura na lista carregada)
    private int obterIdMedicoSelecionado(String exibicao) {
        for (int i = 0; i < listaMedicosNomes.size(); i++) {
            if (listaMedicosNomes.get(i).equalsIgnoreCase(exibicao)) {
                return listaMedicosIds.get(i);
            }
        }
        // tentativa alternativa: remover "Dr." caso usuário tenha digitado sem prefixo
        String semDr = exibicao.replaceFirst("(?i)dr\\.?", "").trim();
        for (int i = 0; i < listaMedicosNomes.size(); i++) {
            String nomeSemDr = listaMedicosNomes.get(i).replaceFirst("(?i)dr\\.?", "").trim();
            if (nomeSemDr.equalsIgnoreCase(semDr)) {
                return listaMedicosIds.get(i);
            }
        }
        return -1;
    }

    private void confirmarEEnviarExame() {
        String tipoExameSelecionado = autoCompleteExame.getText().toString().trim();
        String medicoSelecionado = autoCompleteMedico.getText().toString().trim();
        String dataBR = editDate.getText().toString().trim();
        String hora = autoCompleteTime.getText().toString().trim();

        if (tipoExameSelecionado.isEmpty() || medicoSelecionado.isEmpty() || dataBR.isEmpty() || hora.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // encontrar idMedico
        int idMedico = obterIdMedicoSelecionado(medicoSelecionado);
        if (idMedico == -1) {
            Toast.makeText(this, "Selecione um médico válido da lista", Toast.LENGTH_LONG).show();
            return;
        }

        // converter data BR -> yyyy-MM-dd
        String dataMysql = converterDataParaMysql(dataBR);
        String horaMysql = hora;
        if (horaMysql.length() == 5) horaMysql += ":00";

        // pegar idUsuario
        SharedPreferences prefs = getSharedPreferences("loginUsuario_prefs", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", 0);
        if (idUsuario == 0) {
            Toast.makeText(this, "Usuário não identificado. Faça login.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("idUsuario", idUsuario);
            jsonBody.put("idMedico", idMedico);
            jsonBody.put("tipoExame", tipoExameSelecionado);
            jsonBody.put("dataExame", dataMysql);
            jsonBody.put("horarioExame", horaMysql);
            jsonBody.put("valorExame", 0);

            RequestQueue queue = Volley.newRequestQueue(this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL_SALVAR_EXAME, jsonBody,
                    response -> {
                        try {
                            boolean success = response.optBoolean("success", false);
                            String message = response.optString("message", success ? "Agendado com sucesso" : "Erro");
                            Toast.makeText(AgendarExames.this, message, Toast.LENGTH_LONG).show();

                            if (success) {
                                // opcional: abrir tela de pagamento
                                Intent it = new Intent(AgendarExames.this, FormaPagamento.class);
                                // você pode passar extras se necessário
                                startActivity(it);
                                finish();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(AgendarExames.this, "Resposta inválida do servidor", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Toast.makeText(AgendarExames.this, "Erro na requisição ao servidor", Toast.LENGTH_SHORT).show();
                    }
            );

            queue.add(request);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao montar requisição", Toast.LENGTH_SHORT).show();
        }
    }

    private String converterDataParaMysql(String dataBR) {
        try {
            String[] partes = dataBR.split("/");
            String dia = partes[0];
            String mes = partes[1];
            String ano = partes[2];
            return ano + "-" + mes + "-" + dia;
        } catch (Exception e) {
            e.printStackTrace();
            return dataBR;
        }
    }
}
