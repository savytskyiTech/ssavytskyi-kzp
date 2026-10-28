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
| Maven Wrapper | Буде додано на етапі Maven |
| GitHub | Підключений акаунт `savytskyiTech`; репозиторій ще не підтверджено |

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

Створити порожній репозиторій `ssavytskyi-kzp` в акаунті `savytskyiTech`.
Для здачі методичка вимагає Public. Початкові README, `.gitignore` і ліцензію
на GitHub автоматично не додавати: локальний репозиторій уже матиме свій commit.

Після створення прив'язати локальний репозиторій і завантажити гілки:

```sh
git remote add origin https://github.com/savytskyiTech/ssavytskyi-kzp.git
git push -u origin main
git push -u origin codex/lab-01
```

Ці команди виконуються після підтвердження створення віддаленого репозиторію.
Перевірити `git remote -v`, якщо `origin` уже налаштовано.

## Codex замість Copilot

Загальні правила зберігаються в `AGENTS.md`, ролі та промпти — в `ai/`.
Роль можна вибрати запитом «Прочитай ai/devops.md і дій у цій ролі».
Це Markdown-інструкції для поточного чату, а не встановлені розширення Copilot.

Файл `AGENTS.md` підтримується Codex як інструкції проєкту:
[офіційна документація OpenAI](https://learn.chatgpt.com/docs/agent-configuration/agents-md).
Розділ про академічну доброчесність заповнюється фактичними діями Codex.
