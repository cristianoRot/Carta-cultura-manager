package it.unimib.sd2025;

import java.net.*;
import java.io.*;
import java.util.*;

/*
 * Format request:  ACTION collection/document/key value      ( es. SET users/XXXX/name Mario )
 */

public class Main {
    public static final int PORT = 3030;
    private static final Map<String, Collection> database = new HashMap<>();
    
    public static void StartServer() throws IOException {
        var server = new ServerSocket(PORT);

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

    private static class Handler extends Thread {
        private Socket client;

        public Handler(Socket client) {
            this.client = client;
        }

        public void run() {
            try (var out = new PrintWriter(client.getOutputStream(), true);
                 var in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
                
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (".".equals(inputLine)) {
                        out.println("bye");
                        break;
                    }
                    out.println(HandleRequest(inputLine));
                }
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            }
        }

        private String HandleRequest(String command) {
            String[] parts = command.split(" ");
            if (parts.length < 2) return "ERR BAD REQUEST";

            String cmd = parts[0].toUpperCase();
            String path = parts[1];
            String value = parts.length > 2 ? parts[2] : null;

            String[] pathParts = path.split("/");
            if (pathParts.length < 2) return "ERR BAD REQUEST";

            String collection = pathParts[0];
            String document = pathParts[1];
            String parameter = pathParts.length > 2 ? pathParts[2] : null;

            return parameter == null ? 
                HandleDocumentRequest(cmd, collection, document, value) :
                HandleParameterRequest(cmd, collection, document, parameter, value);
        }

        private String HandleDocumentRequest(String command, String colKey, String docKey, String value) {
            Collection collection = GetOrCreateCollection(colKey);
            if (collection == null) return "ERR INTERNAL ERROR";

            switch (command) {
                case "GET": return collection.Get(docKey).toString();
                case "SET": return collection.Set(docKey, value);
                case "DEL": return collection.Delete(docKey);
                case "EXISTS": return collection.Exists(docKey);
                default: return "ERR BAD REQUEST";
            }
        }

        private String HandleParameterRequest(String command, String colKey, String docKey, String parmKey, String value) {
            Collection collection = GetOrCreateCollection(colKey);
            if (collection == null) return "ERR INTERNAL ERROR";

            Document document = collection.Get(docKey);
            if (document == null && command.equals("SET")) {
                document = new Document();
                collection.Set(docKey, document.toString());
            }
            if (document == null) return "null";

            switch (command) {
                case "GET": return document.Get(parmKey);
                case "SET": return document.Set(parmKey, value);
                case "DEL": return document.Delete(parmKey);
                case "EXISTS": return document.Exists(parmKey);
                default: return "ERR BAD REQUEST";
            }
        }

        private Collection GetOrCreateCollection(String key) {
            synchronized(database) {
                return database.computeIfAbsent(key, k -> new Collection());
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
        StartServer();
    }
}
