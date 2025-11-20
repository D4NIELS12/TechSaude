package com.example.techsaude;


import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "TechSaude.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Metodo para criar tabelas
        db.execSQL("PRAGMA foreign_keys=ON;");

        db.execSQL("CREATE TABLE IF NOT EXISTS TB_Medico (" +
                "idMedico INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nome_completoMedico TEXT NOT NULL," +
                "crmMedico TEXT NOT NULL UNIQUE," +
                "cpfMedico TEXT NOT NULL UNIQUE," +
                "emailMedico TEXT NOT NULL UNIQUE," +
                "data_nascMedico TEXT NOT NULL," +
                "sexoMedico TEXT NOT NULL," +
                "especialidadeMedico TEXT NOT NULL," +
                "senhaMedico TEXT NOT NULL," +
                "telefoneMedico TEXT NOT NULL UNIQUE" +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS TB_Usuario (" +
                "idUsuario INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nome_completoUsuario TEXT NOT NULL," +
                "cpfUsuario TEXT NOT NULL UNIQUE," +
                "emailUsuario TEXT NOT NULL UNIQUE," +
                "data_nascUsuario TEXT NOT NULL," +
                "enderecoUsuario TEXT NOT NULL," +
                "sexoUsuario TEXT NOT NULL," +
                "senhaUsuario TEXT NOT NULL," +
                "telefoneUsuario TEXT NOT NULL UNIQUE" +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS TB_Consulta (" +
                "idConsulta INTEGER PRIMARY KEY AUTOINCREMENT," +
                "cpfUsuario TEXT," +
                "especialidadeConsulta TEXT NOT NULL," +
                "medicoConsulta TEXT NOT NULL," +
                "dataConsulta TEXT NOT NULL," +
                "horarioConsulta TEXT NOT NULL," +
                "statusConsulta TEXT," +
                "valorConsulta REAL," +
                "FOREIGN KEY(cpfUsuario) REFERENCES TB_Usuario(cpfUsuario)" +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS TB_Exame (" +
                "idExame INTEGER PRIMARY KEY AUTOINCREMENT," +
                "cpfUsuario TEXT," +
                "tipoExame TEXT NOT NULL," +
                "medicoExame TEXT NOT NULL," +
                "dataExame TEXT NOT NULL," +
                "horarioExame TEXT NOT NULL," +
                "statusExame TEXT," +
                "valorExame REAL," +
                "FOREIGN KEY(cpfUsuario) REFERENCES TB_Usuario(cpfUsuario)" +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS TB_Vacina (" +
                "idVacina INTEGER PRIMARY KEY AUTOINCREMENT," +
                "cpfUsuario TEXT," +
                "tipoVacina TEXT NOT NULL," +
                "dataVacina TEXT NOT NULL," +
                "valorVacina REAL," +
                "FOREIGN KEY(cpfUsuario) REFERENCES TB_Usuario(cpfUsuario)" +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS TB_Agenda_Medico (" +
                "idAgenda INTEGER PRIMARY KEY AUTOINCREMENT," +
                "crmMedico TEXT," +
                "dataAgenda TEXT," +
                "inicioAgenda TEXT," +
                "fimAgenda TEXT," +
                "statusAgenda TEXT," +
                "FOREIGN KEY(crmMedico) REFERENCES TB_Medico(crmMedico)" +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS TB_Prontuario (" +
                "idProntuario INTEGER PRIMARY KEY AUTOINCREMENT," +
                "cpfUsuario TEXT," +
                "data_registroProntuario TEXT," +
                "peso_kgProntuario REAL NOT NULL," +
                "altura_cmProntuario REAL NOT NULL," +
                "sintomasProntuario TEXT NOT NULL," +
                "alergiasProntuario TEXT NOT NULL," +
                "condicoes_chronicasProntuario TEXT NOT NULL," +
                "observacoesProntuario TEXT," +
                "alertasProntuario TEXT," +
                "FOREIGN KEY(cpfUsuario) REFERENCES TB_Usuario(cpfUsuario)" +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS TB_Dicas (" +
                "idDica INTEGER PRIMARY KEY AUTOINCREMENT," +
                "crmMedico TEXT," +
                "idProntuario INTEGER," +
                "dataDica TEXT NOT NULL," +
                "tituloDica TEXT NOT NULL," +
                "descricaoDica TEXT NOT NULL," +
                "observacoesDica TEXT NOT NULL," +
                "FOREIGN KEY(idProntuario) REFERENCES TB_Prontuario(idProntuario)," +
                "FOREIGN KEY(crmMedico) REFERENCES TB_Medico(crmMedico)" +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS TB_Receituario (" +
                "idReceituario INTEGER PRIMARY KEY AUTOINCREMENT," +
                "idProntuario INTEGER," +
                "crmMedico TEXT," +
                "data_EmissaoProntuario TEXT NOT NULL UNIQUE," +
                "medicamentoProntuario TEXT NOT NULL," +
                "dosagemProntuario TEXT NOT NULL," +
                "duracaoProntuario TEXT NOT NULL," +
                "observacoesProntuario TEXT NOT NULL," +
                "FOREIGN KEY(idProntuario) REFERENCES TB_Prontuario(idProntuario)," +
                "FOREIGN KEY(crmMedico) REFERENCES TB_Medico(crmMedico)" +
                ");");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Exemplo simples — em produção, ideal é fazer migração sem perder dados
        db.execSQL("DROP TABLE IF EXISTS TB_Receituario");
        db.execSQL("DROP TABLE IF EXISTS TB_Dicas");
        db.execSQL("DROP TABLE IF EXISTS TB_Prontuario");
        db.execSQL("DROP TABLE IF EXISTS TB_Agenda_Medico");
        db.execSQL("DROP TABLE IF EXISTS TB_Vacina");
        db.execSQL("DROP TABLE IF EXISTS TB_Exame");
        db.execSQL("DROP TABLE IF EXISTS TB_Consulta");
        db.execSQL("DROP TABLE IF EXISTS TB_Usuario");
        db.execSQL("DROP TABLE IF EXISTS TB_Medico");
        onCreate(db);
    }
    // ======================================
    // CRUD: TABELA TB_MEDICO
    // ======================================

    public boolean inserirMedico(String nome, String crm, String cpf, String dataNasc, String email,
                                 String sexo, String especialidade, String senha, String telefone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nome_completoMedico", nome);
        valores.put("crmMedico", crm);
        valores.put("cpfMedico", cpf);
        valores.put("data_nascMedico", dataNasc);
        valores.put("emailMedico", email);
        valores.put("sexoMedico", sexo);
        valores.put("especialidadeMedico", especialidade);
        valores.put("senhaMedico", senha);
        valores.put("telefoneMedico", telefone);
        long res = db.insert("TB_Medico", null, valores);
        return res != -1;
    }

    public boolean validarLoginMedico(String crm, String senha) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        boolean existe = false;

        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT * FROM TB_Medico WHERE crmMedico = ? AND senhaMedico = ?",
                    new String[]{crm, senha}
            );

            if (cursor != null && cursor.moveToFirst()) {
                existe = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return existe;
    }

    public boolean atualizarMedico(int id, String nome, String email, String telefone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nome_completoMedico", nome);
        valores.put("emailMedico", email);
        valores.put("telefoneMedico", telefone);
        int linhas = db.update("TB_Medico", valores, "idMedico = ?", new String[]{String.valueOf(id)});
        return linhas > 0;
    }

    public boolean excluirMedico(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int linhas = db.delete("TB_Medico", "idMedico = ?", new String[]{String.valueOf(id)});
        return linhas > 0;
    }

    // ======================================
    // CRUD: TABELA TB_USUARIO
    // ======================================

    public boolean inserirUsuario(String nome, String cpf, String email, String dataNasc,
                                  String endereco, String sexo, String senha, String telefone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nome_completoUsuario", nome);
        valores.put("cpfUsuario", cpf);
        valores.put("emailUsuario", email);
        valores.put("data_nascUsuario", dataNasc);
        valores.put("enderecoUsuario", endereco);
        valores.put("sexoUsuario", sexo);
        valores.put("senhaUsuario", senha);
        valores.put("telefoneUsuario", telefone);
        long res = db.insert("TB_Usuario", null, valores);
        return res != -1;
    }

    public boolean validarLoginPaciente(String cpf, String senha) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        boolean existe = false;

        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT * FROM TB_Usuario WHERE cpfUsuario = ? AND senhaUsuario = ?",
                    new String[]{cpf, senha}
            );

            if (cursor != null && cursor.moveToFirst()) {
                existe = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return existe;
    }

    public boolean atualizarUsuario(int id, String nome, String email, String telefone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nome_completoUsuario", nome);
        valores.put("emailUsuario", email);
        valores.put("telefoneUsuario", telefone);
        int linhas = db.update("TB_Usuario", valores, "idUsuario = ?", new String[]{String.valueOf(id)});
        return linhas > 0;
    }

    public boolean excluirUsuario(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int linhas = db.delete("TB_Usuario", "idUsuario = ?", new String[]{String.valueOf(id)});
        return linhas > 0;
    }

    // ---------------------------------------------
    // CRUD da Tabela TB_Consulta
    // ---------------------------------------------

    // 🔹 INSERIR nova consulta
    public boolean inserirConsulta(String cpfUsuario, String especialidade, String medico,
                                   String data, String horario, String status, double valor) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("cpfUsuario", cpfUsuario);
        valores.put("especialidadeConsulta", especialidade);
        valores.put("medicoConsulta", medico);
        valores.put("dataConsulta", data);
        valores.put("horarioConsulta", horario);
        valores.put("statusConsulta", status);
        valores.put("valorConsulta", valor);

        long resultado = db.insert("TB_Consulta", null, valores);
        db.close();
        return resultado != -1;
    }

    // 🔹 ATUALIZAR consulta existente
    public boolean atualizarConsulta(int idConsulta, String especialidade, String medico,
                                     String data, String horario, String status, double valor) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("especialidadeConsulta", especialidade);
        valores.put("medicoConsulta", medico);
        valores.put("dataConsulta", data);
        valores.put("horarioConsulta", horario);
        valores.put("statusConsulta", status);
        valores.put("valorConsulta", valor);

        int linhasAfetadas = db.update("TB_Consulta", valores, "idConsulta = ?", new String[]{String.valueOf(idConsulta)});
        db.close();
        return linhasAfetadas > 0;
    }

    public Cursor listarConsultasPorUsuario(String cpfUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM TB_Consulta WHERE cpfUsuario = ?", new String[]{cpfUsuario});
    }

    // 🔹 EXCLUIR consulta pelo ID
    public boolean excluirConsulta(int idConsulta) {
        SQLiteDatabase db = this.getWritableDatabase();
        int linhasAfetadas = db.delete("TB_Consulta", "idConsulta = ?", new String[]{String.valueOf(idConsulta)});
        db.close();
        return linhasAfetadas > 0;
    }

    // ----------------------------
    // CRUD - Tabela TB_Exame
    // ----------------------------

    public boolean inserirExame(String cpfUsuario, String tipoExame, String medicoExame,
                                String dataExame, String horarioExame, String statusExame, double valorExame) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();

        valores.put("cpfUsuario", cpfUsuario);
        valores.put("tipoExame", tipoExame);
        valores.put("medicoExame", medicoExame);
        valores.put("dataExame", dataExame);
        valores.put("horarioExame", horarioExame);
        valores.put("statusExame", statusExame);
        valores.put("valorExame", valorExame);

        long resultado = db.insert("TB_Exame", null, valores);
        db.close();
        return resultado != -1;
    }

    public Cursor listarExamesPorUsuario(String cpfUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM TB_Exame WHERE cpfUsuario = ?", new String[]{cpfUsuario});
    }

    public boolean atualizarExame(int idExame, String tipoExame, String medicoExame,
                                  String dataExame, String horarioExame, String statusExame, double valorExame) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();

        valores.put("tipoExame", tipoExame);
        valores.put("medicoExame", medicoExame);
        valores.put("dataExame", dataExame);
        valores.put("horarioExame", horarioExame);
        valores.put("statusExame", statusExame);
        valores.put("valorExame", valorExame);

        int linhas = db.update("TB_Exame", valores, "idExame = ?", new String[]{String.valueOf(idExame)});
        db.close();
        return linhas > 0;
    }

    public boolean excluirExame(int idExame) {
        SQLiteDatabase db = this.getWritableDatabase();
        int linhas = db.delete("TB_Exame", "idExame = ?", new String[]{String.valueOf(idExame)});
        db.close();
        return linhas > 0;
    }

    // ----------------------------
    // CRUD - Tabela TB_Vacina
    // ----------------------------

    public boolean inserirVacina(String cpfUsuario, String tipoVacina, String dataVacina, double valorVacina) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();

        valores.put("cpfUsuario", cpfUsuario);
        valores.put("tipoVacina", tipoVacina);
        valores.put("dataVacina", dataVacina);
        valores.put("valorVacina", valorVacina);

        long resultado = db.insert("TB_Vacina", null, valores);
        db.close();
        return resultado != -1;
    }

    public Cursor listarVacinasPorUsuario(String cpfUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM TB_Vacina WHERE cpfUsuario = ?", new String[]{cpfUsuario});
    }

    public boolean atualizarVacina(int idVacina, String tipoVacina, String dataVacina, double valorVacina) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();

        valores.put("tipoVacina", tipoVacina);
        valores.put("dataVacina", dataVacina);
        valores.put("valorVacina", valorVacina);

        int linhas = db.update("TB_Vacina", valores, "idVacina = ?", new String[]{String.valueOf(idVacina)});
        db.close();
        return linhas > 0;
    }

    public boolean excluirVacina(int idVacina) {
        SQLiteDatabase db = this.getWritableDatabase();
        int linhas = db.delete("TB_Vacina", "idVacina = ?", new String[]{String.valueOf(idVacina)});
        db.close();
        return linhas > 0;
    }



    // ======================================
    // CRUD: TABELA TB_Prontuario
    // ======================================

    public boolean inserirProntuario(String cpf, String data, double peso, double altura,
                                  String sintomas, String alergias, String condicoes, String observacoes, String alertas) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("cpfUsuario", cpf);
        valores.put("data_registroProntuario", data);
        valores.put("peso_kgProntuario", peso);
        valores.put("altura_cmProntuario", altura);
        valores.put("sintomasProntuario", sintomas);
        valores.put("alergiasProntuario", alergias);
        valores.put("condicoes_chronicasProntuario", condicoes);
        valores.put("observacoesProntuario", observacoes);
        valores.put("alertasProntuario", alertas);
        long res = db.insert("TB_Prontuario", null, valores);
        return res != -1;
    }

    public boolean existeProntuario(String cpfUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT idProntuario FROM TB_Prontuario WHERE cpfUsuario = ?", new String[]{cpfUsuario});
        boolean existe = cursor.moveToFirst();
        cursor.close();
        db.close();
        return existe;
    }


    // Atualizar
    public boolean atualizarProntuario(String cpfUsuario, double peso, double altura, String sintomas,
                                       String alergias, String condicoes, String observacoes, String alertas) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("peso_kgProntuario", peso);
        valores.put("altura_cmProntuario", altura);
        valores.put("sintomasProntuario", sintomas);
        valores.put("alergiasProntuario", alergias);
        valores.put("condicoes_chronicasProntuario", condicoes);
        valores.put("observacoesProntuario", observacoes);
        valores.put("alertasProntuario", alertas);

        int linhasAfetadas = db.update("TB_Prontuario", valores, "cpfUsuario = ?", new String[]{cpfUsuario});
        db.close();
        return linhasAfetadas > 0;
    }


    public boolean excluirProntuario(int idProntuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        int linhasAfetadas = db.delete("TB_Prontuario", "idProntuario = ?", new String[]{String.valueOf(idProntuario)});
        db.close();
        return linhasAfetadas > 0;
    }

}