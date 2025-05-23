# Progetto Sistemi Distribuiti 2024-2025 - API REST

Documentare qui l'API REST progettata. Di seguito è presente un esempio.

**Attenzione**: l'unica rappresentazione ammessa è in formato JSON. Pertanto vengono assunti gli header `Content-Type: application/json` e `Accept: application/json`.

## `/contacts`

Ogni risorsa ha la sua sezione dedicata con i metodi ammessi. In questo caso si riferisce alla risorsa `/contacts`.

### GET

**Descrizione**: breve descrizione di cosa fa il metodo applicato alla risorsa. In questo caso restituisce l'elenco dei contatti presenti.

**Parametri**: elenco dei parametri. In questo caso non sono previsti. Se la risorsa fosse stata `/contacts/{id}` allora andava specificato cosa deve essere `{id}`.

**Header**: solo gli header importanti. In questo caso nessuno oltre a quelli già impostati automaticamente dal client. Si può evitare di specificare gli hader riguardanti la rappresentazione dei dati (JSON).

**Body richiesta**: cosa ci deve essere nel body della richiesta (se previsto). In questo caso nulla perché non è previsto.

**Risposta**: cosa viene restituito in caso di successo. In questo caso una lista con ogni elemento un contatto con i seguenti campi: `id` (intero), `name` (stringa) e `number` (stringa).

**Codici di stato restituiti**: elenco dei codici di stato restituiti in caso di successo e di fallimento. In questo caso restituisce sempre `200 OK`. Viene dato per assunto che in caso di problemi lato server si restituisce `500 Internal Server Error`, non è necessario specificarlo ogni volta.

### POST

**Descrizione**: aggiunge un contatto alla rubrica telefonica.

**Parametri**: nessuno.

**Header**: nessuno.

**Body richiesta**: singolo contatto con i campi `name` e `number`.

**Risposta**: body vuoto e la risorsa creata è indicata nell'header `Location`.

**Codici di stato restituiti**:

* 201 Created: successo.
* 400 Bad Request: c'è un errore del client (JSON, campo mancante o altro).