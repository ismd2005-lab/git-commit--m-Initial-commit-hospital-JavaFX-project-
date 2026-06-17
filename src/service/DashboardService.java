package service;

import model.DashboardMetrics;
import model.Patient;
import repository.PatientRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardService {
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM");

    private final PatientRepository patientRepository;
    private final AppointmentService appointmentService;
    private final BillingService billingService;
    private final AiSimulationService aiSimulationService;

    public DashboardService(PatientRepository patientRepository, AppointmentService appointmentService,
                            BillingService billingService, AiSimulationService aiSimulationService) {
        this.patientRepository = patientRepository;
        this.appointmentService = appointmentService;
        this.billingService = billingService;
        this.aiSimulationService = aiSimulationService;
    }

    public DashboardMetrics metrics() {
        int totalPatients = patientRepository.findAll().size();
        int activePatients = (int) patientRepository.findAll().stream().filter(Patient::isActive).count();
        int todayAppointments = (int) appointmentService.countToday();
        int overdueInvoices = billingService.overdueInvoices().size();
        int averageRisk = totalPatients == 0 ? 0 : (int) Math.round(patientRepository.findAll().stream()
                .mapToInt(aiSimulationService::noShowRiskScore)
                .average()
                .orElse(0));
        double occupancy = Math.min(1.0, todayAppointments / 24.0);
        return new DashboardMetrics(totalPatients, todayAppointments, billingService.paidRevenue(),
                occupancy, activePatients, overdueInvoices, averageRisk);
    }

    public Map<String, Integer> patientsCreatedByMonth(int months) {
        Map<String, Integer> result = new LinkedHashMap<>();
        YearMonth start = YearMonth.now().minusMonths(Math.max(0, months - 1));
        for (int i = 0; i < months; i++) {
            YearMonth month = start.plusMonths(i);
            result.put(month.format(MONTH_FORMATTER), 0);
        }
        patientRepository.findAll().stream()
                .filter(patient -> patient.getCreatedAt() != null)
                .filter(patient -> !patient.getCreatedAt().isBefore(start.atDay(1)))
                .filter(patient -> !patient.getCreatedAt().isAfter(LocalDate.now()))
                .forEach(patient -> {
                    String key = YearMonth.from(patient.getCreatedAt()).format(MONTH_FORMATTER);
                    result.computeIfPresent(key, (month, total) -> total + 1);
                });
        return result;
    }
}
