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
                                Intent i = new Intent(this, AlterarSenha.class);
                                i.putExtra("cpf", cpf);
                                startActivity(i);
                                finish();
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
                txtCpfEsqueci.setText(mascara.toString());
                txtCpfEsqueci.setSelection(txtCpfEsqueci.getText().length());
                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
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
                String str = s.toString().replaceAll("[^\\d]", "");
                StringBuilder mascara = new StringBuilder();

                if (str.length() > 0) {
                    mascara.append(str.substring(0, Math.min(2, str.length())));
                }
                if (str.length() > 2) {
                    mascara.append("/").append(str.substring(2, Math.min(4, str.length())));
                }
                if (str.length() > 4) {
                    mascara.append("/").append(str.substring(4, Math.min(8, str.length())));
                }
                isUpdating = true;
                txtDataEsqueci.setText(mascara.toString());
                txtDataEsqueci.setSelection(txtDataEsqueci.getText().length());
                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

}
