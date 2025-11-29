package com.example.techsaude;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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

import java.util.HashMap;
import java.util.Map;

public class AlterarSenhaMedico extends AppCompatActivity {

    Button btnRecuperarSenhaMedico;
    EditText txtNovaSenhaMedico;
    ImageView imgVoltarAlterar;
    String crmRecebido;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar_senha_medico);
        imgVoltarAlterar = findViewById(R.id.imgVoltarAlterar);
        txtNovaSenhaMedico = findViewById(R.id.txtNovaSenhaMedico);
        btnRecuperarSenhaMedico = findViewById(R.id.btnRecuperarSenhaMedico);

        crmRecebido = getIntent().getStringExtra("crm");

        if (crmRecebido == null) {
            Toast.makeText(this, "Erro: CRM não recebido!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        imgVoltarAlterar.setOnClickListener(v -> finish());

        btnRecuperarSenhaMedico.setOnClickListener(v ->{
            String novaSenha = txtNovaSenhaMedico.getText().toString().trim();
            enviarRedefinicao(crmRecebido, novaSenha);
        });
    }

    private void enviarRedefinicao(String crm, String novaSenhaMedico) {
        String url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/atualizar_senha_medico.php";

        JSONObject dados = new JSONObject();
        try {
            dados.put("crm", crm);
            dados.put("nova_senha", novaSenhaMedico);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest requisicao = new JsonObjectRequest(
                Request.Method.POST,
                url,
                dados,
                response -> {
                    Log.d("API_ALTERAR", "Resposta:" + response.toString());
                    Toast.makeText(this, "Senha alterada com sucesso", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Log.e("API_ALTERAR", "Erro volley: " + error.toString());
                }
        );

        Volley.newRequestQueue(this).add(requisicao);
    }
}