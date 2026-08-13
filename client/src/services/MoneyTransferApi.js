import { API_CONFIG } from "./Api.js";
import { authenticatedFetch } from "../redux/AuthenticatedFetch.js";
import { handleApiResponse } from "./HandleApiResponse.js";

export const moneyTransferApi = {
  transferMoney: async (transferData, authContext) => {
    const idempotencyKey = crypto.randomUUID();
    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/user/transfer`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Idempotency-Key": idempotencyKey,
        },
        body: JSON.stringify({
          recipientEmail: transferData.email,
          amount: transferData.amount,
          description: transferData.description,
        }),
      },
      authContext,
    );

    const response = await handleApiResponse(res);
    return response.json();
  },
};
