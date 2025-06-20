package it.unimib.sd2025.System;

import java.io.OutputStream;
import java.util.*;
import java.net.*;

public class DatabaseConnection 
{
    private static final String DB_HOST = "localhost";
    private static final int DB_PORT = 3030;

    /**
     * Esegue un comando sul database.
     * @param command Stringa del comando da eseguire
     * @return Risposta dal database
     */
    private static String executeCommand(String command) throws Exception 
    {
        try (Socket socket = new Socket(DB_HOST, DB_PORT);
            OutputStream out = socket.getOutputStream();
            Scanner in = new Scanner(socket.getInputStream());
            )
        {
            out.write((command + '\n').getBytes());

            String response = in.nextLine();
            out.write((".\n").getBytes());

            return response;
        }
    }

    /**
     * Recupera un valore dal database.
     * @param path Path della chiave da recuperare
     * @return Valore associato al path
     */
    public static String Get(String path) throws Exception {
        return executeCommand("GET " + path);
    }

    /**
     * Imposta un valore nel database.
     * @param path  Path della chiave da impostare
     * @param value Valore da associare al path
     * @return Risposta dal database
     */
    public static String Set(String path, String value) throws Exception {
        return executeCommand("SET " + path + " " + value);
    }

    /**
     * Elimina un valore dal database.
     * @param path Path della chiave da eliminare
     * @return Risposta dal database
     */
    public static String Delete(String path) throws Exception {
        return executeCommand("DEL " + path);
    }

    /**
     * Verifica se un path esiste nel database.
     * @param path Path da verificare
     * @return true se il path esiste, false altrimenti
     */
    public static boolean Exists(String path) throws Exception {
        String response = executeCommand("EXISTS " + path);
        return "true".equals(response);
    }
}
