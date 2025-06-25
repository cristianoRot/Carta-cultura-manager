# Progetto Sistemi Distribuiti 2024-2025 - API REST – Carta Cultura Manager

Tutte le API espongono risorse JSON e **accettano/rendono** solo `application/json` (eccetto dove diversamente indicato). Il prefisso base del servizio è implicito nelle rotte mostrate (`http://localhost:8080/`).

---

## 1. Utenti

### 1.1 `POST /api/users`
Registra un nuovo utente.

Body JSON:
```json
{
  "name": "Mario",
  "surname": "Rossi",
  "email": "mario.rossi@example.com",
  "fiscalCode": "RSSMRA80A01H501U"
}
```
Risposte:
* `201 Created` – utente creato, contributo iniziale 500 €.
* `409 Conflict` – codice fiscale già registrato.
* `400 Bad Request` – campi mancanti/non validi.
* `500 Internal Server Error` – errore lato server.

---

### 1.2 `GET /api/users/{fiscalCode}`
Recupera i dati di un utente.

Risposte:
* `200 OK` – oggetto `User`.
* `404 Not Found` – utente inesistente.
* `500 Internal Server Error` – errore lato server.

---

### 1.3 `GET /api/users/{fiscalCode}/contribution`
Restituisce lo stato del contributo residuo.

Esempio risposta:
```json
{
  "balance": 450.00,
  "contribAllocated": 25.00,
  "contribSpent": 25.00
}
```

---

## 2. Voucher

### 2.1 `GET /api/users/{fiscalCode}/vouchers`
Lista di tutti i voucher di un utente.

Risposta `200 OK` – array di oggetti `Voucher`.

Esempio risposta:
```json
[
  {
    "id": "abc123",
    "amount": 25.0,
    "category": "libri",
    "status": "generated",
    "createdAt": "2024-06-24T10:00:00Z",
    "consumedAt": null
  },
  {
    "id": "def456",
    "amount": 25.0,
    "category": "cinema",
    "status": "consumed",
    "createdAt": "2024-06-20T10:00:00Z",
    "consumedAt": "2024-06-21T12:00:00Z"
  }
]
```

---

### 2.2 `POST /api/users/{fiscalCode}/voucher`
Crea un nuovo voucher.

Body JSON (il campo `id` viene sempre generato dal server e **sovrascrive** qualsiasi valore passato; `status` e `createdAt` sono facoltativi – se omessi rimangono `null` nel voucher risultante):
```json
{
  "amount": 25.0,
  "category": "libri",
  "status": "generated"
}
```
Risposte:
* `201 Created` – voucher generato, restituisce l'oggetto completo.
* `400 Bad Request` – importo non valido o saldo insufficiente.
* `500 Internal Server Error` – errore lato server.

---

### 2.3 `POST /api/users/{fiscalCode}/voucher/{voucherId}`
Consuma (spende) un voucher **ancora in stato `generated`**.

Risposte:
* `200 OK` – voucher aggiornato (`status": "consumed"`, `consumedAt` impostata).
* `400 Bad Request` – voucher già consumato.
* `403 Forbidden` – operazione non permessa.
* `404 Not Found` – voucher non trovato.
* `500 Internal Server Error` – errore lato server.

---

### 2.4 `PUT /api/users/{fiscalCode}/voucher/{voucherId}`
Aggiorna la **categoria** di un voucher non ancora consumato.

Content-Type: `text/plain` (solo la nuova categoria nel body).

Risposte:
* `200 OK` – voucher aggiornato.
* `400 Bad Request` – voucher consumato.
* `404 Not Found` – voucher non trovato.
* `500 Internal Server Error` – errore lato server.

---

### 2.5 `DELETE /api/users/{fiscalCode}/voucher/{voucherId}`
Elimina un voucher non ancora consumato.

Risposte:
* `204 No Content` – eliminato.
* `400 Bad Request` – voucher consumato.
* `404 Not Found` – voucher non trovato.
* `500 Internal Server Error` – errore lato server.

---

## 3. Statistiche di sistema

### `GET /api/system/stats`
Restituisce il riepilogo globale.

Esempio risposta:
```json
{
  "userCount": 10,
  "totalAvailable": 2345.00,
  "totalAllocated": 150.00,
  "totalSpent": 105.00,
  "totalVouchers": 95,
  "vouchersConsumed": 40
}
```

---

## 4. Codici di stato riassuntivi
| Codice | Significato |
|--------|-------------|
| 200 OK | Operazione riuscita |
| 201 Created | Risorsa creata |
| 204 No Content | Operazione riuscita, nessun corpo |
| 400 Bad Request | Richiesta malformata o non permessa |
| 404 Not Found | Risorsa inesistente |
| 409 Conflict | Violazione vincoli unici |
| 500 Internal Server Error | Errore lato server |
