package it.unimib.sd2025;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONException;

public class Document
{
    private static final Map<String, String> document = new HashMap<>();

    public Document() {}

    public Document(String json) 
    {
        try 
        {
            JSONObject jsonObj = new JSONObject(json);

            synchronized (document) 
            {
                document.clear();
                
                for (String key : jsonObj.keySet()) 
                {
                    Object valObj = jsonObj.opt(key);
                    String value = (valObj != null) ? valObj.toString() : null;
                    document.put(key, value);
                }
            }
        } 
        catch (JSONException e) 
        {
            System.err.println("Error parsing JSON with org.json: " + e.getMessage());
        }
    }

    public String Get(String key) 
    {
        synchronized(document) {
            return document.get(key);
        }
    }
    
    public String Set(String key, String value) 
    {
        try 
        {
            synchronized(document) {
                 document.put(key, value);
            }

            return "OK";
        } 
        catch (Exception e) 
        {
            System.err.println("Error setting value: " + e.getMessage());
            return "ERR";
        }
    }

    public String Delete(String key) 
    {
        try 
        {
            synchronized (document) {
                if (document.containsKey(key)) 
                {
                    document.remove(key);
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
            System.err.println("Error deleting value: " + e.getMessage());
            return "ERR";
        }
    }

    public String Exists(String key) 
    {
        synchronized (document) {
            return document.containsKey(key) ? "true" : "false";
        }
    }

    public String toString() 
    {
        synchronized (document) {
            StringBuilder json = new StringBuilder();
            json.append("{");
            
            boolean first = true;
            for (Map.Entry<String, String> entry : document.entrySet()) 
            {
                if (!first) {
                    json.append(",");
                }
                json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
                first = false;
            }
            
            json.append("}");
            return json.toString();
        }
    }
}
