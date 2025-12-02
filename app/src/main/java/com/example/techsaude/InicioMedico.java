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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class InicioMedico extends Fragment {

    public InicioMedico() {
        // Construtor padrão
    }
    TextView txtProximasConsultas;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflando o layout do fragmento
        View view = inflater.inflate(R.layout.activity_inicio_medico, container, false);

        // Referência ao CardView do XML
        View cardProximasConsultas = view.findViewById(R.id.cardProximasConsultas);
        View cardCalendario = view.findViewById(R.id.cardCalendario);
        View cardPacientes = view.findViewById(R.id.cardPacientes);
        View cardReceituario = view.findViewById(R.id.cardReceituario);
        txtProximasConsultas = view.findViewById(R.id.txtProximasConsultas);


        buscarProximasConsultas();

        // Clique para abrir a Agenda Médica
        cardProximasConsultas.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent it = new Intent(getActivity(), AgendaMedica.class);
                startActivity(it);
            }
        });

        cardCalendario.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent it = new Intent(getActivity(), AgendamentosMedico.class);
                startActivity(it);
            }
        });

        cardReceituario.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent it = new Intent(getActivity(), Receituario.class);
                startActivity(it);
            }
        });
        return view;
    }

    private void buscarProximasConsultas() {

        Log.e("DEBUG", "Enviando requisição para proximas_consultas.php");

        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs_medico", Context.MODE_PRIVATE);
        int idMedico = prefs.getInt("idMedico", 0);

        Log.e("DEBUG", "idMedico = " + idMedico);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("idMedico", idMedico);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String url = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/proximas_consultas.php";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    try {
                        Log.e("RESPOSTA_RAW", response.toString());
                        Log.e("IDMEDICO", "Resposta: " + idMedico);
                        Log.e("JSON_ENVIADO", jsonBody.toString());

                        boolean success = response.getBoolean("success");
                        String agendaHoje = response.getString("quantidadeHoje");
                        txtProximasConsultas.setText(agendaHoje);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String erroServidor = new String(error.networkResponse.data);
                        Log.e("ERRO_SERVIDOR", "Resposta servidor: " + erroServidor);
                    } else {
                        Log.e("ERRO_SERVIDOR", "Sem resposta do servidor: " + error.toString());
                    }
                }
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }
}
