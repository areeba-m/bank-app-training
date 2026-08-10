import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { authApi } from '../../services/AuthApi.js';

export const loginUser = createAsyncThunk(
    'auth/loginUser',
    async ({ email, password }, { rejectWithValue }) => {
        try {
            const user = await authApi.login(email, password);
            return user;
        } catch (err) {
            return rejectWithValue(err.message || 'Login failed');
        }
    }
);

export const logoutUser = createAsyncThunk(
    "auth/logoutUser",
    async (_, {  getState,rejectWithValue }) => {
        try {
            const token = getState().auth.accessToken;
            await authApi.logout(token);
            return true;
        } catch (err) {
            return rejectWithValue(err.message || "Logout failed");
        }
    }
);
export const refreshToken = createAsyncThunk(
    "auth/refreshToken",
    async (_, { rejectWithValue }) =>
    {
        console.log("REFRESH THUNK CALLED");

        try
        {
            const response = await authApi.refresh();
            // console.log("REFRESH RESPONSE", response);
            return response;
        }
        catch(err)
        {
            console.log("REFRESH ERROR", err);
            return rejectWithValue(err.message);
        }
    });

export const refreshUser = createAsyncThunk('auth/refreshUser',
    async (id, { rejectWithValue }) =>
    {
        try
        {
            const user = await authApi.getAccountById(id);
            return user;
        } catch (err) {
            return rejectWithValue(err.message || 'Failed to refresh user');
        }
    }
);

const authSlice = createSlice({
    name: 'auth',
    initialState: {
        user: null,
        accessToken: null,
        isAuthenticated:null ,
        role: null,
        status: 'checking',
        error: null,
    },
    reducers: {
        logout: (state) => {
            state.user = null;
            state.isAuthenticated = false;
            state.role = null;
            state.status = "idle";
            state.error = null;
        },
        clearAuthError: (state) => {
            state.error = null;
        },
        updateLocalUser: (state, action) => {
            state.user = { ...state.user, ...action.payload };
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
                state.user = {
                    userId: action.payload.userId,
                    email: action.payload.email,
                    role: action.payload.role
                };
                state.accessToken = action.payload.accessToken;
                state.isAuthenticated = true;
                state.role = action.payload.role;
                state.error = null;
            })

            .addCase(loginUser.rejected, (state, action) => {
                state.status = 'failed';
                state.error = action.payload;
            })

            .addCase(logoutUser.fulfilled, (state) => {
                state.user = null;
                state.isAuthenticated = false;
                state.role = null;
                state.status = "idle";
                state.error = null;
                state.accessToken = null;
            })

            .addCase(logoutUser.rejected, (state, action) => {
                state.error = action.payload;
            })

            .addCase(refreshToken.pending,(state)=>{
                console.log("REFRESH STARTED");
                state.status="checking";
            })


            .addCase(refreshToken.fulfilled,(state,action)=>{

                console.log("REFRESH SUCCESS", action.payload);

                state.status="succeeded";
                state.user={
                    userId: action.payload.userId,
                    email: action.payload.email,
                    role: action.payload.role
                };

                state.accessToken=action.payload.accessToken;
                state.isAuthenticated=true;
                state.role=action.payload.role;

            })


            .addCase(refreshToken.rejected,(state,action)=> {

                console.log("REFRESH FAILED", action.payload);

                state.status = "failed";
                state.user = null;
                state.accessToken = null;
                state.isAuthenticated = false;
                state.role = null;
            })

            .addCase(refreshUser.fulfilled, (state, action) =>
            {
                state.user = {...state.user, ...action.payload};
                state.role = action.payload.role;
            })

            .addCase(refreshUser.rejected, (state, action) =>
            {
                state.error = action.payload;
            })
    },
});

export const { logout, clearAuthError, updateLocalUser } = authSlice.actions;
export default authSlice.reducer;