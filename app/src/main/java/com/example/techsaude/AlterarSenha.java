package com.example.techsaude;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AlterarSenha extends AppCompatActivity {

    EditText txtNovaSenha;
    Button btnRedefinir;
    ImageView imgVoltarAlterar;
    String cpfRecebido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar_senha);

        txtNovaSenha = findViewById(R.id.txtNovaSenha);
        btnRedefinir = findViewById(R.id.btnRecuperar);
        imgVoltarAlterar = findViewById(R.id.imgVoltarAlterar);
        imgVoltarAlterar.setOnClickListener(v -> finish());

        // pega CPF da tela anterior
        cpfRecebido = getIntent().getStringExtra("cpf");

        if (cpfRecebido == null) {
            Toast.makeText(this, "Erro: CPF não recebido!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnRedefinir.setOnClickListener(view -> {
            String novaSenha = txtNovaSenha.getText().toString().trim();
            enviarRedefinicao(cpfRecebido, novaSenha);
        });
    }

    private void enviarRedefinicao(String cpf, String novaSenha) {

        String url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/atualizar_senha.php";

        // Criando o JSON que o PHP espera
        JSONObject dados = new JSONObject();
        try {
            dados.put("cpf", cpf);
            dados.put("nova_senha", novaSenha);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest requisicao = new JsonObjectRequest(
                Request.Method.POST,
                url,
                dados,
                response -> {
                    Log.d("API_ALTERAR", "Resposta: " + response.toString());
                    Toast.makeText(this, "Senha alterada com sucesso", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Log.e("API_ALTERAR", "Erro Volley: " + error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(requisicao);
    }

}
