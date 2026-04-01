# Usa un'immagine base di OpenJDK per la fase di build
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Imposta la directory di lavoro
WORKDIR /app

# Copia il file pom.xml e scarica le dipendenze
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia il codice sorgente e costruisci l'applicazione
COPY src ./src
RUN mvn clean package -DskipTests

# Usa un'immagine base di OpenJDK per la fase di runtime
FROM eclipse-temurin:21-jre-jammy

# Imposta la directory di lavoro
WORKDIR /app

# Copia il file JAR dall'immagine di build
COPY --from=build /app/target/dieti-estate25-backend-0.0.1-SNAPSHOT.jar app.jar

# Espone la porta su cui l'applicazione ascolta
EXPOSE 8080

# Comando per eseguire l'applicazione
ENTRYPOINT ["java", "-jar", "app.jar"]
