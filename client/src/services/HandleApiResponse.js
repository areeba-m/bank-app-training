import { ApiError } from "./ApiError.js";

export const handleApiResponse = async (response) => {
  if (response.ok) {
    return response;
  }

  let errorData = null;

  try {
    const text = await response.text();

    if (text.trim()) {
      try {
        errorData = JSON.parse(text);
      } catch {
        errorData = {
          message: text,
        };
      }
    }
  } catch (error) {
    console.error("Failed to read error response:", error);
  }

  throw new ApiError(
    errorData?.message || `Request failed with status ${response.status}`,
    errorData?.status || response.status,
    errorData?.path || null,
    errorData?.timestamp || null,
  );
};
