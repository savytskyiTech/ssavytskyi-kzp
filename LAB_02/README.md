# Лабораторна робота № 2 — Ветклініка

**Варіант 21.** Класи, record та інкапсуляція в Java-проєкті.
Предметну частину програми з [`LAB_01`](../LAB_01/) перебудовано: замість масивів рядків
і масиву `double[]` тепер використовуються клас-сутність `VetVisit` і record `PriceUrgency`.
Вхідний CSV, правила валідації, показники та формат звіту не змінилися.

## Що змінилося порівняно з LAB_01

```text
LAB_01: рядок → String[] (поля за індексами) → double[] (показники за індексами) → звіт
LAB_02: рядок → VetVisit.fromCsv → List<VetVisit> → PriceUrgency → той самий звіт
```

| Що | LAB_01 | LAB_02 |
| --- | --- | --- |
| Один запис | `String[]`, `record[3]` — ціна, `record[4]` — ургентність | об'єкт `VetVisit` з `getPrice()`, `isUrgent()` |
| Перевірка | `Main.validateLine` | конструктор `VetVisit` (інваріанти) і `VetVisit.fromCsv` (структура й типи) |
| Набір даних | `List<String[]>` | `List<VetVisit>` |
| Показники | `double[] {count, average, revenue, urgent}` | `record PriceUrgency(count, averagePrice, revenue, urgentCount)` |
| Ціна в обчисленні | `Double.parseDouble(record[3])` повторно | число вже розібране один раз у `fromCsv` |
| Звіт | `formatReport(double[])` | `formatReport(PriceUrgency)`, текст ідентичний |

## Структура коду

```text
src/main/java/ua/lpnu/kzp/
├── Main.java          # CLI, читання файла, обчислення, форматування й запис звіту
├── VetVisit.java      # клас-сутність: private final поля, конструктор, fromCsv, геттери, toString
└── PriceUrgency.java  # record із підсумковими показниками
src/test/java/ua/lpnu/kzp/
├── MainTest.java          # тести LAB_01 для читання, показників, звіту й CLI
├── VetVisitTest.java      # конструктор, fromCsv (зокрема всі випадки валідації з LAB_01), toString
└── PriceUrgencyTest.java  # компоненти, рівність і перевірки record
```

### `VetVisit` — клас-сутність

| Поле | Тип | Інваріант |
| --- | --- | --- |
| `animal` | `String` | не `null`, не порожнє й не лише пробіли |
| `species` | `String` | не `null`, не порожнє й не лише пробіли |
| `visitType` | `String` | не `null`, не порожнє й не лише пробіли |
| `price` | `double` | скінченне (`NaN` і ∞ заборонені), `>= 0`; нуль допустимий |
| `urgent` | `boolean` | у CSV — рівно літерал `true` або `false` |

- Усі поля `private final`, сетерів немає: після створення візит не змінюється, тому
  перевірений стан не можна зіпсувати пізніше. Клас `final` — від нього не можна
  успадкуватися й обійти перевірки.
- **Конструктор** перевіряє інваріанти до присвоєння полів. `null` дає
  `NullPointerException` (помилка програміста), порожній текст і недопустима ціна —
  `IllegalArgumentException` (хибні дані).
- **`fromCsv(String line)`** перевіряє лише структуру й типи: порожній рядок, рівно 5 полів
  (`split(";", -1)`), число з крапкою в `price`, літерал `true`/`false` в `urgent`.
  Потім викликає конструктор, тому обійти інваріанти через фабрику неможливо, а правила
  не дублюються.
- **Геттери**: `getAnimal()`, `getSpecies()`, `getVisitType()`, `getPrice()`, `isUrgent()`.
- **`toString()`**: `Мурчик (Кіт), Огляд: 450.00 грн, ургентний: ні` — ціна форматується
  через `Locale.ROOT`. Для звіту та CSV не використовується.

### `PriceUrgency` — record підсумку

`record PriceUrgency(int count, double averagePrice, double revenue, int urgentCount)`.
Компілятор створює конструктор, методи доступу (`count()`, `revenue()` …), `equals`,
`hashCode` і `toString`. Компактний конструктор відхиляє від'ємні лічильники,
`urgentCount > count`, від'ємні, `NaN` чи нескінченні суми. Компоненти — примітиви, тож
record повністю незмінний.

## Формат даних і показники

Формат рядка `data/input.csv` (UTF-8, без заголовка, десяткова крапка) — як у `LAB_01`:

```text
animal;species;visitType;price;urgent
```

Показники: кількість коректних записів, середня вартість (`0.00` для порожнього набору),
загальний виторг, кількість ургентних візитів. Хибний рядок дає в stderr
`Рядок N: причина` і не змінює показників; обробка решти рядків триває.

Очікуваний результат на `data/input.csv`:

```text
Коректних записів: 3
Середня вартість: 600.00 грн
Загальний виторг: 1800.00 грн
Ургентних візитів: 1
```

```text
Рядок 4: порожнє поле animal
Рядок 5: price має бути скінченним невід'ємним числом
```

## Збірка та запуск

Потрібна JDK 21. Maven Wrapper (Maven 3.9.11) успадковано з `LAB_01`.
Усі команди — з каталогу `LAB_02/`.

macOS / Ubuntu:

```sh
./mvnw test
./mvnw verify
./mvnw package
java -jar target/lab02-2.0.0-all.jar
java -jar target/lab02-2.0.0-all.jar --help
java -jar target/lab02-2.0.0-all.jar --version     # 2.0.0+local
java -jar target/lab02-2.0.0-all.jar --input data/input.csv --output output/report.txt
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify
.\mvnw.cmd package
java -jar target/lab02-2.0.0-all.jar --help
```

`verify` запускає 83 тестові випадки JUnit 5 і SpotBugs, `package` створює
виконуваний JAR через Shade.

## GitHub Actions

Workflow `.github/workflows/lab02-ci.yml` (**Lab 02 CI**) — копія `lab01-ci.yml`
для теки `LAB_02/`: `clean verify` на `ubuntu-latest`, `windows-latest`, `macos-latest`
(Temurin JDK 21), запуск JAR без аргументів, з `--help` і `--version`, перевірка номера збірки
та артефакти `lab02-<os>-java21` і `lab02-reports-<os>` (7 днів).
Workflow `LAB_01` не змінено.

Звіт виконання: [REPORT.md](REPORT.md). План і Issues: [PLAN.md](PLAN.md).
