import { Navigate, useLocation } from 'react-router-dom';
import { useSelector } from 'react-redux';
import PropTypes from "prop-types";

export const ProtectedRoute = ({ children, requiredRole }) => {
    const { user, isAuthenticated ,status} = useSelector((state) => state.auth);
    const location = useLocation();

    if(status==="checking" && !user){
        return <div>Loading authentication...</div>;
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    if (requiredRole && user?.role !== requiredRole) {
        const fallbackPath = user?.role === 'ADMIN' ? '/admin/dashboard' : '/user/dashboard';
        return <Navigate to={fallbackPath} replace />;
    }

    return children;
};
ProtectedRoute.propTypes = {
    children: PropTypes.node.isRequired,
    requiredRole: PropTypes.string
};