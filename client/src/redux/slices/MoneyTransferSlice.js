import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { fetchBalance, fetchTransactions } from "./UserTransactionSlice.js";
import { moneyTransferApi } from "../../services/MoneyTransferApi.js";

export const executeTransfer = createAsyncThunk(
  "userTransfer/executeTransfer",
  async (transferData, { getState, rejectWithValue, dispatch }) => {
    try {
      if (!transferData) {
        throw new Error("Transfer data is missing");
      }

      if (!transferData.email) {
        throw new Error("Recipient email is required");
      }

      if (!transferData.amount) {
        throw new Error("Transfer amount is required");
      }

      if (!transferData.description) {
        throw new Error("Transfer description is required");
      }

      const result = await moneyTransferApi.transferMoney(transferData, {
        getState,
        dispatch,
      });
      await Promise.all([
        dispatch(fetchBalance()).unwrap(),
        dispatch(fetchTransactions()).unwrap(),
      ]);

      return result;
    } catch (err) {
      return rejectWithValue(err?.message || "Transfer failed");
    }
  },
);

const initialState = {
  status: "idle",
  error: null,
  lastTransfer: null,
};

const moneyTransferSlice = createSlice({
  name: "moneyTransfer",

  initialState,

  reducers: {
    clearTransferError: (state) => {
      state.error = null;
    },

    clearLastTransfer: (state) => {
      state.lastTransfer = null;
    },
  },

  extraReducers: (builder) => {
    builder
      .addCase(executeTransfer.pending, (state) => {
        state.status = "loading";
        state.error = null;
      })

      .addCase(executeTransfer.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.error = null;
        state.lastTransfer = action.payload;
      })

      .addCase(executeTransfer.rejected, (state, action) => {
        state.status = "failed";
        state.error =
          action.payload || action.error?.message || "Transfer failed";
      });
  },
});

export const { clearTransferError, clearLastTransfer } =
  moneyTransferSlice.actions;

export default moneyTransferSlice.reducer;
