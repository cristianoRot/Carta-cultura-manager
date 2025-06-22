package it.unimib.sd2025.User;

public class User 
{
    private String fiscalCode;
    private String name;
    private String surname;
    private String email;
    private double balance;
    private String contribAllocated;
    private String contribSpent;

    public User() {}

    public User(String id, String name, String surname, String email, String fiscalCode, double balance, String contribAllocated, String contribSpent) 
    {
        this.fiscalCode = fiscalCode;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.balance = balance;
        this.contribAllocated = contribAllocated;
        this.contribSpent = contribSpent;
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

    public double getBalance() {
        return balance;
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

    public String getContribAllocated() {
        return contribAllocated;
    }

    public void setContribAllocated(String contribAllocated) {
        this.contribAllocated = contribAllocated;
    }
    
    public String getContribSpent() {
        return contribSpent;
    }

    public void setContribSpent(String contribSpent) {
        this.contribSpent = contribSpent;
    }
}
