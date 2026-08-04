import { Navigate, useLocation } from 'react-router-dom';
import { useSelector } from 'react-redux';

export const ProtectedRoute = ({ children, requiredRole }) => {
    const { user, isAuthenticated } = useSelector((state) => state.auth);
    const location = useLocation();

    if (!isAuthenticated) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    if (requiredRole && user?.role !== requiredRole) {
        const fallbackPath = user?.role === 'ADMIN' ? '/admin/dashboard' : '/user/dashboard';
        return <Navigate to={fallbackPath} replace />;
    }

    return children;
};