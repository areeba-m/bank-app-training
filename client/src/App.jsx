import { BrowserRouter, Route, Routes } from "react-router-dom";
import { useAuthInit } from "./hooks/UseAuthInit.js";
import { Navbar } from "./components/Navbar";
import { LandingPage } from "./pages/LandingPage";
import { LoginPage } from "./pages/LoginPage";
import { UserDashboard } from "./pages/user/UserDashboard";
import { ProtectedRoute } from "./components/ProtectedRoute";
import AuthenticatedLayout from "./layout/AuthenticatedSidbarLayout.jsx";
import { ProfilePage } from "./pages/user/ProfilePage";
import { TransactionsPage } from "./pages/user/TransactionsPage";
import { AdminDashboard } from "./pages/admin/AdminDashboard";
import { AccountDetailView } from "./pages/admin/AccountDetailView";
import { OtpVerificationPage } from "./pages/OtpVerificationPage.jsx";
export function AppRoutes() {
  useAuthInit();
  return (
    <div className="min-h-screen bg-[#FAF9FA] text-slate-800 flex flex-col font-sans">
      <Navbar />
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/user/dashboard"
          element={
            <ProtectedRoute>
              <AuthenticatedLayout>
                <UserDashboard />
              </AuthenticatedLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/user/profile"
          element={
            <ProtectedRoute>
              <AuthenticatedLayout>
                <ProfilePage />
              </AuthenticatedLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="user/transactions"
          element={
            <ProtectedRoute>
              <AuthenticatedLayout>
                <TransactionsPage />
              </AuthenticatedLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/dashboard"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <AuthenticatedLayout>
                <AdminDashboard />
              </AuthenticatedLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/account/:id"
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <AuthenticatedLayout>
                <AccountDetailView />
              </AuthenticatedLayout>
            </ProtectedRoute>
          }
        />
          <Route
              path="/activate"
              element={<OtpVerificationPage />}
          />
      </Routes>
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AppRoutes />
    </BrowserRouter>
  );
}
