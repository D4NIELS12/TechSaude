package com.example.techsaude;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CadastrarMedico extends AppCompatActivity {

    ImageView imgVoltar3;
    EditText txtCrmMedico2, txtCpfMedico, txtNomeMedico, txtEmailMedico, txtSenhaMedico2, txtTelefoneMedico, txtNascMedico, txtEndereco;
    RadioGroup rdgGenero;
    AutoCompleteTextView autoEspecialidadeMedico;
    RadioButton rdbMasculino, rdbFeminino;
    Button btnCadastrar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_medico);

        imgVoltar3 = findViewById(R.id.imgVoltar3);
        txtNomeMedico = findViewById(R.id.txtNomeMedico);
        txtCrmMedico2 = findViewById(R.id.txtCrmMedico2);
        txtCpfMedico = findViewById(R.id.txtCpfMedico);
        txtNascMedico = findViewById(R.id.txtNascMedico);
        txtEmailMedico = findViewById(R.id.txtEmailMedico);
        autoEspecialidadeMedico = findViewById(R.id.auto_complete_especialidade);
        txtSenhaMedico2 = findViewById(R.id.txtSenhaMedico2);
        txtTelefoneMedico = findViewById(R.id.txtTelefoneMedico);
        rdgGenero = findViewById(R.id.rdgGenero);
        rdbMasculino = findViewById(R.id.rdbMasculino);
        rdbFeminino = findViewById(R.id.rdbFeminino);
        btnCadastrar = findViewById(R.id.btnCadastrar);

        imgVoltar3.setOnClickListener(v -> {
            startActivity(new Intent(CadastrarMedico.this, LoginMedico.class));
            finish();
        });

        // 🔹 Máscaras
        aplicarMascaraCPF();
        aplicarMascaraCRM();
        aplicarMascaraTelefone();
        aplicarMascaraData();

        // 🔹 Limites
        aplicarLimiteCaracteres(txtNomeMedico, 50);
        aplicarLimiteCaracteres(txtCrmMedico2, 10);

        btnCadastrar.setOnClickListener(v -> cadastrarMedico());

        autoEspecialidadeMedico = findViewById(R.id.auto_complete_especialidade);

// 🔹 Lista de especialidades
        String[] especialidades = {
                "Cardiologia", "Dermatologia", "Endocrinologia", "Gastroenterologia",
                "Ginecologia", "Neurologia", "Ortopedia", "Pediatria", "Psiquiatria",
                "Urologia", "Oftalmologia", "Otorrinolaringologia", "Reumatologia"
        };

// 🔹 Adapter para o autocomplete
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, especialidades
        );

        autoEspecialidadeMedico.setAdapter(adapter);
        autoEspecialidadeMedico.setThreshold(1); // começa a sugerir após 1 letra

    }

    // =====================================================
    // 🔸 Máscaras
    // =====================================================

    private void aplicarMascaraCPF() {
        txtCpfMedico.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;
                isUpdating = true;

                String str = s.toString().replaceAll("[^\\d]", "");
                if (str.length() > 11) str = str.substring(0, 11);

                StringBuilder mascara = new StringBuilder();
                int i = 0;
                for (char m : "###.###.###-##".toCharArray()) {
                    if (m != '#') mascara.append(m);
                    else if (i < str.length()) mascara.append(str.charAt(i++));
                }

                txtCpfMedico.setText(mascara.toString());
                txtCpfMedico.setSelection(txtCpfMedico.getText().length());
                isUpdating = false;
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void aplicarMascaraCRM() {
        txtCrmMedico2.addTextChangedListener(new TextWatcher() {
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
                txtCrmMedico2.setText(mascara.toString());
                txtCrmMedico2.setSelection(txtCrmMedico2.getText().length());
                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }


    private void aplicarMascaraTelefone() {
        txtTelefoneMedico.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;
                isUpdating = true;

                String str = s.toString().replaceAll("[^\\d]", "");
                if (str.length() > 11) str = str.substring(0, 11);

                StringBuilder mascara = new StringBuilder();
                int i = 0;
                for (char m : "(##) #####-####".toCharArray()) {
                    if (m != '#') mascara.append(m);
                    else if (i < str.length()) mascara.append(str.charAt(i++));
                }

                txtTelefoneMedico.setText(mascara.toString());
                txtTelefoneMedico.setSelection(txtTelefoneMedico.getText().length());
                isUpdating = false;
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void aplicarMascaraData() {
        txtNascMedico.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;
                isUpdating = true;

                String str = s.toString().replaceAll("[^\\d]", "");
                if (str.length() > 8) str = str.substring(0, 8);

                StringBuilder mascara = new StringBuilder();
                int i = 0;
                for (char m : "##/##/####".toCharArray()) {
                    if (m != '#') mascara.append(m);
                    else if (i < str.length()) mascara.append(str.charAt(i++));
                }

                txtNascMedico.setText(mascara.toString());
                txtNascMedico.setSelection(mascara.length());
                isUpdating = false;
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    public static boolean validarCPF(String cpf) {
        cpf = cpf.replaceAll("[^\\d]", "");
        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) return false;

        try {
            for (int j = 9; j < 11; j++) {
                int soma = 0;
                for (int i = 0; i < j; i++)
                    soma += (cpf.charAt(i) - 48) * ((j + 1) - i);

                int resto = (soma * 10) % 11;
                if (resto == 10) resto = 0;
                if (resto != (cpf.charAt(j) - 48)) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    // =====================================================
    // 🔸 Limite de caracteres
    // =====================================================
    private void aplicarLimiteCaracteres(EditText campo, int limite) {
        campo.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > limite) {
                    campo.setText(s.subSequence(0, limite));
                    campo.setSelection(campo.getText().length());
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // =====================================================
    // 🔸 Cadastro
    // =====================================================
    private void cadastrarMedico() {
        String url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/inserir_medico.php";

        String crm = txtCrmMedico2.getText().toString().trim();
        String cpf = txtCpfMedico.getText().toString().trim();
        String nome = txtNomeMedico.getText().toString().trim();
        String email = txtEmailMedico.getText().toString().trim();
        String senha = txtSenhaMedico2.getText().toString().trim();
        String telefone = txtTelefoneMedico.getText().toString().trim().replaceAll("[^\\d]", "");
        String dataNascInput = txtNascMedico.getText().toString().trim();
        String especialidade = autoEspecialidadeMedico.getText().toString().trim();
        String dataNasc = "";
        String sexo = "";
        if (rdbMasculino.isChecked()) {
            sexo = "M";
        } else if (rdbFeminino.isChecked()) {
            sexo = "F";
        }

        if (!validarCPF(cpf)) {
            txtCpfMedico.setError("CPF inválido");
            return;
        }
        try {
            SimpleDateFormat inFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date d = inFormat.parse(dataNascInput);
            dataNasc = outFormat.format(d);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (crm.isEmpty() || cpf.isEmpty() || nome.isEmpty() || email.isEmpty() ||
                senha.isEmpty() || telefone.isEmpty() || dataNasc.isEmpty() ||
                especialidade.isEmpty() || sexo.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject dados = new JSONObject();

        try {
            dados.put("crmMedico", crm);
            dados.put("cpfMedico", cpf);
            dados.put("nome_completoMedico", nome);
            dados.put("emailMedico", email);
            dados.put("senhaMedico", senha);
            dados.put("telefoneMedico", telefone);
            dados.put("data_nascMedico", dataNasc);
            dados.put("especialidadeMedico", especialidade);
            dados.put("sexoMedico", sexo);
        } catch (Exception e ) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                dados,
                response -> {
                    try {
                        boolean sucesso = response.getBoolean("sucesso");
                        String msg = response.getString("mensagem");
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        if (sucesso) {
                            finish(); // ou abrir tela de login
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("API_RESPOSTA", e.getMessage());
                    }
                },
                error -> {
                    Toast.makeText(this, "Erro ao conectar: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
        );
        Volley.newRequestQueue(this).add(request);
    }
}
