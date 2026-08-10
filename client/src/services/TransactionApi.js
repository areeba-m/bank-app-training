import { API_CONFIG } from "./api.js";
import { authenticatedFetch } from "../redux/authenticatedFetch.js";

export const TransactionApi = {

    getTransactions: async (accountId) => {
            const res = await fetch(`${API_CONFIG.BASE_URL}/user/transactions`);
            if (!res.ok) throw new Error('Failed to fetch transactions');
            return await res.json();
    },
}
