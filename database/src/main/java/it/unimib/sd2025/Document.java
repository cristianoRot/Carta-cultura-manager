package it.unimib.sd2025;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONException;

public class Document
{
    private final Map<String, String> parameters = new HashMap<>();

    public Document() {}

    public Document(String json) 
    {
        try 
        {
            JSONObject jsonObj = new JSONObject(json);

            synchronized (parameters) 
            {
                parameters.clear();
                
                for (String key : jsonObj.keySet()) 
                {
                    Object valObj = jsonObj.opt(key);
                    String value = (valObj != null) ? valObj.toString() : null;
                    parameters.put(key, value);
                }
            }
        } 
        catch (JSONException e) 
        {
            System.err.println("Error parsing JSON: " + e.getMessage());
        }
    }

    public String Get(String key) 
    {
        synchronized(parameters) {
            return parameters.get(key);
        }
    }
    
    public String Set(String key, String value) 
    {
        try 
        {
            synchronized(parameters) {
                 parameters.put(key, value);
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
            synchronized (parameters) {
                if (parameters.containsKey(key)) 
                {
                    parameters.remove(key);
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
        synchronized (parameters) {
            return parameters.containsKey(key) ? "true" : "false";
        }
    }

    public String Increment(String key, String value)
    {
        try
        {
            synchronized (parameters) {
                String value_s = parameters.get(key);
                int value_i = Integer.parseInt(value_s);

                parameters.put(key, Integer.toString(value_i + Integer.parseInt(value)));
                return ResponseCode.OK;
            }
        }
        catch (Exception e)
        {
            try
            {
                synchronized (parameters) {
                    String value_s = parameters.get(key);
                    double value_d = Double.parseDouble(value_s);

                    parameters.put(key, Double.toString(value_d + Double.parseDouble(value)));
                    return ResponseCode.OK;
                }
            }
            catch (Exception ex)
            {
                return ResponseCode.BAD_REQUEST;
            }
        }
    }

    public String toString() 
    {
        synchronized (parameters) {
            StringBuilder json = new StringBuilder();
            json.append("{");
            
            boolean first = true;
            for (Map.Entry<String, String> entry : parameters.entrySet()) 
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
