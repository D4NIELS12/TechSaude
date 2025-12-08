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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class PacientesMedico extends AppCompatActivity {

    ImageView btnVoltar;
    EditText edtFiltro;
    LinearLayout listaVerPaciente;

    String URL_LISTAR = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/listar_pacientes.php";

    JSONArray listaCompleta;        // lista com JSON dos pacientes
    ArrayList<View> listaItens;     // lista com os CARDS (Views)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pacientes_medico);

        btnVoltar = findViewById(R.id.btnVoltar);
        edtFiltro = findViewById(R.id.edtFiltro);
        listaVerPaciente = findViewById(R.id.listaVerPaciente);

        listaItens = new ArrayList<>();

        btnVoltar.setOnClickListener(v -> finish());

        carregarPacientes();

        // ---------- FILTRO EM TEMPO REAL ----------
        edtFiltro.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarPacientes(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // ============================
    // BUSCAR PACIENTES
    // ============================
    private void carregarPacientes() {

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                URL_LISTAR,
                null,
                response -> {
                    listaCompleta = response;
                    preencherLista(response);
                },
                error -> Toast.makeText(this, "Erro ao carregar lista", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(req);
    }

    // ============================
    // MONTAR LISTA
    // ============================
    private void preencherLista(JSONArray pacientes) {

        listaVerPaciente.removeAllViews();
        listaItens.clear();

        try {

            for (int i = 0; i < pacientes.length(); i++) {

                JSONObject obj = pacientes.getJSONObject(i);

                int id = obj.getInt("idUsuario");
                String nome = obj.getString("nome_completoUsuario");
                String cpf = obj.getString("cpfUsuario");

                // CARREGA O CARD
                LinearLayout item = (LinearLayout) LayoutInflater.from(this)
                        .inflate(R.layout.activity_item_prontuario, listaVerPaciente, false);

                TextView txtPaciente = item.findViewById(R.id.txtPaciente);
                txtPaciente.setText("Paciente: " + nome + "\nCPF: " + cpf);

                int finalId = id;

                // ABRIR PERFIL DO PACIENTE
                item.setOnClickListener(v -> {
                    Intent it = new Intent(PacientesMedico.this, VerPerfilPaciente.class);
                    it.putExtra("idUsuario", finalId);
                    startActivity(it);
                });

                listaVerPaciente.addView(item);
                listaItens.add(item); // <-- AGORA SIM! Lista de Views correta
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================
    // FILTRAR LISTA — CORRIGIDO
    // ============================
    private void filtrarPacientes(String filtro) {
        filtro = filtro.toLowerCase();

        for (View item : listaItens) {
            TextView txt = item.findViewById(R.id.txtPaciente);

            boolean visivel = txt.getText().toString().toLowerCase().contains(filtro);

            item.setVisibility(visivel ? View.VISIBLE : View.GONE);
        }
    }
}
