
package it.unimib.sd2025;

/**
 * Classe che rappresenta il contributo di un utente.
 */
public class UserContribution {
    private String userId;
    private double available;
    private double allocated;
    private double spent;
    private double total;

    // Costruttore vuoto richiesto per la deserializzazione JSON
    public UserContribution() {}

    public UserContribution(String userId, double available, double allocated, double spent, double total) {
        this.userId = userId;
        this.available = available;
        this.allocated = allocated;
        this.spent = spent;
        this.total = total;
    }

    // Getter e setter
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getAvailable() {
        return available;
    }

    public void setAvailable(double available) {
        this.available = available;
    }

    public double getAllocated() {
        return allocated;
    }

    public void setAllocated(double allocated) {
        this.allocated = allocated;
    }

    public double getSpent() {
        return spent;
    }

    public void setSpent(double spent) {
        this.spent = spent;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
