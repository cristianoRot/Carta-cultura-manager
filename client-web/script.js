const API_BASE_URL = 'http://localhost:8080'; 
let currentFiscalCode = null;
let contributionChart = null;
let vouchersChart = null;

function displayMessage(elementId, message, isError = false, isPermanentError = false) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = message;
        element.style.color = isError ? 'red' : 'green';
        element.style.display = message ? 'block' : 'none'; 

        if (!isPermanentError) {
            setTimeout(() => {
                element.style.opacity = '0';
                setTimeout(() => {
                    element.style.display = 'none';
                    element.textContent = '';
                    element.style.opacity = '1';
                }, 500);
            }, 5000);
        }
    }
}

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

async function handleLookupUser() {
    // Svuota la visualizzazione dei buoni all'inizio di ogni ricerca utente
    document.getElementById('voucherList').innerHTML = '';

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

        if (userResponse.ok) 
        {
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
            // Ordina i buoni per data di creazione (dal più recente al più vecchio)
            vouchers.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
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
                            <button class="modifyVoucherBtn" data-voucher-id="${voucher.id}" data-current-category="${voucher.category}">Modifica</button>
                            <button class="deleteVoucherBtn" data-voucher-id="${voucher.id}"></button>
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

async function handleVoucherGeneration() {
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
        const response = await fetch(`${API_BASE_URL}/api/users/${currentFiscalCode}/voucher`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                id: "",
                amount: amount,
                category: category,
                status: "generated",
                createdAt: new Date().toISOString(),
                consumedAt: null,
                userId: currentFiscalCode
            })
        });

        switch (response.status) {
            case 201:
                displayMessage('voucherStatus', 'Buono generato con successo!', false);
                document.getElementById('generateVoucherForm').reset();
                await loadUserVouchers();
                await loadUserContribution(currentFiscalCode);
                break;
            case 400: {
                const errorData = await response.json();
                displayMessage('voucherStatus', `Errore: ${errorData.message || 'Richiesta non valida (es. fondi insufficienti).'}`, true);
                break;
            }
            case 404:
                displayMessage('voucherStatus', 'Errore: Utente non trovato per la generazione del buono.', true);
                break;
            default:
                displayMessage('voucherStatus', `Errore durante la generazione del buono: ${response.statusText}`, true);
        }
    } catch (error) {
        console.error('Generate voucher error:', error);
        displayMessage('voucherStatus', 'Errore di connessione o richiesta fallita.', true);
    }
}

async function handleConsumeVoucher(event) {
    const voucherId = event.target.dataset.voucherId;
    displayMessage('voucherStatus', '', false);

    if (!voucherId) {
        displayMessage('voucherStatus', 'ID Buono non trovato.', true);
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/api/users/${currentFiscalCode}/voucher/${voucherId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
        });

        switch (response.status) {
            case 200: {
                displayMessage('voucherStatus', `Buono ${voucherId} consumato con successo!`, false);
                await loadUserVouchers();
                await loadUserContribution(currentFiscalCode);
                break;
            }
            case 400: {
                const errorData = await response.json();
                displayMessage('voucherStatus', `Errore: ${errorData.message || 'Impossibile consumare il buono.'}`, true);
                break;
            }
            case 404:
                displayMessage('voucherStatus', 'Errore: Buono non trovato o già consumato/cancellato.', true);
                break;
            default:
                displayMessage('voucherStatus', `Errore durante il consumo del buono: ${response.statusText}`, true);
        }
    } catch (error) {
        console.error('Consume voucher error:', error);
        displayMessage('voucherStatus', 'Errore di connessione o richiesta fallita.', true);
    }
}

async function handleDeleteVoucher(event) {
    const voucherId = event.target.dataset.voucherId;
    displayMessage('voucherStatus', '', false);

    if (!voucherId) {
        displayMessage('voucherStatus', 'ID Buono non trovato.', true);
        return;
    }

    if (!confirm(`Sei sicuro di voler cancellare il buono ${voucherId}?`)) return;

    try 
    {
        const response = await fetch(`${API_BASE_URL}/api/users/${currentFiscalCode}/voucher/${voucherId}`, {
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

async function handleModifyVoucherCategory(event) {
    const voucherId = event.target.dataset.voucherId;
    const currentCategory = event.target.dataset.currentCategory;
    const voucherItem = event.target.closest('.voucher-item');

    if (voucherItem.classList.contains('is-editing')) {
        return;
    }
    voucherItem.classList.add('is-editing');

    const categorySelect = document.createElement('select');
    categorySelect.innerHTML = document.getElementById('voucherCategory').innerHTML;
    categorySelect.value = currentCategory; 

    const categoryParagraph = Array.from(voucherItem.querySelectorAll('p')).find(p => p.textContent.includes('Categoria:'));
    
    if (!categoryParagraph) {
        console.error("Elemento 'p' della categoria non trovato.");
        voucherItem.classList.remove('is-editing');
        return;
    }

    const originalCategoryText = categoryParagraph.innerHTML;
    
    const restoreUI = () => {
        categoryParagraph.innerHTML = originalCategoryText;
        categoryParagraph.classList.remove('category-edit-container');
        voucherItem.classList.remove('is-editing');
    };

    const saveButton = document.createElement('button');
    saveButton.textContent = 'Salva';
    saveButton.className = 'modifyVoucherBtn'; 

    const cancelButton = document.createElement('button');
    cancelButton.textContent = 'Annulla';
    cancelButton.className = 'cancelBtn';

    categoryParagraph.innerHTML = '';
    categoryParagraph.classList.add('category-edit-container');
    categoryParagraph.appendChild(categorySelect);
    categoryParagraph.appendChild(saveButton);
    categoryParagraph.appendChild(cancelButton);

    cancelButton.onclick = restoreUI;

    saveButton.onclick = async () => {
        const newCategory = categorySelect.value;
        if (!newCategory || newCategory === currentCategory) {
            restoreUI();
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/users/${currentFiscalCode}/voucher/${voucherId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'text/plain' },
                body: newCategory
            });

            if (response.ok) {
                displayMessage('voucherStatus', 'Categoria del buono aggiornata con successo!', false);
                await loadUserVouchers();
                await loadUserContribution(currentFiscalCode);
            } else {
                const errorData = await response.json().catch(() => null);
                const message = errorData?.message || `Errore durante la modifica: ${response.statusText}`;
                displayMessage('voucherStatus', message, true);
                restoreUI();
            }
        } catch (error) {
            console.error('Modify category error:', error);
            displayMessage('voucherStatus', 'Errore di connessione durante la modifica del buono.', true);
            restoreUI();
        }
    };
}

function renderContributionChart(stats) {
    const ctx = document.getElementById('contributionChart').getContext('2d');

    if (contributionChart) {
        contributionChart.destroy();
    }

    contributionChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Disponibile', 'Allocato', 'Speso'],
            datasets: [{
                label: 'Distribuzione Contributi',
                data: [
                    stats.totalContributionAvailable,
                    stats.totalContributionAllocated,
                    stats.totalContributionSpent
                ],
                backgroundColor: [
                    'rgba(67, 97, 238, 0.8)',
                    'rgba(255, 152, 0, 0.8)',
                    'rgba(239, 83, 80, 0.8)'
                ],
                borderColor: [
                    'rgba(67, 97, 238, 1)',
                    'rgba(255, 152, 0, 1)',
                    'rgba(239, 83, 80, 1)'
                ],
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'top',
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            let label = context.label || '';
                            if (label) {
                                label += ': ';
                            }
                            if (context.parsed !== null) {
                                label += new Intl.NumberFormat('it-IT', { style: 'currency', currency: 'EUR' }).format(context.parsed);
                            }
                            return label;
                        }
                    }
                }
            }
        }
    });
}

function renderVouchersChart(stats) {
    const ctx = document.getElementById('vouchersChart').getContext('2d');

    if (vouchersChart) {
        vouchersChart.destroy();
    }

    vouchersChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Buoni Generati', 'Buoni Consumati'],
            datasets: [{
                label: 'Numero di Buoni',
                data: [stats.totalVouchersGenerated, stats.totalVouchersConsumed],
                backgroundColor: [
                    'rgba(54, 162, 235, 0.8)',
                    'rgba(75, 192, 192, 0.8)'
                ],
                borderColor: [
                    'rgba(54, 162, 235, 1)',
                    'rgba(75, 192, 192, 1)'
                ],
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                }
            }
        }
    });
}

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

            renderContributionChart(stats);
            renderVouchersChart(stats);

            displayMessage('systemStatsStatus', '', false);
        } else {
            const errorText = `Errore nel caricamento delle statistiche.`;
            displayMessage('systemStatsStatus', errorText, true, true);
            // Nascondi i valori se c'è un errore
            const statValues = document.querySelectorAll('#systemStatsDisplay .stats-value');
            statValues.forEach(el => el.textContent = 'N/D');
        }
    } catch (error) {
        console.error('System stats fetch error:', error);
        const errorText = 'Errore di connessione durante il caricamento delle statistiche.';
        displayMessage('systemStatsStatus', errorText, true, true);
        const statValues = document.querySelectorAll('#systemStatsDisplay .stats-value');
        statValues.forEach(el => el.textContent = 'N/D');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const registerUserBtn = document.getElementById('registerUserBtn');
    if (registerUserBtn) {
        registerUserBtn.addEventListener('click', handleRegisterUser);
    }
    // Listener per il tasto Invio nel form di registrazione
    const registrationForm = document.getElementById('registrationForm');
    if (registrationForm) {
        registrationForm.addEventListener('keydown', (event) => {
            if (event.key === 'Enter') {
                event.preventDefault(); // Previene il submit di default
                handleRegisterUser();
            }
        });
    }
    const lookupUserBtn = document.getElementById('lookupUserBtn');
    if (lookupUserBtn) {
        lookupUserBtn.addEventListener('click', handleLookupUser);
    }

    const lookupFiscalCodeInput = document.getElementById('lookupFiscalCode');
    if (lookupFiscalCodeInput) {
        lookupFiscalCodeInput.addEventListener('keydown', (event) => {
            if (event.key === 'Enter') {
                event.preventDefault(); // Previene qualsiasi azione predefinita del browser
                handleLookupUser();
            }
        });
    }

    const generateVoucherBtn = document.getElementById('generateVoucherBtn');
    const voucherAmountInput = document.getElementById('voucherAmount');
    if (generateVoucherBtn) {
        generateVoucherBtn.addEventListener('click', handleVoucherGeneration);
    }

    const voucherManagementSection = document.getElementById('voucherManagement');
    if (voucherManagementSection) voucherManagementSection.style.display = 'none';

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

    const refreshStatsBtn = document.getElementById('refreshStatsBtn');
    if (refreshStatsBtn) {
        refreshStatsBtn.addEventListener('click', fetchAndDisplaySystemStats);
    }

    fetchAndDisplaySystemStats();

    const sidebarToggleBtn = document.getElementById('sidebarToggleBtn');
    const dashboardContainer = document.getElementById('dashboardContainer');

    sidebarToggleBtn.addEventListener('click', () => {
        dashboardContainer.classList.toggle('sidebar-collapsed');
    });
});

const lookupFiscalCodeInput = document.getElementById('lookupFiscalCode');
if (lookupFiscalCodeInput) {
    lookupFiscalCodeInput.addEventListener('keydown', (event) => {
        if (event.key === 'Enter') {
            event.preventDefault();
            handleLookupUser();
        }
    });
}