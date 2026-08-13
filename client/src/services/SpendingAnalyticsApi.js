import { authenticatedFetch } from "../redux/AuthenticatedFetch.js";
import { handleApiResponse } from "./HandleApiResponse.js";
import { API_CONFIG } from "./Api.js";

export const spendingAnalyticsApi = {
  getSpendingAnalytics: async ({ from, to, insight = false }, authContext) => {
    const params = new URLSearchParams({ from, to, insight: String(insight) });

    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/user/analytics/spending?${params.toString()}`,
      {},
      authContext,
    );
    const response = await handleApiResponse(res);
    return response.json();
  },
};
