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

        private String HandleRequest(String command) 
        {
            String[] parts = command.split(" ");

            if (parts.length < 2) return "ERR BAD REQUEST";

            String cmd = parts[0].toUpperCase();
            String path = parts[1];
            String value = parts.length > 2 ? parts[2] : null;

            String[] pathParts = path.split("/");
            if (pathParts.length < 2) return "ERR BAD REQUEST";

            String collection = pathParts[0];
            String document = pathParts[1];
            String parameter = pathParts[2];
            
            if (parameter == null)
                return HandleDocumentRequest(command, document, parameter, value);
            
            return HandleParameterRequest(command, collection, document, parameter, value);
        }

        private String HandleDocumentRequest(String command, String colKey, String docKey, String value)
        {
            Collection collection = null;

            synchronized(database) {
                collection = database.get(colKey);
            }

            switch (command) {
                case "GET":
                    if (collection == null) return "null";

                    return collection.Get(docKey).toString();
                case "SET":
                    if (collection == null)
                        collection = CreateNewCollection(colKey);

                    return collection.Set(docKey, value);
                case "DEL":
                    if (collection == null) return "null";

                    return collection.Delete(docKey);
                case "EXISTS":
                    if (collection == null) return "false";

                    return collection.Exists(docKey);
                default:
                    return "ERR BAD REQUEST";
            }
        }

        private String HandleParameterRequest(String command, String colKey, String docKey, String parmKey, String value)
        {
            Collection collection = null;
            Document document = null;

            synchronized(database) {
                collection = database.get(colKey);
            }

            if (collection != null)
                document = collection.Get(docKey);

            switch (command) {
                case "GET":
                    if (collection == null) return "null";
                    if (document == null) return "null";

                    return document.Get(parmKey);
                case "SET":
                    if (collection == null)
                        collection = CreateNewCollection(colKey);

                    if (document == null)
                    {
                        document = new Document();
                        String res = document.Set(parmKey, value);
                        collection.Set(docKey, document.toString());

                        return res;
                    }

                    return document.Set(parmKey, value);
                case "DEL":
                    if (collection == null) return "null";
                    if (document == null) return "null";

                    return document.Delete(parmKey);
                case "EXISTS":
                    if (collection == null) return "false";
                    if (document == null) return "false";
                    
                    return document.Exists(parmKey);
                default:
                    return "ERR BAD REQUEST";
            }
        }

        private Collection CreateNewCollection(String key)
        {
            Collection collection = new Collection();

            synchronized(database)
            {
                database.put(key, collection);
            }

            return collection;
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
