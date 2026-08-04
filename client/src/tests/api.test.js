import { describe, it, expect } from 'vitest';
import { bankApi } from '../services/api';


describe('Bank API Service Layer (Mock & Spring REST Ready)', () => {


    it('should authenticate predefined admin account using email', async () => {

        const user = await bankApi.login(
            'admin@aurabank.com',
            'admin123'
        );

        expect(user).toBeDefined();
        expect(user.role).toBe('ADMIN');
        expect(user.email).toBe('admin@aurabank.com');
        expect(user.name).toBe('System Admin');

    });


    it('should authenticate predefined user account using email', async () => {

        const user = await bankApi.login(
            'eleanor.vance@example.com',
            'user123'
        );

        expect(user).toBeDefined();
        expect(user.role).toBe('USER');
        expect(user.email).toBe('eleanor.vance@example.com');
        expect(user.name).toBe('Eleanor Vance');

    });


    it('should throw error on invalid email login', async () => {

        await expect(
            bankApi.login(
                'wrong@example.com',
                'wrongPass'
            )
        ).rejects.toThrow();

    });


    it('should retrieve list of registered accounts', async () => {

        const accounts = await bankApi.getAccounts();

        expect(Array.isArray(accounts)).toBe(true);
        expect(accounts.length).toBeGreaterThan(0);

    });


    it('should create a new account successfully', async () => {

        const newAcc = {

            password: 'password123',
            name: 'Test Customer',
            email: 'test@example.com',
            address: '123 Test St',
            initialBalance: '500.00',
            role: 'USER'

        };
        const created = await bankApi.createAccount(newAcc);
        expect(created).toBeDefined();
        expect(created.email).toBe('test@example.com');
        expect(created.balance).toBe(500.00);
        const fetched = await bankApi.getAccountByEmail(
            'test@example.com'
        );
        expect(fetched.name).toBe('Test Customer');

    });

    it('should execute deposit and update balance', async () => {

        const accountId = 'user1';
        const initialBal = await bankApi.getBalance(accountId);
        const depositAmount = 250.00;
        const res = await bankApi.createTransaction(
            accountId,
            {
                description: 'Test Deposit',
                amount: depositAmount,
                type: 'DEPOSIT'
            }
        );
        expect(res.transaction.dbCrIndicator)
            .toBe('CR');
        expect(res.account.balance)
            .toBe(initialBal.amount + depositAmount);

    });
});