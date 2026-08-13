export const apiFetch = async (url, accessToken = null, options = {}) => {
  const headers = {
    ...options.headers,
    ...(accessToken
      ? {
          Authorization: `Bearer ${accessToken}`,
        }
      : {}),
  };

  return fetch(url, {
    ...options,
    credentials: "include",
    headers,
  });
};
