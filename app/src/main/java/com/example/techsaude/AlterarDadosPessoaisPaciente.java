package com.example.techsaude;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlterarDadosPessoaisPaciente extends AppCompatActivity {
    private EditText edtNome, edtNascimento, edtCPF, edtEmail, edtCEP, edtRua, edtNumero,
            edtBairro, edtCidade, edtUF, edtTelefone;
    private RadioGroup groupSexo;
    private RadioButton sexoM, sexoF;
    Button btnBuscarCep, btnAlterarDados;
    ImageView imgVoltarDadosPaciente;

    String URL_PESQUISAR = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/buscar_dados_pessoais.php?idUsuario=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar_dados_pessoais_paciente);

        btnBuscarCep = findViewById(R.id.btnBuscarCep);
        edtNome = findViewById(R.id.edtNome);
        edtNascimento = findViewById(R.id.edtNascimento);
        edtCPF = findViewById(R.id.edtCPF);
        edtEmail = findViewById(R.id.edtEmail);
        edtCEP = findViewById(R.id.edtCEP);
        edtRua = findViewById(R.id.edtRua);
        edtNumero = findViewById(R.id.edtNumero);
        edtBairro = findViewById(R.id.edtBairro);
        edtCidade = findViewById(R.id.edtCidade);
        edtUF = findViewById(R.id.edtUF);
        edtTelefone = findViewById(R.id.edtTelefone);
        btnAlterarDados = findViewById(R.id.btnAlterarDados);
        groupSexo = findViewById(R.id.groupSexo);
        sexoM = findViewById(R.id.sexoM);
        sexoF = findViewById(R.id.sexoF);
        imgVoltarDadosPaciente = findViewById(R.id.imgVoltarDadosPaciente);

        // Buscar ID salvo no login
        SharedPreferences prefs = getSharedPreferences("loginUsuario_prefs", MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", 0);

        if (idUsuario != 0) {
            carregarDados(idUsuario);
        }

        btnBuscarCep.setOnClickListener(v -> {
            String cep = edtCEP.getText().toString().trim();

            if (cep.length() != 8) {
                edtCEP.setError("Digite um CEP válido");
                return;
            }

            buscarCep(cep);
        });

        imgVoltarDadosPaciente.setOnClickListener(v -> finish());
        btnAlterarDados.setOnClickListener(v -> atualizaDados(idUsuario));
    }

    private void atualizaDados(int idUsuario) {
        String url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/alterar_dados_usuario.php";

        String nome = edtNome.getText().toString().trim();
        String cpf = edtCPF.getText().toString().trim();
        String dataNascInput = edtNascimento.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String telefone = edtTelefone.getText().toString().trim().replaceAll("[^\\d]", "");
        String rua = edtRua.getText().toString().trim();
        String bairro = edtBairro.getText().toString().trim();
        String cidade = edtCidade.getText().toString().trim();
        String uf = edtUF.getText().toString().trim();
        String cep = edtCEP.getText().toString().trim();
        String numero = edtNumero.getText().toString().trim();
        String dataNasc = "";

        String enderecoCompleto = rua + ", " + numero + ", " + bairro + ", " + cidade + ", " + uf + ", " + "CEP: " + cep;

        try {
            SimpleDateFormat inFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date d = inFormat.parse(dataNascInput);
            dataNasc = outFormat.format(d);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String sexo = "";
        if (sexoM.isChecked()) {
            sexo = "M";
        } else if (sexoF.isChecked()) {
            sexo = "F";
        }

        // Verificação simples
        if (nome.isEmpty() || cpf.isEmpty() || dataNasc.isEmpty() || email.isEmpty() || telefone.isEmpty() || cep.isEmpty() || rua.isEmpty() || numero.isEmpty() || bairro.isEmpty() || cidade.isEmpty() || uf.isEmpty() || sexo.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 Criar JSON para enviar
        JSONObject dados = new JSONObject();
        try {
            dados.put("idUsuario", idUsuario);
            dados.put("nome_completoUsuario", nome);
            dados.put("cpfUsuario", cpf);
            dados.put("emailUsuario", email);
            dados.put("data_nascUsuario", dataNasc);
            dados.put("enderecoUsuario", enderecoCompleto);
            dados.put("sexoUsuario", sexo);
            dados.put("telefoneUsuario", telefone);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 🔹 Enviar usando Volley
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                dados,
                response -> {
                    Log.d("ID", "id: " + idUsuario);
                    Log.e("API_RESPOSTA", response.toString());
                    try {
                        boolean sucesso = response.getBoolean("success");
                        String msg = response.getString("message");
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        if (sucesso) {
                            finish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("API_RESPOSTA", response.toString());
                    }
                },
                error -> {
                    Toast.makeText(this, "Erro ao conectar: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
        );
        Volley.newRequestQueue(this).add(request);
    }
    private void carregarDados(int idUsuario) {

        String url = URL_PESQUISAR + idUsuario;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        boolean sucesso = response.getBoolean("success");
                        String message = response.getString("message");
                        Log.d("RESPOSTA", "Resposta: " + response);
                        edtNome.setText(response.getString("nome"));
                        edtCPF.setText(response.getString("cpf"));
                        edtEmail.setText(response.getString("email"));

                        // Data → converte para BR

                        String datamysql = response.getString("data_nasc");
                        String databr = formatarDataParaBR(datamysql);
                        edtNascimento.setText(databr);
                        String enderecoCompleto = response.getString("endereco"); // do PHP

// Dividir usando vírgula como separador
                        String[] partes = enderecoCompleto.split(",");

                        String rua = partes.length > 0 ? partes[0].trim() : "";
                        String numero = partes.length > 1 ? partes[1].trim() : "";
                        String bairro = partes.length > 2 ? partes[2].trim() : "";
                        String cidade = partes.length > 3 ? partes[3].trim() : "";
                        String uf = partes.length > 4 ? partes[4].trim() : "";

                        String[] partecep = enderecoCompleto.split("CEP:");

                        String cep = partecep.length > 1 ? partecep[1].trim() : "";
// Preencher os campos
                        edtRua.setText(rua);
                        edtNumero.setText(numero);
                        edtBairro.setText(bairro);
                        edtCidade.setText(cidade);
                        edtUF.setText(uf);
                        edtCEP.setText(cep);

                        edtTelefone.setText(response.getString("telefone"));

                        // Sexo → seleciona o radio correto
                        String sexo = response.getString("sexo");
                        if (sexo.equals("M")) {
                            sexoM.setChecked(true);
                        } else if (sexo.equals("F")) {
                            sexoF.setChecked(true);
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
                        Log.e("ERRO", "Erro: " + e.getMessage());
                    }
                },
                error -> Toast.makeText(this, "Erro no servidor", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(req);
    }

    private String formatarDataParaBR(String dataUSA) {
        try {
            SimpleDateFormat formatoEUA = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat formatoBR = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));

            Date date = formatoEUA.parse(dataUSA);
            return formatoBR.format(date);

        } catch (Exception e) {
            return dataUSA;
        }
    }
    private void buscarCep(String cep) {
        String url = "https://viacep.com.br/ws/" + cep + "/json/";

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        edtRua.setText(response.getString("logradouro"));
                        edtBairro.setText(response.getString("bairro"));
                        edtCidade.setText(response.getString("localidade"));
                        edtUF.setText(response.getString("uf"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "CEP não encontrado!", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }


}