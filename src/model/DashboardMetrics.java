package model;

public record DashboardMetrics(
        int totalPatients,
        int appointmentsToday,
        double revenue,
        double occupancyRate,
        int activePatients,
        int overdueInvoices,
        int averageRiskScore
) {
}
