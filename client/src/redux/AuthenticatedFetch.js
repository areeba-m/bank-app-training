import { refreshToken } from "./slices/AuthSlice.js";
import { apiFetch } from "../services/ApiFetch.js";

let refreshPromise = null;
export const authenticatedFetch = async (
  url,
  options = {},
  { getState, dispatch },
) => {
  let accessToken = getState().auth.accessToken;
  let response = await apiFetch(url, accessToken, options);
  if (response.status !== 401) {
    return response;
  }
  if (options.skipRefresh) {
    return response;
  }
  if (!refreshPromise) {
    refreshPromise = dispatch(refreshToken()).finally(() => {
      console.log("401 refresh calls");
      refreshPromise = null;
    });
  }

  const refreshResult = await refreshPromise;
  if (!refreshToken.fulfilled.match(refreshResult)) {
    return response;
  }

  accessToken = getState().auth.accessToken;
  if (!accessToken) {
    return response;
  }
  return apiFetch(url, accessToken, options);
};
