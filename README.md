# Лабораторная работа №1. Основной сервис

**Дисциплина:** Распределённые вычисления и приложения (DCA)  
**Студент:** Калимуллин Артур Рамилевич  
**Группа:** ПИБД-41  
**Вариант:** 14 — Учёт преподавателей ВУЗа

## Предметная область

Ведётся учёт преподавателей ВУЗа. Реализованы операции:

- **Принять человека на должность** — создать запись преподавателя со статусом `WORKING`.
- **Уволить по определённой причине** — перевести преподавателя в статус `FIRED`, указав причину и время увольнения (время выставляется автоматически).
- **Назначить предмет для чтения** — указать предмет, который читает преподаватель.
- **Сформировать отчёт** — сколько преподавателей работает и сколько уволено.

## Структура проекта

| Путь | Что это |
|------|---------|
| `src/main/java/ru/ulstu/teacher_service/` | Исходный код сервиса (Spring Boot 3, Gradle): domain, repository, service, web, dto. |
| `src/main/resources/db/changelog/` | Миграции Liquibase: создание таблицы teacher. |
| `src/main/resources/application.yaml` | Конфигурация Spring: datasource, JPA, Liquibase, порт. |
| `build.gradle` | Зависимости и плагины Gradle. |
| `Dockerfile` | Многоступенчатая сборка docker-образа сервиса. |
| `docker-compose.yml` | Поднимает PostgreSQL и сервис в одной docker-сети `teacher-net`. |
| `.dockerignore` | Исключения для контекста сборки docker-образа. |

## Модель данных

В базе данных **одна таблица** — `teacher`. Служебные таблицы `databasechangelog` и `databasechangeloglock` создаются автоматически самим Liquibase и не считаются таблицами предметной области.

| Колонка | Тип | Ограничения | Описание |
|---|---|---|---|
| `id` | BIGINT | PK, autoincrement | Идентификатор |
| `full_name` | VARCHAR(255) | NOT NULL | ФИО преподавателя |
| `position` | VARCHAR(128) | NOT NULL | Должность |
| `status` | VARCHAR(16) | NOT NULL | `WORKING` или `FIRED` |
| `fire_reason` | VARCHAR(255) | NULL | Причина увольнения |
| `subject` | VARCHAR(255) | NULL | Читаемый предмет |
| `hired_at` | TIMESTAMPTZ | NULL | Дата приёма на работу |
| `fired_at` | TIMESTAMPTZ | NULL | Дата увольнения |
| `created_at` | TIMESTAMPTZ | NOT NULL | Дата создания записи |

## REST API

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

## Запуск

### Вариант 1. Через Docker

Требования: Установлен Docker Desktop.

Запуск:

```bash
docker compose up --build
```

Что произойдёт:

1. Docker скачает образы `postgres:16`, `eclipse-temurin:21-jdk`, `eclipse-temurin:21-jre`.
2. Соберётся образ `teacher-service` — Gradle внутри контейнера соберёт `bootJar`.
3. Поднимется контейнер `teacher-postgres`, в нём создастся БД `teacher_db`.
4. Через healthcheck Postgres перейдёт в статус `healthy`.
5. Запустится контейнер `teacher-service`, Liquibase применит миграции, создаст таблицу `teacher`.
6. Spring Boot поднимется на порту **8082**.

Признак успешного запуска в логах:

```
teacher-service | ... Started TeacherServiceApplication in X seconds
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

1. Поднимите Postgres в контейнере, но **не** поднимайте сервис:

   ```bash
   docker compose up -d postgres
   ```

   Postgres будет доступен на `localhost:5434`, БД `teacher_db`, пользователь `teacher`, пароль `teacher`.

2. Запустите приложение из IDE или из терминала:

   ```bash
   ./gradlew bootRun
   ```

Приложение подключится к `localhost:5434`.

## Swagger UI

После запуска документация API доступна по адресу:

**http://localhost:8082/swagger-ui.html**

Все эндпоинты сгруппированы в разделе **«Преподаватели»**. Каждый можно выполнить прямо из браузера: развернуть → **Try it out** → заполнить тело/параметры → **Execute**.

## Вывод

Разработан REST-сервис на Spring Boot для учёта преподавателей ВУЗа. Реализованы операции приёма на работу, увольнения по причине, назначения предмета и формирования отчёта. Структура БД создаётся автоматически миграциями Liquibase при старте приложения. Сервис и СУБД PostgreSQL упакованы в Docker и запускаются одной командой `docker compose up --build` в изолированной сети. Документация API доступна через Swagger UI.