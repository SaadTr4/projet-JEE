package fr.projetjee;

import fr.projetjee.dao.ProjectDAO;
import fr.projetjee.model.Project;
import fr.projetjee.model.User;
import fr.projetjee.util.HibernateUtil;
import org.hibernate.Session;

public class TestAssign {

    public static void main(String[] args) {
        ProjectDAO projectDAO = new ProjectDAO();

        try {
            // Exemple : assigner l'utilisateur EMP005 au projet "Migration Cloud"
            String matricule = "EMP005";
            Integer projectId = 3; // ID du projet "Migration Cloud"

            boolean result = projectDAO.assignUserToProject(projectId, matricule);

            if (result) {
                System.out.println("✅ Assignation réussie : " + matricule + " → projet ID " + projectId);
            } else {
                System.out.println("❌ Assignation échouée : " + matricule + " → projet ID " + projectId);
            }

            // Vérification rapide : récupérer le projet et afficher ses utilisateurs
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Project project = session.find(Project.class, projectId);
                System.out.println("\n👥 Utilisateurs du projet " + project.getName() + " :");
                for (User u : project.getUsers()) {
                    System.out.println("   → " + u.getFullName() + " (" + u.getMatricule() + ")");
                }
            }

        } finally {
            HibernateUtil.shutdown();
        }
    }
}
