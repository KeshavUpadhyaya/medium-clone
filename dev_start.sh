export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/medium
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=root
export JWT_SECRET_KEY=mySecretKey
export ALLOWED_ORIGINS=http://localhost:4200
export JWT_EXPIRATION_MS=90000
export JWT_REFRESH_TOKEN_EXPIRATION_MS=120000

mvn spring-boot:run
