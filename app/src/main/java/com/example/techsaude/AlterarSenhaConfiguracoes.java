package com.example.techsaude;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class AlterarSenhaConfiguracoes extends AppCompatActivity {
    EditText edtSenhaAtual, edtNovaSenha, edtConfirmarSenha;
    Button btnAlterarSenha;
    ImageView imgVoltarSenha;
    String URL = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/alterar_senha_configuracoes.php";  // coloque sua URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar_senha_configuracoes);

        edtSenhaAtual = findViewById(R.id.edtSenhaAtual);
        edtNovaSenha = findViewById(R.id.edtNovaSenha);
        edtConfirmarSenha = findViewById(R.id.edtConfirmarSenha);
        btnAlterarSenha = findViewById(R.id.btnAlterarSenha);
        imgVoltarSenha = findViewById(R.id.imgVoltarSenha);
        imgVoltarSenha.setOnClickListener(v -> finish());

        btnAlterarSenha.setOnClickListener(v -> alterarSenha());
    }

    private void alterarSenha() {

        String senhaAtual = edtSenhaAtual.getText().toString().trim();
        String novaSenha = edtNovaSenha.getText().toString().trim();
        String confirmar = edtConfirmarSenha.getText().toString().trim();

        if (!novaSenha.equals(confirmar)) {
            Toast.makeText(this, "Senhas não conferem!", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);

                        boolean sucesso = json.getBoolean("sucesso");
                        String mensagem = json.getString("mensagem");

                        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Erro: " + error.getMessage(), Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {

                SharedPreferences prefs = getSharedPreferences("loginUsuario_prefs", MODE_PRIVATE);
                int idUsuario = prefs.getInt("idUsuario", 0);
                Map<String, String> params = new HashMap<>();
                params.put("idUsuario", String.valueOf(idUsuario)); // substitua pelo login real
                params.put("senha_atual", senhaAtual);
                params.put("nova_senha", novaSenha);

                return params;
            }
        };

        queue.add(request);
    }
}