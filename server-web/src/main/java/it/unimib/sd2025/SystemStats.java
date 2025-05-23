
package it.unimib.sd2025;

/**
 * Classe che rappresenta le statistiche globali del sistema.
 */
public class SystemStats {
    private int totalUsers;
    private double totalContributionAvailable;
    private double totalContributionAllocated;
    private double totalContributionSpent;
    private int totalVouchersGenerated;
    private int totalVouchersConsumed;

    // Costruttore vuoto richiesto per la deserializzazione JSON
    public SystemStats() {}

    public SystemStats(int totalUsers, double totalContributionAvailable, 
                      double totalContributionAllocated, double totalContributionSpent,
                      int totalVouchersGenerated, int totalVouchersConsumed) {
        this.totalUsers = totalUsers;
        this.totalContributionAvailable = totalContributionAvailable;
        this.totalContributionAllocated = totalContributionAllocated;
        this.totalContributionSpent = totalContributionSpent;
        this.totalVouchersGenerated = totalVouchersGenerated;
        this.totalVouchersConsumed = totalVouchersConsumed;
    }

    // Getter e setter
    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public double getTotalContributionAvailable() {
        return totalContributionAvailable;
    }

    public void setTotalContributionAvailable(double totalContributionAvailable) {
        this.totalContributionAvailable = totalContributionAvailable;
    }

    public double getTotalContributionAllocated() {
        return totalContributionAllocated;
    }

    public void setTotalContributionAllocated(double totalContributionAllocated) {
        this.totalContributionAllocated = totalContributionAllocated;
    }

    public double getTotalContributionSpent() {
        return totalContributionSpent;
    }

    public void setTotalContributionSpent(double totalContributionSpent) {
        this.totalContributionSpent = totalContributionSpent;
    }

    public int getTotalVouchersGenerated() {
        return totalVouchersGenerated;
    }

    public void setTotalVouchersGenerated(int totalVouchersGenerated) {
        this.totalVouchersGenerated = totalVouchersGenerated;
    }

    public int getTotalVouchersConsumed() {
        return totalVouchersConsumed;
    }

    public void setTotalVouchersConsumed(int totalVouchersConsumed) {
        this.totalVouchersConsumed = totalVouchersConsumed;
    }
}
