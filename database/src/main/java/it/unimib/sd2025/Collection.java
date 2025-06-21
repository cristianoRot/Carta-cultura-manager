package it.unimib.sd2025;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class Collection
{
    private final Map<String, Document> documents = new HashMap<>();

    public Collection() {}

    public Collection(String json) 
    {
        try 
        {
            JSONObject jsonObj = new JSONObject(json);

            synchronized (documents) 
            {
                documents.clear();
                
                for (String key : jsonObj.keySet()) 
                {
                    Object valObj = jsonObj.opt(key);
                    Document doc = (valObj != null) ? new Document(valObj.toString()) : null;
                    documents.put(key, doc);
                }
            }
        } 
        catch (JSONException e) 
        {
            System.err.println("Error parsing JSON: " + e.getMessage());
        }
    }

    public Document Get(String key) 
    {
        synchronized(documents) {
            return documents.get(key);
        }
    }
    
    public String Set(String key, String document)
    {
        try 
        {
            Document doc = new Document(document);

            synchronized(documents) {
                documents.put(key, doc);
            }

            return ResponseCode.OK;
        } 
        catch (Exception e) 
        {
            return ResponseCode.ERROR;
        }
    }

    public String Delete(String key) 
    {
        try 
        {
            synchronized (documents) {
                if (documents.containsKey(key)) 
                {
                    documents.remove(key);
                    return "true";
                } 
                else 
                {
                    return "false";
                }
            }
        } 
        catch (Exception e) 
        {
            return ResponseCode.ERROR;
        }
    }

    public String Exists(String key) 
    {
        synchronized (documents) {
            return documents.containsKey(key) ? "true" : "false";
        }
    }

    public String toString() 
    {
        synchronized (documents) {
            StringBuilder json = new StringBuilder();
            json.append("{");
            
            boolean first = true;
            for (Map.Entry<String, Document> entry : documents.entrySet()) 
            {
                if (!first) {
                    json.append(",");
                }
                json.append("\"").append(entry.getKey()).append("\":").append(entry.getValue().toString());
                first = false;
            }
            
            json.append("}");
            return json.toString();
        }
    }
}
