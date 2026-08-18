import { useCallback, useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import {
  executeTransaction,
  fetchBalance,
  fetchSpendingAnalytics,
  fetchTransactions,
} from "../../redux/slices/UserTransactionSlice.js";
import { executeTransfer } from "../../redux/slices/MoneyTransferSlice.js";
import { TransactionModal } from "../../components/TransactionModal";
import {
  ArrowDownRight,
  ArrowUpRight,
  Filter,
  PlusCircle,
  Receipt,
  Search,
  Send,
} from "lucide-react";
import { TransferModal } from "../../components/TransferModal.jsx";
import { SpendingAnalytics } from "../../components/SpendingAnalytics.jsx";

export const TransactionsPage = () => {
  const dispatch = useDispatch();

  const { user } = useSelector((state) => state.auth);

  const {
    list: transactions,
    status,
    balanceInfo,
    page,
    size,
    totalPages,
    totalElements,
    spendingAnalytics,
    analyticsStatus,
    analyticsError,
  } = useSelector((state) => state.userTransactions);

  const loading = status === "loading";

  const [searchTerm, setSearchTerm] = useState("");
  const [filterType, setFilterType] = useState("ALL");
  const [isModalOpen, setIsModalOpen] = useState(false);

  const { status: transferStatus } = useSelector(
    (state) => state.moneyTransfer,
  );

  const [isTransferModalOpen, setIsTransferModalOpen] = useState(false);

  const [toastMsg, setToastMsg] = useState("");

  const [analyticsFrom, setAnalyticsFrom] = useState(() => {
    const date = new Date();
    date.setDate(date.getDate() - 7);

    return date.toISOString().split("T")[0];
  });

  const [analyticsTo, setAnalyticsTo] = useState(() => {
    return new Date().toISOString().split("T")[0];
  });
  const loadTransactions = useCallback(async () => {
    if (!user) return;

    try {
      await dispatch(
        fetchTransactions({
          page: 0,
          size,
        }),
      ).unwrap();
    } catch (err) {
      console.error("Failed to load transactions:", err);
    }
  }, [user, dispatch, size]);

  useEffect(() => {
    void loadTransactions();
  }, [loadTransactions]);

  useEffect(() => {
    if (!user || !analyticsFrom || !analyticsTo) {
      return;
    }

    void dispatch(
      fetchSpendingAnalytics({
        from: analyticsFrom,
        to: analyticsTo,
        insight: false,
      }),
    );
  }, [user, dispatch, analyticsFrom, analyticsTo]);

  const refreshTransactionData = async () => {
    await Promise.all([
      dispatch(fetchBalance()).unwrap(),

      dispatch(
        fetchTransactions({
          page: 0,
          size,
        }),
      ).unwrap(),

      dispatch(
        fetchSpendingAnalytics({
          from: analyticsFrom,
          to: analyticsTo,
          insight: false,
        }),
      ).unwrap(),
    ]);
  };

  const handleExecute = async (txnData) => {
    try {
      await dispatch(
        executeTransaction({
          accountId: user?.id,
          transactionData: txnData,
        }),
      ).unwrap();

      setIsModalOpen(false);

      await refreshTransactionData();

      setToastMsg("Transaction executed successfully.");

      setTimeout(() => {
        setToastMsg("");
      }, 3000);
    } catch (err) {
      console.error("Transaction failed:", err);
      throw err;
    }
  };

  const filtered = [...transactions]
    .sort((a, b) => new Date(b.date) - new Date(a.date))
    .filter((t) => {
      const description = t.description?.toLowerCase() || "";
      const transactionId = String(t.id || "").toLowerCase();
      const search = searchTerm.toLowerCase();

      const matchesSearch =
        description.includes(search) || transactionId.includes(search);

      const matchesFilter =
        filterType === "ALL" ? true : t.indicator === filterType;

      return matchesSearch && matchesFilter;
    });

  const handleTransfer = async (transferData) => {
    try {
      await dispatch(executeTransfer(transferData)).unwrap();

      setIsTransferModalOpen(false);

      await refreshTransactionData();

      setToastMsg("Money transferred successfully.");

      setTimeout(() => {
        setToastMsg("");
      }, 3000);
    } catch (err) {
      console.error("Transfer failed:", err);
      throw err;
    }
  };

  const handlePreviousPage = () => {
    if (page > 0 && !loading) {
      dispatch(
        fetchTransactions({
          page: page - 1,
          size,
        }),
      );
    }
  };

  const handleNextPage = () => {
    if (page < totalPages - 1 && !loading) {
      dispatch(
        fetchTransactions({
          page: page + 1,
          size,
        }),
      );
    }
  };

  const handleAnalyzeSpending = async (includeInsight = true) => {
    if (!analyticsFrom || !analyticsTo) {
      return;
    }

    if (analyticsFrom > analyticsTo) {
      console.error("Invalid analytics date range");
      return;
    }

    try {
      await dispatch(
        fetchSpendingAnalytics({
          from: analyticsFrom,
          to: analyticsTo,
          insight: includeInsight,
        }),
      ).unwrap();
    } catch (err) {
      console.error("Failed to fetch spending analytics:", err);
    }
  };

  return (
    <div className="space-y-6">
      {toastMsg && (
        <div className="fixed bottom-6 right-6 z-50 bg-burgundy-900 text-white px-4 py-3 rounded-2xl shadow-burgundy-lg flex items-center gap-2 text-xs font-bold animate-bounce">
          {toastMsg}
        </div>
      )}

      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-5 rounded-3xl border border-burgundy-100/80 shadow-xs">
        <div className="flex items-center gap-3">
          <div className="p-2.5 bg-burgundy-700 text-white rounded-xl shadow-burgundy-glow">
            <Receipt className="w-5 h-5" />
          </div>

          <div>
            <h1 className="text-lg font-extrabold text-slate-900">
              Transaction History
            </h1>

            <p className="text-xs text-slate-500">
              Find your recent transactions here
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2 shrink-0">
          <button
            onClick={() => setIsTransferModalOpen(true)}
            className="px-4 py-2 bg-gradient-to-r from-burgundy-700 to-burgundy-900 text-white text-xs font-bold rounded-xl shadow-burgundy-glow hover:shadow-burgundy-lg transition-all flex items-center gap-1.5"
          >
            <Send className="w-4 h-4" />
            Transfer Money
          </button>

          <button
            onClick={() => setIsModalOpen(true)}
            className="px-4 py-2 bg-gradient-to-r from-burgundy-700 to-burgundy-900 text-white text-xs font-bold rounded-xl shadow-burgundy-glow hover:shadow-burgundy-lg transition-all flex items-center gap-1.5 shrink-0"
          >
            <PlusCircle className="w-4 h-4" />
            New Transaction
          </button>
        </div>
      </div>

      <div className="bg-white p-4 rounded-3xl border border-burgundy-100/80 shadow-xs flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
        <div className="relative flex-1 max-w-md">
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search description or transaction ID..."
            className="w-full pl-9 pr-3 py-2 text-xs border border-slate-300 rounded-xl focus:ring-2 focus:ring-burgundy-500 focus:border-burgundy-500"
          />

          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
        </div>

        <div className="flex items-center gap-2">
          <Filter className="w-4 h-4 text-slate-400 shrink-0 hidden sm:block" />

          <div className="bg-sand-50 p-1 rounded-xl border border-slate-200 flex text-xs font-bold">
            <button
              onClick={() => setFilterType("ALL")}
              className={`px-3 py-1 rounded-lg transition-all ${
                filterType === "ALL"
                  ? "bg-burgundy-700 text-white shadow-xs"
                  : "text-slate-600 hover:text-slate-900"
              }`}
            >
              All
            </button>

            <button
              onClick={() => setFilterType("CR")}
              className={`px-3 py-1 rounded-lg transition-all flex items-center gap-1 ${
                filterType === "CR"
                  ? "bg-emerald-600 text-white shadow-xs"
                  : "text-slate-600 hover:text-slate-900"
              }`}
            >
              <ArrowDownRight className="w-3.5 h-3.5" />
              CR
            </button>

            <button
              onClick={() => setFilterType("DB")}
              className={`px-3 py-1 rounded-lg transition-all flex items-center gap-1 ${
                filterType === "DB"
                  ? "bg-rose-600 text-white shadow-xs"
                  : "text-slate-600 hover:text-slate-900"
              }`}
            >
              <ArrowUpRight className="w-3.5 h-3.5" />
              DB
            </button>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-3xl border border-burgundy-100/80 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-slate-600">
            <thead className="bg-burgundy-50/70 text-burgundy-900 font-bold uppercase tracking-wider border-b border-burgundy-100">
              <tr>
                <th className="py-3 px-4">Txn ID</th>
                <th className="py-3 px-4">Date & Time</th>
                <th className="py-3 px-4">Description</th>
                <th className="py-3 px-4">Counter Party </th>
                <th className="py-3 px-4">Indicator</th>
                <th className="py-3 px-4 text-right">Amount ($)</th>
              </tr>
            </thead>

            <tbody className="divide-y divide-slate-100">
              {loading ? (
                <tr>
                  <td colSpan="5" className="py-6 text-center text-slate-400">
                    Loading transactions...
                  </td>
                </tr>
              ) : filtered.length === 0 ? (
                <tr>
                  <td colSpan="5" className="py-6 text-center text-slate-400">
                    No matching transactions.
                  </td>
                </tr>
              ) : (
                filtered.map((t, index) => (
                  <tr
                    key={t.id}
                    className="hover:bg-sand-50/50 transition-colors"
                  >
                    <td className="py-3 px-4 font-mono font-bold text-slate-800">
                      {page * size + index + 1}
                    </td>

                    <td className="py-3 px-4 font-mono text-slate-500">
                      {new Date(t.date).toLocaleString()}
                    </td>

                    <td className="py-3 px-4 font-semibold text-slate-800">
                      {t.description}
                    </td>

                    <td className="py-3 px-4 font-semibold text-slate-800">
                      {t.counterpartyName !== "null" && t.counterpartyName ? (
                        <div>
                          <div>{t.counterpartyName}</div>
                          <div>{t.counterpartyEmail}</div>
                        </div>
                      ) : (
                        "---------"
                      )}
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

        {totalPages > 0 && (
          <div className="flex flex-col sm:flex-row items-center justify-between gap-3 px-4 py-3 border-t border-slate-100">
            <p className="text-xs text-slate-500">
              Showing{" "}
              <span className="font-bold text-slate-700">
                {totalElements === 0 ? 0 : page * size + 1}
              </span>{" "}
              to{" "}
              <span className="font-bold text-slate-700">
                {Math.min((page + 1) * size, totalElements)}
              </span>{" "}
              of{" "}
              <span className="font-bold text-slate-700">{totalElements}</span>{" "}
              transactions
            </p>

            <div className="flex items-center gap-2">
              <button
                onClick={handlePreviousPage}
                disabled={page === 0 || loading}
                className="px-3 py-1.5 text-xs font-bold rounded-lg border border-slate-200 text-slate-600 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed transition-all"
              >
                Previous
              </button>

              <span className="text-xs font-bold text-burgundy-900 px-2">
                Page {page + 1} of {totalPages}
              </span>

              <button
                onClick={handleNextPage}
                disabled={page >= totalPages - 1 || loading}
                className="px-3 py-1.5 text-xs font-bold rounded-lg border border-slate-200 text-slate-600 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed transition-all"
              >
                Next
              </button>
            </div>
          </div>
        )}
      </div>

      <TransactionModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onExecuteTransaction={handleExecute}
        currentBalance={Number(balanceInfo?.amount ?? 0)}
      />

      <TransferModal
        isOpen={isTransferModalOpen}
        onClose={() => setIsTransferModalOpen(false)}
        onExecuteTransfer={handleTransfer}
        loading={transferStatus === "loading"}
      />

      <SpendingAnalytics
        analytics={spendingAnalytics}
        loading={analyticsStatus === "loading"}
        error={analyticsError}
        from={analyticsFrom}
        to={analyticsTo}
        onFromChange={setAnalyticsFrom}
        onToChange={setAnalyticsTo}
        onAnalyze={handleAnalyzeSpending}
      />
    </div>
  );
};
