package it.unimib.sd2025;

import java.net.*;
import java.io.*;
import java.util.*;
import org.json.JSONObject;

/*
 * Format request:  ACTION collection/document/key value    ( es. SET users/XXXX/name Mario )
 */

public class Main 
{
    public static final int PORT = 3030;
    private static final Map<String, Collection> database = new HashMap<>();

    private static final String DB_DATA_PATH = "db_data.json";

    public static void StartServer() throws IOException 
    {
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

    private static class Handler extends Thread 
    {
        private Socket client;

        public Handler(Socket client)
        {
            this.client = client;
        }

        public void run() 
        {
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

        private String HandleRequest(String command) 
        {
            String[] parts = command.split(" ", 3);
            if (parts.length < 2) return ResponseCode.BAD_REQUEST;

            String cmd = parts[0].toUpperCase();
            String path = parts[1];
            String value = parts.length > 2 ? parts[2] : null;

            String[] pathParts = path.split("/");

            if (pathParts.length == 0) return ResponseCode.BAD_REQUEST;

            String collection = pathParts[0];
            String document = pathParts.length > 1 ? pathParts[1] : null;
            String parameter = pathParts.length > 2 ? pathParts[2] : null;

            return document == null ?
                HandleCollectionRequest(cmd, collection, value) :   
                parameter == null ? 
                    HandleDocumentRequest(cmd, collection, document, value) :
                    HandleParameterRequest(cmd, collection, document, parameter, value);
        }

        private String HandleCollectionRequest(String command, String colKey, String json)
        {
            try
            {
                synchronized (database) {
                    switch (command) {
                        case "GET": {
                            Collection col = database.get(colKey);
                            if (col == null) {
                                return ResponseCode.NOT_FOUND;
                            }
                            return col.toString();
                        }
                        case "SET": return database.put(colKey, new Collection(json)).toString();
                        case "DEL": return database.remove(colKey).toString();
                        case "EXISTS": return database.containsKey(colKey) ? "true" : "false";
                        default: return ResponseCode.BAD_REQUEST;
                    }
                }
            }
            catch (Exception e)
            {
                return ResponseCode.ERROR;
            }
        }

        private String HandleDocumentRequest(String command, String colKey, String docKey, String json) 
        {
            Collection collection = GetOrCreateCollection(colKey);
            if (collection == null) return ResponseCode.ERROR;

            switch (command) {
                case "GET": {
                    Document doc = collection.Get(docKey);
                    if (doc == null) {
                        return ResponseCode.NOT_FOUND;
                    }
                    return doc.toString();
                }
                case "SET": return collection.Set(docKey, json);
                case "DEL": return collection.Delete(docKey);
                case "EXISTS": return collection.Exists(docKey);
                default: return ResponseCode.BAD_REQUEST;
            }
        }

        private String HandleParameterRequest(String command, String colKey, String docKey, String parmKey, String value) 
        {
            Collection collection = GetOrCreateCollection(colKey);
            if (collection == null) return ResponseCode.ERROR;

            Document document = collection.Get(docKey);
            if (document == null && command.equals("SET")) {
                document = new Document();
                collection.Set(docKey, document.toString());
            }
            if (document == null) return ResponseCode.BAD_REQUEST;

            switch (command) {
                case "GET": {
                    var par = document.Get(parmKey);
                    if (par == null) {
                        return ResponseCode.NOT_FOUND;
                    }
                    return par.toString();
                }
                case "SET": return document.Set(parmKey, value);
                case "DEL": return document.Delete(parmKey);
                case "EXISTS": return document.Exists(parmKey);
                case "INCREMENT": return document.Increment(parmKey, value);
                default: return ResponseCode.BAD_REQUEST;
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
        loadFromFile(DB_DATA_PATH);
        StartServer();
    }

    public static void loadFromFile(String filePath) 
    {
        try 
        {
            InputStream is = Main.class.getClassLoader().getResourceAsStream("db_data.json");

            if (is == null) 
                throw new FileNotFoundException("db_data.json non trovato nel classpath.");

            String content = new String(is.readAllBytes());
            JSONObject root = new JSONObject(content);

            synchronized (database) 
            {
                database.clear();

                for (Iterator<String> it = root.keys(); it.hasNext(); ) 
                {
                    String collectionName = it.next();
                    JSONObject docsObj = root.optJSONObject(collectionName);

                    if (docsObj == null) continue;

                    Collection coll = new Collection();
                    database.put(collectionName, coll);

                    for (Iterator<String> dit = docsObj.keys(); dit.hasNext(); ) 
                    {
                        String docKey = dit.next();
                        JSONObject fieldsObj = docsObj.optJSONObject(docKey);

                        if (fieldsObj == null) continue;

                        String docJson = fieldsObj.toString();
                        coll.Set(docKey, docJson);
                    }
                }
            }

            System.out.println("Database initialized from file: " + filePath);
        } 
        catch (Exception e) 
        {
            System.err.println("Error loading database from file: " + e.getMessage());
        }
    }
}
