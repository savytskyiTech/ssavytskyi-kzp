# План лабораторної роботи № 3

Мета: розділити `VetVisit` на абстрактний базовий тип і підтипи `RoutineVisit`, `UrgentVisit`,
додати `enum VisitKind`, поліморфну підсумкову вартість `finalCost()` і узгоджені
`equals`/`hashCode`, не змінивши вхідний CSV і чотири показники звіту `LAB_02`.
Рівень: 3 без додаткових балів (sealed-ієрархія із зірочкою не виконується).

## GitHub Issues

Створено Issues #18–#23. Посилання додано до таблиці `REPORT.md` (розділ 11);
ці номери потрібно вказати також в описі PR.

| # | Мітка | Назва | Критерій готовності | Перевірка |
| --- | --- | --- | --- | --- |
| [#18](https://github.com/savytskyiTech/ssavytskyi-kzp/issues/18) | `infra` | LAB_03: тека, POM `lab03:3.0.0`, workflow Lab 03 CI | `--version` → `3.0.0+local`; CI на трьох ОС | `./mvnw verify`, Actions |
| [#19](https://github.com/savytskyiTech/ssavytskyi-kzp/issues/19) | `feature` | Абстрактний базовий тип `VetVisit` | спільні поля й перевірка лише в базовому класі; `abstract finalCost()` | `VetVisitTest` |
| [#20](https://github.com/savytskyiTech/ssavytskyi-kzp/issues/20) | `feature` | Підтипи `RoutineVisit`, `UrgentVisit` і `enum VisitKind` | `@Override finalCost()`; власне правило `UrgentVisit` (ціна > 0); `fromCsv` створює підтип | `VisitSubtypesTest`, `VisitKindTest` |
| [#21](https://github.com/savytskyiTech/ssavytskyi-kzp/issues/21) | `feature` | `equals`/`hashCode` і перевірка в `HashSet`/`HashMap` | критерій: клас + усі поля; рівні об'єкти — один елемент множини | `VisitEqualityTest` |
| [#22](https://github.com/savytskyiTech/ssavytskyi-kzp/issues/22) | `feature` | Поліморфна сума у звіті | цикл без `instanceof`/`switch`; 4 старі рядки без змін + 5-й рядок | `MainTest`, порівняння з `LAB_02` |
| [#23](https://github.com/savytskyiTech/ssavytskyi-kzp/issues/23) | `docs` | README, REPORT, javadoc для LAB_03 | 15 розділів REPORT за `LAB_03`, с. 20 | рев'ю PR |

## Порядок

1. Зберегти контрольний вивід `LAB_02` (stdout, stderr, файл звіту).
2. Гілка `java/lab03` від актуальної `main` (підпадає під тригер CI `java/**`).
3. Issues #18 → #23, по одному логічному коміту на Issue.
4. `./mvnw clean verify`, `./mvnw package`, запуск JAR; порівняння з кроком 1.
5. PR `java/lab03` → `main` з `Closes #N`; зелений CI на трьох ОС.
6. Після злиття — тег версії `3.0.0` (формат — див. відкриту розбіжність в `AGENTS.md`, § 3).

## Перевірка збереження поведінки

```sh
cd LAB_02 && ./mvnw -q package && java -jar target/lab02-2.0.0-all.jar --output /tmp/r2.txt > /tmp/o2.txt 2> /tmp/e2.txt
cd ../LAB_03 && ./mvnw -q package && java -jar target/lab03-3.0.0-all.jar --output /tmp/r3.txt > /tmp/o3.txt 2> /tmp/e3.txt
diff /tmp/e2.txt /tmp/e3.txt && echo "stderr OK"
diff /tmp/o2.txt /tmp/o3.txt   # очікувано: лише доданий 5-й рядок
```
