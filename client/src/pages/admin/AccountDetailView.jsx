import {useState, useEffect, useCallback} from 'react';
import {useParams, useNavigate, Link} from 'react-router-dom';
import {useSelector, useDispatch} from 'react-redux';
import {fetchAccountById} from '../../redux/slices/accountsSlice.js';
import {
    fetchBalance,
    fetchTransactions
} from '../../redux/slices/transactionsSlice.js';
import {
    ArrowLeft,
    Mail,
    MapPin,
    Key,
    Receipt,
    Shield,
    Calendar
} from 'lucide-react';

export const AccountDetailView = () => {

    const {id} = useParams();
    const navigate = useNavigate();
    const dispatch = useDispatch();

    const {selectedAccount: account} = useSelector(
        (state) => state.accounts
    );

    const {
        balanceInfo,
        list: transactions
    } = useSelector(
        (state) => state.transactions
    );

    const [loading, setLoading] = useState(true);

    const loadData = useCallback(async () => {
        setLoading(true);

        try {
            await Promise.all([
                dispatch(fetchAccountById(id)).unwrap(),
                dispatch(fetchBalance(id)).unwrap(),
                dispatch(fetchTransactions(id)).unwrap()
            ]);
        } catch (err) {
            console.error('Failed to load account:', err);
        } finally {
            setLoading(false);
        }
    }, [id, dispatch]);

    useEffect(() => {
        const fetchData = async () => {
            await loadData();
        };

        void fetchData();
    }, [loadData]);

    if (loading) {
        return (
            <div className="p-8 text-center text-slate-400 text-xs">
                Loading statement details...
            </div>
        );
    }

    if (!account) {
        return (
            <div className="p-8 space-y-4">
                <p className="text-xs font-semibold text-rose-700">
                    Account not found.
                </p>

                <button
                    onClick={() => navigate('/admin/dashboard')}
                    className="text-xs font-bold text-burgundy-700 underline"
                >
                    Back to Directory
                </button>
            </div>
        );
    }

    return (
        <div className="space-y-6">

            {/* Back button */}
            <div className="flex items-center">
                <Link
                    to="/admin/dashboard"
                    className="inline-flex items-center gap-2 text-xs font-bold text-slate-600 hover:text-burgundy-800 transition-colors"
                >
                    <ArrowLeft className="w-4 h-4 text-burgundy-700"/>
                    Directory
                </Link>
            </div>

            {/* Account Information */}
            <div
                className="bg-white rounded-3xl p-6 border border-burgundy-100/80 shadow-xs grid grid-cols-1 lg:grid-cols-12 gap-6 items-center">

                <div className="lg:col-span-7 space-y-3">

                    <div className="flex items-center gap-3">

                        <div
                            className="w-10 h-10 rounded-xl bg-burgundy-700 text-white flex items-center justify-center font-bold text-lg shadow-burgundy-glow">
                            {account.name.charAt(0)}
                        </div>

                        <div>

                            <div className="flex items-center gap-2">

                                <h1 className="text-lg font-extrabold text-slate-900">
                                    {account.name}
                                </h1>

                                <span
                                    className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-burgundy-50 text-burgundy-800 border border-burgundy-200">
                                    {account.role}
                                </span>

                            </div>

                            <p className="text-[11px] text-slate-400 font-mono">
                                ID: {account.id}
                            </p>

                        </div>

                    </div>

                    <div
                        className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs text-slate-600 pt-2 border-t border-slate-100">

                        <div className="flex items-center gap-2">
                            <Mail className="w-3.5 h-3.5 text-burgundy-600 shrink-0"/>
                            <span>{account.email}</span>
                        </div>

                        <div className="flex items-center gap-2">
                            <MapPin className="w-3.5 h-3.5 text-burgundy-600 shrink-0"/>
                            <span className="truncate">
                                {account.address || 'N/A'}
                            </span>
                        </div>

                        <div className="flex items-center gap-2">
                            <Key className="w-3.5 h-3.5 text-burgundy-600 shrink-0"/>
                            <span>Password protected</span>
                        </div>

                        <div className="flex items-center gap-2">
                            <Shield className="w-3.5 h-3.5 text-burgundy-600 shrink-0"/>
                            <span>Active</span>
                        </div>

                    </div>

                </div>

                {/* Current Balance */}
                <div
                    className="lg:col-span-5 bg-gradient-to-br from-burgundy-900 via-burgundy-800 to-burgundy-950 p-5 rounded-2xl text-white shadow-burgundy-lg">

                    <div className="flex justify-between items-center mb-3">

                        <span className="text-[9px] uppercase font-bold tracking-widest text-burgundy-200">
                            Current Balance
                        </span>

                        <span
                            className="px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-emerald-500/20 text-emerald-300 border border-emerald-400/30">
                            {balanceInfo?.dbCrIndicator || 'CR'}
                        </span>

                    </div>

                    <p className="text-2xl font-extrabold font-mono tracking-tight text-white mb-2">
                        ${(balanceInfo?.amount || 0).toLocaleString(
                        'en-US',
                        {minimumFractionDigits: 2}
                    )}
                    </p>

                    <div className="flex items-center gap-1.5 text-[10px] text-burgundy-200 font-mono">

                        <Calendar className="w-3.5 h-3.5"/>

                        <span>
                            Date: {balanceInfo?.date || 'Today'}
                        </span>

                    </div>

                </div>

            </div>

            {/* Transactions */}
            <div className="bg-white rounded-3xl border border-burgundy-100/80 shadow-xs p-5 space-y-4">

                <div className="flex items-center justify-between">

                    <div className="flex items-center gap-2">

                        <Receipt className="w-4 h-4 text-burgundy-700"/>

                        <h2 className="text-sm font-extrabold text-slate-900">
                            Ledger Statement
                        </h2>

                    </div>

                    <span className="text-[11px] text-slate-400 font-mono">
                        {transactions.length} Records
                    </span>

                </div>

                <div className="overflow-x-auto">

                    <table className="w-full text-left text-xs text-slate-600">

                        <thead
                            className="bg-sand-50 text-burgundy-900 font-bold uppercase tracking-wider border-b border-burgundy-100">

                        <tr>
                            <th className="py-3 px-4">Txn ID</th>
                            <th className="py-3 px-4">Date & Time</th>
                            <th className="py-3 px-4">Description</th>
                            <th className="py-3 px-4">Indicator</th>
                            <th className="py-3 px-4 text-right">
                                Amount ($)
                            </th>
                        </tr>

                        </thead>

                        <tbody className="divide-y divide-slate-100">

                        {transactions.length === 0 ? (

                            <tr>
                                <td
                                    colSpan="5"
                                    className="py-6 text-center text-slate-400"
                                >
                                    No transactions recorded.
                                </td>
                            </tr>

                        ) : (

                            transactions.map((t) => (

                                <tr
                                    key={t.id}
                                    className="hover:bg-sand-50/50 transition-colors"
                                >

                                    <td className="py-3 px-4 font-mono font-bold text-slate-800">
                                        {t.id}
                                    </td>

                                    <td className="py-3 px-4 font-mono text-slate-500">
                                        {new Date(t.date).toLocaleString()}
                                    </td>

                                    <td className="py-3 px-4 font-semibold text-slate-800">
                                        {t.description}
                                    </td>

                                    <td className="py-3 px-4">

                                            <span
                                                className={`px-2 py-0.5 rounded text-[10px] font-mono font-bold ${
                                                    t.dbCrIndicator === 'CR'
                                                        ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                                                        : 'bg-rose-50 text-rose-700 border border-rose-200'
                                                }`}
                                            >
                                                {t.dbCrIndicator}
                                            </span>

                                    </td>

                                    <td className="py-3 px-4 text-right font-mono font-bold">

                                            <span
                                                className={
                                                    t.dbCrIndicator === 'CR'
                                                        ? 'text-emerald-700'
                                                        : 'text-rose-700'
                                                }
                                            >
                                                {t.dbCrIndicator === 'CR'
                                                    ? '+'
                                                    : '-'}
                                                ${t.amount.toLocaleString(
                                                'en-US',
                                                {
                                                    minimumFractionDigits: 2
                                                }
                                            )}
                                            </span>

                                    </td>

                                </tr>

                            ))

                        )}

                        </tbody>

                    </table>

                </div>

            </div>

        </div>
    );
};
