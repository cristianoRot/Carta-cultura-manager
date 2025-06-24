# Progetto Sistemi Distribuiti 2024-2025 - TCP

## 1. Panoramica

- **Tipo:** Testuale
- **Porta Utilizzata:** 3030
- **Modello:** Comando-risposta

Il protocollo implementato nel database è un protocollo testuale ispirato a Redis. Il server accetta connessioni TCP su cui vengono inviate comandi e risposte in formato testo.

## 2. Struttura dei Messaggi

### 2.1. Protocollo testuale

- **Encoding:** UTF-8
- **Fine linea:** LF (`\n`) o CRLF (`\r\n`)
- **Delimitatori Messaggio:** newline (`\n`)

Ogni comando è inviato come una singola linea di testo. Ogni risposta è restituita come una singola linea di testo.

**Esempio:**
```
GET users/12345
```

### 2.2. Comandi

Il database supporta i seguenti comandi:

| Comando | Parametri         | Descrizione                                | Esempio                |
|---------|-------------------|--------------------------------------------|------------------------|
| GET     | percorso         | Restituisce il valore associato | `GET users/12345` |
| SET     | percorso, valore | Imposta il valore | `SET users/12345 {"name":"Mario"}` |
| DEL     | percorso         | Elimina la risorsa | `DEL users/12345` |
| EXISTS  | percorso         | Verifica se la risorsa esiste | `EXISTS users/12345` |
| INCREMENT | percorso, delta | Incrementa numerico (anche negativo) | `INCREMENT system/stats/totalAvailable -25.0` |
| .       | —               | Chiude la connessione | `.` |

### 2.3. Formato chiavi

Le chiavi seguono la seguente convenzione:

- `users/{fiscalCode}`: JSON con i dati anagrafici dell'utente
- `users/{fiscalCode}/balance`: saldo residuo del contributo
- `users/{fiscalCode}/contribAllocated`: contributo già trasformato in voucher
- `users/{fiscalCode}/contribSpent`: contributo speso tramite voucher consumati
- `vouchers/{voucherId}`: JSON con i dati di un buono
- `vouchers`: mappa JSON *id → Voucher* utilizzata per elencare tutti i buoni
- `system/stats/userCount`: numero totale di utenti
- `system/stats/totalAvailable`: somma dei contributi ancora disponibili
- `system/stats/totalAllocated`: somma dei contributi trasformati in voucher
- `system/stats/totalSpent`: somma dei contributi spesi
- `system/stats/totalVouchers`: numero totale di voucher generati
- `system/stats/vouchersConsumed`: numero totale di voucher consumati

## 3. Gestione degli Errori

- **Risposte di successo:**  
  - "OK": Operazione completata con successo
  - "1": Operazione booleana completata con successo e risultato vero
  - "0": Operazione booleana completata con successo e risultato falso
  - "{valore}": Il valore associato alla chiave richiesta

- **Risposte di errore:**  
  - "null": Chiave non trovata
  - "BAD_REQUEST": sintassi comando non valida
  - "NOT_FOUND": risorsa inesistente
  - "ERROR": errore interno inaspettato

**Esempi di messaggi di errore:**
- "ERR missing key": Chiave non specificata
- "ERR unknown command 'XYZ'": Comando non supportato
- "ERR missing key or value": Parametri mancanti

## 4. Gestione della Concorrenza

Il database implementa un meccanismo di lock basato sulla chiave per gestire la concorrenza. Quando più client tentano di modificare contemporaneamente la stessa chiave, le operazioni vengono serializzate per evitare race condition.

## 5. Scambio di Esempio

```
Client: SET users/12345 {"name":"Mario","surname":"Rossi","email":"mario@example.com","fiscalCode":"12345"}
Server: OK
Client: GET users/12345
Server: {"name":"Mario","surname":"Rossi","email":"mario@example.com","fiscalCode":"12345"}
Client: EXISTS users/12345
Server: 1
Client: DEL users/12345
Server: 1
Client: EXISTS users/12345
Server: 0
Client: GET users/12345
Server: null
Client: .
Server: bye
```

## 6. Chiusura della Connessione

Per chiudere correttamente la connessione con il database, il client deve inviare un singolo punto (`.`). Il server risponderà con `bye` e chiuderà la connessione.
