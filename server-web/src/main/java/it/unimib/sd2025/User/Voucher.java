package it.unimib.sd2025.User;

public class Voucher 
{
    private String id;
    private double amount;
    private String category;
    private String status;
    private String createdAt;
    private String consumedAt;
    private String userId;

    public Voucher() {}

    public Voucher(String id, double amount, String category, String status, String createdAt, String consumedAt, String userId) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.status = status;
        this.createdAt = createdAt;
        this.consumedAt = consumedAt;
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getConsumedAt() {
        return consumedAt;
    }

    public void setConsumedAt(String consumedAt) {
        this.consumedAt = consumedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
