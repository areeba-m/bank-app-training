import { refreshToken } from "./slices/authSlice.js";
import { apiFetch } from "../services/apiFetch.js";

let refreshPromise = null;
export const authenticatedFetch = async (url, options = {}, { getState, dispatch },) => {

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
