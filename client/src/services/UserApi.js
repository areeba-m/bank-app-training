import { API_CONFIG } from "./api.js";
import { authenticatedFetch } from "../redux/authenticatedFetch.js";

export const userApi = {
  getCurrentUser: async (authContext) => {
    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/user/me`,
      {},
      authContext,
    );

    if (!res.ok) {
      throw new Error("Failed to fetch current user");
    }

    return await res.json();
  },
};
