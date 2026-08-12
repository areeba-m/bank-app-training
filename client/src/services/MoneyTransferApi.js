import { API_CONFIG } from "./api.js";
import { authenticatedFetch } from "../redux/authenticatedFetch.js";

export const moneyTransferApi = {
  transferMoney: async (transferData, authContext) => {
    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/user/transfer`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          recipientEmail: transferData.email,
          amount: transferData.amount,
          description: transferData.description,
        }),
      },
      authContext,
    );

    if (!res.ok) {
      let message = "Transfer failed";

      try {
        const errorBody = await res.json();
        message = errorBody?.message || message;
      } catch {}
      throw new Error(message);
    }
    return await res.json();
  },
};
