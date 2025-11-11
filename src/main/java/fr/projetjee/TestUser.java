package fr.projetjee;

import fr.projetjee.dao.UserDAO;
import fr.projetjee.enums.Grade;
import fr.projetjee.enums.Role;
import fr.projetjee.model.User;
import fr.projetjee.util.HibernateUtil;

import java.util.List;
import java.util.Optional;

public class TestUser {

    public static void main(String[] args) {

        UserDAO userDAO = new UserDAO();

        try {
            System.out.println("🚀 DÉBUT DU TEST USER DAO 🚀");

            // === 1️⃣ Création et enregistrement ===
            User user = new User("EMP_TEST", "Durandal", "Alicia", "alicia.durandal@entreprise.fr");
            user.setPhone("0600000000");
            user.setAddress("1 Rue de la Paix, Paris");
            user.setGrade(Grade.JUNIOR);
            user.setRole(Role.EMPLOYE);

            userDAO.save(user);
            System.out.println("✅ Utilisateur enregistré avec ID : " + user.getId());

            // === 2️⃣ Lecture par ID ===
            Optional<User> foundById = userDAO.findById(user.getId());
            foundById.ifPresentOrElse(
                    u -> System.out.println("🔍 Trouvé par ID : " + u.getFullName()),
                    () -> System.out.println("❌ Aucun utilisateur trouvé avec cet ID.")
            );

            // === 3️⃣ Recherche par matricule ===
            Optional<User> foundByMatricule = userDAO.findByMatricule("EMP_TEST");
            foundByMatricule.ifPresentOrElse(
                    u -> System.out.println("🔍 Trouvé par matricule : " + u.getEmail()),
                    () -> System.out.println("❌ Aucun utilisateur trouvé avec ce matricule.")
            );

            // === 4️⃣ Recherche par email ===
            Optional<User> foundByEmail = userDAO.findByEmail("alicia.durandal@entreprise.fr");
            foundByEmail.ifPresentOrElse(
                    u -> System.out.println("🔍 Trouvé par email : " + u.getFullName()),
                    () -> System.out.println("❌ Aucun utilisateur trouvé avec cet email.")
            );

            // === 5️⃣ Recherche par nom ===
            List<User> byLastName = userDAO.findByLastName("Durandal");
            System.out.println("📋 Utilisateurs trouvés par nom : " + byLastName.size());

            // === 6️⃣ Recherche par prénom ===
            List<User> byFirstName = userDAO.findByFirstName("Alicia");
            System.out.println("📋 Utilisateurs trouvés par prénom : " + byFirstName.size());

            // === 7️⃣ Recherche par grade ===
            List<User> byGrade = userDAO.findByGrade(Grade.JUNIOR);
            System.out.println("📋 Utilisateurs avec grade JUNIOR : " + byGrade.size());

            // === 8️⃣ Recherche par rôle ===
            List<User> byRole = userDAO.findByRole(Role.EMPLOYE);
            System.out.println("📋 Utilisateurs avec rôle EMPLOYE : " + byRole.size());

           // ===  Recherche tous les utilisateurs ===
            List<User> allUsers = userDAO.findAll();
            System.out.println("📋 Nombre total d’utilisateurs avec methode findAll() : " + allUsers.size());

            // === 9️⃣ Vérification existence ===
            boolean exists = userDAO.exists(user.getId());
            System.out.println(exists ? "✅ L’utilisateur existe bien." : "❌ L’utilisateur n’existe pas.");

            // === 🔟 Compte total ===
            long count = userDAO.count();
            System.out.println("📊 Nombre total d’utilisateurs avec methode count() : " + count);

            // === Compte par grade ===
            long countByGrade = userDAO.countByGrade(Grade.JUNIOR);
            System.out.println("📊 Nombre d’utilisateurs avec grade JUNIOR : " + countByGrade);

            // === 1️⃣1️⃣ Mise à jour ===
            user.setAddress("99 Avenue de la République, Lyon");
            userDAO.update(user);
            System.out.println("✏️ Adresse mise à jour !");

            // === 1️⃣2️⃣ Suppression ===
            boolean deleted = userDAO.deleteById(user.getId());
            System.out.println(deleted ? "🗑️ Utilisateur supprimé avec succès." : "❌ Erreur lors de la suppression.");

            // === 1️⃣3️⃣ Vérification post-suppression ===
            boolean stillExists = userDAO.exists(user.getId());
            System.out.println(stillExists ? "❌ L’utilisateur est encore présent !" : "✅ L’utilisateur a bien été supprimé.");

            System.out.println("🏁 FIN DU TEST USER DAO 🏁");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
