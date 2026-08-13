import { API_CONFIG } from "./Api.js";
import { authenticatedFetch } from "../redux/AuthenticatedFetch.js";
import { handleApiResponse } from "./HandleApiResponse.js";

export const adminApi = {
  getAccounts: async (page = 0, size = 10, authContext) => {
    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/admin/accounts?page=${page}&size=${size}`,
      {},
      authContext,
    );
    const response = await handleApiResponse(res);
    return response.json();
  },

  getAccountById: async (id, authContext) => {
    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/admin/accounts/${id}`,
      {},
      authContext,
    );
    const response = await handleApiResponse(res);
    return response.json();
  },

  createAccount: async (accountData, authContext) => {
    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/admin/accounts`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(accountData),
      },
      authContext,
    );
    const response = await handleApiResponse(res);
    return response.json();
  },

  updateAccount: async (id, updatedData, authContext) => {
    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/admin/accounts/${id}`,
      {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(updatedData),
      },
      authContext,
    );
    const response = await handleApiResponse(res);
    return response.json();
  },

  deleteAccount: async (id, authContext) => {
    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/admin/accounts/${id}`,
      {
        method: "DELETE",
      },
      authContext,
    );
    await handleApiResponse(res);
  },
};
