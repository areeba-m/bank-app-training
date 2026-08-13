import { API_CONFIG } from "./Api.js";
import { authenticatedFetch } from "../redux/AuthenticatedFetch.js";
import { handleApiResponse } from "./HandleApiResponse.js";

export const getCsrfToken = () => {
  const cookies = document.cookie.split("; ");

  const csrfCookie = cookies.find((cookie) => cookie.startsWith("XSRF-TOKEN="));

  console.log("ALL COOKIES:", cookies);
  console.log("CSRF COOKIE:", csrfCookie);

  if (!csrfCookie) {
    return null;
  }

  return decodeURIComponent(csrfCookie.split("=")[1]);
};
export const authApi = {
  login: async (email, password) => {
    const res = await fetch(`${API_CONFIG.BASE_URL}/auth/login`, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        email,
        password,
      }),
    });

    const response = await handleApiResponse(res);
    return response.json();
  },

  logout: async (authContext) => {
    const res = await authenticatedFetch(
      `${API_CONFIG.BASE_URL}/auth/logout`,
      {
        method: "POST",
      },
      authContext,
    );
    await handleApiResponse(res);
  },

  initCsrf: async () => {
    const res = await fetch(`${API_CONFIG.BASE_URL}/auth/csrf`, {
      method: "GET",
      credentials: "include",
    });
    if (!res.ok) {
      throw new Error("Failed to initialize CSRF token");
    }
    return await res.json();
  },

  refresh: async () => {
    const csrfToken = getCsrfToken();
    console.log("Sending refresh CSRF:", csrfToken);

    const res = await fetch(`${API_CONFIG.BASE_URL}/auth/refresh`, {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
        "X-XSRF-TOKEN": csrfToken,
      },
    });
    const response = await handleApiResponse(res);
    return response.json();
  },
};
