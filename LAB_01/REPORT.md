# Звіт до лабораторної роботи № 1

## Тема і варіант

- Предметна область: ветеринарна клініка — візити тварин (розгортання Java-проєкту та базова обробка даних).
- Номер варіанта: 21. Запис: `animal;species;visitType;price;urgent`. Показники: кількість коректних
  записів, середня вартість, загальний виторг, кількість ургентних візитів.
- Операційна система: macOS (ARM64) для розробки; CI додатково перевіряє Ubuntu і Windows.
- Студент: Савицький Сергій Васильович, група **КІ-304**.
- Репозиторій: [savytskyiTech/ssavytskyi-kzp](https://github.com/savytskyiTech/ssavytskyi-kzp).

## Реалізація

Клас `ua.lpnu.kzp.Main` (Java 21) читає `data/input.csv` (UTF-8, `;`, без заголовка), перевіряє
кожен рядок, обчислює чотири показники та формує один текст звіту для консолі й файла `output/report.txt`.

- Валідація (`validateLine`): рівно 5 полів (`split(";", -1)`); `animal`, `species`, `visitType`
  непорожні; `price` — скінченне невід'ємне число з крапкою; `urgent` — рівно `true` або `false`
  (без `Boolean.parseBoolean`). Хибний рядок дає `Рядок N: причина` у stderr, обробка триває.
- Показники (`calculateStatistics`): кількість, середня (для порожнього набору `0.00`), виторг, ургентні.
- Звіт (`formatReport`, `writeReport`): `Locale.ROOT`, `%.2f грн`, `%n`, запис у UTF-8.
- CLI: `--help`, `--version`, `--input <файл>`, `--output <файл>`; коди завершення 0 / 1 / 2.
- `--version` читає `version.properties` (Maven resource filtering) і виводить `<версія>+<збірка>`:
  `1.0.0+local` локально, `1.0.0+<run_number>` у CI. Основна версія — номер лабораторної
  (`LAB_01` → `1.0.0`, `LAB_02` → `2.0.0`).
- Читання, перевірка, обчислення й форматування — окремі методи: у `LAB_02` рядки й масиви замінять класи.

Інфраструктура: Maven POM (`ua.lpnu.kzp.ssavytskyi:lab01:1.0.0`, Java 21, JUnit 5.12.2, Surefire 3.5.3,
SpotBugs 4.9.7.0 у фазі `verify`, Shade 3.6.0 → `lab01-1.0.0-all.jar`), Maven Wrapper 3.3.4
(Maven 3.9.11), workflow `.github/workflows/lab01-ci.yml`, шаблони Issues і PR з Додатка А методички.

## Збірка і запуск

Усі команди — з каталогу `LAB_01/`. Потрібна JDK 21.

```sh
./mvnw --batch-mode --no-transfer-progress clean verify   # Windows PowerShell: .\mvnw.cmd ...
java -jar target/lab01-1.0.0-all.jar
java -jar target/lab01-1.0.0-all.jar --help
java -jar target/lab01-1.0.0-all.jar --version
java -jar target/lab01-1.0.0-all.jar --input data/input.csv --output output/report.txt
```

Приклад на `data/input.csv` (3 коректні й 2 хибні рядки):

```text
Коректних записів: 3
Середня вартість: 600.00 грн
Загальний виторг: 1800.00 грн
Ургентних візитів: 1
```

Повідомлення в stderr (рядки пропущено, решту оброблено):

```text
Рядок 4: порожнє поле animal
Рядок 5: price має бути скінченним невід'ємним числом
```

`--version` локально: `1.0.0+local`. Однакові `--input` і `--output` дають
`Помилка CLI: вхідний файл і файл звіту мають бути різними` і код 2.

## GitHub Issues і Pull Request

| Issue | Зміна | Коміт / PR | Стан |
| --- | --- | --- | --- |
| № 1 `infra` (Issue видалено) | Структура, правила, план, дані | `aa2c0c1`, `38b16f3` | Історичний етап (`codex/lab-01`) |
| № 2 `infra` (Issue видалено) | Maven, Wrapper, тести, аналізатор, JAR, CI | `a76741c` | Історичний CI запуск № 1 успішний |
| [#3](https://github.com/savytskyiTech/ssavytskyi-kzp/issues/3) `infra` | `AGENTS.md`, агенти, шаблони Issues і PR, тригер CI | `7e9a110` | Опубліковано в `java/infra` |
| [#4](https://github.com/savytskyiTech/ssavytskyi-kzp/issues/4) `feature` | Читання й валідація записів | `0223a0c` | Перевірено локально й у CI |
| [#5](https://github.com/savytskyiTech/ssavytskyi-kzp/issues/5) `feature` | Показники та звіт у консоль і файл | `0223a0c` | Перевірено локально й у CI |
| [#6](https://github.com/savytskyiTech/ssavytskyi-kzp/issues/6) `feature` | Тести JUnit 5 для парсера й показників | `0223a0c` | 51 випадок проходить на трьох ОС |
| [#7](https://github.com/savytskyiTech/ssavytskyi-kzp/issues/7) `docs` | README, javadoc, REPORT, звіт для ВНС | `TODO` | У роботі |
| [#8](https://github.com/savytskyiTech/ssavytskyi-kzp/issues/8) `infra` | Рівень 3: версія, тег, докази CI | `0223a0c` | CI успішний; тег ще потрібен |

Гілки: `main` (захищена), `java/infra` (поточна робота).
Pull Request `java/infra` → `main` з `Closes #N`: `TODO` (ще не створено).
Окремі bug Issues для виявлених проблем версії, довідки й кодування не створювалися.

## Тестування, CI і артефакти

Локально, macOS ARM64, OpenJDK 21.0.13.0.3, 2026-11-01:

- `./mvnw --batch-mode --no-transfer-progress clean verify`: 51 тестовий випадок, 0 помилок, SpotBugs — 0 дефектів;
- `-Dbuild.number=42 verify`: JAR вивів `1.0.0+42`;
- `package` і запуск JAR: вивід збігається з розділом «Збірка і запуск»;
- регресійний тест у JVM з ASCII за замовчуванням: український текст у консолі й файлі коректний (UTF-8).

`MainTest` (JUnit 5, `@ParameterizedTest`, `@TempDir`) перевіряє валідацію полів, показники, звіт,
файлові операції та CLI, зокрема `--version`.

CI [Lab 01 CI](https://github.com/savytskyiTech/ssavytskyi-kzp/actions/workflows/lab01-ci.yml),
`clean verify` на трьох ОС. Перевірений запуск — № 3, коміт `0223a0c`
(поточна обробка CSV, 51 тестовий випадок, версія `1.0.0+3`):
[run](https://github.com/savytskyiTech/ssavytskyi-kzp/actions/runs/41311971239).

| Runner | JUnit | SpotBugs | Запуск JAR / версія | Артефакт |
| --- | --- | --- | --- | --- |
| `ubuntu-latest` | 51 випадок, 0 помилок | 0 дефектів | успішно, `1.0.0+3` | [JAR](https://github.com/savytskyiTech/ssavytskyi-kzp/actions/runs/41311971239/artifacts/13085880633) |
| `windows-latest` | 51 випадок, 0 помилок | 0 дефектів | успішно, `1.0.0+3` | [JAR](https://github.com/savytskyiTech/ssavytskyi-kzp/actions/runs/41311971239/artifacts/13086180787) |
| `macos-latest` | 51 випадок, 0 помилок | 0 дефектів | успішно, `1.0.0+3` | [JAR](https://github.com/savytskyiTech/ssavytskyi-kzp/actions/runs/41311971239/artifacts/13085746709) |

Артефакти зберігаються 7 днів (`lab01-<os>-java21` — JAR і результати запуску, `lab01-reports-<os>` —
звіти JUnit і SpotBugs). Переглянуто логи всіх трьох jobs: пройшли тести, SpotBugs,
запуск упакованої програми й перевірка версії; артефакти доступні.
Git-тег `v1.0.0`: (після злиття в `main`).

## Академічна доброчесність

Цей текст підготовлено з використанням OpenAI Codex і Claude Code.
Модель Claude Code, зазначена студентом: `claude-sonnet-5-5`;
точну модель поточної сесії Codex не підтверджено.

**Ваш внесок:** Під час роботи Codex і Claude надавали пояснення, шаблони та готові фрагменти коду. Я переписував і адаптував запропонований код до вимог лабораторної роботи. Мій особистий внесок — перероблення цих фрагментів і прийняття рішень щодо реалізації; участь ШІ у створенні початкового коду зазначено в цьому звіті.

**Внесок ШІ:**

- Claude Code: об'єднав правила в `AGENTS.md`, додав `CLAUDE.md` і шість ролей у `.claude/agents/`;
  взяв шаблони Issues і PR з Додатка А; змінив тригер CI на `main` та `java/**`; підготував Issues #3–#8
  (створив їх студент через `gh`); перевірив локальні файли за вимогами рівня 3 і порадив формат
  `1.0.0+<build>`, UTF-8 та правильну команду `java -jar` у довідці.
- OpenAI Codex: правила, план, синтетичні дані,
  POM, Wrapper, workflow, стартовий CLI і чотири тести; створив Issues #1–#2. Пізніше за
  запитом замінив стартові тести перевірками валідації, виправив довідку й вивід UTF-8 та тест кодування.

**Редакційний процес:** прийнято Maven Wrapper, JUnit 5, SpotBugs, явне UTF-8 та
регресійні тести. Від формату версії без суфікса (`1.0.0`) відмовилися після звірки з рівнем 3:
повернуто `1.0.0+local` або `1.0.0+<build>`. Тести перед виправленням відтворили
помилки версії, довідки й кодування; після виправлення пройшов 51 випадок.
Локальні команди Maven, запуск JAR і перегляд CI в цьому сеансі також виконував Codex.
Самостійні перевірки студента й підтвердження власного розуміння: Я власноруч виконав збірку через Maven Wrapper і переглянув результати тестів та SpotBugs. Вручну обчислив очікувані показники для вхідного файла та порівняв їх із результатом програми. Зіставив звіт у консолі й файлі, перевірив українські символи та повідомлення про некоректні записи.

**Джерела:** методичка `LAB_01`, посібники `MAVEN_ACTIONS_GUIDE` і `COPILOT_GUIDE`
(матеріали курсу поза репозиторієм); офіційна документація:
[Maven](https://maven.apache.org/guides/getting-started/),
[Maven Wrapper](https://maven.apache.org/tools/wrapper/),
[SpotBugs Maven Plugin](https://spotbugs.github.io/spotbugs-maven-plugin/),
[GitHub Actions](https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-syntax).

**Ваша відповідальність:** студент власними словами підтверджує правдивість результатів
і що може пояснити код.

## Висновки

Створено консольну програму для варіанта 21: вона читає й перевіряє CSV, обчислює чотири показники,
формує однаковий звіт у консоль і файл, має `--help`, `--version` та виконуваний JAR. Проєкт збирається
Maven Wrapper, перевіряється JUnit 5 і SpotBugs у `verify`; стартова інфраструктура CI пройшла
на Ubuntu, Windows і macOS, включно з 51 тестовим випадком поточної реалізації.
Для завершення потрібні остаточна редакція звіту, рев'ю PR, погоджений тег і здача у ВНС.

У лабораторній роботі № 2 (`2.0.0`) цей код продовжиться: читання, валідація, обчислення й форматування
вже розділені на методи, тож масиви та рядки записів замінять власні класи, а Maven/CI-основа лишиться.
