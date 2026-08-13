import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { clearAuthError, loginUser } from "../redux/slices/AuthSlice.js";
import {
  AlertCircle,
  Eye,
  EyeOff,
  KeyRound,
  Lock,
  Sparkles,
  User,
} from "lucide-react";

export const LoginPage = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const {
    isAuthenticated,
    user,
    status,
    error: reduxError,
  } = useSelector((state) => state.auth);

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [localError, setLocalError] = useState("");
  const [showPassword, setShowPassword] = useState(false);

  useEffect(() => {
    if (isAuthenticated && user) {
      const redirectPath =
        user.role === "ADMIN" ? "/admin/dashboard" : "/user/dashboard";

      navigate(redirectPath, { replace: true });
    }
  }, [isAuthenticated, user, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    setLocalError("");
    dispatch(clearAuthError());

    if (!email.trim() || !password.trim()) {
      setLocalError("Please enter both Email Address and Password.");
      return;
    }

    const resultAction = await dispatch(
      loginUser({
        email: email.trim(),
        password,
      }),
    );

    if (loginUser.fulfilled.match(resultAction)) {
      const loggedUser = resultAction.payload;

      const targetPath =
        loggedUser.role === "ADMIN" ? "/admin/dashboard" : "/user/dashboard";

      navigate(targetPath, { replace: true });
    }
  };

  const activeError = localError || reduxError;

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center p-4 bg-[#FAF9FA]">
      <div className="max-w-md w-full">
        <div className="bg-white rounded-3xl shadow-xl border border-burgundy-100/80 overflow-hidden">
          <div className="bg-gradient-to-br from-burgundy-800 via-burgundy-900 to-burgundy-950 p-6 text-center text-white relative">
            <div className="w-12 h-12 mx-auto rounded-2xl bg-white/10 backdrop-blur-md flex items-center justify-center border border-white/20 mb-2 shadow-burgundy-glow">
              <KeyRound className="w-6 h-6 text-burgundy-100" />
            </div>

            <h2 className="text-xl font-extrabold tracking-tight">
              Portal Sign In
            </h2>

            <p className="text-xs text-burgundy-200 mt-0.5">
              Enter your credentials to access your dashboard
            </p>
          </div>
          <form onSubmit={handleSubmit} className="p-6 space-y-4">
            {activeError && (
              <div className="p-3 bg-rose-50 border border-rose-200 text-rose-800 text-xs font-semibold rounded-xl flex items-center gap-2">
                <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
                <span>{activeError}</span>
              </div>
            )}
            <label
              htmlFor="email"
              className="block text-[11px] font-bold text-slate-700 uppercase tracking-wider mb-1"
            >
              Email Address *
            </label>

            <div className="relative">
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Enter your email address"
                className="w-full pl-9 pr-3 py-2.5 text-xs border border-slate-300 rounded-xl focus:ring-2 focus:ring-burgundy-500 focus:border-burgundy-500 font-semibold"
                required
              />

              <User className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
            </div>

            <label
              htmlFor="password"
              className="block text-[11px] font-bold text-slate-700 uppercase tracking-wider mb-1"
            >
              Password *
            </label>

            <div className="relative">
              <input
                id="password"
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full pl-9 pr-10 py-2.5 text-xs border border-slate-300 rounded-xl focus:ring-2 focus:ring-burgundy-500 focus:border-burgundy-500 font-mono"
                required
              />

              <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-3" />

              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-3 text-slate-400 hover:text-burgundy-700"
                aria-label={showPassword ? "Hide password" : "Show password"}
              >
                {showPassword ? (
                  <EyeOff className="w-4 h-4" />
                ) : (
                  <Eye className="w-4 h-4" />
                )}
              </button>
            </div>

            <button
              type="submit"
              disabled={status === "loading"}
              className="w-full py-3 bg-gradient-to-r from-burgundy-700 via-burgundy-800 to-burgundy-900 hover:from-burgundy-800 hover:to-burgundy-950 text-white font-bold rounded-xl shadow-burgundy-lg hover:shadow-burgundy-glow transition-all duration-200 flex items-center justify-center gap-1.5 text-xs disabled:opacity-50"
            >
              <Sparkles className="w-4 h-4 text-burgundy-200" />

              {status === "loading" ? "Authenticating..." : "Sign In"}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};
