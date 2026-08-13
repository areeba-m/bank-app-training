import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { adminTransactionApi } from "../../services/AdminTransactionApi.js";

export const fetchBalance = createAsyncThunk(
  "transactions/fetchBalance",
  async (accountId, { getState, dispatch, rejectWithValue }) => {
    try {
      return await adminTransactionApi.getBalance(accountId, {
        getState,
        dispatch,
      });
    } catch (err) {
      return rejectWithValue(err.message);
    }
  },
);

export const fetchTransactions = createAsyncThunk(
  "transactions/fetchTransactions",
  async (
    { accountId, page = 0, size = 10 },
    { getState, dispatch, rejectWithValue },
  ) => {
    try {
      return await adminTransactionApi.getTransactions(accountId, page, size, {
        getState,
        dispatch,
      });
    } catch (err) {
      return rejectWithValue(err.message);
    }
  },
);

const adminTransactionsSlice = createSlice({
  name: "adminTransactions",

  initialState: {
    balanceInfo: null,
    list: [],
    page: 0,
    size: 10,
    totalPages: 0,
    totalElements: 0,
    status: "idle",
    error: null,
  },

  reducers: {
    clearTransactionsError: (state) => {
      state.error = null;
    },
  },

  extraReducers: (builder) => {
    builder

      .addCase(fetchBalance.fulfilled, (state, action) => {
        state.balanceInfo = action.payload;
      })

      .addCase(fetchTransactions.pending, (state) => {
        state.status = "loading";
      })

      .addCase(fetchTransactions.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.list = action.payload.content;
        state.page = action.payload.number;
        state.size = action.payload.size;
        state.totalPages = action.payload.totalPages;
        state.totalElements = action.payload.totalElements;

        state.error = null;
      })

      .addCase(fetchTransactions.rejected, (state, action) => {
        state.status = "failed";
        state.error = action.payload;
      });
  },
});

export const { clearTransactionsError } = adminTransactionsSlice.actions;

export default adminTransactionsSlice.reducer;
