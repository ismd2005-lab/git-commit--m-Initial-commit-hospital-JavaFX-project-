package service;

import model.Appointment;
import model.Invoice;
import model.NotificationItem;
import model.Patient;
import repository.PatientRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class NotificationService {
    private final AppointmentService appointmentService;
    private final BillingService billingService;
    private final PatientRepository patientRepository;
    private final AiSimulationService aiSimulationService;

    public NotificationService(AppointmentService appointmentService, BillingService billingService,
                               PatientRepository patientRepository, AiSimulationService aiSimulationService) {
        this.appointmentService = appointmentService;
        this.billingService = billingService;
        this.patientRepository = patientRepository;
        this.aiSimulationService = aiSimulationService;
    }

    public List<NotificationItem> notifications() {
        List<NotificationItem> appointmentAlerts = appointmentService.upcoming(8).stream()
                .filter(appointment -> appointment.getDate() != null)
                .filter(appointment -> !appointment.getDate().isAfter(LocalDate.now().plusDays(2)))
                .map(this::appointmentNotification)
                .toList();

        List<NotificationItem> invoiceAlerts = billingService.overdueInvoices().stream()
                .map(this::invoiceNotification)
                .toList();

        List<NotificationItem> riskAlerts = patientRepository.findAll().stream()
                .filter(patient -> aiSimulationService.noShowRiskScore(patient) >= 72)
                .map(this::riskNotification)
                .toList();

        return java.util.stream.Stream.of(appointmentAlerts, invoiceAlerts, riskAlerts)
                .flatMap(List::stream)
                .sorted(Comparator.comparing(NotificationItem::severity))
                .toList();
    }

    private NotificationItem appointmentNotification(Appointment appointment) {
        return new NotificationItem("INFO", "Upcoming appointment",
                appointment.getPatientName() + " with " + appointment.getDoctor() + " at " + appointment.getScheduleLabel());
    }

    private NotificationItem invoiceNotification(Invoice invoice) {
        return new NotificationItem("WARN", "Overdue payment",
                invoice.getPatientName() + " owes " + invoice.getAmountLabel());
    }

    private NotificationItem riskNotification(Patient patient) {
        int score = aiSimulationService.noShowRiskScore(patient);
        return new NotificationItem("AI", "No-show risk",
                patient.getFullName() + " risk score " + score + "%");
    }
}
