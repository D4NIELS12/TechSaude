package com.example.techsaude;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AlterarDadosPessoaisMedico extends AppCompatActivity {

    private EditText edtNome, edtNascimento, edtCPF, edtEmail, edtCrm, edtTelefone, edtEndereco;
    private RadioGroup groupSexo;
    private RadioButton sexoM, sexoF;
    Button btnAlterarDados;
    ImageView imgVoltar;

    AutoCompleteTextView autoCompleteEspecialidade;

    String URL_PESQUISAR = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/buscar_dados_pessoais_medico.php?idMedico=";
    String URL_ATUALIZAR = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/alterar_dados_pessoais_medico.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar_dados_pessoais_medico);

        inicializarCampos();
        configurarEspecialidades();
        carregarDadosMedico();

        imgVoltar.setOnClickListener(v -> finish());

        btnAlterarDados.setOnClickListener(v -> atualizarDados());
    }

    private void inicializarCampos() {
        edtNome = findViewById(R.id.edtNome);
        edtNascimento = findViewById(R.id.edtNascimento);
        edtCPF = findViewById(R.id.edtCPF);
        edtEmail = findViewById(R.id.edtEmail);
        edtCrm = findViewById(R.id.edtCrm);
        edtTelefone = findViewById(R.id.edtTelefone);

        groupSexo = findViewById(R.id.groupSexo);
        sexoM = findViewById(R.id.sexoM);
        sexoF = findViewById(R.id.sexoF);

        autoCompleteEspecialidade = findViewById(R.id.auto_complete_especialidade);

        btnAlterarDados = findViewById(R.id.btnAlterarDados);
        imgVoltar = findViewById(R.id.imgVoltar);
    }

    private void configurarEspecialidades() {

        String[] especialidades = {
                "Cardiologia", "Dermatologia", "Endocrinologia", "Gastroenterologia",
                "Ginecologia", "Neurologia", "Ortopedia", "Pediatria", "Psiquiatria",
                "Urologia", "Oftalmologia", "Otorrinolaringologia", "Reumatologia"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                especialidades
        );
        autoCompleteEspecialidade.setAdapter(adapter);
        autoCompleteEspecialidade.setOnClickListener(v -> autoCompleteEspecialidade.showDropDown());
    }

    private void carregarDadosMedico() {

        SharedPreferences prefs = getSharedPreferences("user_prefs_medico", Context.MODE_PRIVATE);
        int idMedico = prefs.getInt("idMedico", 0);

        if (idMedico == 0) {
            Toast.makeText(this, "ID do médico não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = URL_PESQUISAR + idMedico;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {

                        edtNome.setText(response.getString("nome"));
                        edtCPF.setText(response.getString("cpf"));
                        edtEmail.setText(response.getString("email"));
                        edtCrm.setText(response.getString("crm"));
                        edtTelefone.setText(response.getString("telefone"));
                        autoCompleteEspecialidade.setText(response.getString("especialidade"));
                        configurarEspecialidades();

                        // Data EUA → BR
                        String dataEua = response.getString("data_nasc");
                        edtNascimento.setText(formatarDataParaBR(dataEua));

                        String sexo = response.getString("sexo");
                        if (sexo.equals("M")) sexoM.setChecked(true);
                        else sexoF.setChecked(true);

                    } catch (Exception e) {
                        Toast.makeText(this, "Erro ao preencher dados", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void atualizarDados() {

        SharedPreferences prefs = getSharedPreferences("user_prefs_medico", MODE_PRIVATE);
        int idMedico = prefs.getInt("idMedico", 0);

        if (idMedico == 0) {
            Toast.makeText(this, "Erro ao identificar médico", Toast.LENGTH_SHORT).show();
            return;
        }

        String nome = edtNome.getText().toString();
        String cpf = edtCPF.getText().toString();
        String email = edtEmail.getText().toString();
        String crm = edtCrm.getText().toString();
        String telefone = edtTelefone.getText().toString();
        String especialidade = autoCompleteEspecialidade.getText().toString();

        String sexo = "";

        if (sexoM.isChecked()) {
            sexo = "M";
        } else {
            sexo = "F";
        }

        // Data BR → EUA
        String dataNasc = formatarDataParaEUA(edtNascimento.getText().toString());

        Map<String, String> params = new HashMap<>();
        params.put("idMedico", String.valueOf(idMedico));
        params.put("nome", nome);
        params.put("cpf", cpf);
        params.put("email", email);
        params.put("crm", crm);
        params.put("telefone", telefone);
        params.put("especialidade", especialidade);
        params.put("sexo", sexo);
        params.put("data_nasc", dataNasc);

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                URL_ATUALIZAR,
                new JSONObject(params),
                response -> {
                    try {
                        Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(this, "Erro ao ler resposta", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Erro ao atualizar", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(req);
    }

    private String formatarDataParaEUA(String dataBR) {
        try {
            SimpleDateFormat br = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
            SimpleDateFormat eua = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date data = br.parse(dataBR);
            return eua.format(data);
        } catch (Exception e) {
            return dataBR;
        }
    }

    private String formatarDataParaBR(String dataEUA) {
        try {
            SimpleDateFormat eua = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat br = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
            Date data = eua.parse(dataEUA);
            return br.format(data);
        } catch (Exception e) {
            return dataEUA;
        }
    }
}
