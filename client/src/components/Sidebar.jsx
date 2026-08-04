import { NavLink } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { LayoutDashboard, Receipt, User, Database, RefreshCw } from 'lucide-react';
import { bankApi } from '../services/api';

export const Sidebar = () => {
    const { user } = useSelector((state) => state.auth);
    const isAdmin = user?.role === 'ADMIN';

    const adminLinks = [
        { to: '/admin/dashboard', label: 'Accounts Management', icon: LayoutDashboard },
    ];

    const userLinks = [
        { to: '/user/dashboard', label: 'Dashboard & Balance', icon: LayoutDashboard },
        { to: '/user/transactions', label: 'Transactions Ledger', icon: Receipt },
        { to: '/user/profile', label: 'Account Information', icon: User },
    ];

    const links = isAdmin ? adminLinks : userLinks;

    return (
        <aside className="w-60 bg-white border-r border-burgundy-100/80 min-h-[calc(100vh-4rem)] p-4 flex flex-col justify-between hidden md:flex">
            <div className="space-y-6">
                <div>
                    <p className="px-3 text-[10px] font-bold uppercase tracking-widest text-burgundy-600 mb-3">
                        {isAdmin ? 'Admin Portal' : 'Navigation'}
                    </p>
                    <nav className="space-y-1">
                        {links.map((link) => {
                            const Icon = link.icon;
                            return (
                                <NavLink
                                    key={link.to}
                                    to={link.to}
                                    className={({ isActive }) =>
                                        `flex items-center gap-3 px-3.5 py-2 rounded-xl font-medium text-xs transition-all duration-150 ${
                                            isActive
                                                ? 'bg-burgundy-700 text-white font-bold shadow-burgundy-glow'
                                                : 'text-slate-600 hover:bg-burgundy-50 hover:text-burgundy-800'
                                        }`
                                    }
                                >
                                    <Icon className="w-4 h-4 shrink-0" />
                                    <span>{link.label}</span>
                                </NavLink>
                            );
                        })}
                    </nav>
                </div>
            </div>

        </aside>
    );
};