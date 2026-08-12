import { useEffect } from "react";
import { useDispatch } from "react-redux";
import { refreshToken } from "../redux/slices/authSlice.js";
import { authApi } from "../services/AuthApi.js";

export const useAuthInit = () => {
  const dispatch = useDispatch();
  useEffect(() => {
    const initializeAuth = async () => {
      try {
        console.log("Initializing CSRF token...");
        await authApi.initCsrf();
        console.log("CSRF token initialized");
        console.log("Attempting authentication refresh...");
        await dispatch(refreshToken());
      } catch (error) {
        console.error("Authentication initialization failed:", error);
      }
    };
    initializeAuth();
  }, [dispatch]);
};
