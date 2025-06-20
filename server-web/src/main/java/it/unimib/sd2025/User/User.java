package it.unimib.sd2025.User;

/**
 * Classe che rappresenta un utente nel sistema.
 */

public class User 
{
    private String id;
    private String name;
    private String surname;
    private String email;
    private String fiscalCode;

    public User() {}

    public User(String id, String name, String surname, String email, String fiscalCode) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.fiscalCode = fiscalCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFiscalCode() {
        return fiscalCode;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }
}
