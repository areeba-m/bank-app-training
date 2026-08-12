import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { userTransactionApi } from "../../services/UserTransactionApi.js";
import { refreshUser } from "./authSlice.js";

export const fetchBalance = createAsyncThunk(
  "userTransactions/fetchBalance",

  async (_, { getState, dispatch, rejectWithValue }) => {
    try {
      return await userTransactionApi.getBalance({
        getState,
        dispatch,
      });
    } catch (err) {
      return rejectWithValue(err?.message || "Failed to fetch balance");
    }
  },
);

export const fetchTransactions = createAsyncThunk(
  "userTransactions/fetchTransactions",

  async (
    { page = 0, size = 10 } = {},
    { getState, dispatch, rejectWithValue },
  ) => {
    try {
      return await userTransactionApi.getTransactions(page, size, {
        getState,
        dispatch,
      });
    } catch (err) {
      return rejectWithValue(err?.message || "Failed to fetch transactions");
    }
  },
);

export const executeTransaction = createAsyncThunk(
  "userTransactions/executeTransaction",

  async (
    { accountId, transactionData },
    { getState, rejectWithValue, dispatch },
  ) => {
    try {
      if (!transactionData) {
        throw new Error("Transaction data is missing");
      }

      const result = await userTransactionApi.createTransaction(
        transactionData,
        { getState, dispatch },
      );

      dispatch(fetchBalance());
      dispatch(fetchTransactions());

      if (accountId) {
        dispatch(refreshUser(accountId));
      }

      return result;
    } catch (err) {
      return rejectWithValue(err?.message || "Transaction failed");
    }
  },
);

const userTransactionSlice = createSlice({
  name: "userTransactions",

  initialState: {
    balanceInfo: null,
    balanceStatus: "idle",
    balanceError: null,

    list: [],
    page: 0,
    size: 10,
    totalPages: 0,
    totalElements: 0,

    status: "idle",
    error: null,

    executeStatus: "idle",
    executeError: null,
  },

  reducers: {
    clearTransactionsError: (state) => {
      state.error = null;
    },

    clearExecuteTransactionError: (state) => {
      state.executeError = null;
    },

    clearTransactions: (state) => {
      state.list = [];
      state.page = 0;
      state.totalPages = 0;
      state.totalElements = 0;
      state.error = null;
    },
  },

  extraReducers: (builder) => {
    builder

      .addCase(fetchBalance.pending, (state) => {
        state.balanceStatus = "loading";
        state.balanceError = null;
      })

      .addCase(fetchBalance.fulfilled, (state, action) => {
        state.balanceStatus = "succeeded";
        state.balanceInfo = action.payload;
        state.balanceError = null;
      })

      .addCase(fetchBalance.rejected, (state, action) => {
        state.balanceStatus = "failed";
        state.balanceError = action.payload || "Failed to fetch balance";
      })

      .addCase(fetchTransactions.pending, (state) => {
        state.status = "loading";
        state.error = null;
      })

      .addCase(fetchTransactions.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.list = action.payload?.content || [];
        state.page = action.payload?.number ?? 0;
        state.size = action.payload?.size ?? 10;
        state.totalPages = action.payload?.totalPages ?? 0;
        state.totalElements = action.payload?.totalElements ?? 0;
        state.error = null;
      })

      .addCase(fetchTransactions.rejected, (state, action) => {
        state.status = "failed";
        state.error = action.payload || "Failed to fetch transactions";
      })

      .addCase(executeTransaction.pending, (state) => {
        state.executeStatus = "loading";
        state.executeError = null;
      })

      .addCase(executeTransaction.fulfilled, (state) => {
        state.executeStatus = "succeeded";
        state.executeError = null;
      })

      .addCase(executeTransaction.rejected, (state, action) => {
        state.executeStatus = "failed";

        state.executeError = action.payload || "Transaction failed";
      });
  },
});

export const {
  clearTransactionsError,
  clearExecuteTransactionError,
  clearTransactions,
} = userTransactionSlice.actions;

export default userTransactionSlice.reducer;
