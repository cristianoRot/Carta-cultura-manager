package it.unimib.sd2025;

public interface DbComponentInterface 
{
    public Object Get(String key);

    public String Set(String key, String value);

    public String Delete(String key);

    public String Exists(String key);
}

