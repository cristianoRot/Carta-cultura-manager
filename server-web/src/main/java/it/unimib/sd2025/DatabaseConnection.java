package it.unimib.sd2025;

import java.io.*;
import java.net.*;

public class DatabaseConnection {
    private static final String DB_HOST = "localhost";
    private static final int DB_PORT = 3030;

    /**
     * Esegue un comando sul database.
     * @param command Stringa del comando da eseguire
     * @return Risposta dal database
     */
    private static String executeCommand(String command) throws IOException {
        try (Socket socket = new Socket(DB_HOST, DB_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) 
        {
            out.println(command);
            String response = in.readLine();
            out.println(".");
            in.readLine();
            return response;
        }
    }

    /**
     * Recupera un valore dal database.
     * @param path Path della chiave da recuperare
     * @return Valore associato al path
     */
    public static String Get(String path) throws IOException {
        return executeCommand("GET " + path);
    }

    /**
     * Imposta un valore nel database.
     * @param path  Path della chiave da impostare
     * @param value Valore da associare al path
     * @return Risposta dal database
     */
    public static String Set(String path, String value) throws IOException {
        return executeCommand("SET " + path + " " + value);
    }

    /**
     * Elimina un valore dal database.
     * @param path Path della chiave da eliminare
     * @return Risposta dal database
     */
    public static String Delete(String path) throws IOException {
        return executeCommand("DEL " + path);
    }

    /**
     * Verifica se un path esiste nel database.
     * @param path Path da verificare
     * @return true se il path esiste, false altrimenti
     */
    public static boolean Exists(String path) throws IOException {
        String response = executeCommand("EXISTS " + path);
        return "1".equals(response);
    }
}
