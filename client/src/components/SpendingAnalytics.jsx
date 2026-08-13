import {
  BarChart3,
  CalendarDays,
  DollarSign,
  PieChart,
  Sparkles,
  TrendingDown,
} from "lucide-react";
import PropTypes from "prop-types";

export const SpendingAnalytics = ({
  analytics,
  loading,
  error,
  from,
  to,
  onFromChange,
  onToChange,
  onAnalyze,
}) => {
  const formatAmount = (amount) =>
    Number(amount ?? 0).toLocaleString("en-US", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });

  const formatDate = (date) => {
    if (!date) return "";

    return new Date(date).toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    });
  };

  const categories = analytics?.byCategory
    ? Object.entries(analytics.byCategory)
    : [];

  const handleAnalyze = () => {
    if (!from || !to || loading) return;
    onAnalyze(true);
  };

  return (
    <section className="bg-white rounded-3xl border border-burgundy-100/80 shadow-xs overflow-hidden">
      <div className="p-5 border-b border-slate-100">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-burgundy-700 text-white rounded-xl shadow-burgundy-glow">
              <BarChart3 className="w-5 h-5" />
            </div>

            <div>
              <h2 className="text-lg font-extrabold text-slate-900">
                Spending Analytics
              </h2>

              <p className="text-xs text-slate-500">
                Analyze your spending by date and category
              </p>
            </div>
          </div>

          <div className="flex flex-col sm:flex-row items-stretch sm:items-end gap-2">
            <div>
              <label className="block text-[10px] font-bold uppercase tracking-wider text-slate-500 mb-1">
                From
              </label>

              <div className="relative">
                <CalendarDays className="absolute left-2.5 top-2.5 w-4 h-4 text-slate-400" />

                <input
                  type="date"
                  value={from}
                  onChange={(e) => onFromChange(e.target.value)}
                  disabled={loading}
                  className="pl-8 pr-3 py-2 text-xs border border-slate-300 rounded-xl focus:ring-2 focus:ring-burgundy-500 focus:border-burgundy-500 disabled:bg-slate-100 disabled:cursor-not-allowed"
                />
              </div>
            </div>

            <div>
              <label className="block text-[10px] font-bold uppercase tracking-wider text-slate-500 mb-1">
                To
              </label>

              <div className="relative">
                <CalendarDays className="absolute left-2.5 top-2.5 w-4 h-4 text-slate-400" />

                <input
                  type="date"
                  value={to}
                  onChange={(e) => onToChange(e.target.value)}
                  disabled={loading}
                  className="pl-8 pr-3 py-2 text-xs border border-slate-300 rounded-xl focus:ring-2 focus:ring-burgundy-500 focus:border-burgundy-500 disabled:bg-slate-100 disabled:cursor-not-allowed"
                />
              </div>
            </div>

            <button
              type="button"
              onClick={handleAnalyze}
              disabled={loading || !from || !to}
              className="px-4 py-2 bg-gradient-to-r from-burgundy-700 to-burgundy-900 text-white text-xs font-bold rounded-xl shadow-burgundy-glow hover:shadow-burgundy-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? "Generating..." : "Ai Insight"}
            </button>
          </div>
        </div>
      </div>

      {error && (
        <div className="mx-5 mt-5 p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs font-semibold">
          {error}
        </div>
      )}

      {loading ? (
        <div className="p-8 text-center">
          <Sparkles className="w-8 h-8 mx-auto text-burgundy-400 mb-3 animate-pulse" />

          <p className="text-sm font-semibold text-slate-500">
            Generating spending analysis...
          </p>

          <p className="text-xs text-slate-400 mt-1">
            Calculating your spending and generating AI insights.
          </p>
        </div>
      ) : !analytics ? (
        <div className="p-8 text-center">
          <BarChart3 className="w-8 h-8 mx-auto text-slate-300 mb-2" />

          <p className="text-sm font-semibold text-slate-500">
            No analytics available
          </p>

          <p className="text-xs text-slate-400 mt-1">
            Select a date range and click Analyze.
          </p>
        </div>
      ) : (
        <div className="p-5 space-y-5">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="rounded-2xl border border-burgundy-100 bg-burgundy-50/40 p-5">
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-[10px] uppercase tracking-wider font-bold text-slate-500">
                    Total Spending
                  </p>

                  <p className="mt-2 text-2xl font-extrabold text-slate-900">
                    ${formatAmount(analytics.totalSpending)}
                  </p>

                  <p className="text-xs text-slate-500 mt-1">
                    {formatDate(analytics.from)} - {formatDate(analytics.to)}
                  </p>
                </div>

                <div className="p-2.5 rounded-xl bg-burgundy-700 text-white">
                  <DollarSign className="w-5 h-5" />
                </div>
              </div>
            </div>

            <div className="rounded-2xl border border-slate-200 bg-slate-50/70 p-5">
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-[10px] uppercase tracking-wider font-bold text-slate-500">
                    Spending Categories
                  </p>

                  <p className="mt-2 text-2xl font-extrabold text-slate-900">
                    {categories.length}
                  </p>

                  <p className="text-xs text-slate-500 mt-1">
                    Categories with spending
                  </p>
                </div>

                <div className="p-2.5 rounded-xl bg-slate-800 text-white">
                  <PieChart className="w-5 h-5" />
                </div>
              </div>
            </div>
          </div>

          <div className="rounded-2xl border border-slate-200 overflow-hidden">
            <div className="px-4 py-3 bg-burgundy-50/70 border-b border-burgundy-100">
              <div className="flex items-center gap-2">
                <TrendingDown className="w-4 h-4 text-burgundy-700" />

                <h3 className="text-sm font-extrabold text-burgundy-900">
                  Spending by Category
                </h3>
              </div>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-50 text-slate-500 uppercase tracking-wider font-bold">
                  <tr>
                    <th className="py-3 px-4">Category</th>
                    <th className="py-3 px-4 text-right">Spending</th>
                    <th className="py-3 px-4 text-right">Percentage</th>
                  </tr>
                </thead>

                <tbody className="divide-y divide-slate-100">
                  {categories.length === 0 ? (
                    <tr>
                      <td
                        colSpan="3"
                        className="py-6 px-4 text-center text-slate-400"
                      >
                        No spending recorded for this period.
                      </td>
                    </tr>
                  ) : (
                    categories.map(([category, amount]) => {
                      const percentage = Number(
                        analytics.percentageByCategory?.[category] ?? 0,
                      );

                      return (
                        <tr
                          key={category}
                          className="hover:bg-sand-50/50 transition-colors"
                        >
                          <td className="py-3 px-4">
                            <span className="font-bold text-slate-800">
                              {category.replaceAll("_", " ")}
                            </span>
                          </td>

                          <td className="py-3 px-4 text-right">
                            <span className="font-mono font-bold text-slate-800">
                              ${formatAmount(amount)}
                            </span>
                          </td>

                          <td className="py-3 px-4 text-right">
                            <span className="inline-flex px-2 py-1 rounded-lg bg-burgundy-50 text-burgundy-800 border border-burgundy-100 font-mono font-bold">
                              {percentage.toFixed(1)}%
                            </span>
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
            </div>
          </div>

          {analytics.aiInsight && (
            <div className="rounded-2xl border border-burgundy-200 bg-gradient-to-br from-burgundy-50 to-sand-50 p-5">
              <div className="flex gap-3">
                <div className="p-2.5 h-fit rounded-xl bg-burgundy-700 text-white">
                  <Sparkles className="w-5 h-5" />
                </div>

                <div>
                  <h3 className="text-sm font-extrabold text-burgundy-900">
                    AI Spending Insight
                  </h3>

                  <p className="text-xs text-slate-600 mt-2 leading-5">
                    {analytics.aiInsight}
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>
      )}
    </section>
  );
};
SpendingAnalytics.propTypes = {
  analytics: PropTypes.shape({
    from: PropTypes.string,
    to: PropTypes.string,
    totalSpending: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
    byCategory: PropTypes.object,
    percentageByCategory: PropTypes.object,
    aiInsight: PropTypes.string,
  }),
  loading: PropTypes.bool.isRequired,
  error: PropTypes.string,
  from: PropTypes.string.isRequired,
  to: PropTypes.string.isRequired,
  onFromChange: PropTypes.func.isRequired,
  onToChange: PropTypes.func.isRequired,
  onAnalyze: PropTypes.func.isRequired,
};
