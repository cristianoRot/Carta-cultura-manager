package it.unimib.sd2025;

import java.net.*;
import java.io.*;
import java.util.*;

public class Main {
    public static final int PORT = 3030;
    private static final Map<String, String> database = new HashMap<>();
    public static void startServer() throws IOException {
        var server = new ServerSocket(PORT);
        
        LoadData();

        System.out.println("Database listening at localhost:" + PORT);

        try 
        {
            while (true)
            {
                new Handler(server.accept()).start();
            }
        } 
        catch (IOException e) 
        {
            System.err.println(e);
        } 
        finally {
            server.close();
        }
    }

    private static void LoadData() {
        boolean loadedFromClasspath = false;
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream("initial_data.txt")) {
            if (is != null) {
                System.out.println("Attempting to load initial data from classpath...");
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            database.put(parts[0].trim(), parts[1].trim());
                        }
                    }
                    System.out.println("Initial data loaded successfully from classpath.");
                    loadedFromClasspath = true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading initial data from classpath: " + e.getMessage());
        }

        if (loadedFromClasspath) {
            return; 
        }

        
        System.out.println("Attempting to load initial data from file system (initial_data.txt)...");
        try {
            File file = new File("initial_data.txt");
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        database.put(parts[0].trim(), parts[1].trim());
                    }
                }
                reader.close();
                System.out.println("Initial data loaded from file system.");
                return;
            } else {
                System.out.println("initial_data.txt not found in file system.");
            }
        } catch (IOException e) {
            System.err.println("Error loading initial data from file system: " + e.getMessage());
        }
        
        
        System.out.println("No initial data file found. Initializing default empty statistics.");
        database.put("stats:userCount", "0");
        database.put("stats:totalAvailable", "0.0");
        database.put("stats:totalAllocated", "0.0");
        database.put("stats:totalSpent", "0.0");
        database.put("stats:totalVouchers", "0");
        database.put("stats:vouchersConsumed", "0");
        
        System.out.println("Default empty statistics initialized.");
    }

    private static class Handler extends Thread {
        private Socket client;

        public Handler(Socket client) {
            this.client = client;
        }

        public void run() {
            try {
                var out = new PrintWriter(client.getOutputStream(), true);
                var in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    if (".".equals(inputLine)) {
                        out.println("bye");
                        break;
                    }
                    
                    String response = HandleRequest(inputLine);
                    out.println(response);
                }

                in.close();
                out.close();
                client.close();
            } catch (IOException e) {
                System.err.println(e);
            }
        }

        private String HandleRequest(String command) {
            String[] parts = command.split(" ", 3);
            String cmd = parts[0].toUpperCase();
            
            try {
                switch (cmd) {
                    case "GET":
                        if (parts.length < 2) return "ERR missing key";
                        return GetValue(parts[1]);
                        
                    case "SET":
                        if (parts.length < 3) return "ERR missing key or value";
                        return SetValue(parts[1], parts[2]);
                        
                    case "DEL":
                        if (parts.length < 2) return "ERR missing key";
                        return DeleteValue(parts[1]);
                        
                    case "EXISTS":
                        if (parts.length < 2) return "ERR missing key";
                        return ValueExists(parts[1]);
                        
                    default:
                        return "ERR unknown command '" + cmd + "'";
                }
            } catch (Exception e) {
                return "ERR " + e.getMessage();
            }
        }

        private String GetValue(String key) 
        {
            synchronized(database) {
                return database.get(key);
            }
        }

        private String SetValue(String key, String value) 
        {
            try {
                synchronized(database) {
                     database.put(key, value);
                }
                return "OK";
            } catch (Exception e) {
                System.err.println("Error setting value: " + e.getMessage());
                return "ERR";
            }
        }

        private String DeleteValue(String key) 
        {
            try {
                synchronized (database) {
                    if (database.containsKey(key)) {
                        database.remove(key);
                        return "1";
                    } else {
                        return "0";
                    }
                }
            } catch (Exception e) {
                System.err.println("Error deleting value: " + e.getMessage());
                return "ERR";
            }
        }

        private String ValueExists(String key) {
            synchronized (database) {
                return database.containsKey(key) ? "1" : "0";
            }
        }
    }

    /**
     * Metodo principale di avvio del database.
     *
     * @param args argomenti passati a riga di comando.
     *
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        startServer();
    }
}
