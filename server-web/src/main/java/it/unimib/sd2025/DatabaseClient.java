package it.unimib.sd2025;

import java.io.*;
import java.net.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Client per comunicare con il database tramite socket TCP.
 */
public class DatabaseClient {
    private static final String DB_HOST = "localhost";
    private static final int DB_PORT = 3030;

    /**
     * Esegue un comando sul database.
     * 
     * @param command Stringa del comando da eseguire
     * @return Risposta dal database
     */
    public static String executeCommand(String command) throws IOException {
        try (Socket socket = new Socket(DB_HOST, DB_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Invia il comando
            out.println(command);
            
            // Leggi la risposta
            String response = in.readLine();
            
            // Chiudi la connessione
            out.println(".");
            in.readLine(); // Leggi il "bye" di risposta
            return response;
        }
    }

    /**
     * Recupera un valore dal database.
     * 
     * @param key Chiave da recuperare
     * @return Valore associato alla chiave
     */
    public static String get(String key) throws IOException {
        return executeCommand("GET " + key);
    }

    /**
     * Imposta un valore nel database.
     * 
     * @param key   Chiave da impostare
     * @param value Valore da associare alla chiave
     * @return Risposta dal database
     */
    public static String set(String key, String value) throws IOException {
        return executeCommand("SET " + key + " " + value);
    }

    /**
     * Elimina una chiave dal database.
     * 
     * @param key Chiave da eliminare
     * @return Risposta dal database
     */
    public static String delete(String key) throws IOException {
        return executeCommand("DEL " + key);
    }

    /**
     * Verifica se una chiave esiste nel database.
     * 
     * @param key Chiave da verificare
     * @return true se la chiave esiste, false altrimenti
     */
    public static boolean exists(String key) throws IOException {
        String response = executeCommand("EXISTS " + key);
        return "1".equals(response);
    }
}
