import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { bankApi } from '../../services/api';
import { refreshUser } from './authSlice';

export const fetchBalance = createAsyncThunk(
    'transactions/fetchBalance',
    async (accountId, { rejectWithValue }) => {
        try {
            return await bankApi.getBalance(accountId);
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

export const fetchTransactions = createAsyncThunk(
    'transactions/fetchTransactions',
    async (accountId, { rejectWithValue }) => {
        try {
            return await bankApi.getTransactions(accountId);
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

export const executeTransaction = createAsyncThunk(
    'transactions/executeTransaction',
    async ({ accountId, transactionData }, { rejectWithValue, dispatch }) => {
        try {
            const result = await bankApi.createTransaction(accountId, transactionData);
            dispatch(fetchBalance(accountId));
            dispatch(fetchTransactions(accountId));
            dispatch(refreshUser(accountId));
            return result;
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

const transactionsSlice = createSlice({
    name: 'transactions',
    initialState: {
        balanceInfo: null,
        list: [],
        status: 'idle',
        error: null,
    },
    reducers: {
        clearTransactionsError: (state) => {
            state.error = null;
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchBalance.fulfilled, (state, action) => {
                state.balanceInfo = action.payload;
            })
            .addCase(fetchTransactions.pending, (state) => {
                state.status = 'loading';
            })
            .addCase(fetchTransactions.fulfilled, (state, action) => {
                state.status = 'succeeded';
                state.list = action.payload;
                state.error = null;
            })
            .addCase(fetchTransactions.rejected, (state, action) => {
                state.status = 'failed';
                state.error = action.payload;
            });
    },
});

export const { clearTransactionsError } = transactionsSlice.actions;
export default transactionsSlice.reducer;