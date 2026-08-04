import { Link } from 'react-router-dom';
import { AlertTriangle, Home } from 'lucide-react';

export const NotFoundPage = () => {
    return (
        <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center p-4 bg-[#FAF9FA]">
            <div className="max-w-md w-full text-center space-y-4">
                <div className="w-16 h-16 mx-auto rounded-2xl bg-rose-50 text-rose-600 flex items-center justify-center border border-rose-200">
                    <AlertTriangle className="w-8 h-8" />
                </div>
                <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">404 - Page Not Found</h1>
                <p className="text-xs text-slate-500">
                    The banking resource or route you requested does not exist.
                </p>
                <div>
                    <Link
                        to="/"
                        className="inline-flex items-center gap-2 px-5 py-2.5 bg-burgundy-700 hover:bg-burgundy-800 text-white text-xs font-bold rounded-xl shadow-burgundy-glow transition-all"
                    >
                        <Home className="w-4 h-4" />
                        Return to Homepage
                    </Link>
                </div>
            </div>
        </div>
    );
};