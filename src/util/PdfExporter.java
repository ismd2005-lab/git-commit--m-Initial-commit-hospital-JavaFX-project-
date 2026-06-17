package util;

import model.Appointment;
import model.Invoice;
import model.Patient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public final class PdfExporter {
    private PdfExporter() {
    }

    public static void writeInvoice(File file, Invoice invoice, Patient patient, List<Appointment> appointments)
            throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("MedVision AI Healthcare Dashboard");
        lines.add("Invoice #" + invoice.getId());
        lines.add("Patient: " + invoice.getPatientName());
        lines.add("Date: " + invoice.getInvoiceDate() + "     Due: " + invoice.getDueDate());
        lines.add("Status: " + invoice.getStatus() + "     Amount: " + invoice.getAmountLabel());
        lines.add("Description: " + invoice.getDescription());
        lines.add("");
        lines.add("Patient profile");
        lines.add(patient == null ? "Patient details unavailable" : patient.getAge() + " years | "
                + patient.getGender() + " | Blood type " + patient.getBloodType());
        lines.add("");
        lines.add("Recent appointments");
        if (appointments.isEmpty()) {
            lines.add("No appointment history available.");
        } else {
            appointments.forEach(appointment -> lines.add(appointment.getScheduleLabel() + " | "
                    + appointment.getSpecialty() + " | " + appointment.getDoctor()));
        }
        writeSimplePdf(file, "MedVision Invoice", lines);
    }

    public static void writePatientReport(File file, Patient patient, List<String> timeline, int riskScore,
                                          String diagnosisSuggestion) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("MedVision AI Patient Report");
        lines.add("Patient: " + patient.getFullName());
        lines.add("Age: " + patient.getAge() + "     Gender: " + patient.getGender()
                + "     Blood type: " + patient.getBloodType());
        lines.add("Phone: " + patient.getPhone() + "     Email: " + patient.getEmail());
        lines.add("No-show risk simulation: " + riskScore + "%");
        lines.add("Diagnosis suggestion: " + diagnosisSuggestion);
        lines.add("");
        lines.add("Medical notes");
        lines.add(patient.getMedicalNotes());
        lines.add("");
        lines.add("Timeline");
        if (timeline.isEmpty()) {
            lines.add("No timeline events available.");
        } else {
            timeline.forEach(lines::add);
        }
        writeSimplePdf(file, "MedVision Patient Report", lines);
    }

    private static void writeSimplePdf(File file, String title, List<String> lines) throws IOException {
        StringBuilder content = new StringBuilder();
        content.append("BT\n/F1 18 Tf\n50 790 Td\n(").append(escape(title)).append(") Tj\n");
        content.append("/F1 10 Tf\n0 -28 Td\n");
        int lineCount = 0;
        for (String line : lines) {
            if (lineCount > 0) {
                content.append("0 -16 Td\n");
            }
            content.append("(").append(escape(line)).append(") Tj\n");
            lineCount++;
            if (lineCount > 43) {
                content.append("0 -16 Td\n(Report truncated for this lightweight PDF export.) Tj\n");
                break;
            }
        }
        content.append("ET");

        List<String> objects = List.of(
                "<< /Type /Catalog /Pages 2 0 R >>",
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 842] "
                        + "/Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>",
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
                "<< /Length " + content.toString().getBytes(StandardCharsets.ISO_8859_1).length + " >>\nstream\n"
                        + content + "\nendstream"
        );

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(output, "%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(output.size());
            write(output, (i + 1) + " 0 obj\n" + objects.get(i) + "\nendobj\n");
        }
        int xrefOffset = output.size();
        write(output, "xref\n0 " + (objects.size() + 1) + "\n");
        write(output, "0000000000 65535 f \n");
        for (Integer offset : offsets) {
            write(output, String.format("%010d 00000 n \n", offset));
        }
        write(output, "trailer\n<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\n");
        write(output, "startxref\n" + xrefOffset + "\n%%EOF");
        Files.write(file.toPath(), output.toByteArray());
    }

    private static void write(ByteArrayOutputStream output, String value) {
        output.writeBytes(value.getBytes(StandardCharsets.ISO_8859_1));
    }

    private static String escape(String value) {
        String safe = value == null ? "" : value;
        return safe.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
