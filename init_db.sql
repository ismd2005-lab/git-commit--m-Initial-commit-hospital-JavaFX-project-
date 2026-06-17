CREATE DATABASE IF NOT EXISTS medvision_ai
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE medvision_ai;

DROP TABLE IF EXISTS RENDEZVOUS;
DROP TABLE IF EXISTS PATIENT;

CREATE TABLE PATIENT (
    id_patient INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(50) NOT NULL,
    prenom VARCHAR(50) NOT NULL,
    age INT NOT NULL,
    sexe VARCHAR(20) NOT NULL,
    telephone VARCHAR(20),
    email VARCHAR(100),
    description TEXT,
    date_creation DATE DEFAULT (CURRENT_DATE),
    actif BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE RENDEZVOUS (
    id_rdv INT PRIMARY KEY AUTO_INCREMENT,
    id_patient INT NOT NULL,
    date_rdv DATE NOT NULL,
    heure VARCHAR(10) NOT NULL,
    medecin VARCHAR(100) NOT NULL,
    specialite VARCHAR(100) NOT NULL,
    statut VARCHAR(30) NOT NULL,
    remarque TEXT,
    CONSTRAINT fk_rendezvous_patient
        FOREIGN KEY (id_patient)
        REFERENCES PATIENT(id_patient)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_patient_nom ON PATIENT(nom, prenom);
CREATE INDEX idx_patient_telephone ON PATIENT(telephone);
CREATE INDEX idx_rdv_date ON RENDEZVOUS(date_rdv);
CREATE INDEX idx_rdv_specialite ON RENDEZVOUS(specialite);
CREATE INDEX idx_rdv_statut ON RENDEZVOUS(statut);

INSERT INTO PATIENT (nom, prenom, age, sexe, telephone, email, description, date_creation, actif) VALUES
('Benali', 'Sara', 31, 'Femme', '0601000001', 'sara.benali@example.com', 'Suivi cardiologique intelligent. Risque stable.', DATE_SUB(CURDATE(), INTERVAL 5 MONTH), TRUE),
('El Amrani', 'Youssef', 46, 'Homme', '0601000002', 'youssef.elamrani@example.com', 'Controle post-operatoire. Priorite moyenne.', DATE_SUB(CURDATE(), INTERVAL 4 MONTH), TRUE),
('Mansouri', 'Nadia', 27, 'Femme', '0601000003', 'nadia.mansouri@example.com', 'Consultation dermatologie. Allergies saisonnieres.', DATE_SUB(CURDATE(), INTERVAL 3 MONTH), TRUE),
('Idrissi', 'Karim', 58, 'Homme', '0601000004', 'karim.idrissi@example.com', 'Diabete type 2. Surveillance reguliere.', DATE_SUB(CURDATE(), INTERVAL 2 MONTH), FALSE),
('Alaoui', 'Meryem', 39, 'Femme', '0601000005', 'meryem.alaoui@example.com', 'Bilan neurologique avec suivi IA.', DATE_SUB(CURDATE(), INTERVAL 1 MONTH), TRUE);

INSERT INTO RENDEZVOUS (id_patient, date_rdv, heure, medecin, specialite, statut, remarque) VALUES
(1, CURDATE(), '09:30', 'Dr. Lina Berrada', 'Cardiologie', 'Confirmé', 'Verifier ECG et tension.'),
(2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '11:00', 'Dr. Amine Saidi', 'Chirurgie', 'Planifié', 'Controle cicatrisation.'),
(3, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '14:15', 'Dr. Salma Naciri', 'Dermatologie', 'En attente', 'Analyse cutanee.'),
(4, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:45', 'Dr. Omar Rami', 'Endocrinologie', 'Confirmé', 'Controle glycemie.'),
(5, DATE_ADD(CURDATE(), INTERVAL 5 DAY), '16:00', 'Dr. Ines Tazi', 'Neurologie', 'Planifié', 'Bilan cognitif assiste IA.');
