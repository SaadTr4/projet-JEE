package fr.projetjee.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

/**
 * Classe utilitaire pour gérer la SessionFactory Hibernate
 * Pattern Singleton pour garantir une seule instance
 */
public class HibernateUtil {
    
    private static SessionFactory sessionFactory;
    private static ServiceRegistry serviceRegistry;
    
    // Constructeur privé pour empêcher l'instanciation
    private HibernateUtil() {}
    
    /**
     * Initialise et retourne la SessionFactory
     * @return SessionFactory instance unique
     */
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                // Créer la configuration Hibernate
                Configuration configuration = new Configuration();
                configuration.configure("hibernate.cfg.xml");
                
                // Créer le ServiceRegistry
                serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties())
                        .build();
                
                // Créer la SessionFactory
                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
                
                System.out.println("✅ SessionFactory créée avec succès!");
                
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de la création de la SessionFactory: " + e.getMessage());
                e.printStackTrace();
                
                if (serviceRegistry != null) {
                    StandardServiceRegistryBuilder.destroy(serviceRegistry);
                }
                throw new ExceptionInInitializerError(e);
            }
        }
        return sessionFactory;
    }
    
    /**
     * Ferme la SessionFactory proprement
     */
    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            System.out.println("✅ SessionFactory fermée avec succès!");
        }
        if (serviceRegistry != null) {
            StandardServiceRegistryBuilder.destroy(serviceRegistry);
        }
    }
}
