package com.example.techsaude;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VerPerfilPaciente extends AppCompatActivity {

    ImageView btnVoltar;
    TextView tvNomeView, tvNascimentoView, tvCPFView, tvEmailView,
            tvEnderecoView, tvTelefoneView, tvSexoView;

    TextView tvPeso, tvAltura, tvSintomas, tvAlergias, tvObs, tvAlertas, tvCondicoes;

    int idUsuario;

    String URL_DADOS = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/buscar_dados_medico.php?idUsuario=";
    String URL_PRONTUARIO = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/buscar_prontuario_medico.php?idUsuario=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_perfil_paciente);

        // Recebe ID
        idUsuario = getIntent().getIntExtra("idUsuario", 0);

        if (idUsuario == 0) {
            Toast.makeText(this, "Erro: ID inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar views
        inicializarViews();

        // Funções
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
                response -> preencherDadosPessoais(response),
                error -> Toast.makeText(this, "Erro ao carregar dados pessoais", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void preencherDadosPessoais(JSONObject obj) {

        try {

            tvNomeView.setText("Nome: " + obj.getString("nome_completoUsuario"));
            tvCPFView.setText("CPF: " + obj.getString("cpfUsuario"));
            tvEmailView.setText("Email: " + obj.getString("emailUsuario"));
            tvEnderecoView.setText("Endereço: " + obj.getString("enderecoUsuario"));
            tvTelefoneView.setText("Telefone: " + formatarTelefone(obj.getString("telefoneUsuario")));

            String sexo = obj.getString("sexoUsuario");
            if (sexo.equals("M")) sexo = "Masculino";
            else if (sexo.equals("F")) sexo = "Feminino";

            tvSexoView.setText("Sexo: " + sexo);

            // Converter data EUA para BR
            String dataEua = obj.getString("data_nascUsuario");
            tvNascimentoView.setText("Nascimento: " + converterData(dataEua));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =======================
    //  PRONTUÁRIO
    // =======================
    private void carregarProntuario() {

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                URL_PRONTUARIO + idUsuario,
                null,
                response -> preencherProntuario(response),
                error -> Toast.makeText(this, "Erro ao carregar prontuário", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void preencherProntuario(JSONObject obj) {
        try {

            ((TextView) findViewById(R.id.tvProntuario2)).setText("Prontuário\n");

            adicionarInfo("Peso (kg): ", obj.getString("peso_kgProntuario"));
            adicionarInfo("Altura (cm): ", obj.getString("altura_cmProntuario"));
            adicionarInfo("Sintomas: ", obj.getString("sintomasProntuario"));
            adicionarInfo("Alergias: ", obj.getString("alergiasProntuario"));
            adicionarInfo("Observações: ", obj.getString("observacoesProntuario"));
            adicionarInfo("Alertas: ", obj.getString("alertasProntuario"));
            adicionarInfo("Condições Crônicas: ", obj.getString("condicoes_chronicasProntuario"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void adicionarInfo(String titulo, String texto) {
        LinearLayout layout = findViewById(R.id.linearProntuario);
        TextView tv = new TextView(this);
        tv.setText(titulo + (texto.isEmpty() ? "Não informado" : texto));
        tv.setTextSize(18);
        tv.setPadding(0, 15, 0, 0);
        layout.addView(tv);
    }

    // =======================
    //  FORMATADORES
    // =======================
    private String formatarTelefone(String tel) {
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
