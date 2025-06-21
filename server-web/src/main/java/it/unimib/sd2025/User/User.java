package it.unimib.sd2025.User;

/**
 * Classe che rappresenta un utente nel sistema.
 */

public class User 
{
    private String fiscalCode;
    private String name;
    private String surname;
    private String email;
    private double available;
    private double allocated;
    private double spent;
    private double total;

    public User() {}

    public User(String id, String name, String surname, String email, String fiscalCode) 
    {
        this.fiscalCode = fiscalCode;
        this.name = name;
        this.surname = surname;
        this.email = email;
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
