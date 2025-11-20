package com.example.techsaude;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class EsqueciSenha extends AppCompatActivity
{
    ImageView imgVoltarEsqueci;
    EditText txtCpfEsqueci, txtNovaSenha;
    Button btnRecuperar;
    private static final String URL_API = "https://techsaude-api.vercel.app/api/alterar_senha";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esqueci_senha);

        imgVoltarEsqueci = findViewById(R.id.imgVoltarEsqueci);
        txtCpfEsqueci = findViewById(R.id.txtCpfEsqueci);
        txtNovaSenha = findViewById(R.id.txtNovaSenha);
        btnRecuperar = findViewById(R.id.btnRecuperar);

        imgVoltarEsqueci.setOnClickListener(v -> finish());

        btnRecuperar.setOnClickListener(v -> alterarSenha());
        aplicarMascaraCPF();
    }

    private void alterarSenha() {
        String cpf = txtCpfEsqueci.getText().toString().trim();
        String novaSenha = txtNovaSenha.getText().toString().trim();

        if (cpf.isEmpty() || novaSenha.isEmpty()) {
            Toast.makeText(this, "Preencha CPF e nova senha!", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                JSONObject json = new JSONObject();
                json.put("cpfUsuario", cpf);
                json.put("novaSenha", novaSenha);

                URL url = new URL(URL_API);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream();
                     OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                    writer.write(json.toString());
                    writer.flush();
                }

                int code = conn.getResponseCode();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream(),
                        StandardCharsets.UTF_8
                ));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) response.append(line);

                JSONObject resp = new JSONObject(response.toString());
                runOnUiThread(() -> {
                    if (resp.optBoolean("sucesso")) {
                        Toast.makeText(this, resp.optString("mensagem", "Senha alterada!"), Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this, resp.optString("erro", "Erro ao alterar senha!"), Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
    private void aplicarMascaraCPF() {
        txtCpfEsqueci.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;
                isUpdating = true;

                String str = s.toString().replaceAll("[^\\d]", "");
                txtCpfEsqueci.setText(formatarCPF(str));
                txtCpfEsqueci.setSelection(txtCpfEsqueci.getText().length());

                isUpdating = false;
            }
            @Override public void afterTextChanged(Editable s) {}
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
}
