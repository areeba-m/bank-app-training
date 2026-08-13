import { API_CONFIG } from "./Api.js";
import { authenticatedFetch } from "../redux/AuthenticatedFetch.js";
import { handleApiResponse } from "./HandleApiResponse.js";

export const adminTransactionApi = {
  getTransactions: async (accountId, page = 0, size = 10, authContext) => {
    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/admin/accounts/transactions?userId=${accountId}&page=${page}&size=${size}`,
      {},
      authContext,
    );
    const response = await handleApiResponse(res);
    return response.json();
  },

  getBalance: async (accountId, authContext) => {
    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/admin/accounts/${accountId}/balance`,
      {},
      authContext,
    );
    const response = await handleApiResponse(res);
    return response.json();
  },
};
