import { API_CONFIG } from "./Api.js";
import { authenticatedFetch } from "../redux/AuthenticatedFetch.js";
import { handleApiResponse } from "./HandleApiResponse.js";

export const userApi = {
  getCurrentUser: async (authContext) => {
    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/user/me`,
      {},
      authContext,
    );
    const response = await handleApiResponse(res);
    return response.json();
  },
};
