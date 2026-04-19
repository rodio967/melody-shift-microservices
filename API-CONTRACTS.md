# Melody Shift — API Contracts

**Версия:** 1.0  
**Дата:** 20 апреля 2026  
**Проект:** Melody Shift — сервис переноса музыкальных плейлистов между платформами

## Общая информация

Все внешние запросы идут через **API Gateway** (порт 8080).  
Gateway выполняет:
- Валидацию JWT-токена
- Добавление заголовка `X-User-Id`
- Проксирование запроса в нужный микросервис

**Микросервисы:**
- **User Service** — 8081 (пользователи, аутентификация, OAuth)
- **Provider Service** — 8087 (работа с музыкальными платформами)
- **Transfer Service** — 8088 (перенос плейлистов)
- **API Gateway** — 8080

---

## 1. User Service

### Публичные эндпоинты

| Метод | Путь                  | Описание                          | Тело запроса          | Заголовки                  | Ответ                          | Авторизация |
|-------|-----------------------|-----------------------------------|-----------------------|----------------------------|--------------------------------|-------------|
| POST  | `/api/auth/register`  | Регистрация нового пользователя   | `RegisterRequest`     | -                          | `{success, message, userId}`   | Нет |
| POST  | `/api/auth/login`     | Авторизация                       | `LoginRequest`        | -                          | `AuthResponse` (JWT)           | Нет |
| GET   | `/api/auth/me`        | Информация о текущем пользователе | -                     | `Authorization: Bearer ...`| `UserInfoResponse`             | JWT |

### Внутренние эндпоинты (для других сервисов)

| Метод | Путь                                           | Описание                                | Параметры                  | Заголовки               | Ответ                |
|-------|------------------------------------------------|-----------------------------------------|----------------------------|-------------------------|----------------------|
| GET   | `/api/internal/users/{userId}/tokens`          | Получить валидный OAuth-токен           | `?platform=SPOTIFY`        | `X-Internal-Secret`     | `OAuthTokenDto`      |
| GET   | `/api/internal/users/{userId}/platforms`       | Список подключённых платформ            | -                          | `X-Internal-Secret`     | `{platforms: [...]}` |

---

## 2. Provider Service

**Базовый путь:** `/api/providers`

Все эндпоинты требуют заголовок `X-User-Id` (добавляется Gateway).

| Метод | Путь                                                      | Описание                                   | Параметры / Query          | Ответ                  |
|-------|-----------------------------------------------------------|--------------------------------------------|----------------------------|------------------------|
| GET   | `/{provider}/search`                                      | Поиск треков                               | `query`, `artist`          | `List<TrackDto>`       |
| GET   | `/{provider}/playlists/{playlistId}/tracks`               | Получить треки из плейлиста                | -                          | `List<TrackDto>`       |
| POST  | `/{provider}/playlists`                                   | Создать новый плейлист                     | `?name=...`                | `String` (playlistId)  |
| POST  | `/{provider}/playlists/{playlistId}/tracks`               | Добавить трек в плейлист                   | `?trackId=...`             | 204 No Content         |
| GET   | `/{provider}/playlists`                                   | Получить плейлисты пользователя            | -                          | `List<PlaylistDto>`    |

**Пример:**  
`GET /api/providers/spotify/search?query=Blinding&artist=The Weeknd`

---

## 3. Transfer Service

**Базовый путь:** `/api/transfers`

Все эндпоинты требуют заголовок `X-User-Id`.

| Метод | Путь                          | Описание                                           | Тело / Параметры                    | Ответ                              |
|-------|-------------------------------|----------------------------------------------------|-------------------------------------|------------------------------------|
| POST  | `/`                           | Создать перенос плейлиста                          | `CreateTransferRequest`             | `TransferResponse`                 |
| GET   | `/`                           | Список всех переносов пользователя                 | `?page=0&size=20`                   | `List<TransferSummaryDto>`         |
| GET   | `/{id}`                       | Получить детали переноса                           | -                                   | `Transfer`                         |
| GET   | `/{id}/progress`              | Получить прогресс выполнения                       | -                                   | `TransferProgressDto`              |
| POST  | `/{id}/retry`                 | Повторить неудачные треки                          | -                                   | 202 Accepted                       |
| DELETE| `/{id}`                       | Отменить перенос (только PENDING)                  | -                                   | 204 No Content                     |
| GET   | `/playlists`                  | Получить плейлисты пользователя на платформе       | `?provider=spotify`                 | `List<PlaylistDto>`                |

### CreateTransferRequest
```json
{
  "sourceProvider": "spotify",
  "targetProvider": "yandex",
  "sourcePlaylistId": "37i9dQZF1DXcBWIGoYBM5M"
}
