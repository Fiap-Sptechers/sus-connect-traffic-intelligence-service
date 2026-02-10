# Multi-stage build para otimizar tamanho da imagem
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copiar arquivos de dependências primeiro (cache layer)
# Esta camada será reutilizada se o pom.xml não mudar
COPY pom.xml .
# Baixar dependências (cache layer - só reexecuta se pom.xml mudar)
RUN mvn dependency:go-offline -B || true

# Copiar código fonte (camada separada para melhor cache)
COPY src ./src
# Compilar (só recompila se código fonte mudar)
RUN mvn clean package -DskipTests -B

# Stage final - imagem runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Criar usuário não-root para segurança
RUN addgroup -S spring && adduser -S spring -G spring

# Copiar JAR da aplicação
COPY --from=build /app/target/*.jar app.jar

# Mudar ownership para usuário não-root
RUN chown spring:spring app.jar

# Usar usuário não-root
USER spring:spring

# Expor porta (Cloud Run usa $PORT)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --retries=3 --start-period=40s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/actuator/health || exit 1

# Variável de ambiente para porta (Cloud Run define $PORT)
ENV PORT=8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Comando para iniciar aplicação
# Cloud Run define $PORT, então usamos essa variável
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
