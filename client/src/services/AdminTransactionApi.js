import { API_CONFIG } from "./api.js";
import { authenticatedFetch } from "../redux/authenticatedFetch.js";

export const adminTransactionApi = {
  getTransactions: async (accountId, page = 0, size = 10, authContext) => {
    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/admin/accounts/transactions?userId=${accountId}&page=${page}&size=${size}`,
      {},
      authContext,
    );
    if (!res.ok) {
      throw new Error("Failed to fetch transactions");
    }

    return await res.json();
  },

  getBalance: async (accountId, authContext) => {
    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/admin/accounts/${accountId}/balance`,
      {},
      authContext,
    );
    if (!res.ok) throw new Error("Failed to fetch balance");
    return await res.json();
  },
};
