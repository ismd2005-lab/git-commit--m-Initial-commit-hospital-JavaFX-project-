package dao;

import models.RendezVous;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RendezVousDAO {
    private static final String BASE_SELECT = """
            SELECT r.*, CONCAT(p.prenom, ' ', p.nom) AS patient_nom_complet
            FROM RENDEZVOUS r
            INNER JOIN PATIENT p ON p.id_patient = r.id_patient
            """;

    public List<RendezVous> findAll() throws SQLException {
        String sql = BASE_SELECT + " ORDER BY r.date_rdv DESC, r.heure";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return mapList(resultSet);
        }
    }

    public List<RendezVous> findByCriteria(String keyword, String specialite, String statut) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(p.nom) LIKE ? OR LOWER(p.prenom) LIKE ? OR LOWER(r.medecin) LIKE ?)");
            String like = "%" + keyword.toLowerCase().trim() + "%";
            parameters.add(like);
            parameters.add(like);
            parameters.add(like);
        }

        if (specialite != null && !specialite.isBlank() && !"Toutes".equalsIgnoreCase(specialite)) {
            sql.append(" AND r.specialite = ?");
            parameters.add(specialite);
        }

        if (statut != null && !statut.isBlank() && !"Tous".equalsIgnoreCase(statut)) {
            sql.append(" AND r.statut = ?");
            parameters.add(statut);
        }

        sql.append(" ORDER BY r.date_rdv DESC, r.heure");

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            setParameters(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    public List<RendezVous> findUpcoming(int limit) throws SQLException {
        String sql = BASE_SELECT + """
                WHERE r.date_rdv >= CURDATE()
                ORDER BY r.date_rdv, r.heure
                LIMIT ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        }
    }

    public void save(RendezVous rendezVous) throws SQLException {
        String sql = """
                INSERT INTO RENDEZVOUS (id_patient, date_rdv, heure, medecin, specialite, statut, remarque)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillRendezVousStatement(statement, rendezVous);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    rendezVous.setIdRdv(keys.getInt(1));
                }
            }
        }
    }

    public void update(RendezVous rendezVous) throws SQLException {
        String sql = """
                UPDATE RENDEZVOUS
                SET id_patient = ?, date_rdv = ?, heure = ?, medecin = ?,
                    specialite = ?, statut = ?, remarque = ?
                WHERE id_rdv = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillRendezVousStatement(statement, rendezVous);
            statement.setInt(8, rendezVous.getIdRdv());
            statement.executeUpdate();
        }
    }

    public void delete(int idRdv) throws SQLException {
        String sql = "DELETE FROM RENDEZVOUS WHERE id_rdv = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idRdv);
            statement.executeUpdate();
        }
    }

    public int countAll() throws SQLException {
        return count("SELECT COUNT(*) FROM RENDEZVOUS");
    }

    public int countToday() throws SQLException {
        return count("SELECT COUNT(*) FROM RENDEZVOUS WHERE date_rdv = CURDATE()");
    }

    private int count(String sql) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private void fillRendezVousStatement(PreparedStatement statement, RendezVous rendezVous) throws SQLException {
        statement.setInt(1, rendezVous.getIdPatient());
        statement.setDate(2, Date.valueOf(rendezVous.getDateRdv()));
        statement.setString(3, rendezVous.getHeure());
        statement.setString(4, rendezVous.getMedecin());
        statement.setString(5, rendezVous.getSpecialite());
        statement.setString(6, rendezVous.getStatut());
        statement.setString(7, rendezVous.getRemarque());
    }

    private void setParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            statement.setObject(i + 1, parameters.get(i));
        }
    }

    private List<RendezVous> mapList(ResultSet resultSet) throws SQLException {
        List<RendezVous> rendezVous = new ArrayList<>();
        while (resultSet.next()) {
            rendezVous.add(mapRendezVous(resultSet));
        }
        return rendezVous;
    }

    private RendezVous mapRendezVous(ResultSet resultSet) throws SQLException {
        Date dateRdv = resultSet.getDate("date_rdv");
        return new RendezVous(
                resultSet.getInt("id_rdv"),
                resultSet.getInt("id_patient"),
                resultSet.getString("patient_nom_complet"),
                dateRdv == null ? null : dateRdv.toLocalDate(),
                resultSet.getString("heure"),
                resultSet.getString("medecin"),
                resultSet.getString("specialite"),
                resultSet.getString("statut"),
                resultSet.getString("remarque")
        );
    }
}
