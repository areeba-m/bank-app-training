import {API_CONFIG} from "./api.js";

export const getCsrfToken = () => {

    const cookies = document.cookie.split("; ");

    const csrfCookie = cookies.find(cookie =>
        cookie.startsWith("XSRF-TOKEN=")
    );

    console.log("ALL COOKIES:", cookies);
    console.log("CSRF COOKIE:", csrfCookie);

    if (!csrfCookie) {
        return null;
    }

    return decodeURIComponent(
        csrfCookie.split("=")[1]
    );
};
export const authApi = {
    login: async (email, password) => {
        const res = await fetch(`${API_CONFIG.BASE_URL}/auth/login`,
            {
                method: "POST",
                credentials: "include",
                headers: {"Content-Type": "application/json",},
                body: JSON.stringify({
                    email,
                    password,
                }),
            });

        if (!res.ok) {
            const error = await res.text();
            throw new Error(error || "Invalid email or password");
        }

        const data = await res.json();
        return data;
    },

    logout: async (accessToken) =>
    {
        const res = await fetch(`${API_CONFIG.BASE_URL}/auth/logout`,
            {
                method: "POST",
                credentials: "include",
                headers:
                    {
                        "Authorization": `Bearer ${accessToken}`
                    }
            });
        if (!res.ok) {
            throw new Error("Logout failed");
        }
    },

    initCsrf: async () => {
        const res = await fetch(
            `${API_CONFIG.BASE_URL}/auth/csrf`,
            {
                method:"GET",
                credentials:"include"
            }
        );
        if (!res.ok)
        {
            throw new Error( "Failed to initialize CSRF token" );
        }
        return await res.json();
    },

    refresh: async () =>
    {
        const csrfToken = getCsrfToken();
        console.log("Sending refresh CSRF:", csrfToken);

        const res = await fetch(
            `${API_CONFIG.BASE_URL}/auth/refresh`,
            {
                method: "POST",
                credentials: "include",
                headers:{
                    "Content-Type":"application/json",
                    "X-XSRF-TOKEN": csrfToken
                }
            }
        );
        if (!res.ok)
        {
            throw new Error("Refresh token expired");
        }

        const data = await res.json();
        return data
    },
}