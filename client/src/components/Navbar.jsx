import { Link, useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { logout } from '../redux/    slices  /authSlice.js';
import { Building2, LogOut, User, Shield, Sparkles } from 'lucide-react';

export const Navbar = () => {
    const { user, isAuthenticated } = useSelector((state) => state.auth);
    const dispatch = useDispatch();
    const navigate = useNavigate();

    const handleLogout = () => {
        dispatch(logout());
        navigate('/');
    };

    return (
        <header className="sticky top-0 z-40 bg-white/90 backdrop-blur-md border-b border-burgundy-100/80 shadow-xs">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
                <Link to={isAuthenticated ? (user?.role === 'ADMIN' ? '/admin/dashboard' : '/user/dashboard') : '/'} className="flex items-center gap-2.5 group">
                    <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-burgundy-700 to-burgundy-900 flex items-center justify-center text-white shadow-burgundy-glow group-hover:scale-105 transition-transform duration-200">
                        <Building2 className="w-4 h-4 text-burgundy-100" />
                    </div>
                    <div>
            <span className="text-lg font-extrabold text-burgundy-900 tracking-tight flex items-center gap-1">
              MY <span className="font-light text-burgundy-600">BANK</span>
            </span>
                    </div>
                </Link>
                <div className="flex items-center gap-3">
                    {isAuthenticated ? (
                        <div className="flex items-center gap-3">
                            <span className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[11px] font-bold tracking-wide uppercase shadow-xs ${
                                user?.role === 'ADMIN'
                                    ? 'bg-burgundy-700 text-white shadow-burgundy-glow'
                                    : 'bg-burgundy-50 text-burgundy-800 border border-burgundy-200'
                            }`}>
                {user?.role === 'ADMIN' ? <Shield className="w-3.5 h-3.5" /> : <User className="w-3.5 h-3.5" />}
                                {user?.role}
              </span>
                            <button
                                onClick={handleLogout}
                                className="flex items-center gap-1.5 px-3.5 py-1.5 text-xs font-semibold text-burgundy-800 bg-burgundy-50 hover:bg-burgundy-100 border border-burgundy-200 rounded-lg transition-all duration-150"
                                title="Log Out"
                            >
                                <LogOut className="w-3.5 h-3.5 text-burgundy-700" />
                                <span>Logout</span>
                            </button>
                        </div>
                    ) : (
                        <div className="flex items-center gap-3">
                            <Link
                                to="/login"
                                className="px-4 py-2 text-xs font-bold text-white bg-gradient-to-r from-burgundy-700 to-burgundy-900 hover:from-burgundy-800 hover:to-burgundy-950 rounded-xl shadow-burgundy-glow transition-all duration-200 flex items-center gap-1.5"
                            >
                                <Sparkles className="w-3.5 h-3.5 text-burgundy-200" />
                                Sign In
                            </Link>
                        </div>
                    )}
                </div>

            </div>
        </header>
    );
};