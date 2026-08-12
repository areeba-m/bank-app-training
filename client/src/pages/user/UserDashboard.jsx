import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { TransactionModal } from "../../components/TransactionModal";
import { TransferModal } from "../../components/TransferModal.jsx";
import {
  ArrowDownRight,
  ArrowRight,
  ArrowUpRight,
  CheckCircle2,
  Mail,
  MapPin,
  Receipt,
  Send,
  Sparkles,
  User,
  Wallet,
} from "lucide-react";

import {
  executeTransaction,
  fetchBalance,
  fetchTransactions,
} from "../../redux/slices/UserTransactionSlice.js";
import { fetchCurrentUser } from "../../redux/slices/UserSlice.js";
import { executeTransfer } from "../../redux/slices/MoneyTransferSlice.js";
import { Link } from "react-router-dom";

export const UserDashboard = () => {
  const dispatch = useDispatch();

  const {
    user,
    status: userStatus,
    error: userError,
  } = useSelector((state) => state.user);

  const {
    balanceInfo,
    balanceStatus,
    balanceError,
    list: allTransactions,
    status: transactionStatus,
    error: transactionError,
  } = useSelector((state) => state.userTransactions);

  const [isTxnModalOpen, setIsTxnModalOpen] = useState(false);
  const [toastMsg, setToastMsg] = useState("");
  const [isTransferModalOpen, setIsTransferModalOpen] = useState(false);

  const { status: transferStatus, error: transferError } = useSelector(
    (state) => state.moneyTransfer,
  );

  useEffect(() => {
    const loadData = async () => {
      try {
        await dispatch(fetchCurrentUser()).unwrap();

        await Promise.all([
          dispatch(fetchBalance()).unwrap(),
          dispatch(fetchTransactions()).unwrap(),
        ]);
      } catch (err) {
        console.error("Failed to load user dashboard:", err);
      }
    };

    loadData();
  }, [dispatch]);

  const handleTransactionExecute = async (txnData) => {
    try {
      await dispatch(
        executeTransaction({
          accountId: user?.id,
          transactionData: txnData,
        }),
      ).unwrap();

      setToastMsg("Transaction executed successfully.");

      setIsTxnModalOpen(false);

      await Promise.all([
        dispatch(fetchBalance()).unwrap(),
        dispatch(fetchTransactions()).unwrap(),
      ]);

      setTimeout(() => {
        setToastMsg("");
      }, 3000);
    } catch (err) {
      console.error("Transaction failed:", err);
      throw err;
    }
  };

  const handleTransfer = async (transferData) => {
    try {
      await dispatch(executeTransfer(transferData)).unwrap();

      setToastMsg("Money transferred successfully.");
      setIsTransferModalOpen(false);

      setTimeout(() => {
        setToastMsg("");
      }, 3000);
    } catch (err) {
      console.error("Transfer failed:", err);
      throw err;
    }
  };

  const recentTransactions = allTransactions.slice(0, 5);

  if (userStatus === "loading" && !user) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <p className="text-sm text-slate-500">Loading account information...</p>
      </div>
    );
  }

  if (userError && !user) {
    return (
      <div className="p-6 bg-red-50 border border-red-200 rounded-2xl text-red-700">
        Failed to load account information.
        <p className="text-xs mt-1">{userError}</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {toastMsg && (
        <div className="fixed bottom-6 right-6 z-50 bg-burgundy-900 text-white px-4 py-3 rounded-2xl shadow-burgundy-lg flex items-center gap-2 text-xs font-bold animate-bounce">
          <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          {toastMsg}
        </div>
      )}

      <div className="bg-gradient-to-br from-burgundy-900 via-burgundy-800 to-burgundy-950 rounded-3xl p-6 text-white shadow-burgundy-lg flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <span className="text-[10px] font-bold uppercase tracking-widest text-burgundy-200 bg-white/10 px-3 py-1 rounded-full">
            PERSONAL BANKING
          </span>

          <h1 className="text-xl sm:text-2xl font-extrabold tracking-tight mt-1.5">
            Welcome, {user?.name || "User"}
          </h1>

          <p className="text-xs text-burgundy-200 mt-0.5 font-mono">
            ID: {user?.id || "N/A"}
          </p>
        </div>

        <div className="flex items-center gap-2 shrink-0">
          <button
            onClick={() => setIsTransferModalOpen(true)}
            className="px-4 py-2.5 bg-white text-burgundy-900 font-extrabold text-xs rounded-xl shadow-md hover:bg-burgundy-50 transition-all flex items-center gap-1.5"
          >
            <Send className="w-4 h-4" />
            Transfer Money
          </button>

          <button
            onClick={() => setIsTxnModalOpen(true)}
            className="px-4 py-2.5 bg-white text-burgundy-900 font-extrabold text-xs rounded-xl shadow-md hover:bg-burgundy-50 transition-all flex items-center gap-1.5"
          >
            <Sparkles className="w-4 h-4 text-burgundy-700" />
            New Transaction
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
        <div className="lg:col-span-7 bg-white rounded-3xl p-6 border border-burgundy-100/80 shadow-xs space-y-5">
          <div className="flex justify-between items-center border-b border-slate-100 pb-3">
            <div className="flex items-center gap-2">
              <div className="p-2 bg-burgundy-50 text-burgundy-700 rounded-xl">
                <Wallet className="w-4 h-4" />
              </div>

              <h3 className="font-extrabold text-slate-900 text-xs">
                Account Balance Statement
              </h3>
            </div>
            <span
              className={`px-2.5 py-0.5 rounded-full text-[10px] font-mono font-bold ${
                balanceInfo?.indicator === "DB"
                  ? "bg-rose-50 text-rose-700 border border-rose-200"
                  : "bg-emerald-50 text-emerald-700 border border-emerald-200"
              }`}
            >
              {balanceInfo?.indicator || "CR"}
            </span>
          </div>

          <div>
            <p className="text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-0.5">
              Available Balance
            </p>

            {balanceStatus === "loading" ? (
              <p className="text-xl font-bold text-slate-400">Loading...</p>
            ) : (
              <p className="text-3xl sm:text-4xl font-extrabold font-mono text-burgundy-900 tracking-tight">
                $
                {Number(balanceInfo?.amount ?? 0).toLocaleString("en-US", {
                  minimumFractionDigits: 2,
                })}
              </p>
            )}

            <p className="text-[10px] text-slate-400 font-mono mt-1">
              Date:{" "}
              {balanceInfo?.date
                ? new Date(balanceInfo.date).toLocaleString()
                : "N/A"}
            </p>

            {balanceError && (
              <p className="text-xs text-red-500 mt-2">{balanceError}</p>
            )}
          </div>

          <div className="grid grid-cols-2 gap-3 pt-1">
            <button
              onClick={() => setIsTxnModalOpen(true)}
              className="py-2.5 px-3 bg-emerald-50 hover:bg-emerald-100 text-emerald-800 border border-emerald-200 font-bold rounded-xl text-xs flex items-center justify-center gap-1.5 transition-all"
            >
              <ArrowDownRight className="w-4 h-4 text-emerald-600" />
              Deposit
            </button>

            <button
              onClick={() => setIsTxnModalOpen(true)}
              className="py-2.5 px-3 bg-rose-50 hover:bg-rose-100 text-rose-800 border border-rose-200 font-bold rounded-xl text-xs flex items-center justify-center gap-1.5 transition-all"
            >
              <ArrowUpRight className="w-4 h-4 text-rose-600" />
              Withdraw
            </button>
          </div>
        </div>

        <div className="lg:col-span-5 bg-sand-50/70 rounded-3xl p-5 border border-burgundy-100/80 shadow-xs space-y-3">
          <div className="flex items-center gap-2 text-burgundy-900 font-bold text-xs border-b border-burgundy-100/80 pb-2">
            <User className="w-4 h-4 text-burgundy-700" />

            <span>Account Attributes</span>
          </div>

          <div className="space-y-2 text-xs">
            <div>
              <p className="text-[9px] text-slate-400 uppercase font-bold">
                Holder Name
              </p>

              <p className="font-bold text-slate-800">{user?.name || "N/A"}</p>
            </div>

            <div>
              <p className="text-[9px] text-slate-400 uppercase font-bold">
                Email
              </p>

              <p className="font-semibold text-slate-700 flex items-center gap-1 mt-0.5">
                <Mail className="w-3.5 h-3.5 text-burgundy-600" />
                {user?.email || "N/A"}
              </p>
            </div>

            <div>
              <p className="text-[9px] text-slate-400 uppercase font-bold">
                Address
              </p>

              <p className="font-semibold text-slate-700 flex items-start gap-1 mt-0.5">
                <MapPin className="w-3.5 h-3.5 text-burgundy-600 shrink-0 mt-0.5" />

                <span className="truncate">{user?.address || "N/A"}</span>
              </p>
            </div>
          </div>

          <div className="pt-1">
            <Link
              to="/user/profile"
              className="text-xs font-bold text-burgundy-700 hover:text-burgundy-900 inline-flex items-center gap-1"
            >
              Full Profile
              <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>
        </div>
      </div>
      <div className="bg-white rounded-3xl border border-burgundy-100/80 shadow-xs p-5 space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Receipt className="w-4 h-4 text-burgundy-700" />

            <h3 className="font-extrabold text-slate-900 text-xs">
              Recent Ledger
            </h3>
          </div>

          <Link
            to="/user/transactions"
            className="text-xs font-bold text-burgundy-700 hover:text-burgundy-900 flex items-center gap-1"
          >
            View All
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-slate-600">
            <thead className="bg-burgundy-50/70 text-burgundy-900 font-bold uppercase tracking-wider border-b border-burgundy-100">
              <tr>
                <th className="py-3 px-4">Txn ID</th>
                <th className="py-3 px-4">Date & Time</th>
                <th className="py-3 px-4">Description</th>
                <th className="py-3 px-4">Indicator</th>
                <th className="py-3 px-4 text-right">Amount ($)</th>
              </tr>
            </thead>

            <tbody className="divide-y divide-slate-100">
              {transactionStatus === "loading" ? (
                <tr>
                  <td colSpan="5" className="py-6 text-center text-slate-400">
                    Loading transactions...
                  </td>
                </tr>
              ) : recentTransactions.length === 0 ? (
                <tr>
                  <td colSpan="5" className="py-6 text-center text-slate-400">
                    No matching transactions.
                  </td>
                </tr>
              ) : (
                recentTransactions.map((t, index) => (
                  <tr
                    key={t.id}
                    className="hover:bg-sand-50/50 transition-colors"
                  >
                    <td className="py-3 px-4 font-mono font-bold text-slate-800">
                      {index + 1}
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
                          t.indicator === "CR"
                            ? "bg-emerald-50 text-emerald-700 border border-emerald-200"
                            : "bg-rose-50 text-rose-700 border border-rose-200"
                        }`}
                      >
                        {t.indicator}
                      </span>
                    </td>

                    <td className="py-3 px-4 text-right font-mono font-bold">
                      <span
                        className={
                          t.indicator === "CR"
                            ? "text-emerald-700"
                            : "text-rose-700"
                        }
                      >
                        {t.indicator === "CR" ? "+" : "-"}$
                        {Number(t.amount).toLocaleString("en-US", {
                          minimumFractionDigits: 2,
                        })}
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {transactionError && (
          <p className="text-xs text-red-500">{transactionError}</p>
        )}
      </div>

      <TransactionModal
        isOpen={isTxnModalOpen}
        onClose={() => setIsTxnModalOpen(false)}
        onExecuteTransaction={handleTransactionExecute}
        currentBalance={Number(balanceInfo?.amount ?? 0)}
      />

      <TransferModal
        isOpen={isTransferModalOpen}
        onClose={() => setIsTransferModalOpen(false)}
        onExecuteTransfer={handleTransfer}
        loading={transferStatus === "loading"}
      />
    </div>
  );
};
