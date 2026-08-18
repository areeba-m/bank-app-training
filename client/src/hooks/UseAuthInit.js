import { useEffect } from "react";
import { useDispatch } from "react-redux";
import { refreshToken } from "../redux/slices/AuthSlice.js";
import { authApi } from "../services/AuthApi.js";

export const useAuthInit = () => {
  const dispatch = useDispatch();

  useEffect(() => {
    const publicPaths = [
      "/login",
      "/activate",
    ];

    const currentPath = window.location.pathname;

    if (publicPaths.includes(currentPath)) {
      console.log(
          `Skipping authentication initialization for ${currentPath}`,
      );
      return;
    }

    const initializeAuth = async () => {
      try {
        console.log("Initializing CSRF token...");

        await authApi.initCsrf();

        console.log("CSRF token initialized");

        console.log("Attempting authentication refresh...");

        await dispatch(refreshToken()).unwrap();

        console.log("Authentication refresh successful");
      } catch (error) {
        console.error(
            "Authentication initialization failed:",
            error,
        );
      }
    };

    initializeAuth();
  }, [dispatch]);
};