import { useState } from "react";
import { CheckCircle, DollarSign, FileText, Mail, Send, X } from "lucide-react";
import PropTypes from "prop-types";

export const TransferModal = ({ isOpen, onClose, onExecuteTransfer }) => {
  const [email, setEmail] = useState("");
  const [amount, setAmount] = useState("");
  const [description, setDescription] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    const numAmount = parseFloat(amount);
    if (!email.trim()) {
      setError("Recipient email is required.");
      return;
    }
    if (isNaN(numAmount) || numAmount <= 0) {
      setError("Please enter a valid positive transfer amount.");
      return;
    }
    setLoading(true);
    try {
      const transferData = {
        email: email.trim(),
        amount: numAmount,
        description: description.trim() || "Money transfer",
      };
      await onExecuteTransfer(transferData);
      setEmail("");
      setAmount("");
      setDescription("");
      setError("");
      onClose();
    } catch (err) {
      setError(err?.message || "Transfer failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
      {" "}
      <div className="bg-white rounded-2xl max-w-md w-full shadow-2xl border border-burgundy-100 overflow-hidden">
        {" "}
        <div className="bg-gradient-to-r from-burgundy-800 to-burgundy-950 px-6 py-4 flex items-center justify-between text-white">
          {" "}
          <div className="flex items-center gap-2">
            {" "}
            <Send className="w-5 h-5 text-burgundy-200" />{" "}
            <h3 className="font-bold text-base"> Transfer Money </h3>{" "}
          </div>{" "}
          <button
            type="button"
            onClick={onClose}
            disabled={loading}
            className="text-burgundy-200 hover:text-white disabled:opacity-50"
          >
            {" "}
            <X className="w-5 h-5" />{" "}
          </button>{" "}
        </div>{" "}
        {/* Form */}{" "}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {" "}
          {error && (
            <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs font-semibold rounded-xl">
              {" "}
              {error}{" "}
            </div>
          )}{" "}
          <div>
            {" "}
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              {" "}
              Recipient Email *{" "}
            </label>{" "}
            <div className="relative">
              {" "}
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="recipient@example.com"
                className="w-full pl-9 pr-3 py-2.5 text-sm border border-slate-300 rounded-xl focus:ring-2 focus:ring-burgundy-500 focus:border-burgundy-500"
                required
              />{" "}
              <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-3" />{" "}
            </div>{" "}
          </div>{" "}
          <div>
            {" "}
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              {" "}
              Amount ($) *{" "}
            </label>{" "}
            <div className="relative">
              {" "}
              <input
                type="number"
                step="0.01"
                min="0.01"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                placeholder="0.00"
                className="w-full pl-9 pr-3 py-2.5 text-base font-mono font-bold text-slate-800 border border-slate-300 rounded-xl focus:ring-2 focus:ring-burgundy-500 focus:border-burgundy-500"
                required
              />{" "}
              <DollarSign className="w-4 h-4 text-slate-400 absolute left-3 top-3.5" />{" "}
            </div>{" "}
          </div>{" "}
          <div>
            {" "}
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
              {" "}
              Description{" "}
            </label>{" "}
            <div className="relative">
              {" "}
              <input
                type="text"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="e.g. Payment for order"
                className="w-full pl-9 pr-3 py-2.5 text-sm border border-slate-300 rounded-xl focus:ring-2 focus:ring-burgundy-500 focus:border-burgundy-500"
              />{" "}
              <FileText className="w-4 h-4 text-slate-400 absolute left-3 top-3" />{" "}
            </div>{" "}
          </div>{" "}
          <div className="pt-4 border-t border-slate-100 flex items-center justify-end gap-3">
            {" "}
            <button
              type="button"
              onClick={onClose}
              disabled={loading}
              className="px-4 py-2 text-xs font-bold text-slate-600 hover:bg-slate-100 rounded-xl disabled:opacity-50"
            >
              {" "}
              Cancel{" "}
            </button>{" "}
            <button
              type="submit"
              disabled={loading}
              className="px-5 py-2.5 text-xs font-bold text-white bg-gradient-to-r from-burgundy-700 to-burgundy-900 hover:from-burgundy-800 hover:to-burgundy-950 rounded-xl shadow-burgundy-glow transition-all flex items-center gap-1.5 disabled:opacity-50"
            >
              {" "}
              <CheckCircle className="w-4 h-4" />{" "}
              {loading ? "Transferring..." : "Transfer Money"}{" "}
            </button>{" "}
          </div>{" "}
        </form>{" "}
      </div>{" "}
    </div>
  );
};
TransferModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onExecuteTransfer: PropTypes.func.isRequired,
};
