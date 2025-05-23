
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
GET user:12345
```

### 2.2. Comandi

Il database supporta i seguenti comandi:

| Comando | Parametri         | Descrizione                                | Esempio                |
|---------|-------------------|--------------------------------------------|------------------------|
| GET     | chiave            | Restituice il valore associato alla chiave | `GET user:12345`      |
| SET     | chiave, valore    | Imposta il valore alla chiave              | `SET user:12345 {"name":"Mario"}` |
| DEL     | chiave            | Elimina una chiave dal database            | `DEL user:12345`      |
| EXISTS  | chiave            | Verifica se una chiave esiste              | `EXISTS user:12345`   |
| .       |                   | Chiude la connessione                      | `.`                   |

### 2.3. Formato chiavi

Le chiavi seguono la seguente convenzione:

- `user:{fiscalCode}`: Memorizza i dati di un utente (serializzato come JSON)
- `contribution:{userId}`: Memorizza i dati del contributo di un utente (serializzato come JSON)
- `voucher:{voucherId}`: Memorizza i dati di un buono (serializzato come JSON)
- `vouchersCount:{userId}`: Memorizza il numero di buoni di un utente
- `voucherIdByIndex:{userId}:{index}`: Memorizza l'ID di un buono all'indice specificato per un utente
- `stats:userCount`: Memorizza il numero totale di utenti
- `stats:totalAvailable`: Memorizza la somma di tutti i contributi disponibili
- `stats:totalAllocated`: Memorizza la somma di tutti i contributi allocati
- `stats:totalSpent`: Memorizza la somma di tutti i contributi spesi
- `stats:totalVouchers`: Memorizza il numero totale di buoni generati
- `stats:vouchersConsumed`: Memorizza il numero totale di buoni consumati

## 3. Gestione degli Errori

- **Risposte di successo:**  
  - "OK": Operazione completata con successo
  - "1": Operazione booleana completata con successo e risultato vero
  - "0": Operazione booleana completata con successo e risultato falso
  - "{valore}": Il valore associato alla chiave richiesta

- **Risposte di errore:**  
  - "null": Chiave non trovata
  - "ERR {messaggio}": Errore con messaggio esplicativo

**Esempi di messaggi di errore:**
- "ERR missing key": Chiave non specificata
- "ERR unknown command 'XYZ'": Comando non supportato
- "ERR missing key or value": Parametri mancanti

## 4. Gestione della Concorrenza

Il database implementa un meccanismo di lock basato sulla chiave per gestire la concorrenza. Quando più client tentano di modificare contemporaneamente la stessa chiave, le operazioni vengono serializzate per evitare race condition.

## 5. Scambio di Esempio

```
Client: SET user:12345 {"name":"Mario","surname":"Rossi","email":"mario@example.com","fiscalCode":"12345"}
Server: OK
Client: GET user:12345
Server: {"name":"Mario","surname":"Rossi","email":"mario@example.com","fiscalCode":"12345"}
Client: EXISTS user:12345
Server: 1
Client: DEL user:12345
Server: 1
Client: EXISTS user:12345
Server: 0
Client: GET user:12345
Server: null
Client: .
Server: bye
```

## 6. Chiusura della Connessione

Per chiudere correttamente la connessione con il database, il client deve inviare un singolo punto (`.`). Il server risponderà con `bye` e chiuderà la connessione.
