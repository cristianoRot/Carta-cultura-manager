# Progetto Sistemi Distribuiti 2024-2025 - TCP

Documentare qui il protocollo su socket TCP che espone il database.

Come scritto anche nel documento di consegna del progetto, si ha completa libertà su come implementare il protoccolo e i comandi del database. Alcuni suggerimenti sono:

1. Progettare un protocollo testuale (tipo HTTP), è più semplice da implementare anche se meno efficiente.
2. Dare un'occhiata al protocollo di [Redis](https://redis.io/docs/reference/protocol-spec/). Si può prendere ispirazione anche solo in alcuni punti.

Di solito il protocollo e i comandi del database sono due cose diverse. Tuttavia per il progetto, per evitare troppa complessità, si può documentare insieme il protocollo e i comandi implementati nel database.

La documentazione può variare molto in base al tipo di protocollo che si vuole costruire:

* Se è un protocollo testuale simile a quello di Redis, è necessario indicare il formato delle richieste e delle risposte, sia dei comandi sia dei dati.

* Se è un protocollo binario, è necessario specificare bene il formato di ogni pacchetto per le richieste e per le risposte, come vengono codificati i comandi e i dati.

Di seguito il template di documentazione:

## 1. Panoramica

- **Tipo:** (binario o testuale)  
- **Porta Utilizzate:**  

## 2. Struttura dei Messaggi

### 2.1. Protocollo binario

Descrivi la struttura dei messaggi binari scambiati.

| Nome Campo    | Offset (byte) | Lunghezza (byte) | Tipo Dato | Descrizione                 | Esempio Valore |
|---------------|---------------|------------------|-----------|-----------------------------|----------------|
| Header        | 0             | 2                | uint16    | Conando     | 0x0102         |
| Lung. Payload | 2             | 2                | uint16    | Lunghezza del messagio       | 0x0010         |
| Payload       | 4             | variabile        | bytes     | Dati effettivi del messaggio| ...            |

### 2.2. Protocollo testuale

Descrivi la struttura dei messaggi testuali scambiati.

- **Encoding:** (UTF-8/ASCII/Altro)
- **Fine linea:** (LF/CRLF)
- **Delimitatori Messaggio:** (es. newline, byte NULL)

**Esempio:**
```
COMANDO arg1 arg2\r\n
```

#### Comandi

| Comando | Parametri         | Descrizione                                | Esempio                |
|---------|-------------------|--------------------------------------------|------------------------|
| SET     | chiave, valore    | Imposta il valore alla chiave              | `LOGIN chiave1 42`   |
| GET     | chiave            | Restituice il valore associato alla chiave | `GET chiave1`      |
| QUIT    |                   | Chiude la connessione                      | `QUIT`                 |


## 3. Gestione degli Errori

- **Protocollo binario:**  
  - Codici errore e loro significato
  - Formato messaggio di errore

- **Protocollo testuale:**  
  - Risposte di errore (es. `ERR <codice> <descrizione>`)
  - Esempi di messaggi di errore

## 4. Scambio di Esempio

### 4.1. Protocollo binario

```
Client → Server: [Hex dump o dettaglio dei campi]
Server → Client: [Esempio di risposta]
```

### 4.2. Protocollo testuale

```
Client: SET ciao mondo
Server: OK
Client: GET ciao
Server: OK, mondo
```