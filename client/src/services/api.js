import { INITIAL_ACCOUNTS, INITIAL_TRANSACTIONS } from './mockData';
export const API_CONFIG = {
    USE_MOCK_API: true,
    BASE_URL: 'http://localhost:8080/api'
};

const initStorage = () => {
    if (!localStorage.getItem('bank_accounts')) {
        localStorage.setItem('bank_accounts', JSON.stringify(INITIAL_ACCOUNTS));
    }
    if (!localStorage.getItem('bank_transactions')) {
        localStorage.setItem('bank_transactions', JSON.stringify(INITIAL_TRANSACTIONS));
    }
};

initStorage();

const getStoredAccounts = () => {
    const data = localStorage.getItem('bank_accounts');
    if (!data) {
        localStorage.setItem('bank_accounts', JSON.stringify(INITIAL_ACCOUNTS));
        return INITIAL_ACCOUNTS;
    }
    return JSON.parse(data);
};

const setStoredAccounts = (accounts) => localStorage.setItem('bank_accounts', JSON.stringify(accounts));

const getStoredTransactions = () => {
    const data = localStorage.getItem('bank_transactions');
    if (!data) {
        localStorage.setItem('bank_transactions', JSON.stringify(INITIAL_TRANSACTIONS));
        return INITIAL_TRANSACTIONS;
    }
    return JSON.parse(data);
};

const setStoredTransactions = (txns) => localStorage.setItem('bank_transactions', JSON.stringify(txns));

export const bankApi = {
    login: async (id, password) => {
        if (API_CONFIG.USE_MOCK_API) {
            const accounts = getStoredAccounts();
            const user = accounts.find(a => a.id.toLowerCase() === id.toLowerCase() && a.password === password);
            if (!user) {
                throw new Error('Invalid account ID or password.');
            }
            return user;
        } else {
            const res = await fetch(`${API_CONFIG.BASE_URL}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ id, password })
            });
            if (!res.ok) throw new Error('Authentication failed');
            return await res.json();
        }
    },
    getAccounts: async () => {
        if (API_CONFIG.USE_MOCK_API) {
            return getStoredAccounts();
        } else {
            const res = await fetch(`${API_CONFIG.BASE_URL}/accounts`);
            if (!res.ok) throw new Error('Failed to fetch accounts');
            return await res.json();
        }
    },

    getAccountById: async (id) => {
        if (API_CONFIG.USE_MOCK_API) {
            const accounts = getStoredAccounts();
            const account = accounts.find(a => a.id === id);
            if (!account) throw new Error('Account not found');
            return account;
        } else {
            const res = await fetch(`${API_CONFIG.BASE_URL}/accounts/${id}`);
            if (!res.ok) throw new Error('Failed to fetch account details');
            return await res.json();
        }
    },

    createAccount: async (accountData) => {
        if (API_CONFIG.USE_MOCK_API) {
            const accounts = getStoredAccounts();
            if (accounts.some(a => a.id === accountData.id)) {
                throw new Error(`Account ID '${accountData.id}' already exists.`);
            }
            const newAccount = {
                ...accountData,
                role: accountData.role || 'USER',
                balance: parseFloat(accountData.initialBalance) || 0.00,
                dbCrIndicator: 'CR',
                lastBalanceDate: new Date().toISOString().split('T')[0]
            };
            accounts.push(newAccount);
            setStoredAccounts(accounts);
            if (newAccount.balance > 0) {
                const txns = getStoredTransactions();
                txns.unshift({
                    id: `TXN-${Math.floor(1000 + Math.random() * 9000)}`,
                    accountId: newAccount.id,
                    date: new Date().toISOString(),
                    description: 'Account Opening Deposit',
                    amount: newAccount.balance,
                    dbCrIndicator: 'CR'
                });
                setStoredTransactions(txns);
            }

            return newAccount;
        } else {
            const res = await fetch(`${API_CONFIG.BASE_URL}/accounts`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(accountData)
            });
            if (!res.ok) throw new Error('Failed to create account');
            return await res.json();
        }
    },

    updateAccount: async (id, updatedData) => {
        if (API_CONFIG.USE_MOCK_API) {
            const accounts = getStoredAccounts();
            const index = accounts.findIndex(a => a.id === id);
            if (index === -1) throw new Error('Account not found');
            accounts[index] = { ...accounts[index], ...updatedData };
            setStoredAccounts(accounts);
            return accounts[index];
        } else {
            const res = await fetch(`${API_CONFIG.BASE_URL}/accounts/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(updatedData)
            });
            if (!res.ok) throw new Error('Failed to update account');
            return await res.json();
        }
    },

    deleteAccount: async (id) => {
        if (API_CONFIG.USE_MOCK_API) {
            let accounts = getStoredAccounts();
            accounts = accounts.filter(a => a.id !== id);
            setStoredAccounts(accounts);
            let txns = getStoredTransactions();
            txns = txns.filter(t => t.accountId !== id);
            setStoredTransactions(txns);
            return { success: true, message: `Account ${id} deleted` };
        } else {
            const res = await fetch(`${API_CONFIG.BASE_URL}/accounts/${id}`, {
                method: 'DELETE'
            });
            if (!res.ok) throw new Error('Failed to delete account');
            return await res.json();
        }
    },
    getBalance: async (accountId) => {
        if (API_CONFIG.USE_MOCK_API) {
            const account = await bankApi.getAccountById(accountId);
            return {
                accountId: account.id,
                date: account.lastBalanceDate || new Date().toISOString().split('T')[0],
                amount: account.balance,
                dbCrIndicator: account.dbCrIndicator || 'CR'
            };
        } else {
            const res = await fetch(`${API_CONFIG.BASE_URL}/accounts/${accountId}/balance`);
            if (!res.ok) throw new Error('Failed to fetch balance');
            return await res.json();
        }
    },

    getTransactions: async (accountId) => {
        if (API_CONFIG.USE_MOCK_API) {
            const txns = getStoredTransactions();
            return txns.filter(t => t.accountId === accountId);
        } else {
            const res = await fetch(`${API_CONFIG.BASE_URL}/accounts/${accountId}/transactions`);
            if (!res.ok) throw new Error('Failed to fetch transactions');
            return await res.json();
        }
    },
    createTransaction: async (accountId, { description, amount, type }) => {
        if (API_CONFIG.USE_MOCK_API) {
            const accounts = getStoredAccounts();
            const accountIndex = accounts.findIndex(a => a.id === accountId);
            if (accountIndex === -1) throw new Error('Account not found');

            const numericAmount = parseFloat(amount);
            if (isNaN(numericAmount) || numericAmount <= 0) {
                throw new Error('Transaction amount must be a positive number.');
            }

            const account = accounts[accountIndex];
            const dbCrIndicator = type === 'DEPOSIT' ? 'CR' : 'DB';

            if (type === 'WITHDRAW' && account.balance < numericAmount) {
                throw new Error('Insufficient account balance.');
            }
            if (type === 'DEPOSIT') {
                account.balance += numericAmount;
            } else {
                account.balance -= numericAmount;
            }
            account.lastBalanceDate = new Date().toISOString().split('T')[0];
            account.dbCrIndicator = account.balance >= 0 ? 'CR' : 'DB';
            accounts[accountIndex] = account;
            setStoredAccounts(accounts);
            const txns = getStoredTransactions();
            const newTxn = {
                id: `TXN-${Math.floor(1000 + Math.random() * 9000)}`,
                accountId,
                date: new Date().toISOString(),
                description: description || (type === 'DEPOSIT' ? 'Deposit' : 'Withdrawal'),
                amount: numericAmount,
                dbCrIndicator
            };
            txns.unshift(newTxn);
            setStoredTransactions(txns);

            return { account, transaction: newTxn };
        } else {
            const res = await fetch(`${API_CONFIG.BASE_URL}/accounts/${accountId}/transactions`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ description, amount, dbCrIndicator: type === 'DEPOSIT' ? 'CR' : 'DB' })
            });
            if (!res.ok) throw new Error('Transaction failed');
            return await res.json();
        }
    },

    resetMockData: () => {
        localStorage.setItem('bank_accounts', JSON.stringify(INITIAL_ACCOUNTS));
        localStorage.setItem('bank_transactions', JSON.stringify(INITIAL_TRANSACTIONS));
        window.location.reload();
    }
};