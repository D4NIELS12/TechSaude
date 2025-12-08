package com.example.techsaude;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ReceituarioPaciente extends Fragment {

    CardView cardVerReceituario;
    TextView txtMedico, txtData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_receituario_paciente, container, false);

        cardVerReceituario = view.findViewById(R.id.cardVerReceituario);
        txtMedico = view.findViewById(R.id.txtMedico);
        txtData = view.findViewById(R.id.txtData);
        cardVerReceituario.setOnClickListener(v -> {
            Intent it = new Intent(requireActivity(), ReceituarioRealizados.class);
            startActivity(it);
        });

       mostrarReceituarios();
        return view;
    }

    private void mostrarReceituarios() {

        String URL = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/listar_receituarios.php";

        int idUsuario = requireActivity()
                .getSharedPreferences("loginUsuario_prefs", 0)
                .getInt("idUsuario", -1);

        Log.e("ID_USUARIO", "ID: " + idUsuario);


        if (idUsuario == -1) {
            Toast.makeText(getContext(), "Usuário não identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                URL,
                response -> {
                    Log.e("RETORNO", response); // <-- veja o JSON REAL

                    try {

                        JSONObject obj = new JSONObject(response);
                        boolean success = obj.getBoolean("success");

                        if (!success) {
                            Toast.makeText(getContext(), "Nenhum receituário encontrado", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONArray array = obj.getJSONArray("receituarios");

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject item = array.getJSONObject(i);


                            String medico = item.getString("nome_completoMedico");
                            String data = item.getString("dataEmissao");
                            String dataBR = converterDataBR(data);
                            txtMedico.setText("Médico: " + medico);
                            txtData.setText("Data: " + dataBR);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(getContext(), "Erro ao conectar", Toast.LENGTH_SHORT).show()
        ){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("idUsuario", String.valueOf(idUsuario));
                return map;
            }
        };

        Volley.newRequestQueue(requireActivity()).add(request);
    }
    private String converterDataBR(String dataMYSQL) {
        try {
            String[] partes = dataMYSQL.split("-");
            String dia = partes[2];
            String mes = partes[1];
            String ano = partes[0];
            return dia + "/" + mes + "/" + ano;
        } catch (Exception e) {
            e.printStackTrace();
            return dataMYSQL;
        }
    }
}
