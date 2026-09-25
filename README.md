# Лабораторная работа №2. Сервис отчётов и шлюз

**Дисциплина:** Распределённые вычисления и приложения (DCA)  
**Студент:** Калимуллин Артур Рамилевич  
**Группа:** ПИБД-41  
**Вариант:** 14 — Учёт преподавателей ВУЗа

## Предметная область

Ведётся учёт преподавателей ВУЗа. Реализованы операции:

- **Принять человека на должность** — создать запись преподавателя со статусом `WORKING`.
- **Уволить по определённой причине** — перевести преподавателя в статус `FIRED`, указав причину и время увольнения (время выставляется автоматически).
- **Назначить предмет для чтения** — указать предмет, который читает преподаватель.
- **Сформировать отчёт** — сколько преподавателей работает и сколько уволено. Отчёт вынесен в отдельный сервис.

## Цель работы

Поднять второй сервис, работающий с той же БД, формирующий отчёты. Добавить шлюз и сделать доступ к сервисам через шлюз. Проверять работоспособность через Postman.

## Архитектура

```
Postman  →  Gateway :8080
              ├── /api/teachers/**  →  teacher-service :8082  ─┐
              └── /api/reports/**   →  report-service  :8083  ─┤
                                                                ├→ PostgreSQL :5432 (внутри сети)
                                                                │   (наружу :5434)
```

| Компонент | Порт | Назначение |
|------|------|---------|
| `gateway` | 8080 | Шлюз на Spring Cloud Gateway, единая точка входа. |
| `teacher-service` | 8082 | Основной сервис (ЛР1): CRUD, владелец схемы БД (Liquibase). |
| `report-service` | 8083 | Второй сервис (ЛР2): отчёты, только чтение БД. |
| `postgres` | 5434 (наружу), 5432 (внутри) | Общая БД `teacher_db`. |

## Структура проекта

| Путь | Что это |
|------|---------|
| `src/main/java/ru/ulstu/teacher_service/` | Исходный код основного сервиса (Spring Boot 3, Gradle): domain, repository, service, web, dto. |
| `src/main/resources/db/changelog/` | Миграции Liquibase: создание таблиц `teacher`, `teacher_status`. |
| `src/main/resources/application.yaml` | Конфигурация teacher-service: datasource, JPA, Liquibase, порт. |
| `report-service/src/main/java/ru/ulstu/report_service/` | Исходный код сервиса отчётов (Spring Boot 3, JdbcTemplate): domain, service, web. |
| `report-service/src/main/resources/application.yaml` | Конфигурация report-service: datasource, порт. |
| `gateway/src/main/java/ru/ulstu/gateway/` | Класс запуска шлюза на Spring Cloud Gateway. |
| `gateway/src/main/resources/application.yaml` | Маршруты шлюза и CORS. |
| `build.gradle` | Зависимости и плагины Gradle основного сервиса. |
| `report-service/build.gradle` | Зависимости и плагины Gradle сервиса отчётов. |
| `gateway/build.gradle` | Зависимости и плагины Gradle шлюза. |
| `Dockerfile` | Многоступенчатая сборка docker-образа teacher-service. |
| `report-service/Dockerfile` | Многоступенчатая сборка docker-образа report-service. |
| `gateway/Dockerfile` | Многоступенчатая сборка docker-образа gateway. |
| `docker-compose.yml` | Поднимает PostgreSQL и три сервиса в одной docker-сети `teacher-net`. |
| `.dockerignore` | Исключения для контекста сборки docker-образа. |
| `.env` | Параметры БД и портов. |

## Модель данных

В базе данных две таблицы предметной области — `teacher` и `teacher_status` (справочник статусов). Служебные таблицы `databasechangelog` и `databasechangeloglock` создаются автоматически самим Liquibase и не считаются таблицами предметной области. Структуру создаёт `teacher-service` через миграции Liquibase; `report-service` работает с этими таблицами только на чтение.

### Таблица `teacher`

| Колонка | Тип | Ограничения | Описание |
|---|---|---|---|
| `id` | BIGINT | PK, autoincrement | Идентификатор |
| `full_name` | VARCHAR(255) | NOT NULL | ФИО преподавателя |
| `position` | VARCHAR(128) | NOT NULL | Должность |
| `status_id` | BIGINT | FK → `teacher_status.id`, NOT NULL | Ссылка на статус |
| `fire_reason` | VARCHAR(255) | NULL | Причина увольнения |
| `subject` | VARCHAR(255) | NULL | Читаемый предмет |
| `hired_at` | TIMESTAMPTZ | NULL | Дата приёма на работу |
| `fired_at` | TIMESTAMPTZ | NULL | Дата увольнения |
| `created_at` | TIMESTAMPTZ | NOT NULL | Дата создания записи |

### Таблица `teacher_status`

| Колонка | Тип | Ограничения | Описание |
|---|---|---|---|
| `id` | BIGINT | PK, autoincrement | Идентификатор статуса |
| `code` | VARCHAR(16) | NOT NULL, UNIQUE | `WORKING` или `FIRED` |
| `name` | VARCHAR(64) | NOT NULL | Отображаемое название |

Записи справочника: `1 / WORKING / Работает`, `2 / FIRED / Уволен`.

## REST API

### Основной сервис — `teacher-service`

Базовый путь: `/api/teachers`.

| Метод | URL | Описание |
|---|---|---|
| GET | `/api/teachers` | Список преподавателей (пагинация, сортировка) |
| GET | `/api/teachers/{id}` | Получить по id |
| POST | `/api/teachers` | Принять на должность |
| PUT | `/api/teachers/{id}` | Изменить ФИО и должность |
| POST | `/api/teachers/{id}/fire` | Уволить по причине |
| POST | `/api/teachers/{id}/assign-subject` | Назначить предмет |
| DELETE | `/api/teachers/{id}` | Удалить запись |
| GET | `/api/teachers/report` | Отчёт: работает / уволено |

### Сервис отчётов — `report-service`

Базовый путь: `/api/reports`.

| Метод | URL | Описание |
|---|---|---|
| GET | `/api/reports/teachers` | Список преподавателей для отчёта |
| GET | `/api/reports/summary` | Сводка: работает / уволено |

Пример ответа `GET /api/reports/summary`:

```json
{
  "workingCount": 3,
  "firedCount": 1
}
```

### Шлюз — `gateway`

| Метод | URL | Куда проксируется |
|---|---|---|
| Любой | `/api/teachers/**` | `http://teacher-service:8082` |
| Любой | `/api/reports/**` | `http://report-service:8083` |

## Запуск

### Вариант 1. Через Docker

Перед запуском скопируйте шаблон:

```bash
cp .env.example .env
```

Требования: установлен Docker Desktop.

Запуск:

```bash
docker compose down -v
docker compose up --build
```

Что произойдёт:

1. Docker скачает образы `postgres:16`, `eclipse-temurin:21-jdk`, `eclipse-temurin:21-jre`.
2. Соберутся три образа: `teacher-service`, `report-service`, `gateway` — Gradle внутри контейнеров соберёт `bootJar`.
3. Поднимется контейнер `teacher-postgres`, в нём создастся БД `teacher_db`.
4. Через healthcheck Postgres перейдёт в статус `healthy`.
5. Запустится `teacher-service`, Liquibase применит миграции, создаст таблицы `teacher` и `teacher_status`, Spring Boot поднимется на порту **8082**.
6. Запустится `report-service` на порту **8083**, подключится к той же БД.
7. Запустится `gateway` на порту **8080**, зарегистрирует маршруты.

Признак успешного запуска в логах:

```
teacher-service | ... Started TeacherServiceApplication in X seconds
report-service  | ... Started ReportServiceApplication  in X seconds
gateway         | ... Started GatewayApplication        in X seconds
```

Остановить:

```bash
docker compose down
```

Остановить с удалением данных:

```bash
docker compose down -v
```

### Вариант 2. Локально (без Docker)

Требования:

- Java 21 (проверить: `java -version`).
- Docker — только для Postgres.

Перед запуском скопируйте шаблон:

```bash
cp .env.example .env
```

1. Поднимите Postgres в контейнере, но **не** поднимайте сервисы:

   ```bash
   docker compose up -d postgres
   ```

   Postgres будет доступен на `localhost:5434`, БД `teacher_db`, пользователь `teacher`, пароль `teacher`.

2. Запустите основной сервис из IDE или из терминала:

   ```bash
   ./gradlew bootRun
   ```

3. Запустите сервис отчётов:

   ```bash
   cd report-service
   ../gradlew bootRun
   ```

4. Запустите шлюз:

   ```bash
   cd gateway
   ../gradlew bootRun
   ```

Все три приложения подключатся к `localhost:5434`. Шлюз будет доступен на `localhost:8080`.

## Swagger UI

После запуска документация API доступна по адресам:

| Сервис | URL Swagger |
|---|---|
| teacher-service | http://localhost:8082/swagger-ui.html |
| report-service | http://localhost:8083/swagger-ui.html |

Все эндпоинты сгруппированы: «Преподаватели» и «Отчёты».

## Проверка через Postman

Все запросы — **через шлюз**, базовый URL: `http://localhost:8080`.

### 1. Принять преподавателя

**POST** `http://localhost:8080/api/teachers`  
Headers: `Content-Type: application/json`

```json
{
  "fullName": "Иванов Иван Иванович",
  "position": "Доцент"
}
```

Ожидаем `201 Created`.

### 2. Список преподавателей

**GET** `http://localhost:8080/api/teachers?page=0&size=20`

### 3. Уволить

**POST** `http://localhost:8080/api/teachers/1/fire`

```json
{ "reason": "По собственному желанию" }
```

### 4. Назначить предмет

**POST** `http://localhost:8080/api/teachers/1/assign-subject`

```json
{ "subject": "Математический анализ" }
```

### 5. Отчёт: список преподавателей

**GET** `http://localhost:8080/api/reports/teachers`

Вернёт JSON-массив со всеми преподавателями.

### 6. Отчёт: сводка

**GET** `http://localhost:8080/api/reports/summary`

```json
{
  "workingCount": 1,
  "firedCount": 1
}
```

## Вывод

В рамках ЛР2 поднят второй сервис `report-service`, который работает с той же БД и формирует отчёт по преподавателям. Добавлен шлюз на Spring Cloud Gateway, обеспечивающий единый вход ко всем сервисам. Все сервисы и БД упакованы в Docker и запускаются одной командой `docker compose up --build` в общей сети `teacher-net`. Работоспособность проверена через Postman обращениями через шлюз на порт `8080`. Документация API доступна через Swagger UI каждого сервиса.