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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EsqueciSenha extends AppCompatActivity {
    ImageView imgVoltarEsqueci;
    EditText txtCpfEsqueci, txtDataEsqueci;
    Button btnRecuperar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esqueci_senha);

        imgVoltarEsqueci = findViewById(R.id.imgVoltarEsqueci);
        txtCpfEsqueci = findViewById(R.id.txtCpfEsqueci);
        txtDataEsqueci = findViewById(R.id.txtDataEsqueci);
        btnRecuperar = findViewById(R.id.btnRecuperar);

        imgVoltarEsqueci.setOnClickListener(v -> finish());

        aplicarMascaraCPF();
        aplicarMascaraData();

        btnRecuperar.setOnClickListener(v -> {

            String cpf = txtCpfEsqueci.getText().toString().trim();
            String dataNascInput = txtDataEsqueci.getText().toString().trim(); // DD/MM/YYYY
            String dataNasc = "";
            if (cpf.isEmpty() || dataNascInput.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            Date d;
            try {
                SimpleDateFormat inFormat = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd");
                d = inFormat.parse(dataNascInput);
                dataNasc = outFormat.format(d);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/verificar_redefinicao.php";
            JSONObject dados = new JSONObject();
            try {
                dados.put("cpf", cpf);
                dados.put("data", dataNasc);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, dados,
                    response -> {
                        Log.d("API_REDEFINICAO", "Resposta: " + response.toString());
                        try {
                            if (response.getString("status").equals("ok")) {
                                Toast.makeText(this, "Dados confirmados!", Toast.LENGTH_SHORT).show();
                                txtCpfEsqueci = null;
                                txtDataEsqueci = null;
                                Intent i = new Intent(this, AlterarSenha.class);
                                i.putExtra("cpf", cpf);
                                startActivity(i);
                            } else {
                                Toast.makeText(this, "Dados inválidos!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Toast.makeText(this, "Erro no servidor", Toast.LENGTH_SHORT).show());


            Volley.newRequestQueue(this).add(req);
        });
    }



    private void aplicarMascaraCPF() {
        txtCpfEsqueci.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;
                isUpdating = true;

                String str = s.toString().replaceAll("[^\\d]", "");
                txtCpfEsqueci.setText(formatarCPF(str));
                txtCpfEsqueci.setSelection(txtCpfEsqueci.getText().length());

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

    private void aplicarMascaraData() {
        txtDataEsqueci.addTextChangedListener(new TextWatcher() {
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

                txtDataEsqueci.setText(mascara.toString());
                txtDataEsqueci.setSelection(mascara.length());
                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

}
