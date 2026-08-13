import { ApiError } from "./ApiError.js";

export const handleApiResponse = async (response) => {
  if (response.ok) {
    return response;
  }

  let errorData = null;

  try {
    errorData = await response.json();
  } catch (error) {
    console.error("Failed to parse error response:", error);
  }

  throw new ApiError(
    errorData?.message || "Something went wrong",
    errorData?.status || response.status,
    errorData?.path || null,
    errorData?.timestamp || null,
  );
};
