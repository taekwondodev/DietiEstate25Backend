# Usa un'immagine base di OpenJDK per la fase di build
FROM maven:3.9.13-eclipse-temurin-25 AS build

# Imposta la directory di lavoro
WORKDIR /app

# Copia il file pom.xml e scarica le dipendenze
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia il codice sorgente e costruisci l'applicazione
COPY src ./src
RUN mvn clean package -DskipTests

# Usa un'immagine base di OpenJDK per la fase di runtime
FROM eclipse-temurin:25-jre-alpine

# Aggiorna i pacchetti per eventuali CVE di sicurezza
RUN apk upgrade --no-cache

# Crea utente non privilegiato
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Imposta la directory di lavoro
WORKDIR /app

# Copia il file JAR dall'immagine di build
COPY --from=build --chown=appuser:appgroup /app/target/dieti-estate25-backend-0.0.1-SNAPSHOT.jar app.jar

# Esegui come utente non privilegiato
USER appuser

# Espone la porta su cui l'applicazione ascolta
EXPOSE 8080

# Comando per eseguire l'applicazione
ENTRYPOINT ["java", "-jar", "app.jar"]
