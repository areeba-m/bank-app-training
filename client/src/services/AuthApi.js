import {API_CONFIG} from "./Api.js";
import {authenticatedFetch} from "../redux/AuthenticatedFetch.js";
import {handleApiResponse} from "./HandleApiResponse.js";
import {getCsrfToken} from "./CsrfToken.js";

// export const getCsrfToken = () => {
//     const cookies = document.cookie.split("; ");
//     const csrfCookie = cookies.find((cookie) => cookie.startsWith("XSRF-TOKEN="));
//
//     if (!csrfCookie) {
//         return null;
//     }
//
//     return decodeURIComponent(csrfCookie.split("=")[1]);
// };
let csrfInitPromise = null;
let csrfToken = null;
export const authApi = {
    login: async (email, password) => {
        const res = await fetch(`${API_CONFIG.BASE_URL}/auth/login`, {
            method: "POST",
            credentials: "include",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({email, password}),
        });

        const response = await handleApiResponse(res);
        return await response.json();
    },

    logout: async (authContext) => {
        const res = await authenticatedFetch(
            `${API_CONFIG.BASE_URL}/auth/logout`,
            {method: "POST"},
            authContext,
        );
        await handleApiResponse(res);
    },

    initCsrf: async () => {
        if (!csrfInitPromise) {
            csrfInitPromise = fetch(`${API_CONFIG.BASE_URL}/auth/csrf`,
                {
                    method: "GET",
                    credentials: "include",
                    headers: {
                        "ngrok-skip-browser-warning": "true",
                    },
                },
            )
                .then(async (res) => {
                    if (!res.ok) {
                        throw new Error(
                            "Failed to initialize CSRF token",
                        );
                    }
                    const token = await res.text();
                    csrfToken = token;
                    console.log("CSRF token stored:", csrfToken);
                    return token;
                })
                .finally(() => {
                    csrfInitPromise = null;
                });
        }

        return csrfInitPromise;
    },
    refresh: async () => {
        let csrfToken = csrfToken;
        if (!csrfToken) {
            await authApi.initCsrf();
            csrfToken = getCsrfToken();
        }

        const headers = {
            "Content-Type": "application/json",
        };

        if (csrfToken) {

            headers["X-XSRF-TOKEN"] = csrfToken;
        }

        const res = await fetch(`${API_CONFIG.BASE_URL}/auth/refresh`, {
            method: "POST",
            credentials: "include",
            headers,
        });

        const response = await handleApiResponse(res);
        return response.json();
    },

    verifyOtp: async ({email, otp}) => {
        let csrfToken = getCsrfToken();

        if (!csrfToken) {
            await authApi.initCsrf();
            csrfToken = getCsrfToken();
            console.log(csrfToken);
        }

        const headers = {
            "Content-Type": "application/json",
        };

        if (csrfToken) {
            headers["X-XSRF-TOKEN"] = csrfToken;
        }

        const res = await fetch(
            `${API_CONFIG.BASE_URL}/auth/otp/verify`,
            {
                method: "POST",
                credentials: "include",
                headers,
                body: JSON.stringify({
                    email,
                    otp,
                }),
            },
        );

        const response = await handleApiResponse(res);
        return response.json();
    },

    resendOtp: async ({email}) => {
        let csrfToken = getCsrfToken();

        if (!csrfToken) {
            await authApi.initCsrf();
            csrfToken = getCsrfToken();
        }

        const headers = {
            "Content-Type": "application/json",
        };

        if (csrfToken) {
            headers["X-XSRF-TOKEN"] = csrfToken;
        }

        const res = await fetch(
            `${API_CONFIG.BASE_URL}/auth/otp/resend`,
            {
                method: "POST",
                credentials: "include",
                headers,
                body: JSON.stringify({
                    email,
                }),
            },
        );

        await handleApiResponse(res);
        return true;
    },

    changePassword: async ({resetToken, newPassword}) => {
        let csrfToken = getCsrfToken();

        if (!csrfToken) {
            await authApi.initCsrf();
            csrfToken = getCsrfToken();
        }

        const headers = {
            "Content-Type": "application/json",
        };

        if (csrfToken) {
            headers["X-XSRF-TOKEN"] = csrfToken;
        }

        const res = await fetch(
            `${API_CONFIG.BASE_URL}/auth/password/change`,
            {
                method: "POST",
                credentials: "include",
                headers,
                body: JSON.stringify({
                    resetToken,
                    newPassword,
                }),
            },
        );

        await handleApiResponse(res);
        return true;
    },

};
