### Задание
Разработать систему учета посетителей в спортклубе. Имеются различные планы посещений: неограниченный,
только в первой половине дня, только во второй половине дня, 3 раза в неделю. По идентификатору
посетителя, дате, времени и истории посещений принимается индивидуальное решение о допуске в
клуб. Реализовать операции: регистрация посетителя и тарифного плана, регистрация посещения с
проверкой доступа.

### Модель предметной области
![](./!docs/DomainDiagram.svg)

### Случаи использования (use cases)
- Регистрация посетителя (Member)
- Регистрация тарифного плана (RatePlan)
- Назначение тарифного плана (RatePlan) на посетителя (Member)
- Регистрация посещения (Attendance) с проверкой доступа
- Поиск пользователя по email-адресу
- Обновление цены тарифного плана
- Поиск тарифных планов (RatePlan)
- Удаление тарифного плана (RatePlan)
- Регистрация нового администратора
- Аутентификация администратора

Подробнее: [документация случаев использования](!docs/UseCases.md).

Также в папке [!docs](./!docs/) расположены [диаграммы классов](!docs/classesDiagrams/) (по одной на каждый микросервис) и [диаграммы последовательности](!docs/sequenceDiagrams/).

### Разработанные микросервисы

| Сервис | Порт | Описание                                                                                          |
|--------|------|---------------------------------------------------------------------------------------------------|
| Gateway | 8080 | API-шлюз                                                                                          |
| Auth Service | 8081 | Регистрация и аутентификация администраторов                                                      |
| Member Service | 8082 | Регистрация посетителей и учет посещений, работа с абонементами (подписками)                      |
| RatePlan Service | 8083 | Создание, обновление и деактивация тарифных планов, публикация событий в очередь (outbox паттерн) |
| Eureka Server | 8761 | Сервис обнаружения                                                                                |

### Запуск

Запуск всего приложения через Docker Compose: `docker compose up --build`

Запуск вручную:
1. Поднять базы данных: `docker compose up -d auth-db member-db rateplan-db`
2. Запустить Eureka: `./gradlew eureka:bootRun`
3. Запустить микросервисы и Gateway:
  - `./gradlew gateway:bootRun`
  - `./gradlew auth-service:bootRun`
  - `./gradlew member-service:bootRun`
  - `./gradlew rateplan-service:bootRun`

### OpenAPI / Swagger UI

Документация каждого микросервиса доступна через Swagger UI.

| Сервис            | Swagger UI | OpenAPI yaml                           |
|-------------------|-----------|----------------------------------------|
| Auth microservice | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs.yaml |
| Member microservice    | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs.yaml |
| RatePlan microservice  | http://localhost:8083/swagger-ui.html | http://localhost:8083/v3/api-docs.yaml |

Также спецификация openAPI для API микросервисов размещена в репозитории в каталоге [!docs/openapi/](./!docs/openapi/)

### REST API
Подробнее про REST API, предоставляемое сервисами.

#### Auth Service (`/api/auth`)
| Метод | URL | Описание                                 |
|-------|-----|------------------------------------------|
| POST | `/api/auth/register` | Регистрация администратора               |
| POST | `/api/auth/login` | Аутентификация  администратора |
| POST | `/api/auth/confirm` | Подтверждение регистрации администратора |

#### Member Service (`/api/members`, `/api/attendances`)
| Метод | URL | Описание                                  |
|-------|-----|-------------------------------------------|
| GET | `/api/members/{id}` | Поиск посетителя по Id                    |
| GET | `/api/members?email=...` | Поиск посетителя по email                 |
| POST | `/api/members` | Регистрация посетителя                    |
| POST | `/api/members/assign-rate-plan` | Назначение тарифного плана                |
| POST | `/api/attendances` | Регистрация посещения с проверкой доступа |

#### RatePlan Service (`/api/rate-plans`)
| Метод | URL | Описание                               |
|-------|-----|----------------------------------------|
| GET | `/api/rate-plans` | Поиск тарифных планов (с фильтрацией)  |
| GET | `/api/rate-plans/{id}` | Получение тарифного плана по Id        |
| POST | `/api/rate-plans` | Создание тарифного плана               |
| PUT | `/api/rate-plans/{id}/price` | Обновление цены тарифного плана        |
| DELETE | `/api/rate-plans/{id}` | Удаление (деактивация) тарифного плана |

Ответы содержат HATEOAS-ссылки (`_links`) для навигации между ресурсами

[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/v9cQFoXZ)
