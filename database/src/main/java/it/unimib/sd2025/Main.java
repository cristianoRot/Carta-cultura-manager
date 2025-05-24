package it.unimib.sd2025;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Database chiave-valore in-memory che comunica attraverso socket TCP.
 */
public class Main {
    /**
     * Porta di ascolto.
     */
    public static final int PORT = 3030;

    /**
     * Store chiave-valore in memoria
     */
    private static final Map<String, String> store = new ConcurrentHashMap<>();
    
    /**
     * Lock per operazioni che richiedono atomicità
     */
    private static final Map<String, ReentrantLock> keyLocks = new ConcurrentHashMap<>();

    /**
     * Avvia il database e l'ascolto di nuove connessioni.
     */
    public static void startServer() throws IOException {
        var server = new ServerSocket(PORT);
        
        // Carica alcuni dati di test
        loadInitialData();

        System.out.println("Database listening at localhost:" + PORT);

        try {
            while (true)
                new Handler(server.accept()).start();
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            server.close();
        }
    }

    /**
     * Carica alcuni dati iniziali di test nel database.
     */
    private static void loadInitialData() {
        boolean loadedFromClasspath = false;
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream("initial_data.txt")) {
            if (is != null) {
                System.out.println("Attempting to load initial data from classpath...");
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            store.put(parts[0].trim(), parts[1].trim());
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
                        store.put(parts[0].trim(), parts[1].trim());
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
        store.put("stats:userCount", "0");
        store.put("stats:totalAvailable", "0.0");
        store.put("stats:totalAllocated", "0.0");
        store.put("stats:totalSpent", "0.0");
        store.put("stats:totalVouchers", "0");
        store.put("stats:vouchersConsumed", "0");
        
        System.out.println("Default empty statistics initialized.");
    }

    /**
     * Ottiene un lock per una specifica chiave.
     */
    private static ReentrantLock getLockForKey(String key) {
        keyLocks.putIfAbsent(key, new ReentrantLock());
        return keyLocks.get(key);
    }

    /**
     * Handler di una connessione del client.
     */
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
                    
                    String response = processCommand(inputLine);
                    out.println(response);
                }

                in.close();
                out.close();
                client.close();
            } catch (IOException e) {
                System.err.println(e);
            }
        }

        /**
         * Processa un comando dal client.
         */
        private String processCommand(String command) {
            String[] parts = command.split(" ", 3);
            String cmd = parts[0].toUpperCase();
            
            try {
                switch (cmd) {
                    case "GET":
                        if (parts.length < 2) return "ERR missing key";
                        return get(parts[1]);
                        
                    case "SET":
                        if (parts.length < 3) return "ERR missing key or value";
                        return set(parts[1], parts[2]);
                        
                    case "DEL":
                        if (parts.length < 2) return "ERR missing key";
                        return delete(parts[1]);
                        
                    case "EXISTS":
                        if (parts.length < 2) return "ERR missing key";
                        return exists(parts[1]);
                        
                    default:
                        return "ERR unknown command '" + cmd + "'";
                }
            } catch (Exception e) {
                return "ERR " + e.getMessage();
            }
        }

        /**
         * Esegue il comando GET.
         */
        private String get(String key) {
            return store.getOrDefault(key, "null");
        }

        /**
         * Esegue il comando SET.
         */
        private String set(String key, String value) {
            ReentrantLock lock = getLockForKey(key);
            lock.lock();
            try {
                store.put(key, value);
                return "OK";
            } finally {
                lock.unlock();
            }
        }

        /**
         * Esegue il comando DEL.
         */
        private String delete(String key) {
            ReentrantLock lock = getLockForKey(key);
            lock.lock();
            try {
                if (store.containsKey(key)) {
                    store.remove(key);
                    return "1";
                } else {
                    return "0";
                }
            } finally {
                lock.unlock();
            }
        }

        /**
         * Esegue il comando EXISTS.
         */
        private String exists(String key) {
            return store.containsKey(key) ? "1" : "0";
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
