import { configureStore } from '@reduxjs/toolkit';
import authReducer from './    slices  /authSlice.js';
// import accountsReducer from './slices/accountsSlice';
import transactionsReducer from './    slices  /transactionsSlice.js';

export const store = configureStore({
    reducer: {
        auth: authReducer,
        // accounts: accountsReducer,
        transactions: transactionsReducer,
    },
});