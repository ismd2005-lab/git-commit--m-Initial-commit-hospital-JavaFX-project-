# MedVision AI Healthcare Dashboard

Application desktop JavaFX MVC pour la gestion futuriste d'une clinique medicale : patients, rendez-vous, statistiques, recherche, filtres et export CSV.

## Stack

- Java 17+
- JavaFX + FXML + CSS
- JDBC MySQL
- Pattern DAO
- FontAwesomeFX
- Maven

## Installation

1. Installer Java 17 ou plus et MySQL.
2. Creer la base avec le script :

```bash
mysql -u root -p < init_db.sql
```

3. Configurer la connexion MySQL si vos identifiants ne sont pas ceux par defaut :

```bash
set MEDVISION_DB_URL=jdbc:mysql://localhost:3306/medvision_ai?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
set MEDVISION_DB_USER=root
set MEDVISION_DB_PASSWORD=votre_mot_de_passe
```

Sous PowerShell :

```powershell
$env:MEDVISION_DB_URL="jdbc:mysql://localhost:3306/medvision_ai?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:MEDVISION_DB_USER="root"
$env:MEDVISION_DB_PASSWORD="votre_mot_de_passe"
```

4. Lancer l'application :

```bash
mvn clean javafx:run
```

## Configuration par defaut

Sans variables d'environnement, l'application utilise :

- URL : `jdbc:mysql://localhost:3306/medvision_ai?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true`
- User : `root`
- Password : vide

## Fonctionnalites

- Dashboard medical futuriste avec cartes KPI, graphiques et rendez-vous proches.
- CRUD complet des patients.
- CRUD complet des rendez-vous.
- Recherche dynamique par nom, prenom et telephone.
- Filtres par sexe, statut actif, specialite et statut de rendez-vous.
- Export CSV des tables avec `FileChooser`.
- Alertes de confirmation et messages d'erreur.
- Personnalisation de la couleur d'accent avec `ColorPicker`.

## Structure

```text
src/
 ├── models/
 │     ├── Patient.java
 │     └── RendezVous.java
 ├── dao/
 │     ├── Database.java
 │     ├── PatientDAO.java
 │     └── RendezVousDAO.java
 ├── controllers/
 │     ├── DashboardController.java
 │     ├── PatientController.java
 │     ├── RendezVousController.java
 │     └── CsvExporter.java
 ├── views/
 │     ├── dashboard.fxml
 │     ├── patients.fxml
 │     └── rendezvous.fxml
 ├── css/
 │     └── style.css
 └── Main.java
```

## Diagrammes

Les diagrammes PlantUML sont disponibles dans :

- `diagrams/class-diagram.puml`
- `diagrams/use-case-diagram.puml`
