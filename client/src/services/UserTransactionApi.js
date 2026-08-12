import { API_CONFIG } from "./api.js";
import { authenticatedFetch } from "../redux/authenticatedFetch.js";

export const userTransactionApi = {
  getTransactions: async (page = 0, size = 10, authContext) => {
    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/user/transaction?&page=${page}&size=${size}`,
      {},
      authContext,
    );
    if (!res.ok) {
      throw new Error("Failed to fetch transactions");
    }

    return await res.json();
  },

  getBalance: async (authContext) => {
    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/user/balance`,
      {},
      authContext,
    );
    if (!res.ok) throw new Error("Failed to fetch balance");
    return await res.json();
  },

  createTransaction: async (transactionData, authContext) => {
    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/user/transaction`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          description: transactionData.description,
          amount: transactionData.amount,
          indicator: transactionData.indicator,
        }),
      },
      authContext,
    );

    if (!res.ok) {
      const errorBody = await res.json().catch(() => null);

      console.error("Transaction API error:", {
        status: res.status,
        body: errorBody,
      });

      throw new Error(
        errorBody?.message || errorBody?.error || "Transaction failed",
      );
    }

    return await res.json();
  },
};
