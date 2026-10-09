# Лабораторная работа №4. Файловое хранилище

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
- **Сохранить отчёт в файловое хранилище** — данные отчёта формируются в CSV и через `RestTemplate` отправляются в отдельный сервис, который сохраняет файл на диск.

## Цель работы

Добавить файловое хранилище и сервис, работающий с ним. Данные отчёта сохраняются в CSV-файл и передаются сервису для сохранения в файловое хранилище также по `RestTemplate`.

## Архитектура

```
Postman → gateway :8080
            ├── /api/teachers/**, /api/reports/** → teacher-service :8082
            │                                          └── RestTemplate -> report-service :8083
            │                                                                └── RestTemplate → file-service :8084
            └── /api/files/**                     → file-service :8084
```

| Компонент | Порт | Назначение |
|------|------|---------|
| `gateway` | 8080 | Шлюз на Spring Cloud Gateway. Публикует маршруты `/api/teachers/**`, `/api/reports/**` → `teacher-service` и `/api/files/**` → `file-service`. |
| `teacher-service` | 8082 | Основной сервис (ЛР1): CRUD, владелец схемы БД (Liquibase). Проксирует `/api/reports/**` в `report-service`. |
| `report-service` | 8083 (внутри сети) | Второй сервис (ЛР2): отчёты. Получает данные из `teacher-service` через `RestTemplate`, формирует CSV и отправляет его в `file-service`. |
| `file-service` | 8084 (внутри сети) | Третий сервис (ЛР4): файловое хранилище. Сохраняет файлы на диск в том `file-storage-data`, отдаёт их по REST. |
| `postgres` | 5434 (наружу), 5432 (внутри) | Общая БД `teacher_db`. |

## Структура проекта

| Путь | Что это |
|------|---------|
| `src/main/java/ru/ulstu/teacher_service/` | Исходный код основного сервиса (Spring Boot 3, Gradle): domain, repository, service, web, dto, config. |
| `src/main/java/ru/ulstu/teacher_service/config/RestTemplateConfig.java` | Бин `RestTemplate` для проксирования запросов. |
| `src/main/java/ru/ulstu/teacher_service/web/ReportProxyController.java` | Прокси-контроллер: перенаправляет `/api/reports/**` в `report-service`. |
| `src/main/resources/db/changelog/` | Миграции Liquibase: создание таблиц `teacher`, `teacher_status`. |
| `src/main/resources/application.yaml` | Конфигурация teacher-service: datasource, JPA, Liquibase, порт, URL report-service. |
| `report-service/src/main/java/ru/ulstu/report_service/` | Исходный код сервиса отчётов (Spring Boot 3): domain, service, web, config. |
| `report-service/src/main/java/ru/ulstu/report_service/config/RestTemplateConfig.java` | Бин `RestTemplate` для запросов к teacher-service и file-service. |
| `report-service/src/main/java/ru/ulstu/report_service/service/ReportService.java` | Получает список преподавателей из teacher-service, строит отчёты, формирует CSV. |
| `report-service/src/main/java/ru/ulstu/report_service/service/FileStorageClient.java` | Клиент `file-service` через `RestTemplate`: отправляет CSV в файловое хранилище. |
| `report-service/src/main/resources/application.yaml` | Конфигурация report-service: порт, URL teacher-service, URL file-service. |
| `file-service/src/main/java/ru/ulstu/file_service/` | Исходный код сервиса файлового хранилища (Spring Boot 3): domain, service, web, config. |
| `file-service/src/main/java/ru/ulstu/file_service/service/FileStorageService.java` | Читает/пишет/удаляет файлы в каталоге `storage.path`. |
| `file-service/src/main/java/ru/ulstu/file_service/web/FileController.java` | REST API `/api/files`: загрузка, список, скачивание, удаление. |
| `file-service/src/main/resources/application.yaml` | Конфигурация file-service: порт, путь к хранилищу. |
| `gateway/src/main/java/ru/ulstu/gateway/` | Класс запуска шлюза на Spring Cloud Gateway. |
| `gateway/src/main/resources/application.yaml` | Маршруты шлюза и CORS. |
| `build.gradle` | Зависимости и плагины Gradle основного сервиса. |
| `report-service/build.gradle` | Зависимости и плагины Gradle сервиса отчётов (без JDBC и PostgreSQL). |
| `file-service/build.gradle` | Зависимости и плагины Gradle сервиса файлового хранилища (без JDBC). |
| `gateway/build.gradle` | Зависимости и плагины Gradle шлюза. |
| `Dockerfile` | Многоступенчатая сборка docker-образа teacher-service. |
| `report-service/Dockerfile` | Многоступенчатая сборка docker-образа report-service. |
| `file-service/Dockerfile` | Многоступенчатая сборка docker-образа file-service. |
| `gateway/Dockerfile` | Многоступенчатая сборка docker-образа gateway. |
| `docker-compose.yml` | Поднимает PostgreSQL, три сервиса, file-service и стек ELK в одной docker-сети `teacher-net`. |
| `.dockerignore` | Исключения для контекста сборки docker-образа. |
| `.env` | Параметры БД и портов. |

## Модель данных

В базе данных две таблицы предметной области — `teacher` и `teacher_status` (справочник статусов). Служебные таблицы `databasechangelog` и `databasechangeloglock` создаются автоматически самим Liquibase и не считаются таблицами предметной области. Структуру создаёт `teacher-service` через миграции Liquibase; `report-service` и `file-service` работают с этими данными только косвенно — через API других сервисов.

### Таблица `teacher`

| Колонка | Тип | Ограничения | Описание |
|---|---|---|---|
| `id` | BIGINT | PK, autoincrement | Идентификатор |
| `full_name` | VARCHAR(255) | NOT NULL | ФИО преподавателя |
| `position` | VARCHAR(128) | NOT NULL | Должность |
| `status_id` | BIGINT | FK -> `teacher_status.id`, NOT NULL | Ссылка на статус |
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

## Файловое хранилище

Файлы физически хранятся на диске в каталоге `${storage.path}` (в Docker — `/app/storage`, наружу смонтирован как том `file-storage-data`). Имя файла на диске имеет вид `<uuid>___<original-name>` — это позволяет однозначно находить файл по `id`, при этом сохраняя исходное имя.

## REST API

### Шлюз — `gateway`

Единая точка входа. Публикует маршруты в `teacher-service` и `file-service`.

| Метод | URL | Куда проксируется |
|---|---|---|
| Любой | `/api/teachers/**` | `http://teacher-service:8082` |
| Любой | `/api/reports/**` | `http://teacher-service:8082` |
| Любой | `/api/files/**` | `http://file-service:8084` |

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
| GET | `/api/teachers/all` | Получить всех преподавателей (для внутреннего использования) |

Также `teacher-service` проксирует запросы к сервису отчётов:

| Метод | URL | Описание |
|---|---|---|
| GET | `/api/reports/teachers` | Проксирует в `report-service` |
| GET | `/api/reports/summary` | Проксирует в `report-service` |
| POST | `/api/reports/teachers/csv/save` | Проксирует в `report-service` |

### Сервис отчётов — `report-service`

Базовый путь: `/api/reports`. Доступен только внутри docker-сети `teacher-net`. Внешние клиенты обращаются к нему через `teacher-service`.

| Метод | URL | Описание |
|---|---|---|
| GET | `/api/reports/teachers` | Список преподавателей для отчёта |
| GET | `/api/reports/summary` | Сводка: работает / уволено |
| POST | `/api/reports/teachers/csv/save` | Сформировать CSV и сохранить в `file-service` через `RestTemplate` |

Пример ответа `GET /api/reports/summary`:

```json
{
  "workingCount": 3,
  "firedCount": 1
}
```

### Сервис файлового хранилища — `file-service`

Базовый путь: `/api/files`. Доступен снаружи только через шлюз (`/api/files/**`). В Docker порт 8084 наружу не публикуется.

| Метод | URL | Описание |
|---|---|---|
| POST | `/api/files` | Загрузить файл |
| GET | `/api/files` | Список файлов |
| GET | `/api/files/{id}` | Скачать файл |
| DELETE | `/api/files/{id}` | Удалить файл |

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

1. Docker скачает образы `postgres:16`, `eclipse-temurin:21-jdk`, `eclipse-temurin:21-jre`, `elasticsearch`, `logstash`, `kibana`.
2. Соберутся четыре образа: `teacher-service`, `report-service`, `file-service`, `gateway` — Gradle внутри контейнеров соберёт `bootJar`.
3. Поднимется контейнер `teacher-postgres`, в нём создастся БД `teacher_db`.
4. Через healthcheck Postgres перейдёт в статус `healthy`.
5. Запустится `teacher-service`, Liquibase применит миграции, создаст таблицы `teacher` и `teacher_status`, Spring Boot поднимется на порту **8082**.
6. Запустится `report-service` на порту **8083**, подключится к `teacher-service` и `file-service`.
7. Запустится `file-service` на порту **8084**, создаст каталог `/app/storage` (том `file-storage-data`).
8. Запустится `gateway` на порту **8080**, зарегистрирует маршруты.

Признак успешного запуска в логах:

```
teacher-service | ... Started TeacherServiceApplication in X seconds
report-service  | ... Started ReportServiceApplication  in X seconds
file-service    | ... Started FileServiceApplication    in X seconds
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
4. Запустите сервис файлового хранилища:

   ```bash
   cd file-service
   ../gradlew bootRun
   ```

5. Запустите шлюз:

   ```bash
   cd gateway
   ../gradlew bootRun
   ```

Для локального запуска используются значения по умолчанию:
report.service.url=http://localhost:8083, teacher.service.url=http://localhost:8082, file.service.url=http://localhost:8084.

## Swagger UI

После запуска документация API доступна по адресам:

| Сервис | URL Swagger | Примечание |
|---|---|---|
| teacher-service | http://localhost:8082/swagger-ui.html | Доступен всегда |
| report-service | http://localhost:8083/swagger-ui.html | Только при локальном запуске. В Docker порт 8083 не публикуется наружу. |
| file-service | http://localhost:8084/swagger-ui.html | Только при локальном запуске. В Docker порт 8084 не публикуется наружу. |

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

### 3. Назначить предмет

**POST** `http://localhost:8080/api/teachers/1/assign-subject`

```json
{ "subject": "Математический анализ" }
```

### 4. Уволить

**POST** `http://localhost:8080/api/teachers/1/fire`

```json
{ "reason": "По собственному желанию" }
```

### 5. Отчёт: список преподавателей

**GET** `http://localhost:8080/api/reports/teachers`

Запрос проходит через шлюз -> teacher-service -> report-service.
Вернёт JSON-массив со всеми преподавателями.

### 6. Отчёт: сводка

**GET** `http://localhost:8080/api/reports/summary`

```json
{
  "workingCount": 0,
  "firedCount": 1
}
```

### 7. Сохранить CSV-отчёт в файловое хранилище

**POST** `http://localhost:8080/api/reports/teachers/csv/save`

Цепочка вызовов: gateway -> teacher-service -> report-service -> file-service.

`report-service` получает список преподавателей из `teacher-service`, формирует CSV и по `RestTemplate` отправляет его в `file-service`. В ответ приходят метаданные сохранённого файла:

```json
{
  "id": "b3d1e7b0-9a3f-4a7d-8f0d-1a3e5c7e4f12",
  "originalName": "teachers-report-2026-10-09T12-34-56.789Z.csv",
  "size": 512,
  "storedAt": "2026-10-09T12:34:56.789Z"
}
```

### 8. Список файлов в хранилище

**GET** `http://localhost:8080/api/files`

Вернёт JSON-массив с метаданными всех сохранённых файлов.

### 9. Скачать файл из хранилища

**GET** `http://localhost:8080/api/files/{id}`

Вернёт содержимое файла (`Content-Disposition: attachment`). CSV открывается в Excel/блокноте и содержит заголовок и строки преподавателей.

### 10. Удалить файл из хранилища

**DELETE** `http://localhost:8080/api/files/{id}`

Ожидаем `204 No Content`.

### 11. Проверка отсутствия прямого доступа

Убедитесь, что `report-service` недоступен напрямую:

**GET** `http://localhost:8083/api/reports/summary`
**GET** `http://localhost:8084/api/files`

В Docker-режиме должен вернуться отказ в соединении, так как порты 8083 и 8084 не публикуются наружу.

## Вывод

В рамках ЛР4 в проект добавлено файловое хранилище в виде отдельного микросервиса `file-service`. Данные отчёта формируются в `report-service` в формате CSV и через `RestTemplate` отправляются в `file-service`, который сохраняет их на диск в docker-томе `file-storage-data`. Внешний клиент работает с файловым хранилищем только через шлюз (`/api/files/**`); прямая адресация к `file-service`, `report-service` и `teacher-service` извне невозможна — их порты не публикуются в docker-compose. Все сервисы и БД запускаются одной командой `docker compose up --build` в общей сети `teacher-net`. Работоспособность проверена через Postman обращениями через шлюз на порт `8080`.