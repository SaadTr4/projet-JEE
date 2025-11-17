package fr.projetjee;

import fr.projetjee.model.User;
import fr.projetjee.enums.Grade;
import fr.projetjee.enums.Role;
import fr.projetjee.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class TestUser {

    public static void main(String[] args) {
        // Ouverture d'une session Hibernate
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            Transaction tx = session.beginTransaction();

            System.out.println("🚀 Test d’enregistrement d’un utilisateur...");

            // Création d’un utilisateur de test avec matricule UNIQUE
            User user = new User(
                    "EMP_" + System.currentTimeMillis(),   // 🔥 matricule unique
                    "Durand",
                    "Alice",
                    "alice1.durand@entreprise.fr"
            );

            user.setPhone("0600000000");
            user.setAddress("1 Rue de la Paix, Paris");
            user.setGrade(Grade.JUNIOR);
            user.setRole(Role.EMPLOYE);

            // Sauvegarde en base
            session.persist(user);
            tx.commit();

            System.out.println("✅ Utilisateur enregistré avec succès !");

            // Vérification qu’il existe dans la base
            User found = session.find(User.class, user.getId());
            if (found != null) {
                System.out.println("🔍 Utilisateur trouvé : " + found.getFullName());
            } else {
                System.out.println("❌ Utilisateur non trouvé !");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
