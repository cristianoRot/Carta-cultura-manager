
package it.unimib.sd2025;

/**
 * Classe che rappresenta la richiesta di creazione di un buono.
 */
public class CreateVoucherRequest {
    private double amount;
    private String category;
    private String userId;

    // Costruttore vuoto richiesto per la deserializzazione JSON
    public CreateVoucherRequest() {}

    public CreateVoucherRequest(double amount, String category, String userId) {
        this.amount = amount;
        this.category = category;
        this.userId = userId;
    }

    // Getter e setter
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
