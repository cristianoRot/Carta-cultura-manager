console.log("Script client-web caricato.");

const API_BASE_URL = 'http://localhost:8080'; 
let currentFiscalCode = null;

/**
 * Mostra un messaggio in un elemento specificato, opzionalmente stilizzandolo come errore.
 * @param {string} elementId - L'ID dell'elemento HTML in cui mostrare il messaggio.
 * @param {string} message - Il messaggio da mostrare.
 * @param {boolean} isError - Se il messaggio rappresenta un errore.
 */
function displayMessage(elementId, message, isError = false) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = message;
        element.style.color = isError ? 'red' : 'green';
        element.style.display = message ? 'block' : 'none'; 
    }
}

/**
 * Gestisce il processo di registrazione utente.
 */
async function handleRegisterUser() {
    const name = document.getElementById('regName').value;
    const surname = document.getElementById('regSurname').value;
    const email = document.getElementById('regEmail').value;
    const fiscalCode = document.getElementById('regFiscalCode').value;

    displayMessage('registrationStatus', '', false); 

    if (!name || !surname || !email || !fiscalCode) {
        displayMessage('registrationStatus', 'Tutti i campi sono obbligatori.', true);
        return;
    }
    if (fiscalCode.length !== 16) {
        displayMessage('registrationStatus', 'Il Codice Fiscale deve essere di 16 caratteri.', true);
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/api/users`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, surname, email, fiscalCode }),
        });

        if (response.ok) {
            displayMessage('registrationStatus', `Utente registrato con successo! Codice Fiscale: ${fiscalCode}`, false);
            document.getElementById('registrationForm').reset();
        } else if (response.status === 409) {
            displayMessage('registrationStatus', 'Errore: Utente con questo Codice Fiscale già esistente.', true);
        } else if (response.status === 400) {
            const errorData = await response.json();
            displayMessage('registrationStatus', `Errore di validazione: ${errorData.message || 'Dati non validi.'}`, true);
        } else {
            displayMessage('registrationStatus', `Errore durante la registrazione: ${response.statusText} (Code: ${response.status})`, true);
        }
    } catch (error) {
        console.error('Registration error:', error);
        displayMessage('registrationStatus', 'Errore di connessione o richiesta fallita.', true);
    }
}

/**
 * Carica e mostra lo stato del contributo per un dato codice fiscale.
 * @param {string} fiscalCode - Il codice fiscale dell'utente.
 */
async function loadUserContribution(fiscalCode) {
    if (!fiscalCode) return;

    try {
        const balanceResponse = await fetch(`${API_BASE_URL}/api/users/${fiscalCode}/contribution`);

        if (balanceResponse.ok) 
        {
            const contribution = await balanceResponse.json();
            document.getElementById('contribAvailable').textContent = contribution.balance.toFixed(2);
            document.getElementById('contribAllocated').textContent = contribution.contribAllocated.toFixed(2);
            document.getElementById('contribSpent').textContent = contribution.contribSpent.toFixed(2);
        } 
        else 
        {
            console.error(`Errore nel recupero del contributo: ${balanceResponse.statusText}`);
            document.getElementById('contribAvailable').textContent = 'N/D';
            document.getElementById('contribAllocated').textContent = 'N/D';
            document.getElementById('contribSpent').textContent = 'N/D';
            displayMessage('lookupStatus', `Dati contributo non disponibili (Status: ${balanceResponse.status})`, true);
        }
    } catch (error) {
        console.error('Contribution fetch error:', error);
        displayMessage('lookupStatus', 'Errore di connessione nel recupero del contributo.', true);
    }
}

/**
 * Gestisce la ricerca utente e il recupero dello stato del contributo.
 */
async function handleLookupUser() {
    const fiscalCodeInput = document.getElementById('lookupFiscalCode').value;
    const userDataDisplayDiv = document.getElementById('userDataDisplay');
    const voucherListDiv = document.getElementById('voucherList');
    const voucherManagementSection = document.getElementById('voucherManagement');

    displayMessage('lookupStatus', '', false);
    userDataDisplayDiv.style.display = 'none';
    voucherListDiv.innerHTML = '';
    currentFiscalCode = null;
    if (voucherManagementSection) voucherManagementSection.style.display = 'none';


    if (!fiscalCodeInput) {
        displayMessage('lookupStatus', 'Inserire un Codice Fiscale per la ricerca.', true);
        return;
    }
    if (fiscalCodeInput.length !== 16) {
        displayMessage('lookupStatus', 'Il Codice Fiscale deve essere di 16 caratteri.', true);
        return;
    }

    try {
        const userResponse = await fetch(`${API_BASE_URL}/api/users/${fiscalCodeInput}`);
        if (userResponse.ok) {
            const user = await userResponse.json();
            document.getElementById('userName').textContent = user.name;
            document.getElementById('userSurname').textContent = user.surname;
            document.getElementById('userEmail').textContent = user.email;
            userDataDisplayDiv.style.display = 'block';
            currentFiscalCode = fiscalCodeInput;

            await loadUserContribution(currentFiscalCode);
            await loadUserVouchers();
            if (voucherManagementSection) voucherManagementSection.style.display = 'block';

        } else if (userResponse.status === 404) {
            displayMessage('lookupStatus', 'Utente non trovato.', true);
        } else {
            displayMessage('lookupStatus', `Errore nella ricerca dell'utente: ${userResponse.statusText}`, true);
        }
    } catch (error) {
        console.error('Lookup error:', error);
        displayMessage('lookupStatus', 'Errore di connessione o richiesta fallita durante la ricerca.', true);
    }
}

/**
 * Carica e mostra i buoni per l'utente corrente.
 */
async function loadUserVouchers() {
    const voucherListDiv = document.getElementById('voucherList');
    displayMessage('voucherStatus', '', false);

    if (!currentFiscalCode) {
        voucherListDiv.innerHTML = '<p>Nessun utente selezionato per visualizzare i buoni.</p>';
        return;
    }

    voucherListDiv.innerHTML = '<p>Caricamento buoni...</p>';

    try {
        const response = await fetch(`${API_BASE_URL}/api/users/${currentFiscalCode}/vouchers`);
        if (response.ok) {
            const vouchers = await response.json();
            if (vouchers.length === 0) {
                voucherListDiv.innerHTML = '<p>Nessun buono trovato per questo utente.</p>';
                return;
            }

            voucherListDiv.innerHTML = '';
            vouchers.forEach(voucher => {
                const voucherElement = document.createElement('div');
                voucherElement.classList.add('voucher-item');
                voucherElement.innerHTML = `
                    <p><strong>ID:</strong> ${voucher.id}</p>
                    <p><strong>Importo:</strong> €${voucher.amount.toFixed(2)}</p>
                    <p><strong>Categoria:</strong> ${voucher.category}</p>
                    <p><strong>Stato:</strong> ${voucher.status}</p>
                    <p><strong>Data Creazione:</strong> ${new Date(voucher.createdAt).toLocaleDateString()}</p>
                    ${voucher.status === 'consumed' && voucher.consumedAt ? `<p><strong>Data Consumo:</strong> ${new Date(voucher.consumedAt).toLocaleDateString()}</p>` : ''}
                    <div class="voucher-actions">
                        ${voucher.status === 'generated' ? `
                            <button class="consumeVoucherBtn" data-voucher-id="${voucher.id}">Consuma</button>
                            <button class="deleteVoucherBtn" data-voucher-id="${voucher.id}">Cancella</button>
                            <button class="modifyVoucherBtn" data-voucher-id="${voucher.id}" data-current-category="${voucher.category}">Modifica Categoria</button>
                        ` : ''}
                         ${voucher.status === 'consumed' ? `
                            <p><i>Buono già utilizzato.</i></p>
                            ${!voucher.consumedAt ? '<p><i>Data consumo non disponibile.</i></p>' : ''} 
                        ` : ''}
                    </div>
                `;
                voucherListDiv.appendChild(voucherElement);
            });
        } else {
            displayMessage('voucherStatus', `Errore nel caricamento dei buoni: ${response.statusText}`, true);
            voucherListDiv.innerHTML = '<p>Errore nel caricamento dei buoni.</p>';
        }
    } catch (error) {
        console.error('Voucher load error:', error);
        displayMessage('voucherStatus', 'Errore di connessione durante il caricamento dei buoni.', true);
        voucherListDiv.innerHTML = '<p>Errore di connessione.</p>';
    }
}

/**
 * Gestisce la generazione di un nuovo buono.
 */
async function handleGenerateVoucher() {
    const amountInput = document.getElementById('voucherAmount');
    const categoryInput = document.getElementById('voucherCategory');
    const amount = parseFloat(amountInput.value);
    const category = categoryInput.value;

    displayMessage('voucherStatus', '', false);

    if (!currentFiscalCode) {
        displayMessage('voucherStatus', 'Nessun utente selezionato. Cerca un utente prima di generare un buono.', true);
        return;
    }
    if (isNaN(amount) || amount <= 0) {
        displayMessage('voucherStatus', 'Importo non valido.', true);
        return;
    }
    if (!category) {
        displayMessage('voucherStatus', 'Seleziona una categoria.', true);
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/api/vouchers`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ amount, category, userId: currentFiscalCode }),
        });

        if (response.status === 201) {
            displayMessage('voucherStatus', 'Buono generato con successo!', false);
            document.getElementById('generateVoucherForm').reset();
            await loadUserVouchers();
            await loadUserContribution(currentFiscalCode);
        } else if (response.status === 400) {
            const errorData = await response.json();
            displayMessage('voucherStatus', `Errore: ${errorData.message || 'Richiesta non valida (es. fondi insufficienti).'}`, true);
        } else if (response.status === 404) {
            displayMessage('voucherStatus', 'Errore: Utente non trovato per la generazione del buono.', true);
        }
        else {
            displayMessage('voucherStatus', `Errore durante la generazione del buono: ${response.statusText}`, true);
        }
    } catch (error) {
        console.error('Generate voucher error:', error);
        displayMessage('voucherStatus', 'Errore di connessione o richiesta fallita.', true);
    }
}

/**
 * Gestisce il consumo di un buono.
 * @param {Event} event - L'evento click dal pulsante "consuma".
 */
async function handleConsumeVoucher(event) {
    const voucherId = event.target.dataset.voucherId;
    displayMessage('voucherStatus', '', false);

    if (!voucherId) {
        displayMessage('voucherStatus', 'ID Buono non trovato.', true);
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/api/vouchers/${voucherId}/consume`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
        });

        if (response.ok) {
            displayMessage('voucherStatus', `Buono ${voucherId} consumato con successo!`, false);
            await loadUserVouchers();
            await loadUserContribution(currentFiscalCode);
        } else if (response.status === 400) {
            const errorData = await response.json();
            displayMessage('voucherStatus', `Errore: ${errorData.message || 'Impossibile consumare il buono.'}`, true);
        } else if (response.status === 404) {
            displayMessage('voucherStatus', 'Errore: Buono non trovato o già consumato/cancellato.', true);
        }
        else {
            displayMessage('voucherStatus', `Errore durante il consumo del buono: ${response.statusText}`, true);
        }
    } catch (error) {
        console.error('Consume voucher error:', error);
        displayMessage('voucherStatus', 'Errore di connessione o richiesta fallita.', true);
    }
}

/**
 * Gestisce l'eliminazione di un buono.
 * @param {Event} event - L'evento click dal pulsante "cancella".
 */
async function handleDeleteVoucher(event) {
    const voucherId = event.target.dataset.voucherId;
    displayMessage('voucherStatus', '', false);

    if (!voucherId) {
        displayMessage('voucherStatus', 'ID Buono non trovato.', true);
        return;
    }

    if (!confirm(`Sei sicuro di voler cancellare il buono ${voucherId}?`)) return;


    try {
        const response = await fetch(`${API_BASE_URL}/api/vouchers/${voucherId}`, {
            method: 'DELETE',
        });

        if (response.status === 204) {
            displayMessage('voucherStatus', `Buono ${voucherId} cancellato con successo!`, false);
            await loadUserVouchers();
            await loadUserContribution(currentFiscalCode);
        } else if (response.status === 400) {
            const errorData = await response.json();
            displayMessage('voucherStatus', `Errore: ${errorData.message || 'Impossibile cancellare il buono (es. già speso).'}`, true);
        } else if (response.status === 404) {
            displayMessage('voucherStatus', 'Errore: Buono non trovato.', true);
        }
        else {
            displayMessage('voucherStatus', `Errore durante la cancellazione: ${response.statusText}`, true);
        }
    } catch (error) {
        console.error('Delete voucher error:', error);
        displayMessage('voucherStatus', 'Errore di connessione o richiesta fallita.', true);
    }
}

/**
 * Gestisce la modifica della categoria di un buono.
 * @param {Event} event - L'evento click dal pulsante "modifica".
 */
async function handleModifyVoucherCategory(event) {
    const voucherId = event.target.dataset.voucherId;
    const currentCategory = event.target.dataset.currentCategory;
    displayMessage('voucherStatus', '', false);


    if (!voucherId) {
        displayMessage('voucherStatus', 'ID Buono non trovato.', true);
        return;
    }

    const newCategory = prompt(`Modifica la categoria per il buono ${voucherId}.\nCategoria attuale: ${currentCategory}\nNuova categoria (es. cinema, musica, libri):`);

    if (newCategory === null || newCategory.trim() === '') {
        displayMessage('voucherStatus', 'Modifica annullata o categoria non valida.', false);
        return;
    }

    const availableCategories = Array.from(document.getElementById('voucherCategory').options).map(opt => opt.value).filter(val => val);
    if (!availableCategories.includes(newCategory.trim().toLowerCase())) {
        displayMessage('voucherStatus', `Categoria "${newCategory}" non valida. Scegli tra: ${availableCategories.join(', ')}.`, true);
        return;
    }


    try {
        const response = await fetch(`${API_BASE_URL}/api/vouchers/${voucherId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ category: newCategory.trim().toLowerCase() }),
        });

        if (response.ok) {
            displayMessage('voucherStatus', `Categoria del buono ${voucherId} modificata con successo!`, false);
            await loadUserVouchers();
        } else if (response.status === 400) {
            const errorData = await response.json();
            displayMessage('voucherStatus', `Errore: ${errorData.message || 'Impossibile modificare la categoria (es. buono non modificabile).'}`, true);
        } else if (response.status === 404) {
            displayMessage('voucherStatus', 'Errore: Buono non trovato.', true);
        }
        else {
            displayMessage('voucherStatus', `Errore durante la modifica: ${response.statusText}`, true);
        }
    } catch (error) {
        console.error('Modify voucher error:', error);
        displayMessage('voucherStatus', 'Errore di connessione o richiesta fallita.', true);
    }
}

/**
 * Recupera e mostra le statistiche globali del sistema.
 */
async function fetchAndDisplaySystemStats() {
    displayMessage('systemStatsStatus', 'Caricamento statistiche...', false);
    try {
        const response = await fetch(`${API_BASE_URL}/api/system/stats`);
        if (response.ok) {
            const stats = await response.json();

            document.getElementById('statsTotalUsers').textContent = stats.totalUsers;
            document.getElementById('statsTotalContribAvailable').textContent = stats.totalContributionAvailable.toFixed(2);
            document.getElementById('statsTotalContribAllocated').textContent = stats.totalContributionAllocated.toFixed(2);
            document.getElementById('statsTotalContribSpent').textContent = stats.totalContributionSpent.toFixed(2);
            document.getElementById('statsTotalVouchersGenerated').textContent = stats.totalVouchersGenerated;
            document.getElementById('statsTotalVouchersConsumed').textContent = stats.totalVouchersConsumed;

            // Aggiorna barre grafiche
            updateStatBars(stats);

            displayMessage('systemStatsStatus', '', false);
        } else {
            displayMessage('systemStatsStatus', `Errore nel caricamento delle statistiche: ${response.statusText} (Code: ${response.status})`, true);
        }
    } catch (error) {
        console.error('System stats fetch error:', error);
        displayMessage('systemStatsStatus', 'Errore di connessione durante il caricamento delle statistiche.', true);
    }
}

/**
 * Aggiorna la larghezza delle barre grafiche in base ai valori ricevuti.
 * @param {Object} stats - Oggetto contenente le statistiche del sistema.
 */
function updateStatBars(stats) {
    // Utenti: normalizza su 1000 per esempio
    const maxUsers = 1000;
    const usersPerc = Math.min((stats.totalUsers / maxUsers) * 100, 100);
    document.getElementById('barTotalUsers').style.width = usersPerc + '%';

    // Contributi
    const contribValues = [stats.totalContributionAvailable, stats.totalContributionAllocated, stats.totalContributionSpent];
    const maxContrib = Math.max(...contribValues, 1); // evita divisione per zero
    document.getElementById('barContribAvailable').style.width = ((stats.totalContributionAvailable / maxContrib) * 100) + '%';
    document.getElementById('barContribAllocated').style.width = ((stats.totalContributionAllocated / maxContrib) * 100) + '%';
    document.getElementById('barContribSpent').style.width = ((stats.totalContributionSpent / maxContrib) * 100) + '%';

    // Vouchers
    const voucherValues = [stats.totalVouchersGenerated, stats.totalVouchersConsumed];
    const maxVouchers = Math.max(...voucherValues, 1);
    document.getElementById('barVouchersGenerated').style.width = ((stats.totalVouchersGenerated / maxVouchers) * 100) + '%';
    document.getElementById('barVouchersConsumed').style.width = ((stats.totalVouchersConsumed / maxVouchers) * 100) + '%';
}

// =====================
// Event listeners globali
// =====================

document.addEventListener('DOMContentLoaded', () => {
    // Registrazione utente
    const registerUserBtn = document.getElementById('registerUserBtn');
    if (registerUserBtn) {
        registerUserBtn.addEventListener('click', handleRegisterUser);
    }

    // Ricerca utente
    const lookupUserBtn = document.getElementById('lookupUserBtn');
    if (lookupUserBtn) {
        lookupUserBtn.addEventListener('click', handleLookupUser);
    }

    // Generazione buono
    const generateVoucherBtn = document.getElementById('generateVoucherBtn');
    if (generateVoucherBtn) {
        generateVoucherBtn.addEventListener('click', handleGenerateVoucher);
    }

    const voucherManagementSection = document.getElementById('voucherManagement');
    if (voucherManagementSection) voucherManagementSection.style.display = 'none';

    // Delegazione eventi per le azioni sui buoni
    const voucherListDiv = document.getElementById('voucherList');
    if (voucherListDiv) {
        voucherListDiv.addEventListener('click', function (event) {
            if (event.target.classList.contains('consumeVoucherBtn')) {
                handleConsumeVoucher(event);
            } else if (event.target.classList.contains('deleteVoucherBtn')) {
                handleDeleteVoucher(event);
            } else if (event.target.classList.contains('modifyVoucherBtn')) {
                handleModifyVoucherCategory(event);
            }
        });
    }

    // Statistiche di sistema
    const refreshStatsBtn = document.getElementById('refreshStatsBtn');
    if (refreshStatsBtn) {
        refreshStatsBtn.addEventListener('click', fetchAndDisplaySystemStats);
    }
    // Caricamento iniziale delle statistiche di sistema
    fetchAndDisplaySystemStats();
});