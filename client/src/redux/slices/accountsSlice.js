import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { adminApi } from "../../services/AdminApi.js";

export const fetchAccounts = createAsyncThunk(
  "accounts/fetchAccounts",
  async ({ page, size }, { getState, dispatch, rejectWithValue }) => {
    try {
      return await adminApi.getAccounts(page, size, { getState, dispatch });
    } catch (err) {
      return rejectWithValue(err.message);
    }
  },
);

export const fetchAccountById = createAsyncThunk(
  "accounts/fetchAccountById",
  async (id, { getState, dispatch, rejectWithValue }) => {
    try {
      return await adminApi.getAccountById(id, { getState, dispatch });
    } catch (err) {
      return rejectWithValue(err.message);
    }
  },
);

export const createNewAccount = createAsyncThunk(
  "accounts/createNewAccount",
  async (accountData, { getState, rejectWithValue, dispatch }) => {
    try {
      const created = await adminApi.createAccount(accountData, {
        getState,
        dispatch,
      });
      dispatch(fetchAccounts());
      return created;
    } catch (err) {
      return rejectWithValue(err.message);
    }
  },
);

export const updateExistingAccount = createAsyncThunk(
  "accounts/updateExistingAccount",
  async ({ id, data }, { getState, rejectWithValue, dispatch }) => {
    try {
      const updated = await adminApi.updateAccount(id, data, {
        getState,
        dispatch,
      });
      dispatch(fetchAccounts());
      return updated;
    } catch (err) {
      return rejectWithValue(err.message);
    }
  },
);

export const deleteExistingAccount = createAsyncThunk(
  "accounts/deleteExistingAccount",
  async (id, { getState, rejectWithValue, dispatch }) => {
    try {
      const res = await adminApi.deleteAccount(id, { getState, dispatch });
      dispatch(fetchAccounts());
      return { id, ...res };
    } catch (err) {
      return rejectWithValue(err.message);
    }
  },
);

const accountsSlice = createSlice({
  name: "accounts",
  initialState: {
    items: [],
    page: 0,
    size: 10,
    totalPages: 0,
    totalElements: 0,
    loading: false,
    error: null,
    selectedAccount: null,
    status: "checking",
  },
  reducers: {
    clearSelectedAccount: (state) => {
      state.selectedAccount = null;
    },
    clearAccountsError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchAccounts.pending, (state) => {
        state.status = "loading";
      })
      .addCase(fetchAccounts.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.items = action.payload.content;
        state.page = action.payload.number;
        state.size = action.payload.size;
        state.totalPages = action.payload.totalPages;
        state.totalElements = action.payload.totalElements;
        state.loading = false;
        state.error = null;
      })
      .addCase(fetchAccounts.rejected, (state, action) => {
        state.status = "failed";
        state.error = action.payload;
      })
      .addCase(fetchAccountById.fulfilled, (state, action) => {
        state.selectedAccount = action.payload;
      });
  },
});

export const { clearSelectedAccount, clearAccountsError } =
  accountsSlice.actions;
export default accountsSlice.reducer;
