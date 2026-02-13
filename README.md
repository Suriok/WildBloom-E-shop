# Dokumentace k projektu WildBloom

## Popis aplikace

WildBloom je webová aplikace typu e-shop pro prodej květin a kytic. Uživatel si může prohlížet produkty podle kategorií, zobrazit detail produktu a vytvořit objednávku. Aplikace zároveň obsahuje administrační část pro správu katalogu a zpracování objednávek.

### Role v systému

- **CUSTOMER** – registrace, přihlášení, práce s košíkem, vytvoření objednávky, zobrazení vlastních objednávek a jejich zrušení (jen v určitých stavech).
- **EMPLOYEE** – zobrazení všech objednávek a změna jejich stavu (bez správy produktů/kategorií).
- **ADMINISTRATOR** – správa kategorií a produktů + vytváření účtů pro zaměstnance a administrátory.

## Struktura projektu (architektura)

Aplikace je řešena jako vrstvená webová aplikace: backend v Java/Spring Boot, databáze PostgreSQL a frontend (HTML + JS) volající REST API přes fetch.

### Backend

- **Controller vrstva (…Controller)** – REST endpointy pro autentizaci, produkty, kategorie, košík a objednávky. Přístup je řízený anotacemi `@PreAuthorize` a URL pravidly v Security.
- **Service vrstva (…Service)** – business logika:
  - **UserService** – registrace zákazníka, vytváření employee/admin účtů, kontrola unikátnosti e-mailu, hashování hesel.
  - **CartService** – přidávání/úprava/mazání položek košíku, kontrola skladového množství (`in_stock`), přepočet `totalAmount`.
  - **OrderService** – vytvoření objednávky z košíku, odečet ze skladu, změny statusů (workflow), storno a vrácení zásob.
- **DAO vrstva (…Dao, BaseDao)** – práce s DB.
- **Model/Entity vrstva (…model)** – doménový model:
  - `User` je abstraktní entita s dědičností `JOINED` → `Customer`, `Employee`, `Administrator`,
  - `Customer` má `Cart` (1:1),
  - `Cart` má `CartItem` (1:N), položka je unikátní pro dvojici (`cart`, `product`),
  - `Order` má `OrderItem` (1:N) a status `OrderStatus`.

### Frontend

Frontend je tvořen statickými HTML stránkami s JavaScriptem (fetch na REST API):

- `index.html` – katalog + vyhledávání + přidání do košíku,
- `login.html`, `register.html`,
- `cart.html`, `my-orders.html` (CUSTOMER),
- `admin.html` (ADMINISTRATOR),
- `orders-management.html` (EMPLOYEE/ADMINISTRATOR).

## Návod na instalaci a spuštění

### Požadavky

- JDK 17+, Maven
- Databáze: PostgreSQL (doporučeno), nebo H2 pro rychlý lokální vývoj

### Konfigurace DB (application.properties)

#### Varianta 1: PostgreSQL (doporučeno)

Pro lokální běh je potřeba nastavit (příklad):

```properties
spring.datasource.url=jdbc:postgresql://localhost:8080/wildbloom
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

## Vytvoření databáze (PostgreSQL)

Vytvořit DB např.:

```sql
CREATE DATABASE wildbloom;
```

V projektu je aktuálně nastaven PostgreSQL datasource, takže pro lokální běh je potřeba upravit **URL/credentials**.

---

## Varianta 2: H2 (rychlý lokální vývoj/test)

```properties
spring.datasource.url=jdbc:h2:mem:wildbloom
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
```

H2 konzole: `http://localhost:8080/h2-console`  
(JDBC URL: `jdbc:h2:mem:wildbloom`)

**Výhoda:** rychlé spuštění bez PostgreSQL.  
**Nevýhoda:** data se po restartu smažou.

---

## Inicializace schématu a testovacích dat

- `spring.jpa.hibernate.ddl-auto=update` vytvoří/aktualizuje tabulky automaticky.
- `spring.sql.init.mode=always` + `data.sql` vloží základní kategorie a produkty  
  (s `ON CONFLICT DO NOTHING`, takže je bezpečné spouštět opakovaně).

### Testovací data zahrnují

- **Kategorie (4):** Roses, Spring Flowers, Bouquets, Exotic
- **Produkty (10+):** ukázkové položky v různých cenách a skladovém množství  
  (např. Red Roses Bouquet, White Avalanche Roses, Mixed Spring Bouquet, Exotic Orchids, …)
- **Uživatelé:** vytváří se automaticky při startu přes `DefaultUsersInitializer`  
  (pokud už existují, nevytváří se znovu)

---

## Spuštění

```bash
mvn clean spring-boot:run
```

Nebo přes IDE spustit hlavní třídu `WildBloomApplication`.

Aplikace běží na `http://localhost:8080/` (výchozí port Spring Boot).  
Spring Boot používá **embedded Tomcat**, takže není potřeba instalovat externí aplikační server.

---

## Spuštění testů

### Unit testy (JUnit)

Pro spuštění unit testů použijte Maven:

```bash
mvn test
```

Nebo pro spuštění konkrétního testu:

```bash
mvn test -Dtest=NázevTestu
```

Testy se nacházejí v `src/test/java/` a zahrnují:
- **DAO testy:** `ProductDaoTest`, `CategoryDaoTest`
- **Service testy:** `CartServiceTest`, `OrderServiceTest`, `UserServiceTest`

---

### API testy pomocí Postman

Pro testování REST API endpointů je připravena sada Postman kolekcí.

#### Požadavky

1. **Instalace Postman:**
   - Stáhněte a nainstalujte Postman z oficiálních stránek: https://www.postman.com/downloads/
   - Postman je dostupný pro Windows, macOS a Linux

#### Import kolekcí

V projektu jsou připraveny následující Postman kolekce v adresáři `postman/`:

- `Admin_test.json` – testy administrátorských funkcí
- `Cancell_Test.json` – testy zrušení objednávek
- `Cart_Mechanic_Test.json` – testy košíku
- `EdgeCase.json` – testy hraničních případů
- `Order_Status___Security.json` – testy stavů objednávek a bezpečnosti
- `Search___Filter_Test.json` – testy vyhledávání a filtrování
- `Securite_Test.json` – testy bezpečnosti
- `User_Test.json` – testy uživatelských funkcí

**Postup importu:**

1. Otevřete Postman
2. Klikněte na **Import** (tlačítko v levém horním rohu)
3. Vyberte **File** a zvolte JSON soubory z adresáře `postman/`
   - Můžete importovat všechny soubory najednou (vyberte více souborů)
4. Kolekce se zobrazí v levém panelu Postman

#### Spuštění testů

**Důležité:** Před spuštěním testů musí být aplikace spuštěná na `http://localhost:8080/`

**Postup:**

1. Ujistěte se, že aplikace běží (`mvn spring-boot:run`)
2. V Postman otevřete požadovanou kolekci
3. Klikněte na **Run** (tlačítko vedle názvu kolekce)
4. V okně **Collection Runner**:
   - Klikněte na **Run [název kolekce]**
5. Postman spustí všechny requesty v kolekci a zobrazí výsledky
---

## Testovací účty (vytváří se při startu přes DefaultUsersInitializer)

- **Admin:** `admin@wildbloom.cz` / `admin123`
- **Employee:** `employee@wildbloom.cz` / `employee123`
- **Customer:** vytvořit registrací na `register.html`

---

## Přístupové stránky

- katalog: `/` nebo `/index.html`
- login: `/login.html`
- admin: `/admin.html`
- employee správa objednávek: `/orders-management.html`
- customer košík: `/cart.html`, moje objednávky: `/my-orders.html`

---

## Konfigurace pro různá prostředí

### Lokální vývoj
Doporučeno **H2** (nejrychlejší) nebo lokální **PostgreSQL**.

### Produkční prostředí
- použít PostgreSQL s vlastními credentials,
- nedávat hesla do repozitáře (raději environment proměnné),
- `spring.jpa.hibernate.ddl-auto=validate` místo `update`,
- `spring.sql.init.mode=never` (nevkládat testovací data).

---

## Zkušenosti a poznatky ze semestrální práce

- **Spring Security (role + redirect po přihlášení):** prakticky jsme si vyzkoušely řízení přístupu podle rolí. Výhoda je přehledné řízení URL + možnost používat `@PreAuthorize`. Nevýhoda je, že při špatném nastavení se často projeví problém až „tím, že něco nejde otevřít“, takže je potřeba číst logy.
- **JPA/Hibernate + dědičnost JOINED:** výhoda je čistý model uživatelů (Customer/Employee/Admin jako samostatné entity). Nevýhoda je složitější SQL a nutnost dávat pozor na lazy načítání a dotazy.
- **Práce s penězi (BigDecimal) a snapshot ceny:** výhoda je správná přesnost pro ceny. U `OrderItem` jsme ukládali `priceSnapshot`, aby objednávka zůstala konzistentní i po změně produktu.

---

## Nečekané problémy a řešení

### Špatný redirect po loginu / nepřístupné stránky podle role

- **Problém:** po přihlášení uživatel skončil na stránce, kam neměl právo (např. employee na admin stránce), nebo se zasekl na loginu.
- **Řešení:** nastavili jsme vlastní `successHandler`, který přesměruje podle role  
  (CUSTOMER → `/`, ADMINISTRATOR → `/admin.html`, EMPLOYEE → `/orders-management.html`)  
  a zároveň jsme zpřesnili pravidla přístupu k HTML stránkám.

### Nekonzistence skladu (in_stock) při nákupu / přidávání do košíku

- **Problém:** uživatel mohl zkusit přidat víc kusů než je na skladě, nebo vytvořit objednávku bez kontroly dostupnosti.
- **Řešení:** v `CartService` kontrolujeme `in_stock` při změně množství a v `OrderService` při vytvoření objednávky odečítáme sklad v transakci. Při zrušení objednávky v povolených stavech sklad vracíme zpět.

### Nefungoval login kvůli “ROLE_” prefixu (Spring Security)

- **Problém:** v DB máme roli např. `EMPLOYEE`, ale Spring Security porovnává autority typicky jako `ROLE_EMPLOYEE`. Když se to nesladí, přijdou 403/403 nebo “uživatel nemá roli”.
- **Řešení:** v `UserDetailsService` jsme mapovali roli na `ROLE_<ROLE>` a zároveň v `@PreAuthorize` / konfiguraci používali konzistentní názvy rolí.

