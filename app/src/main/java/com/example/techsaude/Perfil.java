package com.example.techsaude;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Perfil extends Fragment {

    private TextView lblNome, lblCPF, lblEmail, lblNascimento,
            lblEndereco, lblTelefone, lblSexo;
    String URL_BUSCAR = "http://tcc3edsmodetecgr3.hospedagemdesites.ws/buscar_prontuario.php?idUsuario=";
    LinearLayout viewRelatorio;

    public Perfil() {
        super(R.layout.activity_perfil);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewRelatorio = view.findViewById(R.id.viewRelatorio);
        lblNome = view.findViewById(R.id.lblNome);
        lblCPF = view.findViewById(R.id.lblCPF);
        lblEmail = view.findViewById(R.id.lblEmail);
        lblNascimento = view.findViewById(R.id.lblNascimento);
        lblEndereco = view.findViewById(R.id.lblEndereco);
        lblTelefone = view.findViewById(R.id.lblTelefone);
        lblSexo = view.findViewById(R.id.lblSexo);

        SharedPreferences prefs = requireActivity().getSharedPreferences("loginUsuario_prefs", Context.MODE_PRIVATE);

        String nome = prefs.getString("nome", "Nome não encontrado");
        String cpf = prefs.getString("cpfUsuario", "CPF não encontrado");
        String email = prefs.getString("email", "Email não encontrado");
        String nascimento = prefs.getString("data_nasc", "Data não encontrada");
        String endereco = prefs.getString("endereco", "Endereço não encontrado");
        String telefone = prefs.getString("telefone", "Telefone não encontrado");
        String sexo = prefs.getString("sexo", "Sexo não informado");

        String sexoExtenso = "";

        if (sexo.equals("M")) {
            sexoExtenso = "Masculino";
        } else if (sexo.equals("F")) {
            sexoExtenso = "Feminino";
        } else {
            sexoExtenso = "Sexo não informado";
        }

        String telefoneFormatado = formatarTelefone(telefone);
        try {
            // Formatadores
            SimpleDateFormat formatoEUA = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat formatoBR = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));

            // Converter
            Date data = formatoEUA.parse(nascimento);
            String dataBrasileira = formatoBR.format(data);
            lblNascimento.setText("Nascimento: " + dataBrasileira);


        } catch (Exception e) {
            e.printStackTrace();
        }


        // 🔹 Exibe nos TextViews
        lblNome.setText("Nome: " +nome);
        lblCPF.setText("CPF: " + cpf);
        lblEmail.setText("Email: " + email);
        lblEndereco.setText("Endereço: " + endereco);
        lblTelefone.setText("Telefone: " + telefoneFormatado);
        lblSexo.setText("Sexo: " + sexoExtenso);

        int idUsuario = prefs.getInt("idUsuario",0);

        if (idUsuario != 0) {
            carregarProntuario(idUsuario);
        }


    }

    public static String formatarTelefone(String telefone) {

        if (telefone.length() == 11) {
            // Celular
            return String.format("(%s) %s-%s",
                    telefone.substring(0, 2),        // DDD
                    telefone.substring(2, 7),        // 5 primeiros
                    telefone.substring(7, 11));      // últimos 4
        } else {
            return telefone; // Número fora do padrão
        }
    }

    private void adicionarItem(String titulo, String valor) {
        TextView txt = new TextView(requireContext());
        txt.setText(titulo + ": " + (valor == null || valor.isEmpty() ? "Não informado" : valor));
        txt.setPadding(25, 25, 25, 25);
        txt.setTextSize(20);
        txt.setTextColor(getResources().getColor(android.R.color.black));
        viewRelatorio.addView(txt);
    }

    private void preencherRelatorio(JSONObject response) {
        viewRelatorio.removeAllViews(); // limpa antes de preencher novamente

        try {
            adicionarItem("Data de registro", response.getString("data_registroProntuario"));
            adicionarItem("Peso (kg)", response.getString("peso_kgProntuario"));
            adicionarItem("Altura (cm)", response.getString("altura_cmProntuario"));
            adicionarItem("Sintomas", response.getString("sintomasProntuario"));
            adicionarItem("Alergias", response.getString("alergiasProntuario"));
            adicionarItem("Observações", response.getString("observacoesProntuario"));
            adicionarItem("Alertas", response.getString("alertasProntuario"));
            adicionarItem("Condições Crônicas", response.getString("condicoes_chronicasProntuario"));

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Erro ao preencher relatório", Toast.LENGTH_SHORT).show();
        }
    }

    private void carregarProntuario(int idUsuario) {

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                URL_BUSCAR + idUsuario,
                null,
                response -> {
                    try {

                        if (response.has("error")) return;

                        // Preenche o layout do relatório
                        preencherRelatorio(response);

                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(requireContext(), "Erro no servidor", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(requireContext()).add(req);
    }

}
