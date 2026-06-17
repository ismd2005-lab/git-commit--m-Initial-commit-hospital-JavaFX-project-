package config;

import repository.AppointmentRepository;
import repository.InMemoryAppointmentRepository;
import repository.InMemoryInvoiceRepository;
import repository.InMemoryPatientRepository;
import repository.InvoiceRepository;
import repository.PatientRepository;
import service.AiSimulationService;
import service.AppointmentService;
import service.BillingService;
import service.DashboardService;
import service.GlobalSearchService;
import service.NotificationService;
import service.PatientService;

public final class ApplicationContext {
    private static final ApplicationContext INSTANCE = new ApplicationContext();

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final BillingService billingService;
    private final AiSimulationService aiSimulationService;
    private final DashboardService dashboardService;
    private final NotificationService notificationService;
    private final GlobalSearchService globalSearchService;

    private ApplicationContext() {
        patientRepository = new InMemoryPatientRepository();
        appointmentRepository = new InMemoryAppointmentRepository(patientRepository.findAll());
        invoiceRepository = new InMemoryInvoiceRepository(patientRepository.findAll());

        patientService = new PatientService(patientRepository, appointmentRepository, invoiceRepository);
        appointmentService = new AppointmentService(appointmentRepository, patientRepository);
        billingService = new BillingService(invoiceRepository, patientRepository, appointmentRepository);
        aiSimulationService = new AiSimulationService(appointmentRepository, invoiceRepository);
        dashboardService = new DashboardService(patientRepository, appointmentService, billingService, aiSimulationService);
        notificationService = new NotificationService(appointmentService, billingService, patientRepository, aiSimulationService);
        globalSearchService = new GlobalSearchService(patientRepository, appointmentRepository, invoiceRepository);
    }

    public static ApplicationContext getInstance() {
        return INSTANCE;
    }

    public PatientService patientService() {
        return patientService;
    }

    public AppointmentService appointmentService() {
        return appointmentService;
    }

    public BillingService billingService() {
        return billingService;
    }

    public AiSimulationService aiSimulationService() {
        return aiSimulationService;
    }

    public DashboardService dashboardService() {
        return dashboardService;
    }

    public NotificationService notificationService() {
        return notificationService;
    }

    public GlobalSearchService globalSearchService() {
        return globalSearchService;
    }
}
