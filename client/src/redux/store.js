import { configureStore } from "@reduxjs/toolkit";

import authReducer from "./slices/AuthSlice.js";
import accountsReducer from "./slices/AccountsSlice.js";
import adminTransactionsReducer from "./slices/AdminTransactionsSlice.js";
import userTransactionReducer from "./slices/UserTransactionSlice.js";
import userReducer from "./slices/UserSlice.js";
import moneyTransferReducer from "./slices/MoneyTransferSlice.js";
import otpVerificationReducer from "./slices/OtpVerificationSlice";

export const store = configureStore({
  reducer: {
    auth: authReducer,
    accounts: accountsReducer,
    adminTransactions: adminTransactionsReducer,
    userTransactions: userTransactionReducer,
    user: userReducer,
    moneyTransfer: moneyTransferReducer,
    otpVerification: otpVerificationReducer,
  },
});
