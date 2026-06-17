package dao;

import models.Patient;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PatientDAO {
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public List<Patient> findAll() throws SQLException {
        String sql = "SELECT * FROM PATIENT ORDER BY date_creation DESC, nom, prenom";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Patient> patients = new ArrayList<>();
            while (resultSet.next()) {
                patients.add(mapPatient(resultSet));
            }
            return patients;
        }
    }

    public List<Patient> findByCriteria(String keyword, String sexe, Boolean actif) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM PATIENT WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(nom) LIKE ? OR LOWER(prenom) LIKE ? OR telephone LIKE ?)");
            String like = "%" + keyword.toLowerCase().trim() + "%";
            parameters.add(like);
            parameters.add(like);
            parameters.add("%" + keyword.trim() + "%");
        }

        if (sexe != null && !sexe.isBlank() && !"Tous".equalsIgnoreCase(sexe)) {
            sql.append(" AND sexe = ?");
            parameters.add(sexe);
        }

        if (actif != null) {
            sql.append(" AND actif = ?");
            parameters.add(actif);
        }

        sql.append(" ORDER BY date_creation DESC, nom, prenom");

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            setParameters(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Patient> patients = new ArrayList<>();
                while (resultSet.next()) {
                    patients.add(mapPatient(resultSet));
                }
                return patients;
            }
        }
    }

    public void save(Patient patient) throws SQLException {
        String sql = """
                INSERT INTO PATIENT (nom, prenom, age, sexe, telephone, email, description, date_creation, actif)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillPatientStatement(statement, patient);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    patient.setIdPatient(keys.getInt(1));
                }
            }
        }
    }

    public void update(Patient patient) throws SQLException {
        String sql = """
                UPDATE PATIENT
                SET nom = ?, prenom = ?, age = ?, sexe = ?, telephone = ?, email = ?,
                    description = ?, date_creation = ?, actif = ?
                WHERE id_patient = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillPatientStatement(statement, patient);
            statement.setInt(10, patient.getIdPatient());
            statement.executeUpdate();
        }
    }

    public void delete(int idPatient) throws SQLException {
        String sql = "DELETE FROM PATIENT WHERE id_patient = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idPatient);
            statement.executeUpdate();
        }
    }

    public int countAll() throws SQLException {
        return count("SELECT COUNT(*) FROM PATIENT");
    }

    public int countActive() throws SQLException {
        return count("SELECT COUNT(*) FROM PATIENT WHERE actif = TRUE");
    }

    public Map<String, Integer> countBySexe() throws SQLException {
        String sql = "SELECT sexe, COUNT(*) AS total FROM PATIENT GROUP BY sexe";
        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("Homme", 0);
        result.put("Femme", 0);

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                result.put(resultSet.getString("sexe"), resultSet.getInt("total"));
            }
            return result;
        }
    }

    public Map<String, Integer> countCreatedByMonth(int months) throws SQLException {
        Map<String, Integer> result = new LinkedHashMap<>();
        YearMonth startMonth = YearMonth.now().minusMonths(Math.max(months - 1, 0));
        for (int i = 0; i < months; i++) {
            result.put(startMonth.plusMonths(i).format(MONTH_FORMATTER), 0);
        }

        String sql = """
                SELECT DATE_FORMAT(date_creation, '%Y-%m') AS month_label, COUNT(*) AS total
                FROM PATIENT
                WHERE date_creation >= ?
                GROUP BY month_label
                ORDER BY month_label
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(startMonth.atDay(1)));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String label = resultSet.getString("month_label");
                    if (result.containsKey(label)) {
                        result.put(label, resultSet.getInt("total"));
                    }
                }
            }
            return result;
        }
    }

    private int count(String sql) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private void fillPatientStatement(PreparedStatement statement, Patient patient) throws SQLException {
        LocalDate creationDate = patient.getDateCreation() == null ? LocalDate.now() : patient.getDateCreation();
        statement.setString(1, patient.getNom());
        statement.setString(2, patient.getPrenom());
        statement.setInt(3, patient.getAge());
        statement.setString(4, patient.getSexe());
        statement.setString(5, patient.getTelephone());
        statement.setString(6, patient.getEmail());
        statement.setString(7, patient.getDescription());
        statement.setDate(8, Date.valueOf(creationDate));
        statement.setBoolean(9, patient.isActif());
    }

    private void setParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            statement.setObject(i + 1, parameters.get(i));
        }
    }

    private Patient mapPatient(ResultSet resultSet) throws SQLException {
        Date creationDate = resultSet.getDate("date_creation");
        return new Patient(
                resultSet.getInt("id_patient"),
                resultSet.getString("nom"),
                resultSet.getString("prenom"),
                resultSet.getInt("age"),
                resultSet.getString("sexe"),
                resultSet.getString("telephone"),
                resultSet.getString("email"),
                resultSet.getString("description"),
                creationDate == null ? null : creationDate.toLocalDate(),
                resultSet.getBoolean("actif")
        );
    }
}
