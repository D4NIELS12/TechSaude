package com.example.techsaude;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class LoginPaciente extends AppCompatActivity {

    EditText txtCpfPaciente, txtSenhaPaciente;
    TextView lblCadastrar, txtEsqueci;
    Button btnMedico, btnEntrar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_paciente);

        txtCpfPaciente = findViewById(R.id.txtCpfPaciente);
        txtSenhaPaciente = findViewById(R.id.txtSenhaPaciente);
        lblCadastrar = findViewById(R.id.lblCadastrar);
        txtEsqueci = findViewById(R.id.txtEsqueci);
        btnMedico = findViewById(R.id.btnMedico);
        btnEntrar = findViewById(R.id.btnEntrar);

        if (estalogadoMedico()){
            Intent it  = new Intent(this, MedicoLogado.class);
            startActivity(it);
            finish();
            return;
        }
        // 🔹 Se já estiver logado, vai direto para tela logada
        if (estaLogadoUsuario()) {
            Intent it = new Intent(this, PacienteLogado.class);
            startActivity(it);
            finish();
            return;
        }

        // Aplica máscara
        aplicarMascaraCPF();



        txtEsqueci.setOnClickListener(view -> {
            Intent it = new Intent(LoginPaciente.this, EsqueciSenhaPaciente.class);
            startActivity(it);

        });

        lblCadastrar.setOnClickListener(view -> {
            Intent it = new Intent(LoginPaciente.this, CadastrarPaciente.class);
            startActivity(it);

        });

        btnMedico.setOnClickListener(view -> {
            Intent it = new Intent(LoginPaciente.this, LoginMedico.class);
            startActivity(it);

        });

        btnEntrar.setOnClickListener(view -> fazerLogin());
    }

    // ============================
    // 🔹 LOGIN
    // ============================
    private void fazerLogin() {

        String url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/login_usuario.php";

        String cpf = txtCpfPaciente.getText().toString().trim();
        String senha = txtSenhaPaciente.getText().toString().trim();

        // 🔹 Verificação
        if (cpf.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha CPF e senha!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 Criando o JSON
        JSONObject dados = new JSONObject();
        try {
            dados.put("cpfUsuario", cpf);
            dados.put("senhaUsuario", senha);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 🔹 Requisição via Volley
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                dados,
                response -> {
                    try {
                        boolean sucesso = response.getBoolean("sucesso");
                        String mensagem = response.getString("mensagem");

                        if (!sucesso) {
                            Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
                            return;
                        }

                        // 🔹 Usuario retornado pelo PHP
                        JSONObject usuario = response.getJSONObject("usuario");

                        // 🔹 Salvar dados do usuário localmente (SESSION)
                        SharedPreferences prefs = getSharedPreferences("loginUsuario_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();

                        editor.putInt("idUsuario", usuario.getInt("idUsuario"));
                        editor.putString("nome", usuario.getString("nome_completoUsuario"));
                        editor.putString("cpf", usuario.getString("cpfUsuario"));
                        editor.putString("email", usuario.getString("emailUsuario"));
                        editor.putString("data_nasc", usuario.getString("data_nascUsuario"));
                        editor.putString("endereco", usuario.getString("enderecoUsuario"));
                        editor.putString("sexo", usuario.getString("sexoUsuario"));
                        editor.putString("telefone", usuario.getString("telefoneUsuario"));
                        salvarUsuario(usuario);

                        editor.apply();

                        Toast.makeText(this, "Login realizado!", Toast.LENGTH_SHORT).show();

                        // 🔹 Abrir próxima tela
                        Intent intent = new Intent(LoginPaciente.this, PacienteLogado.class);
                        startActivity(intent);
                        finish();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erro ao processar resposta!", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Erro de conexão: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }




    // ============================
    // 🔹 MÁSCARA CPF
    // ============================
    private void aplicarMascaraCPF() {
        txtCpfPaciente.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;

                String str = s.toString().replaceAll("[^\\d]", "");
                StringBuilder mascara = new StringBuilder();

                if (str.length() > 0) {
                    mascara.append(str.substring(0, Math.min(3, str.length())));
                }
                if (str.length() > 3) {
                    mascara.append(".").append(str.substring(3, Math.min(6, str.length())));
                }
                if (str.length() > 6) {
                    mascara.append(".").append(str.substring(6, Math.min(9, str.length())));
                }
                if (str.length() > 9) {
                    mascara.append("-").append(str.substring(9, Math.min(11, str.length())));
                }

                isUpdating = true;
                txtCpfPaciente.setText(mascara.toString());
                txtCpfPaciente.setSelection(txtCpfPaciente.getText().length());
                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }




    // ============================
    // 🔹 SHARED PREFERENCES
    // ============================
    private void salvarUsuario(JSONObject usuario) {
        SharedPreferences prefs = getSharedPreferences("loginUsuario_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("cpfUsuario", usuario.optString("cpfUsuario"));
        editor.putBoolean("logado", true);

        editor.apply();
    }

    private boolean estalogadoMedico() {
        SharedPreferences prefs = getSharedPreferences("user_prefs_medico", MODE_PRIVATE);
        return prefs.getBoolean("logado", false);
    }
    private boolean estaLogadoUsuario() {
        SharedPreferences prefs = getSharedPreferences("loginUsuario_prefs", MODE_PRIVATE);
        return prefs.getBoolean("logado", false);
    }

    private String getCpfUsuario() {
        SharedPreferences prefs = getSharedPreferences("loginUsuario_prefs", MODE_PRIVATE);
        return prefs.getString("cpfUsuario", "");
    }

    public void logout() {
        SharedPreferences prefs = getSharedPreferences("loginUsuario_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
}
