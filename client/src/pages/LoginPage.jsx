import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { loginUser, clearAuthError } from '../redux/    slices  /authSlice.js';
import { Lock, User, Shield, KeyRound, AlertCircle, Sparkles } from 'lucide-react';

export const LoginPage = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const dispatch = useDispatch();
    const { isAuthenticated, user, status, error: reduxError } = useSelector((state) => state.auth);

    const [id, setId] = useState('');
    const [password, setPassword] = useState('');
    const [localError, setLocalError] = useState('');

    useEffect(() => {
        if (location.state?.prefill === 'admin') {
            setId('admin');
            setPassword('admin123');
        } else if (location.state?.prefill === 'user1') {
            setId('user1');
            setPassword('user123');
        }
    }, [location.state]);

    useEffect(() => {
        if (isAuthenticated && user) {
            const redirectPath = user.role === 'ADMIN' ? '/admin/dashboard' : '/user/dashboard';
            navigate(redirectPath, { replace: true });
        }
    }, [isAuthenticated, user, navigate]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLocalError('');
        dispatch(clearAuthError());

        if (!id.trim() || !password.trim()) {
            setLocalError('Please enter both Account ID and Password.');
            return;
        }

        const resultAction = await dispatch(loginUser({ id: id.trim(), password }));
        if (loginUser.fulfilled.match(resultAction)) {
            const loggedUser = resultAction.payload;
            const targetPath = loggedUser.role === 'ADMIN' ? '/admin/dashboard' : '/user/dashboard';
            navigate(targetPath, { replace: true });
        }
    };

    const handleQuickSelect = (quickId, quickPass) => {
        setId(quickId);
        setPassword(quickPass);
        setLocalError('');
        dispatch(clearAuthError());
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
                        <h2 className="text-xl font-extrabold tracking-tight">Portal Sign In</h2>
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
                        <div>
                            <label className="block text-[11px] font-bold text-slate-700 uppercase tracking-wider mb-1">
                                Account ID *
                            </label>
                            <div className="relative">
                                <input
                                    type="text"
                                    value={id}
                                    onChange={(e) => setId(e.target.value)}
                                    placeholder="e.g. admin or user1"
                                    className="w-full pl-9 pr-3 py-2.5 text-xs border border-slate-300 rounded-xl focus:ring-2 focus:ring-burgundy-500 focus:border-burgundy-500 font-mono font-semibold"
                                    required
                                />
                                <User className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                            </div>
                        </div>
                        <div>
                            <label className="block text-[11px] font-bold text-slate-700 uppercase tracking-wider mb-1">
                                Password *
                            </label>
                            <div className="relative">
                                <input
                                    type="password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    placeholder="••••••••"
                                    className="w-full pl-9 pr-3 py-2.5 text-xs border border-slate-300 rounded-xl focus:ring-2 focus:ring-burgundy-500 focus:border-burgundy-500 font-mono"
                                    required
                                />
                                <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                            </div>
                        </div>
                        <button
                            type="submit"
                            disabled={status === 'loading'}
                            className="w-full py-3 bg-gradient-to-r from-burgundy-700 via-burgundy-800 to-burgundy-900 hover:from-burgundy-800 hover:to-burgundy-950 text-white font-bold rounded-xl shadow-burgundy-lg hover:shadow-burgundy-glow transition-all duration-200 flex items-center justify-center gap-1.5 text-xs disabled:opacity-50"
                        >
                            <Sparkles className="w-4 h-4 text-burgundy-200" />
                            {status === 'loading' ? 'Authenticating...' : 'Sign In'}
                        </button>
                        <div className="pt-3 border-t border-slate-100">
                            <p className="text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2 text-center">
                                Demo Credentials
                            </p>
                            <div className="grid grid-cols-2 gap-2 text-xs">
                                <button
                                    type="button"
                                    onClick={() => handleQuickSelect('admin', 'admin123')}
                                    className="p-2 rounded-xl border border-burgundy-200 bg-burgundy-50 hover:bg-burgundy-100 text-burgundy-900 font-bold transition-colors flex items-center justify-center gap-1.5"
                                >
                                    <Shield className="w-3.5 h-3.5 text-burgundy-700" />
                                    Admin
                                </button>

                                <button
                                    type="button"
                                    onClick={() => handleQuickSelect('user1', 'user123')}
                                    className="p-2 rounded-xl border border-slate-200 bg-slate-50 hover:bg-slate-100 text-slate-800 font-bold transition-colors flex items-center justify-center gap-1.5"
                                >
                                    <User className="w-3.5 h-3.5 text-slate-600" />
                                    User1
                                </button>
                            </div>
                        </div>

                    </form>

                </div>
            </div>
        </div>
    );
};