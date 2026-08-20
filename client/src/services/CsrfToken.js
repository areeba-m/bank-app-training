let csrfToken = null;

export const setCsrfToken = (token) => {
    csrfToken = token;
};

export const getCsrfToken = () => {
    return csrfToken;
};