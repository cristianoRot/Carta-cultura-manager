package it.unimib.sd2025.Voucher;

/**
 * Classe che rappresenta la richiesta di aggiornamento di un buono.
 */
public class UpdateVoucherRequest {
    private String category;

    public UpdateVoucherRequest() {}

    public UpdateVoucherRequest(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
