package com.example.techsaude;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VerPerfilPaciente extends AppCompatActivity {

    private static final String TAG = "VerPerfilPaciente";

    ImageView btnVoltar;
    TextView tvNomeView, tvNascimentoView, tvCPFView, tvEmailView,
            tvEnderecoView, tvTelefoneView, tvSexoView;

    int idUsuario;

    String URL_DADOS = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/buscar_dados_medico.php?idUsuario=";
    String URL_PRONTUARIO = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/buscar_prontuario_medico.php?idUsuario=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_perfil_paciente);

        idUsuario = getIntent().getIntExtra("idUsuario", 0);

        if (idUsuario == 0) {
            Toast.makeText(this, "Erro: ID inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        inicializarViews();

        carregarDadosPessoais();
        carregarProntuario();

        btnVoltar.setOnClickListener(v -> finish());
    }

    private void inicializarViews() {
        btnVoltar = findViewById(R.id.btnVoltar2);

        tvNomeView = findViewById(R.id.tvNomeView);
        tvNascimentoView = findViewById(R.id.tvNascimentoView);
        tvCPFView = findViewById(R.id.tvCPFView);
        tvEmailView = findViewById(R.id.tvEmailView);
        tvEnderecoView = findViewById(R.id.tvEnderecoView);
        tvTelefoneView = findViewById(R.id.tvTelefoneView);
        tvSexoView = findViewById(R.id.tvSexoView);
    }

    // =======================
    //  DADOS PESSOAIS
    // =======================
    private void carregarDadosPessoais() {

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                URL_DADOS + idUsuario,
                null,
                response -> {
                    Log.d(TAG, "resp dados pessoais: " + response.toString());
                    preencherDadosPessoais(response);
                },
                error -> {
                    Log.e(TAG, "erro carregar dados pessoais", error);
                    Toast.makeText(this, "Erro ao carregar dados pessoais", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void preencherDadosPessoais(JSONObject obj) {

        try {
            if (obj == null || obj.length() == 0) {
                Toast.makeText(this, "Paciente não possui dados pessoais cadastrados.", Toast.LENGTH_LONG).show();
                exibirAvisoDadosPessoais();
                return;
            }

            // Caso o backend retorne por exemplo { "status":"vazio" }
            if (obj.has("status")) {
                String status = obj.optString("status", "");
                if (status.equalsIgnoreCase("vazio") || status.equalsIgnoreCase("empty")) {
                    Toast.makeText(this, "Paciente não possui dados pessoais cadastrados.", Toast.LENGTH_LONG).show();
                    exibirAvisoDadosPessoais();
                    return;
                }
            }

            // Verificações de segurança para evitar "null" string
            String nome = safeGetString(obj, "nome_completoUsuario");
            if (isNullOrEmpty(nome)) {
                Toast.makeText(this, "Paciente não possui dados pessoais cadastrados.", Toast.LENGTH_LONG).show();
                exibirAvisoDadosPessoais();
                return;
            }

            tvNomeView.setText("Nome: " + nome);
            tvCPFView.setText("CPF: " + safeGetString(obj, "cpfUsuario", "não informado"));
            tvEmailView.setText("Email: " + safeGetString(obj, "emailUsuario", "não informado"));
            tvEnderecoView.setText("Endereço: " + safeGetString(obj, "enderecoUsuario", "não informado"));
            tvTelefoneView.setText("Telefone: " + formatarTelefone(safeGetString(obj, "telefoneUsuario", "")));

            String sexo = safeGetString(obj, "sexoUsuario", "");
            if ("M".equalsIgnoreCase(sexo)) sexo = "Masculino";
            else if ("F".equalsIgnoreCase(sexo)) sexo = "Feminino";
            else if (sexo.isEmpty()) sexo = "não informado";
            tvSexoView.setText("Sexo: " + sexo);

            String dataEua = safeGetString(obj, "data_nascUsuario", "");
            tvNascimentoView.setText("Nascimento: " + (isNullOrEmpty(dataEua) ? "não informado" : converterData(dataEua)));

        } catch (Exception e) {
            Log.e(TAG, "erro preencherDadosPessoais", e);
            Toast.makeText(this, "Erro ao processar dados pessoais.", Toast.LENGTH_SHORT).show();
        }
    }

    private void exibirAvisoDadosPessoais() {
        tvNomeView.setText("Nome: não informado");
        tvCPFView.setText("CPF: não informado");
        tvEmailView.setText("Email: não informado");
        tvEnderecoView.setText("Endereço: não informado");
        tvTelefoneView.setText("Telefone: não informado");
        tvSexoView.setText("Sexo: não informado");
        tvNascimentoView.setText("Nascimento: não informado");
    }


    // =======================
    //  PRONTUÁRIO
    // =======================
    private void carregarProntuario() {

        // 1) tenta JsonObjectRequest
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                URL_PRONTUARIO + idUsuario,
                null,
                response -> {
                    Log.d(TAG, "resp prontuario (obj): " + response.toString());
                    tratarRespostaProntuarioResponse(response);
                },
                error -> {
                    // Se deu erro possivelmente o endpoint retornou um ARRAY ou body vazio -> tentamos JsonArrayRequest como fallback
                    Log.w(TAG, "JsonObjectRequest falhou, tentando JsonArrayRequest. erro: " + error.getMessage(), error);
                    tentarJsonArrayProntuario();
                }
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void tentarJsonArrayProntuario() {
        JsonArrayRequest arrReq = new JsonArrayRequest(
                Request.Method.GET,
                URL_PRONTUARIO + idUsuario,
                null,
                response -> {
                    Log.d(TAG, "resp prontuario (arr): " + response.toString());
                    if (response == null || response.length() == 0) {
                        mostrarProntuarioVazio();
                        return;
                    }
                    try {
                        // assume que o backend devolve um array com 1 objeto
                        JSONObject obj = response.getJSONObject(0);
                        tratarRespostaProntuarioResponse(obj);
                    } catch (Exception e) {
                        Log.e(TAG, "erro tratando array de prontuario", e);
                        mostrarProntuarioVazio();
                    }
                },
                error -> {
                    Log.e(TAG, "erro JsonArrayRequest prontuario", error);
                    // Se mesmo assim falhar, apenas mostrar aviso ao usuário (sem crash)
                    mostrarProntuarioVazio();
                }
        );

        Volley.newRequestQueue(this).add(arrReq);
    }

    private void tratarRespostaProntuarioResponse(JSONObject obj) {
        try {
            if (obj == null || obj.length() == 0) {
                mostrarProntuarioVazio();
                return;
            }

            // se o backend indicar explicitamente que não há prontuario
            if (obj.has("status")) {
                String status = obj.optString("status", "");
                if (status.equalsIgnoreCase("vazio") || status.equalsIgnoreCase("empty")) {
                    mostrarProntuarioVazio();
                    return;
                }
            }

            // campo de peso como verificação principal
            String peso = safeGetString(obj, "peso_kgProntuario", "");
            if (isNullOrEmpty(peso)) {
                // pode ainda existir informações parciais: vamos verificar outros campos
                boolean temAlgo = !isNullOrEmpty(safeGetString(obj, "altura_cmProntuario", ""))
                        || !isNullOrEmpty(safeGetString(obj, "sintomasProntuario", ""))
                        || !isNullOrEmpty(safeGetString(obj, "alergiasProntuario", ""));
                if (!temAlgo) {
                    mostrarProntuarioVazio();
                    return;
                }
            }

            // se chegou aqui -> tem algum conteúdo. limpa e popula
            LinearLayout layout = findViewById(R.id.linearProntuario);
            layout.removeAllViews(); // evita duplicação se método for chamado novamente

            TextView titulo = new TextView(this);
            titulo.setText("Prontuário\n");
            titulo.setTextSize(20);
            titulo.setPadding(0, 0, 0, 10);
            layout.addView(titulo);

            adicionarInfo(obj, layout, "Peso (kg): ", "peso_kgProntuario");
            adicionarInfo(obj, layout, "Altura (cm): ", "altura_cmProntuario");
            adicionarInfo(obj, layout, "Sintomas: ", "sintomasProntuario");
            adicionarInfo(obj, layout, "Alergias: ", "alergiasProntuario");
            adicionarInfo(obj, layout, "Observações: ", "observacoesProntuario");
            adicionarInfo(obj, layout, "Alertas: ", "alertasProntuario");
            adicionarInfo(obj, layout, "Condições Crônicas: ", "condicoes_chronicasProntuario");

        } catch (Exception e) {
            Log.e(TAG, "erro preencher prontuario", e);
            mostrarProntuarioVazio();
        }
    }

    private void mostrarProntuarioVazio() {
        Log.d(TAG, "mostrarProntuarioVazio()");
        Toast.makeText(this, "Paciente não possui prontuário cadastrado.", Toast.LENGTH_LONG).show();

        LinearLayout layout = findViewById(R.id.linearProntuario);
        layout.removeAllViews();

        TextView aviso = new TextView(this);
        aviso.setText("❗ Paciente não possui prontuário cadastrado.");
        aviso.setTextSize(20);
        aviso.setPadding(10, 20, 0, 0);
        layout.addView(aviso);
    }

    private void adicionarInfo(JSONObject obj, LinearLayout layout, String titulo, String chave) {
        try {
            String valor = safeGetString(obj, chave, "");
            TextView tv = new TextView(this);
            tv.setText(titulo + (isNullOrEmpty(valor) ? "Não informado" : valor));
            tv.setTextSize(18);
            tv.setPadding(0, 15, 0, 0);
            layout.addView(tv);
        } catch (Exception e) {
            Log.w(TAG, "erro adicionarInfo para " + chave, e);
        }
    }

    // =======================
    //  FORMATADORES & HELPERS
    // =======================
    private String safeGetString(JSONObject obj, String key) {
        return safeGetString(obj, key, "");
    }

    private String safeGetString(JSONObject obj, String key, String defaultVal) {
        if (obj == null) return defaultVal;
        try {
            if (!obj.has(key)) return defaultVal;
            String v = obj.optString(key, defaultVal);
            if (v == null) return defaultVal;
            v = v.trim();
            if (v.equalsIgnoreCase("null") || v.equalsIgnoreCase("undefined")) return defaultVal;
            return v;
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String formatarTelefone(String tel) {
        if (tel == null) return "";
        tel = tel.replaceAll("\\D+", "");
        if (tel.length() == 11)
            return "(" + tel.substring(0,2) + ") " + tel.substring(2,7) + "-" + tel.substring(7);
        return tel;
    }

    private String converterData(String dataEUA) {
        try {
            SimpleDateFormat formatoEUA = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat formatoBR = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt","BR"));
            Date date = formatoEUA.parse(dataEUA);
            return formatoBR.format(date);
        } catch (Exception e) {
            return dataEUA;
        }
    }
}
