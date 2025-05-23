
package it.unimib.sd2025;

/**
 * Classe che rappresenta la richiesta di aggiornamento di un buono.
 */
public class UpdateVoucherRequest {
    private String category;

    // Costruttore vuoto richiesto per la deserializzazione JSON
    public UpdateVoucherRequest() {}

    public UpdateVoucherRequest(String category) {
        this.category = category;
    }

    // Getter e setter
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
