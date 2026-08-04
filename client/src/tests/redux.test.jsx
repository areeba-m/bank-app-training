import { describe, it, expect } from 'vitest';
import authReducer, { logout, loginUser } from '../redux/    slices  /authSlice';
import accountsReducer, { fetchAccounts } from '../redux/    slices  /accountsSlice';
import transactionsReducer from '../redux/    slices  /transactionsSlice.js';

describe('Redux Toolkit Slices & Store Reducers', () => {

    it('authReducer should handle initial state and logout', () => {
        const initialState = { user: { id: 'admin', role: 'ADMIN' }, isAuthenticated: true, role: 'ADMIN' };
        const nextState = authReducer(initialState, logout());
        expect(nextState.isAuthenticated).toBe(false);
        expect(nextState.user).toBeNull();
    });

    it('authReducer should handle loginUser pending', () => {
        const initialState = { status: 'idle', error: null };
        const nextState = authReducer(initialState, { type: loginUser.pending.type });
        expect(nextState.status).toBe('loading');
    });

    it('accountsReducer should handle fetchAccounts fulfilled', () => {
        const initialState = { items: [], status: 'loading' };
        const mockAccounts = [{ id: 'user1', name: 'Eleanor' }];
        const nextState = accountsReducer(initialState, {
            type: fetchAccounts.fulfilled.type,
            payload: mockAccounts
        });
        expect(nextState.status).toBe('succeeded');
        expect(nextState.items).toHaveLength(1);
    });

    it('transactionsReducer should handle initial state', () => {
        const initialState = transactionsReducer(undefined, { type: 'unknown' });
        expect(initialState.list).toEqual([]);
        expect(initialState.balanceInfo).toBeNull();
    });

});