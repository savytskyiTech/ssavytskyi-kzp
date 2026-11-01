# Початкова підготовка

## Перевірений стан на 2026-10-28

| Компонент | Результат |
| --- | --- |
| Платформа | macOS, ARM64 |
| Git | `2.54.0 (Apple Git-157)` |
| Автор комітів Git | Ім'я та email налаштовано |
| VS Code | Застосунок присутній; Java-розширення ще не перевірені |
| JDK | `java -version` повідомляє, що Java Runtime не знайдено; стандартний каталог JDK порожній |
| Maven | Команда `mvn` не знайдена в PATH |
| Maven Wrapper | Додано офіційні scripts 3.3.4 у `LAB_01/`; Maven 3.9.11 зафіксовано з SHA-256 |
| GitHub | Створено Public-репозиторій `savytskyiTech/ssavytskyi-kzp`; гілки завантажено через наявний SSH-доступ |
| GitHub Actions | Додано `.github/workflows/lab01-ci.yml`: Temurin JDK 21 на Ubuntu, Windows і macOS |

Локальне завантаження JDK 21 через GitHub Releases обірвалося. JDK поки не
підготовлена: локальні Java-тести не виконано. Перевірка збірки виконується
на runners GitHub, де `setup-java` встановлює JDK окремо на кожній ОС.
Після встановлення JDK локально можна використовувати готовий Wrapper.

[Перший запуск CI](https://github.com/savytskyiTech/ssavytskyi-kzp/actions/runs/40777272702)
успішний на всіх трьох ОС: чотири тести, SpotBugs, пакування та запуск JAR.

## Наступний технічний крок: JDK 21

Для цієї машини підходить **Eclipse Temurin JDK 21, macOS, aarch64**.
Офіційне джерело: [Adoptium](https://adoptium.net/temurin/releases/?version=21&os=mac&arch=aarch64).
Потрібна саме JDK, оскільки для роботи необхідні і `java`, і `javac`, і `jar`.

Після встановлення відкрити новий термінал і перевірити:

```sh
java --version
javac --version
jar --version
git --version
```

Очікування: перші три команди показують JDK 21, Git показує встановлену версію.
Фактичні результати зберегти в `LAB_01/REPORT.md`.

## GitHub

Створено [ssavytskyi-kzp](https://github.com/savytskyiTech/ssavytskyi-kzp)
в акаунті `savytskyiTech`. Студент явно підтвердив видимість Public.
Гілки `main` і `codex/lab-01` опубліковано, локальні гілки відстежують їх.
`origin` використовує наявну SSH-авторизацію:

Перевірка підключення та відстеження гілок:

```sh
git remote -v
git branch -vv
```

Очікувана адреса `origin`: `git@github.com:savytskyiTech/ssavytskyi-kzp.git`.
HTTPS-push локально не авторизований; нових ключів чи токенів не створювали.
Підключення GitHub повернуло 403 при спробі запису, тому початкові Issues
створено через браузер, а файли завантажено через SSH.

- [#1 — Налаштування лабораторної роботи №1](https://github.com/savytskyiTech/ssavytskyi-kzp/issues/1).
- [#2 — Maven, Wrapper, тести та JAR](https://github.com/savytskyiTech/ssavytskyi-kzp/issues/2).

## Codex замість Copilot

Загальні правила зберігаються в `AGENTS.md`, ролі та промпти — в `ai/`.
Роль можна вибрати запитом «Прочитай ai/devops.md і дій у цій ролі».
Це Markdown-інструкції для поточного чату, а не встановлені розширення Copilot.

Файл `AGENTS.md` підтримується Codex як інструкції проєкту:
[офіційна документація OpenAI](https://learn.chatgpt.com/docs/agent-configuration/agents-md).
Розділ про академічну доброчесність заповнюється фактичними діями Codex.
