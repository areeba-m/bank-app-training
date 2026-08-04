import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { bankApi } from '../../services/api';

const savedUser = localStorage.getItem('bank_current_user');
const initialUser = savedUser ? JSON.parse(savedUser) : null;

export const loginUser = createAsyncThunk(
    'auth/loginUser',
    async ({ email, password }, { rejectWithValue }) => {
        try {
            const user = await bankApi.login(email, password);
            localStorage.setItem('bank_current_user', JSON.stringify(user));
            return user;
        } catch (err) {
            return rejectWithValue(err.message || 'Login failed');
        }
    }
);

export const refreshUser = createAsyncThunk(
    'auth/refreshUser',
    async (id, { rejectWithValue }) => {
        try {
            const user = await bankApi.getAccountById(id);
            localStorage.setItem('bank_current_user', JSON.stringify(user));
            return user;
        } catch (err) {
            return rejectWithValue(err.message);
        }
    }
);

const authSlice = createSlice({
    name: 'auth',
    initialState: {
        user: initialUser,
        isAuthenticated: !!initialUser,
        role: initialUser?.role || null,
        status: 'idle',
        error: null,
    },
    reducers: {
        logout: (state) => {
            state.user = null;
            state.isAuthenticated = false;
            state.role = null;
            state.status = 'idle';
            state.error = null;
            localStorage.removeItem('bank_current_user');
        },
        clearAuthError: (state) => {
            state.error = null;
        },
        updateLocalUser: (state, action) => {
            state.user = { ...state.user, ...action.payload };
            localStorage.setItem('bank_current_user', JSON.stringify(state.user));
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(loginUser.pending, (state) => {
                state.status = 'loading';
                state.error = null;
            })
            .addCase(loginUser.fulfilled, (state, action) => {
                state.status = 'succeeded';
                state.user = action.payload;
                state.isAuthenticated = true;
                state.role = action.payload.role;
                state.error = null;
            })
            .addCase(loginUser.rejected, (state, action) => {
                state.status = 'failed';
                state.error = action.payload;
            })
            .addCase(refreshUser.fulfilled, (state, action) => {
                state.user = action.payload;
                state.role = action.payload.role;
            });
    },
});

export const { logout, clearAuthError, updateLocalUser } = authSlice.actions;
export default authSlice.reducer;