package com.example.techsaude;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class LoginMedico extends AppCompatActivity
{
    TextView txtEsqueci2, lblCadastrar2;
    Button btnPaciente2, btnEntrar2;
    EditText txtCrmMedico, txtSenhaMedica;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_medico);



        txtSenhaMedica = (EditText) findViewById(R.id.txtSenhaMedico);
        btnPaciente2 = (Button) findViewById(R.id.btnPaciente2);
        btnEntrar2 = (Button) findViewById(R.id.btnEntrar2);
        txtEsqueci2 = (TextView) findViewById(R.id.txtEsqueci2);
        lblCadastrar2 = (TextView) findViewById(R.id.lblCadastrar2);
        txtCrmMedico = (EditText) findViewById(R.id.txtCrmMedico);

        btnPaciente2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent it = new Intent(LoginMedico.this,
                                        LoginPaciente.class);
                startActivity(it);
                finish();
            }
        });

        btnEntrar2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                loginMedico();
            }
        });

        txtEsqueci2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent it = new Intent(LoginMedico.this,
                        EsqueciSenha.class);
                startActivity(it);
                finish();
            }
        });

        lblCadastrar2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent it = new Intent(LoginMedico.this,
                        CadastrarMedico.class);
                startActivity(it);
            }
        });

        txtCrmMedico.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;

                String str = s.toString().replaceAll("[^A-Za-z0-9]", "");
                StringBuilder mascara = new StringBuilder();

                if (str.length() > 6) {
                    // Exemplo: 123456SP → CRM: 123456/SP
                    mascara.append(str.substring(0, 6))
                            .append("/")
                            .append(str.substring(6).toUpperCase());
                } else {
                    mascara.append(str);
                }

                isUpdating = true;
                txtCrmMedico.setText(mascara.toString());
                txtCrmMedico.setSelection(txtCrmMedico.getText().length());
                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    private void loginMedico() {
        String url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/login_medico.php";

        String crm = txtCrmMedico.getText().toString().trim();
        String senha = txtSenhaMedica.getText().toString().trim();

        if (crm.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject dados = new JSONObject();
        try {
            dados.put("crmMedico", crm);
            dados.put("senhaMedico", senha);
        } catch (Exception e) {
            e.printStackTrace();
        }

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

                        JSONObject medico = response.getJSONObject("medico");

                        SharedPreferences prefs = getSharedPreferences("user_prefs_medico", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();

                        editor.putInt("idMedico", medico.getInt("idMedico"));
                        editor.putString("nome", medico.getString("nome_completoMedico"));
                        editor.putString("crm", medico.getString("crmMedico"));
                        editor.putString("cpf", medico.getString("cpfMedico"));
                        editor.putString("email", medico.getString("emailMedico"));
                        editor.putString("data_nasc", medico.getString("data_nascMedico"));
                        editor.putString("especialidade", medico.getString("especialidadeMedico"));
                        editor.putString("sexo", medico.getString("sexoMedico"));
                        editor.putString("telefone", medico.getString("telefoneMedico"));
                        salvarMedico(medico);
                        editor.apply();

                        Toast.makeText(this, "Login realizado!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginMedico.this, MedicoLogado.class);
                        startActivity(intent);
                        finish();

                    } catch (Exception e ) {
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
    private void salvarMedico(JSONObject medico) {
        SharedPreferences prefs = getSharedPreferences("user_prefs_medico", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("logado", true);

        editor.apply();
    }
}