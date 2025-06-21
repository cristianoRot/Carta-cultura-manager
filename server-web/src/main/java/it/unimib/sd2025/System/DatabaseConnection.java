package it.unimib.sd2025.System;

import java.io.OutputStream;
import java.util.*;
import java.net.*;

public class DatabaseConnection 
{
    private static final String DB_HOST = "localhost";
    private static final int DB_PORT = 3030;

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

    public static String Get(String path) throws Exception {
        return executeCommand("GET " + path);
    }

    public static String Set(String path, String value) throws Exception {
        return executeCommand("SET " + path + " " + value);
    }

    public static String Delete(String path) throws Exception {
        return executeCommand("DEL " + path);
    }

    public static boolean Exists(String path) throws Exception {
        String response = executeCommand("EXISTS " + path);
        return "true".equals(response);
    }

    public static String Increment(String path, Object value) throws Exception {
        return executeCommand("INCREMENT " + path + " " + value.toString());
    }
}
