// Main JavaScript file for the client application
console.log("Client-web script loaded.");

const API_BASE_URL = 'http://localhost:8080'; // Assuming the backend runs on port 8080
let currentFiscalCode = null;

/**
 * Displays a message in a specified element, optionally styling it as an error.
 * @param {string} elementId - The ID of the HTML element to display the message in.
 * @param {string} message - The message to display.
 * @param {boolean} isError - Whether the message represents an error.
 */
function displayMessage(elementId, message, isError = false) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = message;
        element.style.color = isError ? 'red' : 'green';
        element.style.display = message ? 'block' : 'none'; // Show or hide based on message
    }
}

/**
 * Handles the user registration process.
 */
async function handleRegisterUser() {
    const name = document.getElementById('regName').value;
    const surname = document.getElementById('regSurname').value;
    const email = document.getElementById('regEmail').value;
    const fiscalCode = document.getElementById('regFiscalCode').value;

    displayMessage('registrationStatus', '', false); // Clear previous messages

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
            const result = await response.json();
            displayMessage('registrationStatus', `Utente registrato con successo! Codice Fiscale: ${result.fiscalCode}`, false);
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
 * Loads and displays the contribution status for a given fiscal code.
 * @param {string} fiscalCode - The fiscal code of the user.
 */
async function loadUserContribution(fiscalCode) {
    if (!fiscalCode) return;

    try {
        const contributionResponse = await fetch(`${API_BASE_URL}/api/users/${fiscalCode}/contribution`);
        if (contributionResponse.ok) {
            const contribution = await contributionResponse.json();
            document.getElementById('contribAvailable').textContent = contribution.available.toFixed(2);
            document.getElementById('contribAllocated').textContent = contribution.allocated.toFixed(2);
            document.getElementById('contribSpent').textContent = contribution.spent.toFixed(2);
            document.getElementById('contribTotal').textContent = contribution.total.toFixed(2);
        } else {
            console.error(`Errore nel recupero del contributo: ${contributionResponse.statusText}`);
            document.getElementById('contribAvailable').textContent = 'N/D';
            document.getElementById('contribAllocated').textContent = 'N/D';
            document.getElementById('contribSpent').textContent = 'N/D';
            document.getElementById('contribTotal').textContent = 'N/D';
            displayMessage('lookupStatus', `Dati contributo non disponibili (Status: ${contributionResponse.status})`, true);
        }
    } catch (error) {
        console.error('Contribution fetch error:', error);
        displayMessage('lookupStatus', 'Errore di connessione nel recupero del contributo.', true);
    }
}


/**
 * Handles the user lookup and contribution status retrieval.
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
 * Loads and displays vouchers for the current user.
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
 * Handles the generation of a new voucher.
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
 * Handles consuming a voucher.
 * @param {Event} event - The click event from the consume button.
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
 * Handles deleting a voucher.
 * @param {Event} event - The click event from the delete button.
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
 * Handles modifying a voucher's category.
 * @param {Event} event - The click event from the modify button.
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
 * Fetches and displays global system statistics.
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

            displayMessage('systemStatsStatus', '', false); // Clear loading/error message
        } else {
            displayMessage('systemStatsStatus', `Errore nel caricamento delle statistiche: ${response.statusText} (Code: ${response.status})`, true);
        }
    } catch (error) {
        console.error('System stats fetch error:', error);
        displayMessage('systemStatsStatus', 'Errore di connessione durante il caricamento delle statistiche.', true);
    }
}


document.addEventListener('DOMContentLoaded', () => {
    // User registration
    const registerUserBtn = document.getElementById('registerUserBtn');
    if (registerUserBtn) {
        registerUserBtn.addEventListener('click', handleRegisterUser);
    }

    // User lookup
    const lookupUserBtn = document.getElementById('lookupUserBtn');
    if (lookupUserBtn) {
        lookupUserBtn.addEventListener('click', handleLookupUser);
    }

    // Generate voucher
    const generateVoucherBtn = document.getElementById('generateVoucherBtn');
    if (generateVoucherBtn) {
        generateVoucherBtn.addEventListener('click', handleGenerateVoucher);
    }

    const voucherManagementSection = document.getElementById('voucherManagement');
    if (voucherManagementSection) voucherManagementSection.style.display = 'none';


    // Event delegation for voucher actions
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

    // System Statistics
    const refreshStatsBtn = document.getElementById('refreshStatsBtn');
    if (refreshStatsBtn) {
        refreshStatsBtn.addEventListener('click', fetchAndDisplaySystemStats);
    }
    // Initial load of system statistics
    fetchAndDisplaySystemStats();
});
