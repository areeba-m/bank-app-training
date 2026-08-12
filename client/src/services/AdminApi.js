import { API_CONFIG } from "./api.js";
import { authenticatedFetch } from "../redux/authenticatedFetch.js";

export const adminApi = {
  getAccounts: async (page = 0, size = 10, authContext) => {
    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/admin/accounts?page=${page}&size=${size}`,
      {},
      authContext,
    );
    if (!res.ok) {
      throw new Error("Failed to fetch accounts");
    }
    return await res.json();
  },

  getAccountById: async (id, authContext) => {
    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/admin/accounts/${id}`,
      {},
      authContext,
    );
    if (!res.ok) throw new Error("Failed to fetch account details");
    return await res.json();
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
    if (!res.ok) throw new Error("Failed to create account");
    return await res.json();
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
    if (!res.ok) throw new Error("Failed to update account");
    return await res.json();
  },

  deleteAccount: async (id, authContext) => {
    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/admin/accounts/${id}`,
      {
        method: "DELETE",
      },
      authContext,
    );
    if (!res.ok) throw new Error("Failed to delete account");
    return true;
  },
};
