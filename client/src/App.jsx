import {BrowserRouter, Routes, Route} from 'react-router-dom';
import {Navbar} from './components/Navbar';
import {LandingPage} from './pages/LandingPage';
import {LoginPage} from './pages/LoginPage';
import { UserDashboard } from './pages/user/UserDashboard';
import { ProtectedRoute } from './components/ProtectedRoute';
import AuthenticatedLayout from './layout/AuthenticatedSidbarLayout.jsx';
import { ProfilePage } from './pages/user/ProfilePage';
import { TransactionsPage } from './pages/user/TransactionsPage';

export function AppRoutes() {
    return (
        <div className="min-h-screen bg-[#FAF9FA] text-slate-800 flex flex-col font-sans">
            <Navbar/>
            <Routes>
                <Route path="/" element={<LandingPage/>}/>
                <Route path="/login" element={<LoginPage/>}/>
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
            </Routes>
        </div>
    );
}

export default function App() {
    return (
        <BrowserRouter>
            <AppRoutes/>
        </BrowserRouter>
    );
}