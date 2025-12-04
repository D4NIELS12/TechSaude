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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AgendarExames extends AppCompatActivity {

    private EditText editDate;
    private AutoCompleteTextView autoCompleteTime;
    private ImageView Voltar;
    private Button ConfirmarExame;
    private AutoCompleteTextView autoCompleteExame, autoCompleteMedico;

    // especialidades que existem no banco (obtidas do servidor)
    private ArrayList<String> especialidadesDisponiveis = new ArrayList<>();

    // mapa local especialidade -> lista de exames (fonte "verdadeira" está no servidor, mas mantemos mapa para identificar especialidade por exame)
    private HashMap<String, List<String>> examesPorEspecialidade = new HashMap<>();
    private HashMap<String, String> especialidadeDoExameServidor = new HashMap<>();


    // lista de médicos retornados pelo servidor
    private ArrayList<String> listaMedicosNomes = new ArrayList<>();
    private ArrayList<Integer> listaMedicosIds = new ArrayList<>();

    // lista agregada de exames (para exibir no AutoComplete)
    private ArrayList<String> examesDisponiveis = new ArrayList<>();

    // URLs (ajuste se necessário)
    private static final String URL_BUSCAR_ESPECIALIDADES =
            "http://tcc3edsmodetecgr3.hospedagemdesites.ws/listar_especialidade_exame.php";

    private static final String URL_BUSCAR_EXAMES_POR_ESP =
            "http://tcc3edsmodetecgr3.hospedagemdesites.ws/listar_exame_por_especialidade.php";

    private static final String URL_BUSCAR_MEDICOS =
            "http://tcc3edsmodetecgr3.hospedagemdesites.ws/listar_medicos_da_especialidade.php";

    private static final String URL_SALVAR_EXAME =
            "http://tcc3edsmodetecgr3.hospedagemdesites.ws/salvar_exame.php";

    // lista oficial permitida
    private final String[] especialidadesPermitidas = {
            "Cardiologia", "Dermatologia", "Endocrinologia", "Gastroenterologia",
            "Ginecologia", "Neurologia", "Ortopedia", "Pediatria", "Psiquiatria",
            "Urologia", "Oftalmologia", "Otorrinolaringologia", "Reumatologia"
    };

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

        configurarListeners();
        prepararHorarios();
        // início do fluxo: buscar especialidades do servidor e em seguida exames
        buscarEspecialidadesDoServidor();
    }
    private void prepararHorarios() {
        String[] times = {
                "08:00","09:00","10:00","11:00","13:00","14:00","15:00","16:00"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, times) {
            @Override public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent); tv.setTextSize(18); return tv; } };

        autoCompleteTime.setAdapter(adapter);
        autoCompleteTime.setOnClickListener(v -> autoCompleteTime.showDropDown());
    }

    // Mapa local para sabermos qual especialidade pertence a um exame (útil para buscar médicos depois)

    private void configurarListeners() {

        // Ao selecionar um exame, descobrir a especialidade desse exame (buscando no mapa local)
        autoCompleteExame.setOnItemClickListener((parent, view, position, id) -> {

            String exameEscolhido = parent.getItemAtPosition(position).toString();
            String especialidadeDoExame = especialidadeDoExameServidor.get(exameEscolhido);

            if (especialidadeDoExame != null) {
                buscarMedicos(especialidadeDoExame);
            } else {
                // Caso não encontre no mapa local, não buscar médicos
                Toast.makeText(this, "Especialidade do exame não encontrada localmente.", Toast.LENGTH_SHORT).show();
                listaMedicosNomes.clear();
                listaMedicosIds.clear();
                autoCompleteMedico.setAdapter(null);
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
                    String dt = String.format(Locale.getDefault(),
                            "%02d/%02d/%04d", day, month + 1, year);
                    editDate.setText(dt);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private boolean isEspecialidadePermitida(String esp) {
        for (String e : especialidadesPermitidas) {
            if (e.equalsIgnoreCase(esp)) return true;
        }
        return false;
    }

    // 1) buscar especialidades (GET) do servidor
    private void buscarEspecialidadesDoServidor() {

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                URL_BUSCAR_ESPECIALIDADES,
                null,
                response -> {
                    try {
                        boolean ok = response.optBoolean("success", false);
                        if (!ok) {
                            Toast.makeText(this, "Erro ao buscar especialidades", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONArray arr = response.optJSONArray("especialidades");
                        if (arr == null || arr.length() == 0) {
                            Toast.makeText(this, "Nenhuma especialidade encontrada no servidor.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        especialidadesDisponiveis.clear();

                        for (int i = 0; i < arr.length(); i++) {
                            String esp = arr.optString(i);
                            if (isEspecialidadePermitida(esp)) {
                                especialidadesDisponiveis.add(esp);
                            }
                        }

                        // Agora para cada especialidade disponível buscamos os exames correspondentes no servidor
                        if (!especialidadesDisponiveis.isEmpty()) {
                            buscarExamesDoServidorParaEspecialidades();
                        } else {
                            Toast.makeText(this, "Nenhuma especialidade permitida encontrada no servidor.", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erro ao processar especialidades", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Erro ao conectar para buscar especialidades", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(req);
    }

    // 2) para cada especialidade disponível, faz POST para obter exames e agrega
    private void buscarExamesDoServidorParaEspecialidades() {

        examesDisponiveis.clear();

        final int total = especialidadesDisponiveis.size();
        final int[] pendentes = {total};

        for (String esp : especialidadesDisponiveis) {

            try {
                JSONObject body = new JSONObject();
                body.put("especialidade", esp);

                JsonObjectRequest req = new JsonObjectRequest(
                        Request.Method.POST,
                        URL_BUSCAR_EXAMES_POR_ESP,
                        body,
                        response -> {
                            try {
                                boolean ok = response.optBoolean("success", false);
                                if (ok) {
                                    JSONArray arr = response.optJSONArray("exames");
                                    if (arr != null) {
                                        for (int i = 0; i < arr.length(); i++) {
                                            String exame = arr.optString(i);
                                            if (exame != null && !exame.isEmpty() && !examesDisponiveis.contains(exame)) {
                                                examesDisponiveis.add(exame);
                                                especialidadeDoExameServidor.put(exame, esp);
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                pendentes[0]--;
                                if (pendentes[0] <= 0) {
                                    carregarExamesFiltrados();
                                }
                            }
                        },
                        error -> {
                            // em erro ainda decrementa e prossegue
                            pendentes[0]--;
                            if (pendentes[0] <= 0) {
                                carregarExamesFiltrados();
                            }
                        }
                );

                Volley.newRequestQueue(this).add(req);

            } catch (Exception e) {
                e.printStackTrace();
                pendentes[0]--;
                if (pendentes[0] <= 0) {
                    carregarExamesFiltrados();
                }
            }
        }
    }

    // popula o AutoComplete com os exames agregados
    private void carregarExamesFiltrados() {

        Collections.sort(examesDisponiveis);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, examesDisponiveis);

        autoCompleteExame.setAdapter(adapter);
        autoCompleteExame.setOnClickListener(v -> autoCompleteExame.showDropDown());
        autoCompleteExame.setThreshold(0);
    }

    // busca médicos por especialidade (POST) — servidor retorna array de objetos {idMedico, nome_completoMedico}
    private void buscarMedicos(String especialidade) {
        try {
            JSONObject body = new JSONObject();
            body.put("especialidade", especialidade);

            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST,
                    URL_BUSCAR_MEDICOS,
                    body,
                    response -> {
                        try {
                            boolean ok = response.optBoolean("success", false);
                            if (!ok) {
                                Toast.makeText(this, "Nenhum médico encontrado.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            JSONArray arr = response.optJSONArray("medicos");
                            listaMedicosIds.clear();
                            listaMedicosNomes.clear();

                            if (arr != null) {
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject o = arr.getJSONObject(i);
                                    int id = o.optInt("idMedico", -1);
                                    // campo de nome pode ser nome_completoMedico ou nome (tente ambos)
                                    String nome = o.has("nome_completoMedico") ? o.optString("nome_completoMedico") : o.optString("nome", "");
                                    if (nome == null) nome = "";
                                    if (!nome.toLowerCase().startsWith("dr") && !nome.toLowerCase().startsWith("dra"))
                                        nome = "Dr. " + nome;

                                    listaMedicosIds.add(id);
                                    listaMedicosNomes.add(nome);
                                }
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
                    error -> Toast.makeText(this, "Erro ao conectar para buscar médicos", Toast.LENGTH_SHORT).show()
            );

            Volley.newRequestQueue(this).add(req);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao montar requisição de médicos", Toast.LENGTH_SHORT).show();
        }
    }

    // encontra especialidade correspondente a um exame usando o mapa local
    private String encontrarEspecialidadePorExame(String exame) {
        for (String esp : examesPorEspecialidade.keySet()) {
            List<String> lista = examesPorEspecialidade.get(esp);
            if (lista != null && lista.contains(exame)) return esp;
        }
        return null;
    }

    private int obterIdMedicoSelecionado(String exibicao) {
        for (int i = 0; i < listaMedicosNomes.size(); i++) {
            if (listaMedicosNomes.get(i).equalsIgnoreCase(exibicao))
                return listaMedicosIds.get(i);
        }
        return -1;
    }



    // confirma e envia o exame ao servidor (POST)
    private void confirmarEEnviarExame() {
        String tipo = autoCompleteExame.getText().toString();
        String medico = autoCompleteMedico.getText().toString();
        String dataBR = editDate.getText().toString();
        String hora = autoCompleteTime.getText().toString();

        if (tipo.isEmpty() || medico.isEmpty() || dataBR.isEmpty() || hora.isEmpty()) {
            // Campos obrigatórios
            autoCompleteExame.setError("Selecione o exame");
            autoCompleteMedico.setError("Selecione o médico");
            editDate.setError("Selecione o dia");
            autoCompleteTime.setError("Selecione o horário ");
            return;
        }

        int idMedico = obterIdMedicoSelecionado(medico);
        if (idMedico == -1) {
            Toast.makeText(this, "Selecione um médico válido!", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("loginUsuario_prefs", MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", 0);
        String cpfUsuario = prefs.getString("cpfUsuario", "");

        if (idUsuario == 0 && (cpfUsuario == null || cpfUsuario.isEmpty())) {
            Toast.makeText(this, "Usuário não identificado!", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences agendamentos = getSharedPreferences("user_prefs_agendamentos", MODE_PRIVATE);
        SharedPreferences.Editor editor = agendamentos.edit();
        String medicoFormatado = medico.replace("Dr. ", "").trim();

        editor.putInt("idMedico", idMedico);
        editor.putString("exame", tipo);
        editor.putString("medicoExame", medicoFormatado);
        editor.putString("dataExame", dataBR);
        editor.putString("horarioExame", hora);
        editor.putString("valorExame", "120.00");
        editor.putString("statusExame", "Agendado");

        editor.apply();

        // Depois de salvar, vai para a tela de pagamento
        Intent it = new Intent(AgendarExames.this, FormaPagamento.class);
        startActivity(it);

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
