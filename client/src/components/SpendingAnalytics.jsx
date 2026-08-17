import { useState } from "react";
import {
  BarChart3,
  CalendarDays,
  DollarSign,
  PieChart,
  Sparkles,
  TrendingDown,
} from "lucide-react";
import PropTypes from "prop-types";

import {
  Cell,
  Pie,
  PieChart as RechartsPieChart,
  ResponsiveContainer,
  Tooltip,
} from "recharts";

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
  const [showPieChart, setShowPieChart] = useState(false);
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
  const pieData = categories.map(([category, amount]) => ({
    name: category.replaceAll("_", " "),
    value: Number(amount ?? 0),
  }));
  const today = new Date().toISOString().split("T")[0];
  const handleAnalyze = () => {
    if (!from || !to || loading) return;

    if (from > today || to > today) {
      return;
    }

    if (from > to) {
      return;
    }

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
                  max={today}
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
                  max={today}
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

                <button
                  type="button"
                  onClick={() => setShowPieChart((prev) => !prev)}
                  className={`p-2.5 rounded-xl text-white transition-all duration-300
                  ${
                    showPieChart
                      ? "bg-gradient-to-br from-burgundy-700 to-burgundy-950 shadow-burgundy-glow scale-105"
                      : "bg-gradient-to-br from-slate-700 to-slate-900 hover:from-burgundy-600 hover:to-burgundy-800 hover:scale-105"
                  }`}
                  title={showPieChart ? "Hide chart" : "Show chart"}
                >
                  <PieChart className="w-5 h-5" />
                </button>
              </div>
            </div>
          </div>

          {showPieChart && (
            <div className="rounded-3xl border border-burgundy-100 bg-gradient-to-br from-white via-burgundy-50/30 to-sand-50/50 p-6 shadow-sm">
              {/* Header */}
              <div className="flex items-center justify-between mb-5">
                <div className="flex items-center gap-3">
                  <div className="p-2.5 rounded-xl bg-gradient-to-br from-burgundy-600 to-burgundy-900 text-white shadow-burgundy-glow">
                    <PieChart className="w-5 h-5" />
                  </div>

                  <div>
                    <h3 className="text-sm font-extrabold text-slate-900">
                      Spending Distribution
                    </h3>

                    <p className="text-xs text-slate-500 mt-0.5">
                      Visual breakdown of your spending
                    </p>
                  </div>
                </div>

                <button
                  type="button"
                  onClick={() => setShowPieChart(false)}
                  className="px-3 py-1.5 rounded-lg text-xs font-semibold
                   text-slate-500 bg-white border border-slate-200
                   hover:bg-slate-50 hover:text-slate-800 transition-all"
                >
                  Hide
                </button>
              </div>

              {pieData.length === 0 ? (
                <div className="py-12 text-center">
                  <PieChart className="w-10 h-10 mx-auto text-slate-300 mb-3" />

                  <p className="text-sm font-semibold text-slate-500">
                    No spending data available
                  </p>

                  <p className="text-xs text-slate-400 mt-1">
                    Try selecting a different date range.
                  </p>
                </div>
              ) : (
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-center">
                  {/* Chart */}
                  <div className="relative h-[330px]">
                    <ResponsiveContainer width="100%" height="100%">
                      <RechartsPieChart>
                        <defs>
                          <linearGradient
                            id="pieGradient1"
                            x1="0"
                            y1="0"
                            x2="1"
                            y2="1"
                          >
                            <stop offset="0%" stopColor="#7a3158" />
                            <stop offset="100%" stopColor="#48182f" />
                          </linearGradient>

                          <linearGradient
                            id="pieGradient2"
                            x1="0"
                            y1="0"
                            x2="1"
                            y2="1"
                          >
                            <stop offset="0%" stopColor="#e7a7c4" />
                            <stop offset="100%" stopColor="#b95f8c" />
                          </linearGradient>

                          <linearGradient
                            id="pieGradient3"
                            x1="0"
                            y1="0"
                            x2="1"
                            y2="1"
                          >
                            <stop offset="0%" stopColor="#d6c2cc" />
                            <stop offset="100%" stopColor="#927080" />
                          </linearGradient>

                          <linearGradient
                            id="pieGradient4"
                            x1="0"
                            y1="0"
                            x2="1"
                            y2="1"
                          >
                            <stop offset="0%" stopColor="#f3d6a2" />
                            <stop offset="100%" stopColor="#d49a45" />
                          </linearGradient>
                        </defs>

                        <Pie
                          data={pieData}
                          cx="50%"
                          cy="50%"
                          innerRadius={78}
                          outerRadius={125}
                          paddingAngle={4}
                          cornerRadius={6}
                          dataKey="value"
                          nameKey="name"
                          stroke="#ffffff"
                          strokeWidth={3}
                        >
                          {pieData.map((entry, index) => (
                            <Cell
                              key={`cell-${entry.name}-${index}`}
                              fill={`url(#pieGradient${(index % 4) + 1})`}
                            />
                          ))}
                        </Pie>

                        <Tooltip
                          contentStyle={{
                            borderRadius: "14px",
                            border: "1px solid #eadde4",
                            boxShadow: "0 10px 30px rgba(72, 24, 47, 0.12)",
                            backgroundColor: "#ffffff",
                            padding: "10px 14px",
                          }}
                          formatter={(value, name) => [
                            `$${formatAmount(value)}`,
                            name,
                          ]}
                        />
                      </RechartsPieChart>
                    </ResponsiveContainer>

                    {/* Center of donut */}
                    <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                      <div className="text-center">
                        <p className="text-[10px] uppercase tracking-widest font-bold text-slate-400">
                          Total
                        </p>

                        <p className="text-xl font-extrabold text-burgundy-900">
                          ${formatAmount(analytics.totalSpending)}
                        </p>
                      </div>
                    </div>
                  </div>

                  {/* Category breakdown */}
                  <div className="space-y-3">
                    <div className="mb-4">
                      <p className="text-xs font-bold uppercase tracking-wider text-slate-400">
                        Categories
                      </p>
                    </div>

                    {pieData.map((item, index) => {
                      const percentage = Number(
                        analytics.percentageByCategory?.[
                          categories[index]?.[0]
                        ] ?? 0,
                      );

                      return (
                        <div
                          key={item.name}
                          className="group p-3 rounded-xl bg-white/80 border border-slate-100
                           hover:border-burgundy-200 hover:shadow-sm
                           transition-all"
                        >
                          <div className="flex items-center justify-between mb-2">
                            <div className="flex items-center gap-2">
                              <span
                                className="w-3 h-3 rounded-full shadow-sm"
                                style={{
                                  background:
                                    index % 4 === 0
                                      ? "linear-gradient(135deg, #7a3158, #48182f)"
                                      : index % 4 === 1
                                        ? "linear-gradient(135deg, #e7a7c4, #b95f8c)"
                                        : index % 4 === 2
                                          ? "linear-gradient(135deg, #d6c2cc, #927080)"
                                          : "linear-gradient(135deg, #f3d6a2, #d49a45)",
                                }}
                              />

                              <span className="text-xs font-bold text-slate-700">
                                {item.name}
                              </span>
                            </div>

                            <span className="text-xs font-bold text-burgundy-800">
                              {percentage.toFixed(1)}%
                            </span>
                          </div>

                          {/* Progress bar */}
                          <div className="h-1.5 bg-slate-100 rounded-full overflow-hidden">
                            <div
                              className="h-full rounded-full transition-all duration-700"
                              style={{
                                width: `${Math.min(percentage, 100)}%`,
                                background:
                                  index % 4 === 0
                                    ? "linear-gradient(90deg, #7a3158, #48182f)"
                                    : index % 4 === 1
                                      ? "linear-gradient(90deg, #e7a7c4, #b95f8c)"
                                      : index % 4 === 2
                                        ? "linear-gradient(90deg, #d6c2cc, #927080)"
                                        : "linear-gradient(90deg, #f3d6a2, #d49a45)",
                              }}
                            />
                          </div>

                          <div className="flex justify-between mt-1.5">
                            <span className="text-[10px] text-slate-400">
                              Spending
                            </span>

                            <span className="text-[10px] font-mono font-bold text-slate-600">
                              ${formatAmount(item.value)}
                            </span>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}
            </div>
          )}

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
