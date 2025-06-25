# Progetto Sistemi Distribuiti 2024-2025 - API REST – Carta Cultura Manager

Tutte le API espongono risorse JSON e **accettano/rendono** solo `application/json` (eccetto dove diversamente indicato). Il prefisso base del servizio è implicito nelle rotte mostrate (`http://localhost:8080/`).

---

## 1. Utenti

### 1.1 `POST /api/users`
Registra un nuovo utente.

**Esempio richiesta:**
```
POST /api/users HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "name": "Mario",
  "surname": "Rossi",
  "email": "mario.rossi@example.com",
  "fiscalCode": "RSSMRA80A01H501U"
}
```

**Esempio risposta 201:**
```
HTTP/1.1 201 Created
Content-Type: application/json

{
  "name": "Mario",
  "surname": "Rossi",
  "email": "mario.rossi@example.com",
  "fiscalCode": "RSSMRA80A01H501U"
}
```

**Esempio risposta 409:**
```
HTTP/1.1 409 Conflict
Content-Type: application/json

{
  "error": "Codice fiscale già registrato"
}
```

**Esempio risposta 400:**
```
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "error": "Campi mancanti o non validi"
}
```

**Esempio risposta 500:**
```
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
  "error": "Errore interno del server"
}
```

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
* `409 Conflict` – errore: "Codice fiscale già registrato"
* `400 Bad Request` – errore: "Campi mancanti o non validi"
* `500 Internal Server Error` – errore: "Errore interno del server"

---

### 1.2 `GET /api/users/{fiscalCode}`
Recupera i dati di un utente.

**Esempio richiesta:**
```
GET /api/users/RSSMRA80A01H501U HTTP/1.1
Host: localhost:8080
Accept: application/json
```

**Esempio risposta 200:**
```
HTTP/1.1 200 OK
Content-Type: application/json

{
  "name": "Mario",
  "surname": "Rossi",
  "email": "mario.rossi@example.com",
  "fiscalCode": "RSSMRA80A01H501U"
}
```

**Esempio risposta 404:**
```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "error": "Utente non trovato"
}
```

**Esempio risposta 500:**
```
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
  "error": "Errore interno del server"
}
```

Risposte:
* `200 OK` – oggetto `User`.
* `404 Not Found` – errore: "Utente non trovato"
* `500 Internal Server Error` – errore: "Errore interno del server"

---

### 1.3 `GET /api/users/{fiscalCode}/contribution`
Restituisce lo stato del contributo residuo.

**Esempio richiesta:**
```
GET /api/users/RSSMRA80A01H501U/contribution HTTP/1.1
Host: localhost:8080
Accept: application/json
```

**Esempio risposta 200:**
```
HTTP/1.1 200 OK
Content-Type: application/json

{
  "balance": 450.00,
  "contribAllocated": 25.00,
  "contribSpent": 25.00
}
```

**Esempio risposta 500:**
```
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
  "error": "Errore interno del server"
}
```


Risposte:
* `200 OK` – stato del contributo
* `500 Internal Server Error` – errore: "Errore interno del server"

---

## 2. Voucher

### 2.1 `GET /api/users/{fiscalCode}/vouchers`
Lista di tutti i voucher di un utente.

**Esempio richiesta:**
```
GET /api/users/RSSMRA80A01H501U/vouchers HTTP/1.1
Host: localhost:8080
Accept: application/json
```

**Esempio risposta 200:**
```
HTTP/1.1 200 OK
Content-Type: application/json

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

**Esempio risposta 500:**
```
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
  "error": "Errore interno del server"
}
```

Risposte:
* `200 OK` – array di oggetti `Voucher`
* `500 Internal Server Error` – errore: "Errore interno del server"

---

### 2.2 `POST /api/users/{fiscalCode}/voucher`
Crea un nuovo voucher.

**Esempio richiesta:**
```
POST /api/users/RSSMRA80A01H501U/voucher HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "amount": 25.0,
  "category": "libri",
  "status": "generated"
}
```

**Esempio risposta 201:**
```
HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": "abc123",
  "amount": 25.0,
  "category": "libri",
  "status": "generated",
  "createdAt": "2024-06-24T10:00:00Z",
  "consumedAt": null
}
```

**Esempio risposta 400:**
```
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "error": "Saldo insufficiente"
}
```

**Esempio risposta 500:**
```
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
  "error": "Errore interno del server"
}
```

Body JSON (il campo `id` viene sempre generato dal server e **sovrascrive** qualsiasi valore passato; `status` e `createdAt` sono facoltativi – se omessi rimangono `null` nel voucher risultante):
```json
{
  "amount": 25.0,
  "category": "libri",
  "status": "generated"
}
```
Risposte:
* `201 Created` – voucher generato, restituisce l'oggetto completo
* `400 Bad Request` – errore: "Saldo insufficiente"
* `500 Internal Server Error` – errore: "Errore interno del server"

---

### 2.3 `POST /api/users/{fiscalCode}/voucher/{voucherId}`
Consuma (spende) un voucher **ancora in stato `generated`**.

**Esempio richiesta:**
```
POST /api/users/RSSMRA80A01H501U/voucher/abc123 HTTP/1.1
Host: localhost:8080
Accept: application/json
```

**Esempio risposta 200:**
```
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "abc123",
  "amount": 25.0,
  "category": "libri",
  "status": "consumed",
  "createdAt": "2024-06-24T10:00:00Z",
  "consumedAt": "2024-06-25T09:00:00Z"
}
```

**Esempio risposta 400:**
```
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "error": "Voucher già consumato"
}
```

**Esempio risposta 403:**
```
HTTP/1.1 403 Forbidden
Content-Type: application/json

{
  "error": "Operazione non permessa"
}
```

**Esempio risposta 404:**
```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "error": "Voucher non trovato"
}
```

**Esempio risposta 500:**
```
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
  "error": "Errore interno del server"
}
```

Risposte:
* `200 OK` – voucher aggiornato (`status": "consumed"`, `consumedAt` impostata)
* `400 Bad Request` – errore: "Voucher già consumato"
* `403 Forbidden` – errore: "Operazione non permessa"
* `404 Not Found` – errore: "Voucher non trovato"
* `500 Internal Server Error` – errore: "Errore interno del server"

---

### 2.4 `PUT /api/users/{fiscalCode}/voucher/{voucherId}`
Aggiorna la **categoria** di un voucher non ancora consumato.

**Esempio richiesta:**
```
PUT /api/users/RSSMRA80A01H501U/voucher/abc123 HTTP/1.1
Host: localhost:8080
Content-Type: text/plain

teatro
```

**Esempio risposta 200:**
```
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "abc123",
  "amount": 25.0,
  "category": "teatro",
  "status": "generated",
  "createdAt": "2024-06-24T10:00:00Z",
  "consumedAt": null
}
```

**Esempio risposta 400:**
```
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "error": "Voucher già consumato"
}
```

**Esempio risposta 404:**
```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "error": "Voucher non trovato"
}
```

**Esempio risposta 500:**
```
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
  "error": "Errore interno del server"
}
```

Content-Type: `text/plain` (solo la nuova categoria nel body).

Risposte:
* `200 OK` – voucher aggiornato
* `400 Bad Request` – errore: "Voucher già consumato"
* `404 Not Found` – errore: "Voucher non trovato"
* `500 Internal Server Error` – errore: "Errore interno del server"

---

### 2.5 `DELETE /api/users/{fiscalCode}/voucher/{voucherId}`
Elimina un voucher non ancora consumato.

**Esempio richiesta:**
```
DELETE /api/users/RSSMRA80A01H501U/voucher/abc123 HTTP/1.1
Host: localhost:8080
```

**Esempio risposta 204:**
```
HTTP/1.1 204 No Content
```

**Esempio risposta 400:**
```
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "error": "Voucher già consumato"
}
```

**Esempio risposta 403:**
```
HTTP/1.1 403 Forbidden
Content-Type: application/json

{
  "error": "Operazione non permessa"
}
```

**Esempio risposta 404:**
```
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "error": "Voucher non trovato"
}
```

**Esempio risposta 500:**
```
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
  "error": "Errore interno del server"
}
```

Risposte:
* `204 No Content` – eliminato
* `400 Bad Request` – errore: "Voucher già consumato"
* `403 Forbidden` – errore: "Operazione non permessa"
* `404 Not Found` – errore: "Voucher non trovato"
* `500 Internal Server Error` – errore: "Errore interno del server"

---

## 3. Statistiche di sistema

### `GET /api/system/stats`
Restituisce il riepilogo globale.

**Esempio richiesta:**
```
GET /api/system/stats HTTP/1.1
Host: localhost:8080
Accept: application/json
```

**Esempio risposta 200:**
```
HTTP/1.1 200 OK
Content-Type: application/json

{
  "userCount": 10,
  "totalAvailable": 2345.00,
  "totalAllocated": 150.00,
  "totalSpent": 105.00,
  "totalVouchers": 95,
  "vouchersConsumed": 40
}
```

**Esempio risposta 500:**
```
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
  "error": "Errore interno del server"
}
```

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
