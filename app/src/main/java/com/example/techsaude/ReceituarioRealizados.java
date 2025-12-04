package com.example.techsaude;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ReceituarioRealizados extends AppCompatActivity {

    ImageView btnVoltar;
    TextView txtMedico, txtMedicamento, txtDosagem, txtDuracao, txtObservacao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receituario_realizados);

        btnVoltar = findViewById(R.id.btnVoltar3);
        txtMedico = findViewById(R.id.txtMedico);
        txtMedicamento = findViewById(R.id.txtMedicamento);
        txtDosagem = findViewById(R.id.txtDosagem);
        txtDuracao = findViewById(R.id.txtDuracao);
        txtObservacao = findViewById(R.id.txtObservacao);

        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        carregarReceituario();
    }
    private void carregarReceituario() {

        String URL = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/listar_receituario_unico.php";

        int idUsuario = getSharedPreferences("loginUsuario_prefs", MODE_PRIVATE)
                .getInt("idUsuario", -1);

        Log.e("ID_USUARIO", "ID: " + idUsuario);

        if (idUsuario == -1) {
            Toast.makeText(this, "Usuário não identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL,
                response -> {

                    Log.e("RETORNO", response);

                    try {
                        JSONObject obj = new JSONObject(response);

                        if (!obj.getBoolean("success")) {
                            Toast.makeText(this, "Nenhum receituário encontrado", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONObject r = obj.getJSONObject("receituario");

                        txtMedico.setText(r.getString("nome_completoMedico"));
                        txtMedicamento.setText(r.getString("medicamentoProntuario"));
                        txtDosagem.setText(r.getString("dosagemProntuario"));
                        txtDuracao.setText(r.getString("duracaoProntuario"));
                        txtObservacao.setText(r.getString("observacoesProntuario"));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Erro ao conectar", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("idUsuario", String.valueOf(idUsuario));
                return map;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

}