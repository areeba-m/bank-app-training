export const INITIAL_ACCOUNTS = [
    {
        id: 'admin',
        password: 'admin123',
        name: 'System Admin',
        email: 'admin@aurabank.com',
        address: '100 Financial Tower, Wall St, New York, NY',
        role: 'ADMIN',
        balance: 500000.00,
        dbCrIndicator: 'CR',
        lastBalanceDate: '2026-08-01'
    },
    {
        id: 'user1',
        password: 'user123',
        name: 'Eleanor Vance',
        email: 'eleanor.vance@example.com',
        address: '742 Evergreen Terrace, Springfield, IL',
        role: 'USER',
        balance: 14850.75,
        dbCrIndicator: 'CR',
        lastBalanceDate: '2026-08-03'
    },
    {
        id: 'user2',
        password: 'user123',
        name: 'Marcus Sterling',
        email: 'marcus.sterling@example.com',
        address: '450 Park Avenue, Suite 1200, New York, NY',
        role: 'USER',
        balance: 82400.00,
        dbCrIndicator: 'CR',
        lastBalanceDate: '2026-08-02'
    },
    {
        id: 'user3',
        password: 'user123',
        name: 'Sophia Chen',
        email: 'sophia.chen@techcorp.io',
        address: '88 Market St, San Francisco, CA',
        role: 'USER',
        balance: 3120.50,
        dbCrIndicator: 'CR',
        lastBalanceDate: '2026-08-01'
    }
];

export const INITIAL_TRANSACTIONS = [
    {
        id: 'TXN-9001',
        accountId: 'user1',
        date: '2026-08-03T10:15:00Z',
        description: 'Salary Direct Deposit - Acme Corp',
        amount: 3500.00,
        dbCrIndicator: 'CR'
    },
    {
        id: 'TXN-9002',
        accountId: 'user1',
        date: '2026-08-02T14:22:00Z',
        description: 'Electric & Utility Bill Payment',
        amount: 145.25,
        dbCrIndicator: 'DB'
    },
    {
        id: 'TXN-9003',
        accountId: 'user1',
        date: '2026-08-01T09:04:00Z',
        description: 'ATM Cash Withdrawal - 5th Ave Branch',
        amount: 200.00,
        dbCrIndicator: 'DB'
    },
    {
        id: 'TXN-9004',
        accountId: 'user1',
        date: '2026-07-28T18:45:00Z',
        description: 'Online Transfer from Marcus Sterling',
        amount: 1200.00,
        dbCrIndicator: 'CR'
    },
    {
        id: 'TXN-9005',
        accountId: 'user2',
        date: '2026-08-02T11:00:00Z',
        description: 'Investment Dividend Credit',
        amount: 4500.00,
        dbCrIndicator: 'CR'
    },
    {
        id: 'TXN-9006',
        accountId: 'user2',
        date: '2026-08-01T16:30:00Z',
        description: 'Commercial Property Rent Payment',
        amount: 2800.00,
        dbCrIndicator: 'DB'
    },
    {
        id: 'TXN-9007',
        accountId: 'user3',
        date: '2026-08-01T13:10:00Z',
        description: 'Initial Opening Deposit',
        amount: 3120.50,
        dbCrIndicator: 'CR'
    }
];