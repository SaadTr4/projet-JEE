package fr.projetjee;

import fr.projetjee.dao.DepartmentDAO;
import fr.projetjee.dao.PositionDAO;
import fr.projetjee.model.Department;
import fr.projetjee.model.Position;
import fr.projetjee.util.HibernateUtil;

import java.util.List;
import java.util.Optional;

public class TestPositionDepartment {

    public static void main(String[] args) {

        PositionDAO positionDAO = new PositionDAO();
        DepartmentDAO departmentDAO = new DepartmentDAO();

        try {
            System.out.println("🚀 DÉBUT DU TEST POSITION & DÉPARTEMENT 🚀");

            // === 1️⃣ Création d'un département ===
            Department dep = new Department();
            dep.setName("Informatique");
            dep.setDescription("Département en charge des systèmes d'information et de la cybersécurité");
            departmentDAO.save(dep);
            System.out.println("✅ Département enregistré avec ID : " + dep.getId());

            // === 2️⃣ Création d’un poste ===
            Position pos = new Position();
            pos.setName("Développeur Java");
            pos.setDescription("Développement et maintenance des applications backend");
            positionDAO.save(pos);
            System.out.println("✅ Poste enregistré avec ID : " + pos.getId());

            // === 3️⃣ Lecture par ID ===
            Optional<Department> depById = departmentDAO.findById(dep.getId());
            depById.ifPresentOrElse(
                    d -> System.out.println("🔍 Département trouvé : " + d.getName()),
                    () -> System.out.println("❌ Département introuvable !")
            );

            Optional<Position> posById = positionDAO.findById(pos.getId());
            posById.ifPresentOrElse(
                    p -> System.out.println("🔍 Poste trouvé : " + p.getName()),
                    () -> System.out.println("❌ Poste introuvable !")
            );

            // === 4️⃣ Recherche par nom ===
            Optional<Department> depByName = departmentDAO.findByName("Informatique");
            depByName.ifPresentOrElse(
                    d -> System.out.println("🔎 Département trouvé par nom : " + d.getName()),
                    () -> System.out.println("❌ Aucun département trouvé avec ce nom.")
            );

            Optional<Position> posByName = positionDAO.findByName("Développeur Java");
            posByName.ifPresentOrElse(
                    p -> System.out.println("🔎 Poste trouvé par nom : " + p.getName()),
                    () -> System.out.println("❌ Aucun poste trouvé avec ce nom.")
            );

            // === 5️⃣ Liste complète ===
            List<Department> allDepartments = departmentDAO.findAll();
            System.out.println("📋 Nombre total de départements : " + allDepartments.size());
            allDepartments.forEach(d -> System.out.println("   → " + d.getName()));

            List<Position> allPositions = positionDAO.findAll();
            System.out.println("📋 Nombre total de postes : " + allPositions.size());
            allPositions.forEach(p -> System.out.println("   → " + p.getName()));

            // === 6️⃣ Mise à jour ===
            dep.setDescription("Département responsable de l’infrastructure IT et des projets logiciels");
            departmentDAO.update(dep);

            pos.setDescription("Conception et développement d’applications Java pour les projets internes");
            positionDAO.update(pos);

            System.out.println("✏️ Descriptions mises à jour avec succès.");

            // === 7️⃣ Suppression ===
            boolean depDeleted = departmentDAO.deleteById(dep.getId());
            System.out.println(depDeleted ? "🗑️ Département supprimé." : "❌ Erreur suppression département.");

            boolean posDeleted = positionDAO.deleteById(pos.getId());
            System.out.println(posDeleted ? "🗑️ Poste supprimé." : "❌ Erreur suppression poste.");

            // === 8️⃣ Vérification post-suppression ===
            boolean depExists = departmentDAO.findById(dep.getId()).isPresent();
            boolean posExists = positionDAO.findById(pos.getId()).isPresent();

            System.out.println(depExists ? "❌ Département encore présent !" : "✅ Département bien supprimé.");
            System.out.println(posExists ? "❌ Poste encore présent !" : "✅ Poste bien supprimé.");

            System.out.println("🏁 FIN DU TEST POSITION & DÉPARTEMENT 🏁");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
