# Carta Cultura Manager

## Membri del Gruppo

* Alessandro Rutigliano, 909971
* Cristiano Rotunno, 914317
* Davide Riccio, 917209

Un'applicazione per la gestione della Carta Cultura. La directory `client-web` contiene l'HTML, JavaScript, e CSS per il frontend dell'applicazione.

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
    (Nota: `mvn clean install` potrebbe essere necessario se ci fossero moduli dipendenti locali, ma per questo progetto `package` è solitamente sufficiente).
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

(Questa sezione è stata aggiunta in uno step precedente e dovrebbe essere mantenuta)

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
        `user:RSSMRA80A01H501U={"id":"some-uuid","name":"Mario","surname":"Rossi","email":"mario.rossi@example.com","fiscalCode":"RSSMRA80A01H501U"}`
    *   **Contributo Utente:**
        `contribution:RSSMRA80A01H501U={"userId":"RSSMRA80A01H501U","available":500.0,"allocated":0.0,"spent":0.0,"total":500.0}`
    *   **Voucher:** (Assicurati che `userId` nel JSON del voucher corrisponda a un utente esistente)
        `voucher:voucher-uuid-123={"id":"voucher-uuid-123","amount":25.0,"category":"libri","status":"generated","creationDate":"2024-07-30T10:00:00","consumedAt":null,"userId":"RSSMRA80A01H501U"}`
    *   **Indici Voucher Utente:** (Questi sono gestiti internamente per listare i voucher di un utente)
        `vouchersCount:RSSMRA80A01H501U=1`
        `voucherIdByIndex:RSSMRA80A01H501U:0=voucher-uuid-123`
    *   **Statistiche Globali:**
        `stats:userCount=1`
        `stats:totalAvailable=500.0`
        `stats:totalAllocated=0.0`
        `stats:totalSpent=0.0`
        `stats:totalVouchers=1`
        `stats:vouchersConsumed=0`

    **Nota:** I valori JSON devono essere su una singola linea senza interruzioni. Assicurati che i dati siano consistenti (es. `userId` in `contribution` e `voucher` deve esistere come chiave `user:`).

## Licenza

MIT
