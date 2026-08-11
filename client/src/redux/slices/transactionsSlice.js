import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { transactionApi } from '../../services/TransactionApi.js';
import { refreshUser } from './authSlice';

export const fetchBalance = createAsyncThunk(
    'transactions/fetchBalance',
    async (accountId, {getState,dispatch, rejectWithValue }) => {
        try {
            return await transactionApi.getBalance(accountId,{getState,dispatch});
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

export const fetchTransactions = createAsyncThunk(
    'transactions/fetchTransactions',
    async (
        { accountId, page = 0, size = 10 },
        { getState, dispatch, rejectWithValue }
    ) => {
        try {
            return await transactionApi.getTransactions(
                accountId,
                page,
                size,
                { getState, dispatch }
            );
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

export const executeTransaction = createAsyncThunk(
    'transactions/executeTransaction',

    async (
        { accountId, transactionData },
        { rejectWithValue, dispatch }
    ) => {

        try {

            const result =
                await bankApi.createTransaction(
                    accountId,
                    transactionData
                );

            dispatch(fetchBalance(accountId));
            dispatch(fetchTransactions(accountId));
            dispatch(refreshUser(accountId));

            return result;

        } catch (err) {

            return rejectWithValue(
                err.message || 'Transaction failed'
            );
        }
    }
);

const transactionsSlice = createSlice({
    name: 'transactions',

    initialState: {
        balanceInfo: null,
        list: [],

        page: 0,
        size: 10,
        totalPages: 0,
        totalElements: 0,

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

            .addCase(
                fetchBalance.fulfilled,
                (state, action) => {
                    state.balanceInfo = action.payload;
                }
            )

            .addCase(
                fetchTransactions.pending,
                (state) => {
                    state.status = 'loading';
                }
            )

            .addCase(
                fetchTransactions.fulfilled,
                (state, action) => {

                    state.status = 'succeeded';

                    // Page<T> response
                    state.list = action.payload.content;

                    state.page = action.payload.number;
                    state.size = action.payload.size;
                    state.totalPages = action.payload.totalPages;
                    state.totalElements = action.payload.totalElements;

                    state.error = null;
                }
            )

            .addCase(
                fetchTransactions.rejected,
                (state, action) => {

                    state.status = 'failed';
                    state.error = action.payload;
                }
            );
    },
});

export const {
    clearTransactionsError
} = transactionsSlice.actions;

export default transactionsSlice.reducer;