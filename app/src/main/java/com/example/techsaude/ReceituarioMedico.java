package com.example.techsaude;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ReceituarioMedico extends AppCompatActivity {

    ImageView btnVoltar;
    private EditText edtFiltro;
    LinearLayout listaPacientes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receituario_medico);

        btnVoltar = findViewById(R.id.btnVoltar);
        listaPacientes = findViewById(R.id.listaPacientes);
        edtFiltro = findViewById(R.id.edtFiltro);

        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        listaPacientes.setOnClickListener(v -> {
            Intent it = new Intent(ReceituarioMedico.this, RealizarReceituario.class);
            startActivity(it);
        });

        carregarProntuarios();

        // Filtro simples por nome
        edtFiltro.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                filtrarProntuarios(s.toString());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private List<View> listaItens = new ArrayList<>(); // Para filtro

    private void carregarProntuarios() {
        String url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/mostrar_prontuarios.php";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (!response.getBoolean("success")) return;

                        JSONArray prontuarios = response.getJSONArray("prontuarios");

                        for (int i = 0; i < prontuarios.length(); i++) {
                            JSONObject p = prontuarios.getJSONObject(i);
                            int idProntuario = p.getInt("idProntuario"); // ID oculto
                            String nomePaciente = p.getString("paciente");
                            String dataRegistro = p.getString("data_registroProntuario");

                            adicionarItemLista(nomePaciente, dataRegistro, idProntuario);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Erro ao carregar prontuários", Toast.LENGTH_SHORT).show()
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void adicionarItemLista(String nomePaciente, String dataRegistro, int idProntuario) {
        LinearLayout item = (LinearLayout) LayoutInflater.from(this)
                .inflate(R.layout.activity_item_prontuario, listaPacientes, false);

        TextView txtPaciente = item.findViewById(R.id.txtPaciente);
        txtPaciente.setText("Paciente: " + nomePaciente + "\nData: " + formatarData(dataRegistro));
        // NÃO adiciona o idProntuario no texto

        // Clique no item envia o id internamente
        item.setOnClickListener(v -> {
            Intent it = new Intent(ReceituarioMedico.this, RealizarReceituario.class);
            it.putExtra("idProntuario", idProntuario); // Envia ID oculto
            it.putExtra("paciente", nomePaciente);    // Nome visível
            startActivity(it);
        });

        listaPacientes.addView(item);
        listaItens.add(item);
    }


    private String formatarData(String dataISO) {
        String[] partes = dataISO.split(" ")[0].split("-");
        return partes[2] + "/" + partes[1] + "/" + partes[0];
    }

    private void filtrarProntuarios(String filtro) {
        filtro = filtro.toLowerCase();
        for (View v : listaItens) {
            TextView txt = v.findViewById(R.id.txtPaciente);
            v.setVisibility(txt.getText().toString().toLowerCase().contains(filtro) ? View.VISIBLE : View.GONE);
        }
    }
}