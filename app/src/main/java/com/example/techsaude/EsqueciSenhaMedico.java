package com.example.techsaude;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

public class EsqueciSenhaMedico extends AppCompatActivity {

    EditText txtCRMEsqueci, txtDataEsqueciMedico;
    Button btnRecuperarMedico;
    ImageView imgVoltar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esqueci_senha_medico);

        txtCRMEsqueci = findViewById(R.id.txtCRMEsqueci);
        txtDataEsqueciMedico = findViewById(R.id.txtDataEsqueciMedico);
        btnRecuperarMedico = findViewById(R.id.btnRecuperarMedico);
        imgVoltar = findViewById(R.id.imgVoltarEsqueciMedico);

        imgVoltar.setOnClickListener(v -> finish());

        btnRecuperarMedico.setOnClickListener(v -> {

            String crm = txtCRMEsqueci.getText().toString().trim();
            String dataNascInput = txtDataEsqueciMedico.getText().toString().trim();
            String data = "";

            if (crm.isEmpty() || dataNascInput.isEmpty()){
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_LONG).show();
                return;
            }

            Date d;
            try {
                SimpleDateFormat inFormat = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd");
                d = inFormat.parse(dataNascInput);
                data = outFormat.format(d);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/verificar_redefinicao_medico.php";
            JSONObject dados = new JSONObject();
            try {
                dados.put("crm", crm);
                dados.put("data", data);
            } catch (JSONException e){
                e.printStackTrace();
            }

            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, dados,
                    response -> {
                        Log.d("API_REDEFINICAO", "Resposta:"+ response.toString());
                        try {
                            if (response.getString("status").equals("ok")) {
                                Toast.makeText(this, "Dados confirmados!", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(this, AlterarSenhaMedico.class);
                                i.putExtra("crm", crm);
                                startActivity(i);
                                finish();
                            }
                            else {
                                Toast.makeText(this, "Dados inválidos!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    },
                    error -> Toast.makeText(this, "Erro no servidor", Toast.LENGTH_SHORT).show());
            Volley.newRequestQueue(this).add(req);
        });
        txtCRMEsqueci.addTextChangedListener(new TextWatcher() {
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
                txtCRMEsqueci.setText(mascara.toString());
                txtCRMEsqueci.setSelection(txtCRMEsqueci.getText().length());
                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        txtDataEsqueciMedico.addTextChangedListener(new TextWatcher() {
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
                txtDataEsqueciMedico.setText(mascara.toString());
                txtDataEsqueciMedico.setSelection(mascara.length());
                isUpdating = false;
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}