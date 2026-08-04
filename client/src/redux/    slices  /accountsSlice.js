import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { bankApi } from '../../services/api';

export const fetchAccounts = createAsyncThunk(
    'accounts/fetchAccounts',
    async (_, { rejectWithValue }) => {
        try {
            return await bankApi.getAccounts();
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

export const fetchAccountById = createAsyncThunk(
    'accounts/fetchAccountById',
    async (id, { rejectWithValue }) => {
        try {
            return await bankApi.getAccountById(id);
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

export const createNewAccount = createAsyncThunk(
    'accounts/createNewAccount',
    async (accountData, { rejectWithValue, dispatch }) => {
        try {
            const created = await bankApi.createAccount(accountData);
            dispatch(fetchAccounts());
            return created;
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

export const updateExistingAccount = createAsyncThunk(
    'accounts/updateExistingAccount',
    async ({ id, data }, { rejectWithValue, dispatch }) => {
        try {
            const updated = await bankApi.updateAccount(id, data);
            dispatch(fetchAccounts());
            return updated;
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

export const deleteExistingAccount = createAsyncThunk(
    'accounts/deleteExistingAccount',
    async (id, { rejectWithValue, dispatch }) => {
        try {
            const res = await bankApi.deleteAccount(id);
            dispatch(fetchAccounts());
            return { id, ...res };
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

const accountsSlice = createSlice({
    name: 'accounts',
    initialState: {
        items: [],
        selectedAccount: null,
        status: 'idle',
        error: null,
    },
    reducers: {
        clearSelectedAccount: (state) => {
            state.selectedAccount = null;
        },
        clearAccountsError: (state) => {
            state.error = null;
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchAccounts.pending, (state) => {
                state.status = 'loading';
            })
            .addCase(fetchAccounts.fulfilled, (state, action) => {
                state.status = 'succeeded';
                state.items = action.payload;
                state.error = null;
            })
            .addCase(fetchAccounts.rejected, (state, action) => {
                state.status = 'failed';
                state.error = action.payload;
            })
            .addCase(fetchAccountById.fulfilled, (state, action) => {
                state.selectedAccount = action.payload;
            });
    },
});

export const { clearSelectedAccount, clearAccountsError } = accountsSlice.actions;
export default accountsSlice.reducer;