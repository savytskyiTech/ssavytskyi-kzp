# Лабораторна робота № 3 — Ветклініка

**Варіант 21.** Спадкування, інтерфейси й поліморфізм у Java-проєкті.
Клас-сутність `VetVisit` з [`LAB_02`](../LAB_02/) став абстрактним базовим типом із двома
підтипами — `RoutineVisit` (плановий візит) і `UrgentVisit` (ургентний). Категорію описує
`enum VisitKind`, поліморфна операція — підсумкова вартість `finalCost()`. Вхідний CSV і
чотири показники звіту не змінилися; додано п'ятий рядок із сумою підсумкових вартостей.

## Що змінилося порівняно з LAB_02

```text
LAB_02: рядок → VetVisit.fromCsv → List<VetVisit>              → PriceUrgency (4 показники) → звіт
LAB_03: рядок → VetVisit.fromCsv → RoutineVisit | UrgentVisit
                                 → List<VetVisit> (поліморфна) → PriceUrgency (5 показників) → звіт
```

| Що | LAB_02 | LAB_03 |
| --- | --- | --- |
| Один запис | `final class VetVisit` | `abstract class VetVisit` + підтипи `RoutineVisit`, `UrgentVisit` |
| Категорія | поле `boolean urgent` | `enum VisitKind { ROUTINE, URGENT }` з підписом і `isUrgent()` |
| Різна поведінка | немає | `finalCost()`: планова — ціна, ургентна — ціна × 1.5 |
| Створення | `new VetVisit(...)`, `fromCsv` | `VetVisit.of(..., urgent)`, `fromCsv` → потрібний підтип |
| Рівність | за посиланням (`Object.equals`) | узгоджені `equals`/`hashCode`: підтип + усі поля |
| Звіт | 4 рядки | ті самі 4 рядки + «Підсумкова вартість із надбавками» |

## Ієрархія

```text
                    VetVisit (abstract)
   поля: animal, species, visitType, price, kind : VisitKind
   спільна перевірка, fromCsv, of, equals/hashCode, toString
   abstract double finalCost()
            ▲                              ▲
   RoutineVisit (final)             UrgentVisit (final)
   kind = ROUTINE                   kind = URGENT
   price >= 0 (нуль допустимий)     price > 0 (власне правило)
   finalCost() = price              finalCost() = price × 1.5
```

- **Чому абстрактний клас, а не інтерфейс.** Підтипи мають спільний стан (5 полів) і готову
  спільну реалізацію (перевірка, `fromCsv`, `equals`, `toString`). Інтерфейс описує лише
  можливість і не зберігає поля — довелося б дублювати їх у кожному підтипі.
- **Чому спадкування, а не композиція.** Плановий і ургентний візит **є різновидами** візиту
  (*is-a*): усюди, де очікується `VetVisit`, можна підставити будь-який підтип. `PriceUrgency`
  і звіт, навпаки, лише *використовують* візити — там спадкування немає.
- **Вибір підтипу — в одному місці.** `VetVisit.of(...)` за ознакою `urgent` створює
  `UrgentVisit` або `RoutineVisit`. Далі `Main` працює лише з типом `VetVisit` і не містить
  `instanceof` чи `switch` за підтипом.
- **Захист від `CT_CONSTRUCTOR_THROW`.** Конструктор базового класу перевіряє аргументи до
  виклику приватного конструктора (`this(requireText(...), …, true)`) — прийом із методички
  `LAB_03`, Додаток А. Підтипи `final`, тому SpotBugs не попереджає про виняток у конструкторі.

### `VisitKind`

| Константа | `label()` | `isUrgent()` | Підтип |
| --- | --- | --- | --- |
| `ROUTINE` | `плановий` | `false` | `RoutineVisit` |
| `URGENT` | `ургентний` | `true` | `UrgentVisit` |

Значення порівнюються через `==`: кожна константа існує в одному екземплярі.

### `equals` і `hashCode`

Критерій рівності: однаковий **клас** (`getClass()`) і однакові `animal`, `species`,
`visitType`, `price` (`Double.compare`). Обидва методи `final` у базовому класі, `hashCode` =
`Objects.hash(getClass(), animal, species, visitType, price)`. Тому два однакові рядки CSV
дають один елемент у `HashSet`, а плановий і ургентний візит з однаковими полями — різні
об'єкти (їхня підсумкова вартість різна). Поля `final`, тож ключ `HashMap` не зміниться.

## Структура коду

```text
src/main/java/ua/lpnu/kzp/
├── Main.java          # CLI, читання, обчислення (поліморфний цикл), звіт
├── VetVisit.java      # абстрактний базовий тип
├── RoutineVisit.java  # плановий візит
├── UrgentVisit.java   # ургентний візит
├── VisitKind.java     # enum категорії
└── PriceUrgency.java  # record підсумку (+ finalRevenue)
src/test/java/ua/lpnu/kzp/
├── MainTest.java          # тести LAB_02 + поліморфна сума
├── VetVisitTest.java      # тести LAB_02 для спільного типу (через VetVisit.of)
├── PriceUrgencyTest.java  # record, зокрема finalRevenue
├── VisitKindTest.java     # enum
├── VisitSubtypesTest.java # підтипи, власні правила, finalCost, фабрики
└── VisitEqualityTest.java # equals/hashCode, HashSet, HashMap
```

## Формат даних і показники

Формат `data/input.csv` (UTF-8, без заголовка, десяткова крапка) — як у `LAB_01`/`LAB_02`:

```text
animal;species;visitType;price;urgent
```

`urgent = false` → `RoutineVisit`, `urgent = true` → `UrgentVisit`. Нове правило: ургентний
візит із ціною `0` — хибний рядок (`Рядок N: ургентний візит не може бути безкоштовним`).

Очікуваний результат на `data/input.csv`:

```text
Коректних записів: 3
Середня вартість: 600.00 грн
Загальний виторг: 1800.00 грн
Ургентних візитів: 1
Підсумкова вартість із надбавками: 2175.00 грн
```

```text
Рядок 4: порожнє поле animal
Рядок 5: price має бути скінченним невід'ємним числом
```

`2175.00 = 450 + 750 × 1.5 + 600`. Перші чотири рядки й stderr збігаються з `LAB_02` побайтово.

## Збірка та запуск

Потрібна JDK 21. Maven Wrapper (Maven 3.9.11) успадковано. Усі команди — з каталогу `LAB_03/`.

macOS / Ubuntu:

```sh
./mvnw test
./mvnw verify
./mvnw package
java -jar target/lab03-3.0.0-all.jar
java -jar target/lab03-3.0.0-all.jar --help
java -jar target/lab03-3.0.0-all.jar --version     # 3.0.0+local
java -jar target/lab03-3.0.0-all.jar --input data/input.csv --output output/report.txt
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify
.\mvnw.cmd package
java -jar target/lab03-3.0.0-all.jar --help
```

`verify` запускає 118 тестових випадків JUnit 5 і SpotBugs, `package` створює виконуваний JAR.

## GitHub Actions

Workflow `.github/workflows/lab03-ci.yml` (**Lab 03 CI**) — копія `lab02-ci.yml` для теки
`LAB_03/`: `clean verify` на `ubuntu-latest`, `windows-latest`, `macos-latest` (Temurin JDK 21),
запуск JAR без аргументів, з `--help` і `--version`, артефакти `lab03-<os>-java21` і
`lab03-reports-<os>`.

Звіт: [REPORT.md](REPORT.md). План і Issues: [PLAN.md](PLAN.md).
