import { API_CONFIG } from "./Api.js";
import { authenticatedFetch } from "../redux/AuthenticatedFetch.js";
import { handleApiResponse } from "./HandleApiResponse.js";

export const userTransactionApi = {
  getTransactions: async (page = 0, size = 10, authContext) => {
    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/user/transaction?&page=${page}&size=${size}`,
      {},
      authContext,
    );
    const response = await handleApiResponse(res);
    return response.json();
  },

  getBalance: async (authContext) => {
    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/user/balance`,
      {},
      authContext,
    );
    const response = await handleApiResponse(res);
    return response.json();
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

    const response = await handleApiResponse(res);
    return response.json();
  },
};
