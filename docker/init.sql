-- Création de la base de données
CREATE DATABASE IF NOT EXISTS entreprise;

-- Table Departement
CREATE TABLE departement (
      id SERIAL PRIMARY KEY,
      nom VARCHAR(100) NOT NULL,
      description TEXT
);

-- Table Poste
CREATE TABLE poste (
       id SERIAL PRIMARY KEY,
       nom VARCHAR(100) NOT NULL,
       description TEXT
);

-- Table Projet
CREATE TABLE projet (
        id SERIAL PRIMARY KEY,
        nom VARCHAR(100) NOT NULL,
        description TEXT,
        etat VARCHAR(20) DEFAULT 'En cours'
);

-- Table Utilisateur
CREATE TABLE utilisateur (
         matricule VARCHAR(20) PRIMARY KEY,
         nom VARCHAR(50) NOT NULL,
         prenom VARCHAR(50) NOT NULL,
         email VARCHAR(100) UNIQUE NOT NULL,
         telephone VARCHAR(20),
         image BYTEA,
         adresse VARCHAR(255),
         grade VARCHAR(20),
         role VARCHAR(20),
         id_departement INT,
         id_poste INT,
         FOREIGN KEY (id_departement) REFERENCES departement(id),
         FOREIGN KEY (id_poste) REFERENCES poste(id)
);

-- Table relation N-N Utilisateur-Projet
CREATE TABLE utilisateur_projet (
        matricule VARCHAR(20),
        id_projet INT,
        PRIMARY KEY (matricule, id_projet),
        FOREIGN KEY (matricule) REFERENCES utilisateur(matricule) ON DELETE CASCADE,
        FOREIGN KEY (id_projet) REFERENCES projet(id) ON DELETE CASCADE
);

-- Table Fiche de Paie
CREATE TABLE fiche_paie (
        id SERIAL PRIMARY KEY,
        matricule VARCHAR(20),
        date DATE NOT NULL,
        salaire_base DECIMAL(10,2),
        primes DECIMAL(10,2),
        deductions DECIMAL(10,2),
        net_a_payer DECIMAL(10,2),
        FOREIGN KEY (matricule) REFERENCES utilisateur(matricule) ON DELETE CASCADE
);
