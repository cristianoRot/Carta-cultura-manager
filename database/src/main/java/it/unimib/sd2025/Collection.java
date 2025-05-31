package it.unimib.sd2025;

import java.util.HashMap;
import java.util.Map;

public class Collection
{
    private static final Map<String, Document> collections = new HashMap<>();

    public Document Get(String key) 
    {
        synchronized(collections) {
            return collections.get(key);
        }
    }
    
    public String Set(String key, String document)
    {
        try 
        {
            Document doc = new Document(document);

            synchronized(collections) {
                collections.put(key, doc);
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
            synchronized (collections) {
                if (collections.containsKey(key)) 
                {
                    collections.remove(key);
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
        synchronized (collections) {
            return collections.containsKey(key) ? "1" : "0";
        }
    }
}
