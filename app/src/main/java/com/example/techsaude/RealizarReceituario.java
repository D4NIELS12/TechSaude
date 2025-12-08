package com.example.techsaude;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RealizarReceituario extends AppCompatActivity {

    ImageView btnVoltar;

    private int idProntuario;
    private EditText nomePaciente, inputMedicamento, inputDosagem, inputDuracao, inputObservacao;
    ImageView imgProntuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realizar_receituario);

        btnVoltar = findViewById(R.id.btnVoltar);
        nomePaciente = findViewById(R.id.inputPaciente);
        imgProntuario = findViewById(R.id.imgProntuario);
        inputMedicamento = findViewById(R.id.inputMedicamento);
        inputDosagem = findViewById(R.id.inputDosagem);
        inputDuracao = findViewById(R.id.inputDuracao);
        inputObservacao = findViewById(R.id.inputObservacao);

        Button btnSalvar = findViewById(R.id.btnGerar);

        btnSalvar.setOnClickListener(v -> enviarReceituario());
        Intent it = getIntent();
        if (it != null) {
            idProntuario = it.getIntExtra("idProntuario", 0);
            nomePaciente.setText(it.getStringExtra("paciente"));

            // Coloca o nome do paciente no EditText

        }
        imgProntuario.setOnClickListener(v -> carregarProntuario());
        Log.d("ID", "id:" + idProntuario);


        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void carregarProntuario() {
        String url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/mostrar_prontuario_usuario.php";

        // Monta o JSON para enviar
        JSONObject json = new JSONObject();
        try {
            json.put("idProntuario", idProntuario);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Requisição POST com o corpo JSON
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, json,
                response -> {
                    try {
                        if (!response.getBoolean("success")) return;

                        JSONArray prontuarios = response.getJSONArray("prontuarios");

                        for (int i = 0; i < prontuarios.length(); i++) {
                            JSONObject p = prontuarios.getJSONObject(i);
                            String dataRegistro = p.getString("data_registroProntuario");
                            String peso_kgProntuario = p.getString("peso_kgProntuario");
                            String altura_cmProntuario = p.getString("altura_cmProntuario");
                            String sintomasProntuario = p.getString("sintomasProntuario");
                            String alergiasProntuario = p.getString("alergiasProntuario");
                            String condicoes_chronicasProntuario = p.getString("condicoes_chronicasProntuario");
                            String observacoesProntuario = p.getString("observacoesProntuario");
                            String alertasProntuario = p.getString("alertasProntuario");

                            // Aqui você pode chamar o AlertDialog
                            mostrarProntuarioDialog(dataRegistro, peso_kgProntuario, altura_cmProntuario, sintomasProntuario, alergiasProntuario, condicoes_chronicasProntuario, observacoesProntuario, alertasProntuario);
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
    private void mostrarProntuarioDialog(String dataRegistro, String peso, String altura, String sintomas,
                                         String alergias, String condicoes, String observacoes, String alertas) {

        // Inflar o layout do dialog
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_prontuario, null);

        TextView txtDialogDataRegistro = dialogView.findViewById(R.id.txtDialogDataRegistro);
        TextView txtDialogPeso = dialogView.findViewById(R.id.txtDialogPeso);
        TextView txtDialogAltura = dialogView.findViewById(R.id.txtDialogAltura);
        TextView txtDialogSintomas = dialogView.findViewById(R.id.txtDialogSintomas);
        TextView txtDialogAlergias = dialogView.findViewById(R.id.txtDialogAlergias);
        TextView txtDialogCondicoes = dialogView.findViewById(R.id.txtDialogCondicoes);
        TextView txtDialogObservacoes = dialogView.findViewById(R.id.txtDialogObservacoes);
        TextView txtDialogAlertas = dialogView.findViewById(R.id.txtDialogAlertas);
        Button btnOK = dialogView.findViewById(R.id.btnDialogOK);

        // Preencher campos, usando "Não informado" se estiver vazio
        txtDialogDataRegistro.setText("Data de registro: " + formatarDataHoraParaBR((dataRegistro.isEmpty() ? "Não informado" : dataRegistro)));
        txtDialogPeso.setText("Peso: " + (peso.isEmpty() ? "Não informado" : peso));
        txtDialogAltura.setText("Altura: " + (altura.isEmpty() ? "Não informado" : altura));
        txtDialogSintomas.setText("Sintomas: " + (sintomas.isEmpty() ? "Não informado" : sintomas));
        txtDialogAlergias.setText("Alergias: " + (alergias.isEmpty() ? "Não informado" : alergias));
        txtDialogCondicoes.setText("Condições: " + (condicoes.isEmpty() ? "Não informado" : condicoes));
        txtDialogObservacoes.setText("Observações: " + (observacoes.isEmpty() ? "Não informado" : observacoes));
        txtDialogAlertas.setText("Alertas: " + (alertas.isEmpty() ? "Não informado" : alertas));

        // Criar e mostrar dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnOK.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
    private String formatarDataHoraParaBR(String dataHoraUSA) {
        try {
            // Formato que vem do servidor (ex: 2025-01-12 14:30:00)
            SimpleDateFormat formatoEUA = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

            // Formato brasileiro desejado (ex: 12/01/2025 14:30)
            SimpleDateFormat formatoBR = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));

            Date date = formatoEUA.parse(dataHoraUSA);
            return formatoBR.format(date);

        } catch (Exception e) {
            return dataHoraUSA; // Caso aconteça erro, retorna como veio
        }
    }


    private void enviarReceituario() {

        String medicamento = inputMedicamento.getText().toString().trim();
        String dosagem = inputDosagem.getText().toString().trim();
        String duracao = inputDuracao.getText().toString().trim();
        String observacao = inputObservacao.getText().toString().trim();

        if (medicamento.isEmpty() || dosagem.isEmpty() || duracao.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show();
            return;
        }

        // PEGAR O ID DO MÉDICO LOGADO
        SharedPreferences prefsMedico = getSharedPreferences("user_prefs_medico", MODE_PRIVATE);
        int idMedico = prefsMedico.getInt("idMedico", 0);

        if (idMedico == 0) {
            Toast.makeText(this, "Erro: médico não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/salvar_receituario.php";

        JSONObject json = new JSONObject();
        try {
            json.put("idProntuario", idProntuario);
            json.put("idMedico", idMedico);
            json.put("medicamento", medicamento);
            json.put("dosagem", dosagem);
            json.put("duracao", duracao);
            json.put("observacao", observacao);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, json,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String msg = response.getString("message");
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

                        if (success) finish();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Erro no servidor!", Toast.LENGTH_SHORT).show();
                    Log.e("ERRO_SERVIDOR", error.toString());
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

}