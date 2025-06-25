# Carta Cultura Manager

## Membri del Gruppo

* Alessandro Rutigliano, 909971, a.rutigliano8@campus.unimib.it
* Cristiano Rotunno, 914317, c.rotunno@campus.unimib.it
* Davide Riccio, 917209, d.riccio3@campus.unimib.it

Un'applicazione per la gestione della Carta Cultura. La directory `client-web` contiene l'HTML, JavaScript, e CSS per il frontend dell'applicazione.

## Struttura del progetto

La code-base è organizzata in tre moduli principali:


`database/`: Server TCP in-memory basato su collezioni e documenti, ispirato a Google Firebase. \n
`server-web/`: Servizio REST basato su Jakarta Servlet + Jersey che funge da **backend** dell'applicazione. Comunica con `database/` via socket TCP. Le API sono documentate in `REST.md`. 
`client-web/`: Front-end web statico (HTML/JS/CSS) che invoca il backend su `http://localhost:8080`. 

File di supporto:

* `REST.md` – specifica completa delle API REST.
* `TCP.md` – dettagli del protocollo TCP adottato dal database.
* `traccia.pdf` – traccia originale del progetto (per riferimento).

## Prerequisiti

* **JDK 21** o superiore
* **Apache Maven 3.9+**
* Browser moderno (Chrome/Edge/Firefox) per il client
* (Opz.) **cURL** o **Postman** per testare manualmente le API


## Avvio rapido (sviluppo)

Apri due terminali:

**Terminale 1 – Database**

```bash
cd database
mvn package          # compila e crea il JAR omnicomprensivo
java -jar target/sd-project-database-0.1.jar
```

**Terminale 2 – Server REST**

```bash
cd server-web
mvn jetty:run        # avvia Jetty su http://localhost:8080
```

A questo punto puoi:

1. Aprire `client-web/index.html` nel browser.  
2. Interagire con l'applicazione oppure chiamare le API REST con:
   ```bash
   curl -X GET http://localhost:8080/api/system/stats
   ```

## Configurazione

Per semplicità di consegna, host e porta del database sono **hard-coded** in  
`server-web/src/main/java/it/unimib/sd2025/System/DatabaseConnection.java` (`localhost:3030`).  
Se vuoi eseguirli su host/porta differenti, modifica tali costanti e ricompila il modulo `server-web`.

## Documentazione

* **API REST:** vedi `REST.md` per la lista completa degli endpoint e dei codici di risposta.
* **Protocollo TCP:** vedi `TCP.md` per il dettaglio dei comandi supportati dal database.

## Esempi di utilizzo API

Di seguito alcuni esempi rapidi con `curl` per testare le principali funzionalità (assumendo server avviato su `localhost:8080`).

### 1. Registrare un nuovo utente

```bash
curl -X POST \
     -H "Content-Type: application/json" \
     -d '{
           "name":"Mario",
           "surname":"Rossi",
           "email":"mario.rossi@example.com",
           "fiscalCode":"RSSMRA80A01H501U"
         }' \
     http://localhost:8080/api/users -i
```

### 2. Ottenere il profilo utente

```bash
curl http://localhost:8080/api/users/RSSMRA80A01H501U
```

### 3. Generare un voucher da 25€ (categoria "libri") con data

```bash
curl -X POST \
     -H "Content-Type: application/json" \
     -d '{
           "amount": 25.0,
           "category": "libri",
           "status": "generated",
           "createdAt": "2025-07-25T10:00:00"
         }' \
     http://localhost:8080/api/users/RSSMRA80A01H501U/voucher -i
```

### 4. Recuperare tutti i voucher di un utente

```bash
curl http://localhost:8080/api/users/RSSMRA80A01H501U/vouchers
```

### 5. Consumare un voucher

```bash
curl -X POST http://localhost:8080/api/users/RSSMRA80A01H501U/voucher/<VOUCHER_ID> -i
```

### 6. Statistiche di sistema

```bash
curl http://localhost:8080/api/system/stats
```

## Compilazione ed Esecuzione

### Database (`database` module)

Il modulo `database` è un semplice server TCP key-value store in-memory.

1.  **Naviga nella directory:**
    ```bash
    cd database
    ```
2.  **Compila:**
    ```bash
    mvn clean package
    ```
    Questo creerà un file JAR eseguibile in `database/target/`, ad esempio `sd-project-database-0.1.jar` (il nome esatto può variare in base alla versione definita nel `pom.xml`).
3.  **Esegui:**
    ```bash
    java -jar target/sd-project-database-0.1.jar
    ```
    *   Il database si metterà in ascolto sulla porta 3030 per default.
    *   Il database può essere pre-popolato con dati da `initial_data.txt`. Vedere la sezione 'Pre-popolamento Dati Iniziali (`initial_data.txt`)' per dettagli.

### Server Web (`server-web` module)

Il modulo `server-web` espone le API REST per l'applicazione.

1.  **Naviga nella directory:**
    ```bash
    cd server-web
    ```
2.  **Compila:**
    ```bash
    mvn clean package
    ```
3.  **Esegui (usando il plugin Jetty):**
    ```bash
    mvn jetty:run
    ```
    *   Il server web sarà accessibile su `http://localhost:8080` (porta di default di Jetty).

### Client Web (`client-web` directory)

Il client web è un'applicazione statica HTML, JavaScript e CSS.

1.  **Nessuna Compilazione Necessaria:**
    Non è richiesta una fase di compilazione per il client web.
2.  **Esegui:**
    Apri il file `client-web/index.html` direttamente nel tuo browser web.
3.  **Prerequisiti:**
    Assicurati che il Server Web (`server-web` module) sia in esecuzione e accessibile (default: `http://localhost:8080`), poiché il client web effettua chiamate API a questo server.

## Pre-popolamento Dati Iniziali (`initial_data.txt`)

Il database può essere pre-popolato con dati da un file denominato `initial_data.txt`.

*   **Ordine di Caricamento:**
    1.  Il server cerca prima `initial_data.txt` nel **classpath**. Se stai eseguendo il JAR, puoi includere questo file in `src/main/resources` prima della compilazione, e verrà pacchettizzato nel JAR.
    2.  Se non trovato nel classpath, il server cerca `initial_data.txt` nella **directory di lavoro corrente** da cui viene eseguito il JAR.
    3.  Se il file non viene trovato in nessuna delle due posizioni, il database si avvierà con le statistiche globali inizializzate a zero e nessun altro dato.

*   **Formato del File:**
    Il file `initial_data.txt` deve contenere coppie chiave-valore, una per riga, nel formato:
    ```
    chiave=valore
    ```

*   **Esempi di Chiavi:**
    *   **Utente:**
        `users/RSSMRA80A01H501U={"name":"Mario","surname":"Rossi","email":"mario.rossi@example.com","fiscalCode":"RSSMRA80A01H501U"}`
    *   **Saldi utente:**
        `users/RSSMRA80A01H501U/balance=500.0`
        `users/RSSMRA80A01H501U/contribAllocated=0.0`
        `users/RSSMRA80A01H501U/contribSpent=0.0`
    *   **Voucher:**
        `vouchers/voucher-uuid-123={"id":"voucher-uuid-123","amount":25.0,"category":"libri","status":"generated","createdAt":"2024-07-30T10:00:00","consumedAt":null,"userId":"RSSMRA80A01H501U"}`
    *   **Statistiche Globali:**
        `system/stats/userCount=1`
        `system/stats/totalAvailable=500.0`
        `system/stats/totalAllocated=0.0`
        `system/stats/totalSpent=0.0`
        `system/stats/totalVouchers=1`
        `system/stats/vouchersConsumed=0`

    **Nota:** I valori JSON devono essere su una singola linea senza interruzioni. Assicurati che i dati siano consistenti (es. `userId` nei voucher deve riferirsi a un utente esistente e le voci `balance`/`contribAllocated`/`contribSpent` siano presenti per lo stesso utente).

## Licenza

MIT
