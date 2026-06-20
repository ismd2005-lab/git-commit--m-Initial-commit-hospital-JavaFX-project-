# MedVision AI - Gestion d'Hôpital JavaFX

## Présentation du projet

MedVision AI est une application desktop développée en JavaFX pour la gestion d'un hôpital ou d'une clinique médicale.

Ce projet a été réalisé dans le cadre du mini-projet JavaFX du module DEV Java IHM. L'objectif est de concevoir une application complète avec une interface graphique JavaFX, connectée à une base de données MySQL, en respectant une architecture organisée.

L'application permet principalement de gérer les patients, les rendez-vous médicaux, les statistiques et l'export des données.

## Sujet choisi

Le sujet choisi est :

**Gestion d'un hôpital**

Ce choix est cohérent avec les exigences du mini-projet, car il permet de gérer deux entités principales liées entre elles :

- Patient
- Rendez-vous

Un patient peut avoir plusieurs rendez-vous, et chaque rendez-vous appartient à un seul patient.

## Objectifs de l'application

L'application permet de :

- Ajouter, modifier, supprimer et afficher les patients.
- Ajouter, modifier, supprimer et afficher les rendez-vous.
- Associer chaque rendez-vous à un patient.
- Rechercher et filtrer les données.
- Afficher des statistiques dans un tableau de bord.
- Exporter les données au format CSV.
- Utiliser une interface graphique claire avec JavaFX, FXML et CSS.

## Fonctionnalités principales

### Gestion des patients

- Ajout d'un nouveau patient.
- Modification des informations d'un patient.
- Suppression d'un patient.
- Affichage des patients dans un tableau.
- Recherche par nom, prénom ou téléphone.
- Filtrage par sexe ou statut actif.

### Gestion des rendez-vous

- Création d'un rendez-vous médical.
- Modification d'un rendez-vous.
- Suppression d'un rendez-vous.
- Association d'un rendez-vous avec un patient.
- Filtrage par spécialité ou statut.
- Affichage des rendez-vous dans un tableau.

### Tableau de bord

- Nombre total de patients.
- Nombre total de rendez-vous.
- Rendez-vous proches.
- Indicateurs visuels.
- Statistiques médicales simples.

### Export

- Export des données au format CSV.
- Utilisation de FileChooser pour choisir l'emplacement du fichier.

## Contrôles JavaFX utilisés

Le projet utilise plusieurs contrôles JavaFX demandés dans le mini-projet :

- TextField
- TextArea
- Button
- Label
- RadioButton
- ToggleGroup
- CheckBox
- ComboBox
- ListView
- TableView
- DatePicker
- Slider
- Spinner
- ProgressBar
- ProgressIndicator
- Tooltip
- MenuBar
- Alert
- Dialog
- Accordion
- TitledPane
- ColorPicker
- FileChooser

## Technologies utilisées

- Java 17 ou version supérieure
- JavaFX
- FXML
- CSS
- Maven
- MySQL
- JDBC
- Pattern DAO
- Architecture MVC

## Structure du projet

```text
hospital-management-javafx/
├── README.md
├── init_db.sql
├── pom.xml
├── diagrams/
│   ├── class-diagram.puml
│   └── use-case-diagram.puml
├── src/
│   ├── Main.java
│   ├── config/
│   ├── controller/
│   ├── dao/
│   ├── model/
│   ├── repository/
│   ├── service/
│   ├── util/
│   ├── view/
│   └── css/

## Vidéo de démonstration

Lien Google Drive de la vidéo :

https://drive.google.com/file/d/1MH-9QFIjpN1XuioeSiGLIeEG2_jH228b/view?usp=sharing

La vidéo présente rapidement l'interface de l'application, la gestion des patients, la gestion des rendez-vous, la recherche, le filtrage, les statistiques et l'export CSV.https://drive.google.com/file/d/1MH-9QFIjpN1XuioeSiGLIeEG2_jH228b/view?usp=sharing
La vidéo présente rapidement l'interface de l'application, la gestion des patients, la gestion des rendez-vous, la recherche, le filtrage, les statistiques et l'export CSV.
