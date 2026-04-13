# Subscription monitor API

RESTful сервис для управления личными подписками с автоматической конвертацией валют и аналитикой затрат.

## Ключевые возможности
- Управление подписками: полный CRUD цикл для ваших регулярных платежей
- Интеграция с Currency API: Автоматическая проверка и поддержка актуальных кодов валют
- Безопасность: Аутентификация на базе JWT (Stateless) с разграничением прав доступа
- Технологический стек: Использование jOOQ для типобезопасных SQL-запросов и PostgreSQL для хранения данных
- Документированность: Полное описание API через Swagger UI

### Технологический стек
- Java 21 & Spring Boot 4.0.4
- Spring Security (JWT Authentication)
- jOOQ (как альтернатива Hibernate для аналитических запросов)
- PostgreSQL
- Liquibase
- Docker & Docker Compose
- Maven
- OpenAPI 3

## Запуск

### Требования
- Docker & Docker Compose
- Java 21+

### Запуск приложения
1. Соберите проект локально для генерации jOOQ метаданных
mvn clean package -DskipTests
2. Запустите инфраструктуру через Docker
docker-compose up --build -d
После запуска сервис будет запущен по адресу http://localhost:8080

### Документация API
Интерактивная документация будет доступна сразу после запуска:
- Swagger UI http://localhost:8080/swagger-ui.html
- OpenAPI JSON http://localhost:8080/api-docs

## Архитектурные особенности
- Query Optimization: ИСпользование jOOQ позволяет строить эффективные запросы для аналитики
