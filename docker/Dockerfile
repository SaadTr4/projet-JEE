FROM tomcat:10.1.48-jdk21-temurin

# Supprimer le ROOT existant
RUN rm -rf /usr/local/tomcat/webapps/ROOT*

# Copier le WAR généré dans Tomcat
COPY ../target/projet-JEE-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
