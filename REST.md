
# Progetto Sistemi Distribuiti 2024-2025 - API REST

L'API REST implementata permette la gestione degli utenti, dei buoni e delle statistiche nel sistema Carta Cultura Giovani.

**Attenzione**: l'unica rappresentazione ammessa è in formato JSON. Pertanto vengono assunti gli header `Content-Type: application/json` e `Accept: application/json`.

## `/api/users`

### POST

**Descrizione**: Registra un nuovo utente nel sistema.

**Parametri**: Nessuno.

**Body richiesta**: Un oggetto utente con i seguenti campi:
- `name` (string): nome dell'utente
- `surname` (string): cognome dell'utente
- `email` (string): email dell'utente
- `fiscalCode` (string): codice fiscale dell'utente

**Risposta**: L'utente creato con l'ID assegnato. Il contributo iniziale di 500€ viene automaticamente creato.

**Codici di stato restituiti**:
- 201 Created: utente registrato con successo.
- 409 Conflict: utente con lo stesso codice fiscale già esistente.
- 400 Bad Request: dati mancanti o non validi.

## `/api/users/{fiscalCode}`

### GET

**Descrizione**: Recupera i dati di un utente tramite il suo codice fiscale.

**Parametri**: `fiscalCode` - Il codice fiscale dell'utente.

**Risposta**: Un oggetto utente con i campi id, name, surname, email e fiscalCode.

**Codici di stato restituiti**:
- 200 OK: utente trovato.
- 404 Not Found: utente non trovato.

## `/api/users/{userId}/contribution`

### GET

**Descrizione**: Ottiene lo stato del contributo di un utente.

**Parametri**: `userId` - L'ID dell'utente (codice fiscale).

**Risposta**: Un oggetto con i dettagli del contributo:
- `userId` (string): ID dell'utente
- `available` (number): importo disponibile per nuovi buoni
- `allocated` (number): importo allocato in buoni non ancora consumati
- `spent` (number): importo speso in buoni consumati
- `total` (number): importo totale del contributo (500€)

**Codici di stato restituiti**:
- 200 OK: contributo trovato.
- 404 Not Found: contributo non trovato.

## `/api/users/{userId}/vouchers`

### GET

**Descrizione**: Ottiene la lista dei buoni di un utente.

**Parametri**: `userId` - L'ID dell'utente (codice fiscale).

**Risposta**: Un array di oggetti buono, ciascuno con i seguenti campi:
- `id` (string): ID del buono
- `amount` (number): importo del buono
- `category` (string): categoria del buono
- `status` (string): stato del buono ('generated' o 'consumed')
- `createdAt` (string): data di creazione del buono
- `consumedAt` (string, opzionale): data di consumo del buono
- `userId` (string): ID dell'utente proprietario del buono

**Codici di stato restituiti**:
- 200 OK: lista dei buoni ottenuta.

## `/api/vouchers`

### POST

**Descrizione**: Crea un nuovo buono per un utente.

**Parametri**: Nessuno.

**Body richiesta**: Un oggetto con i seguenti campi:
- `amount` (number): importo del buono
- `category` (string): categoria del buono
- `userId` (string): ID dell'utente proprietario del buono

**Risposta**: Il buono creato con tutti i suoi dettagli.

**Codici di stato restituiti**:
- 201 Created: buono creato con successo.
- 400 Bad Request: importo non valido o superiore al contributo disponibile.
- 404 Not Found: utente non trovato.

## `/api/vouchers/{voucherId}`

### PUT

**Descrizione**: Modifica un buono esistente.

**Parametri**: `voucherId` - L'ID del buono da modificare.

**Body richiesta**: Un oggetto con il seguente campo:
- `category` (string): nuova categoria del buono

**Risposta**: Il buono aggiornato con tutti i suoi dettagli.

**Codici di stato restituiti**:
- 200 OK: buono aggiornato con successo.
- 400 Bad Request: impossibile modificare un buono già consumato.
- 404 Not Found: buono non trovato.

### DELETE

**Descrizione**: Elimina un buono esistente.

**Parametri**: `voucherId` - L'ID del buono da eliminare.

**Risposta**: Nessun contenuto.

**Codici di stato restituiti**:
- 204 No Content: buono eliminato con successo.
- 400 Bad Request: impossibile eliminare un buono già consumato.
- 404 Not Found: buono non trovato.

## `/api/vouchers/{voucherId}/consume`

### POST

**Descrizione**: Segna un buono come consumato.

**Parametri**: `voucherId` - L'ID del buono da consumare.

**Risposta**: Il buono aggiornato con tutti i suoi dettagli.

**Codici di stato restituiti**:
- 200 OK: buono consumato con successo.
- 400 Bad Request: buono già consumato.
- 404 Not Found: buono non trovato.

## `/api/system/stats`

### GET

**Descrizione**: Ottiene le statistiche globali del sistema.

**Parametri**: Nessuno.

**Risposta**: Un oggetto con i seguenti campi:
- `totalUsers` (number): numero totale di utenti registrati
- `totalContributionAvailable` (number): somma di tutti i contributi disponibili
- `totalContributionAllocated` (number): somma di tutti i contributi allocati
- `totalContributionSpent` (number): somma di tutti i contributi spesi
- `totalVouchersGenerated` (number): numero totale di buoni generati
- `totalVouchersConsumed` (number): numero totale di buoni consumati

**Codici di stato restituiti**:
- 200 OK: statistiche ottenute con successo.
