# План лабораторної роботи № 2

Мета: замінити рядкові записи й масиви з `LAB_01` класом `VetVisit` і record `PriceUrgency`,
не змінивши вхідний формат, показники та звіт. Рівень: 3 без додаткових балів
(builder і завдання із зірочкою не виконуються).

## GitHub Issues

Створити **до** злиття PR; номери вписати в таблицю `REPORT.md` і в опис PR (`Closes #N`).

| # | Мітка | Назва | Критерій готовності | Перевірка |
| --- | --- | --- | --- | --- |
| 1 | `infra` | LAB_02: тека, POM `lab02:2.0.0`, workflow Lab 02 CI | `LAB_02/` має Wrapper, POM, дані; `--version` → `2.0.0+local`; CI на трьох ОС | `./mvnw verify`, вкладка Actions |
| 2 | `feature` | Клас-сутність `VetVisit` | `private final` поля, конструктор перевіряє всі інваріанти, геттери, `toString` з `Locale.ROOT`, javadoc | `VetVisitTest` (конструктор) |
| 3 | `feature` | Фабрика `VetVisit.fromCsv` | структура й типи у фабриці, інваріанти — у конструкторі; правила з `validateLine` не дублюються | `VetVisitTest` (fromCsv) |
| 4 | `feature` | Record `PriceUrgency` і перебудова `Main` | `List<VetVisit>` замість `List<String[]>`, `PriceUrgency` замість `double[]`; звіт ідентичний | `MainTest`, порівняння з виводом `LAB_01` |
| 5 | `feature` | Тести JUnit 5 для класу, фабрики й record | позитивні, граничні, негативні випадки; усі тести `LAB_01` перенесено | `./mvnw test` — 83 випадки |
| 6 | `docs` | README, REPORT, javadoc для LAB_02 | розділи REPORT за методичкою `LAB_02`, с. 19–20 | рев'ю PR |

Створення з терміналу (`gh` уже використовувався в `LAB_01`):

```sh
gh issue create --label infra   --title "LAB_02: тека, POM lab02:2.0.0, workflow Lab 02 CI"
gh issue create --label feature --title "LAB_02: клас-сутність VetVisit"
gh issue create --label feature --title "LAB_02: фабрика VetVisit.fromCsv"
gh issue create --label feature --title "LAB_02: record PriceUrgency і перебудова Main"
gh issue create --label feature --title "LAB_02: тести JUnit 5 для VetVisit і PriceUrgency"
gh issue create --label docs    --title "LAB_02: README, REPORT, javadoc"
```

## Порядок

1. Зберегти контрольний вивід `LAB_01` (`java -jar target/lab01-1.0.0-all.jar`, stdout і stderr).
2. Окрема гілка для роботи (методичка `LAB_02`, с. 15, п. 4), наприклад `java/lab02`:
   вона підпадає під тригер CI `java/**`. `AGENTS.md` (§ 10) називає робочою гілкою
   `java/infra` і дозволяє інші лише за погодженням — рішення за студентом.
3. Issues 1 → 2 → 3 → 4 → 5 → 6, по одному логічному коміту на Issue.
4. `./mvnw clean verify`, `./mvnw package`, запуск JAR; порівняння звіту з кроком 1.
5. PR `java/lab02` → `main` з `Closes #N` для всіх Issues; зелений CI на трьох ОС.
6. Після злиття — тег версії `2.0.0` (формат тегу `v2.0.0` чи `v2` — див. відкриту
   розбіжність в `AGENTS.md`, § 3).

## Перевірка збереження поведінки

Критерій (методичка `LAB_02`, розділ 8): та сама кількість коректних записів, ті самі
показники, порядок рядків звіту й кількість повідомлень про помилки.

```sh
cd LAB_01 && java -jar target/lab01-1.0.0-all.jar --output /tmp/r1.txt > /tmp/o1.txt 2> /tmp/e1.txt
cd ../LAB_02 && java -jar target/lab02-2.0.0-all.jar --output /tmp/r2.txt > /tmp/o2.txt 2> /tmp/e2.txt
diff /tmp/o1.txt /tmp/o2.txt && diff /tmp/e1.txt /tmp/e2.txt && diff /tmp/r1.txt /tmp/r2.txt && echo OK
```
