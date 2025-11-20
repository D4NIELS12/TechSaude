package com.example.techsaude;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
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

public class CadastrarPaciente extends AppCompatActivity {

    ImageView imgVoltar;
    EditText txtCpfCadastrarPaciente, txtNomePaciente, txtNascPaciente, txtSenhaCadastrarPaciente,
            txtEmailPaciente, txtTelefonePaciente, txtEnderecoPaciente;
    RadioGroup rdgGenero;
    RadioButton rdbMasculino, rdbFeminino;
    Button btnCadastrar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_paciente);

        imgVoltar = findViewById(R.id.imgVoltar3);
        txtCpfCadastrarPaciente = findViewById(R.id.txtCpfCadastrarPaciente);
        txtNomePaciente = findViewById(R.id.txtNomePaciente);
        txtNascPaciente = findViewById(R.id.txtNascPaciente);
        txtSenhaCadastrarPaciente = findViewById(R.id.txtSenhaPaciente);
        txtEmailPaciente = findViewById(R.id.txtEmailPaciente);
        txtTelefonePaciente = findViewById(R.id.txtTelefonePaciente);
        txtEnderecoPaciente = findViewById(R.id.txtEndereçoPaciente);
        rdgGenero = findViewById(R.id.rdgGenero);
        rdbMasculino = findViewById(R.id.rdbMasculino);
        rdbFeminino = findViewById(R.id.rdbFeminino);
        btnCadastrar = findViewById(R.id.btnCadastrar);

        imgVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(CadastrarPaciente.this, LoginPaciente.class);
                startActivity(it);
                finish();
            }
        });
        // 🔹 Máscaras
        aplicarMascaraCPF();
        aplicarMascaraTelefone();
        aplicarMascaraData();

        // 🔹 Botão de cadastro
        btnCadastrar.setOnClickListener(view -> cadastrarPaciente());
    }

    // =====================================================
    // 🔸 Máscaras
    // =====================================================
    private void aplicarMascaraCPF() {
        txtCpfCadastrarPaciente.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;
                isUpdating = true;

                String str = s.toString().replaceAll("[^\\d]", "");
                txtCpfCadastrarPaciente.setText(formatarCPF(str));
                txtCpfCadastrarPaciente.setSelection(txtCpfCadastrarPaciente.getText().length());

                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private String formatarCPF(String cpf) {
        if (cpf == null) return "";
        cpf = cpf.replaceAll("[^\\d]", "");
        if (cpf.length() > 11) cpf = cpf.substring(0, 11);

        StringBuilder mascara = new StringBuilder();
        int i = 0;
        for (char m : "###.###.###-##".toCharArray()) {
            if (m != '#') mascara.append(m);
            else if (i < cpf.length()) mascara.append(cpf.charAt(i++));
        }
        return mascara.toString();
    }

    private void aplicarMascaraTelefone() {
        txtTelefonePaciente.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

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

                txtTelefonePaciente.setText(mascara.toString());
                txtTelefonePaciente.setSelection(mascara.length());
                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void aplicarMascaraData() {
        txtNascPaciente.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
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

                txtNascPaciente.setText(mascara.toString());
                txtNascPaciente.setSelection(mascara.length());
                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // =====================================================
    // 🔸 Validações
    // =====================================================
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

    public static boolean validarTelefone(String telefone) {
        telefone = telefone.replaceAll("[^\\d]", "");
        return telefone.length() >= 10 && telefone.length() <= 11;
    }

    public static boolean validarData(String data) {
        if (data == null || !data.matches("\\d{2}/\\d{2}/\\d{4}")) return false;
        try {
            String[] partes = data.split("/");
            int dia = Integer.parseInt(partes[0]);
            int mes = Integer.parseInt(partes[1]);
            int ano = Integer.parseInt(partes[2]);

            if (mes < 1 || mes > 12) return false;
            if (dia < 1 || dia > 31) return false;
            if ((mes == 4 || mes == 6 || mes == 9 || mes == 11) && dia > 30) return false;
            if (mes == 2) {
                boolean bissexto = (ano % 4 == 0 && ano % 100 != 0) || (ano % 400 == 0);
                if (dia > (bissexto ? 29 : 28)) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // =====================================================
    // 🔸 Cadastro
    // =====================================================
    private void cadastrarPaciente() {
        String url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/inserir_usuario.php";

        String nome = txtNomePaciente.getText().toString().trim();
        String cpf = txtCpfCadastrarPaciente.getText().toString().trim();
        String dataNascInput = txtNascPaciente.getText().toString().trim();
        String senha = txtSenhaCadastrarPaciente.getText().toString().trim();
        String email = txtEmailPaciente.getText().toString().trim();
        String telefone = txtTelefonePaciente.getText().toString().trim();
        String endereco = txtEnderecoPaciente.getText().toString().trim();
        String dataNasc = "";

        try {
            SimpleDateFormat inFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date d = inFormat.parse(dataNascInput);
            dataNasc = outFormat.format(d);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String sexo = "";
        if (rdbMasculino.isChecked()) {
            sexo = "M";
        } else if (rdbFeminino.isChecked()) {
            sexo = "F";
        }
            // Verificação simples
        if (nome.isEmpty() || cpf.isEmpty() || dataNasc.isEmpty() || senha.isEmpty() || email.isEmpty() || telefone.isEmpty() || endereco.isEmpty() || sexo.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 Criar JSON para enviar
        JSONObject dados = new JSONObject();
        try {
            dados.put("nome_completoUsuario", nome);
            dados.put("cpfUsuario", cpf);
            dados.put("emailUsuario", email);
            dados.put("data_nascUsuario", dataNasc);
            dados.put("enderecoUsuario", endereco);
            dados.put("sexoUsuario", sexo);
            dados.put("senhaUsuario", senha);
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