import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import {
  clearAccountsError,
  createNewAccount,
  deleteExistingAccount,
  fetchAccounts,
  updateExistingAccount,
} from "../../redux/slices/AccountsSlice.js";
import { AccountModal } from "../../components/AccountModal";
import {
  AlertCircle,
  CheckCircle2,
  DollarSign,
  Edit3,
  Eye,
  Plus,
  RefreshCw,
  Search,
  Shield,
  Trash2,
  Users,
} from "lucide-react";
import { useNavigate } from "react-router-dom";

export const AdminDashboard = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const {
    items: accounts,
    status,
    error,
    page,
    totalPages,
    totalElements,
  } = useSelector((state) => state.accounts);

  const loading = status === "loading";

  const [searchTerm, setSearchTerm] = useState("");
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [accountToEdit, setAccountToEdit] = useState(null);
  const [deleteConfirmId, setDeleteConfirmId] = useState(null);
  const [toastMessage, setToastMessage] = useState(null);
  const [pageSize] = useState(10);
  const [, setError] = useState(null);
  useEffect(() => {
    dispatch(
      fetchAccounts({
        page: 0,
        size: pageSize,
      }),
    );
  }, [dispatch, pageSize]);

  useEffect(() => {
    if (!error) return;

    const timer = setTimeout(() => {
      dispatch(clearAccountsError());
    }, 5000);

    return () => clearTimeout(timer);
  }, [error, dispatch]);

  const showToast = (msg) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3000);
  };

  const handleOpenCreateModal = () => {
    setAccountToEdit(null);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (account) => {
    setAccountToEdit(account);
    setIsModalOpen(true);
  };

  const handleSaveAccount = async (formData) => {
    try {
      if (accountToEdit) {
        await dispatch(
          updateExistingAccount({
            id: accountToEdit.id,
            data: formData,
          }),
        ).unwrap();

        await dispatch(
          fetchAccounts({
            page,
            size: pageSize,
          }),
        ).unwrap();

        showToast(`Account '${accountToEdit.id}' updated successfully.`);
      } else {
        await dispatch(createNewAccount(formData)).unwrap();

        await dispatch(
          fetchAccounts({
            page,
            size: pageSize,
          }),
        ).unwrap();

        showToast("New account created successfully.");
      }

      setAccountToEdit(null);
      setIsModalOpen(false);
    } catch (err) {
      console.error("Save account failed:", err);
      // showToast(err || "Failed to save account");
    }
  };

  const handleDeleteAccount = async (id) => {
    try {
      await dispatch(deleteExistingAccount(id)).unwrap();
      await dispatch(
        fetchAccounts({
          page: page,
          size: pageSize,
        }),
      ).unwrap();
      showToast(`Account ${id} deleted successfully.`);
      setDeleteConfirmId(null);
    } catch (err) {
      setError(err?.message || err || "Delete account failed");
      console.error("Delete failed:", err);
    }
  };

  const filteredAccounts = accounts.filter(
    (acc) =>
      String(acc.id).toLowerCase().includes(searchTerm.toLowerCase()) ||
      acc.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      acc.email.toLowerCase().includes(searchTerm.toLowerCase()),
  );

  const totalSystemBalance = accounts.reduce(
    (acc, curr) => acc + (curr.balance || 0),
    0,
  );

  const handleNextPage = () => {
    if (page + 1 < totalPages) {
      dispatch(
        fetchAccounts({
          page: page + 1,
          size: pageSize,
        }),
      );
    }
  };

  const handlePreviousPage = () => {
    if (page > 0) {
      dispatch(
        fetchAccounts({
          page: page - 1,
          size: pageSize,
        }),
      );
    }
  };

  return (
    <div className="space-y-6">
      {toastMessage && (
        <div className="fixed bottom-6 right-6 z-50 bg-burgundy-900 text-white px-4 py-3 rounded-2xl shadow-burgundy-lg flex items-center gap-2 text-xs font-bold animate-bounce">
          <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          {toastMessage}
        </div>
      )}

      {error && (
        <div className="p-3.5 bg-rose-50 border border-rose-200 text-rose-800 text-xs font-semibold rounded-xl flex items-center gap-2">
          <AlertCircle className="w-4 h-4 text-rose-600" />
          <span>{error}</span>
        </div>
      )}
      <div className="bg-gradient-to-r from-burgundy-900 via-burgundy-800 to-burgundy-950 rounded-3xl p-6 text-white shadow-burgundy-lg flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-white/10 text-[10px] font-bold text-burgundy-200 uppercase tracking-wider mb-1">
            <Shield className="w-3.5 h-3.5 text-burgundy-300" />
            ADMIN PORTAL
          </div>
          <h1 className="text-xl sm:text-2xl font-extrabold tracking-tight">
            Accounts Directory
          </h1>
        </div>

        <button
          onClick={handleOpenCreateModal}
          className="px-4 py-2.5 bg-white text-burgundy-900 font-extrabold text-xs rounded-xl shadow-md hover:bg-burgundy-50 transition-all flex items-center gap-1.5 shrink-0"
        >
          <Plus className="w-4 h-4 text-burgundy-700" />
          New Account
        </button>
      </div>
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white p-4 rounded-2xl border border-burgundy-100/80 shadow-xs flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-burgundy-50 border border-burgundy-200 flex items-center justify-center text-burgundy-700">
            <Users className="w-5 h-5" />
          </div>
          <div>
            <p className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">
              Total Accounts
            </p>
            <p className="text-xl font-extrabold text-slate-900 font-mono">
              {accounts.length}
            </p>
          </div>
        </div>

        <div className="bg-white p-4 rounded-2xl border border-burgundy-100/80 shadow-xs flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-emerald-50 border border-emerald-200 flex items-center justify-center text-emerald-700">
            <DollarSign className="w-5 h-5" />
          </div>
          <div>
            <p className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">
              Total Deposits
            </p>
            <p className="text-xl font-extrabold text-slate-900 font-mono">
              $
              {totalSystemBalance.toLocaleString("en-US", {
                minimumFractionDigits: 2,
              })}
            </p>
          </div>
        </div>
      </div>
      <div className="bg-white rounded-3xl border border-burgundy-100/80 shadow-xs overflow-hidden">
        <div className="p-4 border-b border-slate-100 flex flex-col sm:flex-row justify-between items-stretch sm:items-center gap-3 bg-sand-50/50">
          <div className="relative flex-1 max-w-md">
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Search by ID, Name, or Email..."
              className="w-full pl-9 pr-3 py-2 text-xs border border-slate-300 rounded-xl focus:ring-2 focus:ring-burgundy-500 focus:border-burgundy-500 bg-white"
            />
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
          </div>

          <button
            onClick={() =>
              dispatch(
                fetchAccounts({
                  page: 0,
                  size: pageSize,
                }),
              )
            }
            className="px-3 py-2 text-xs font-bold text-slate-600 hover:bg-slate-100 rounded-xl border border-slate-200 transition-colors flex items-center gap-1.5 justify-center"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            Refresh
          </button>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-slate-600">
            <thead className="bg-burgundy-50/70 text-burgundy-900 font-bold uppercase tracking-wider border-b border-burgundy-100">
              <tr>
                <th className="py-3 px-4">Account ID</th>
                <th className="py-3 px-4">Name</th>
                <th className="py-3 px-4">Email</th>
                <th className="py-3 px-4">Address</th>
                <th className="py-3 px-4">Role</th>
                <th className="py-3 px-4">Balance ($)</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {loading ? (
                <tr>
                  <td colSpan="7" className="py-6 text-center text-slate-400">
                    Loading accounts...
                  </td>
                </tr>
              ) : filteredAccounts.length === 0 ? (
                <tr>
                  <td colSpan="7" className="py-6 text-center text-slate-400">
                    No matching accounts found.
                  </td>
                </tr>
              ) : (
                filteredAccounts.map((acc) => (
                  <tr
                    key={acc.id}
                    className="hover:bg-sand-50/70 transition-colors"
                  >
                    <td className="py-3 px-4 font-mono font-bold text-burgundy-900">
                      {acc.id}
                    </td>
                    <td className="py-3 px-4 font-semibold text-slate-800">
                      {acc.name}
                    </td>
                    <td className="py-3 px-4 text-slate-500">{acc.email}</td>
                    <td className="py-3 px-4 text-slate-500 truncate max-w-xs">
                      {acc.address || "—"}
                    </td>
                    <td className="py-3 px-4">
                      <span
                        className={`px-2.5 py-0.5 rounded-full text-[10px] font-bold ${
                          acc.role === "ADMIN"
                            ? "bg-burgundy-700 text-white"
                            : "bg-slate-100 text-slate-700 border border-slate-200"
                        }`}
                      >
                        {acc.role}
                      </span>
                    </td>
                    <td className="py-3 px-4 font-mono font-bold text-slate-900">
                      $
                      {(acc.balance || 0).toLocaleString("en-US", {
                        minimumFractionDigits: 2,
                      })}
                      <span className="ml-1 text-[10px] text-emerald-600 font-sans font-bold">
                        ({acc.indicator || "CR"})
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right space-x-1">
                      <button
                        onClick={() => navigate(`/admin/account/${acc.id}`)}
                        className="p-1 text-slate-600 hover:bg-slate-100 rounded-lg transition-colors inline-flex"
                        title="View Details"
                      >
                        <Eye className="w-4 h-4 text-burgundy-700" />
                      </button>

                      <button
                        onClick={() => handleOpenEditModal(acc)}
                        className="p-1 text-slate-600 hover:bg-slate-100 rounded-lg transition-colors inline-flex"
                        title="Edit Account"
                      >
                        <Edit3 className="w-4 h-4 text-slate-700" />
                      </button>

                      <button
                        onClick={() => setDeleteConfirmId(acc.id)}
                        disabled={acc.role === "ADMIN"}
                        className="p-1 text-slate-600 hover:bg-rose-50 rounded-lg transition-colors inline-flex disabled:opacity-30"
                        title={
                          acc.role === "ADMIN"
                            ? "Cannot delete admin"
                            : "Delete Account"
                        }
                      >
                        <Trash2 className="w-4 h-4 text-rose-600" />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
        <div className="flex items-center justify-between px-4 py-3 border-t border-slate-100">
          <div className="text-xs text-slate-500 font-semibold">
            Showing page {page + 1} of {totalPages}
            <span className="ml-2">({totalElements} accounts)</span>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handlePreviousPage}
              disabled={page === 0}
              className="px-3 py-1.5 text-xs font-bold rounded-lg border border-slate-200 disabled:opacity-40 hover:bg-slate-100"
            >
              ← Previous
            </button>

            <button
              onClick={handleNextPage}
              disabled={page + 1 >= totalPages}
              className="px-3 py-1.5 text-xs font-bold rounded-lg border border-slate-200 disabled:opacity-40 hover:bg-slate-100"
            >
              Next →
            </button>
          </div>
        </div>
      </div>
      <AccountModal
        key={accountToEdit?.id ?? "new"}
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSave={handleSaveAccount}
        accountToEdit={accountToEdit}
      />
      {deleteConfirmId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <div className="bg-white rounded-2xl max-w-sm w-full p-6 shadow-2xl space-y-4 border border-rose-100">
            <div className="flex items-center gap-3 text-rose-700">
              <div className="p-2 bg-rose-100 rounded-xl">
                <AlertCircle className="w-6 h-6" />
              </div>
              <h3 className="font-extrabold text-base text-slate-900">
                Delete Account?
              </h3>
            </div>
            <p className="text-xs text-slate-600 leading-relaxed">
              Are you sure you want to delete account{" "}
              <span className="font-mono font-bold text-slate-900">
                &apos;{deleteConfirmId}&apos;
              </span>
              ?
            </p>
            <div className="flex items-center justify-end gap-2 pt-2">
              <button
                onClick={() => setDeleteConfirmId(null)}
                className="px-4 py-2 text-xs font-bold text-slate-600 hover:bg-slate-100 rounded-xl"
              >
                Cancel
              </button>
              <button
                onClick={() => handleDeleteAccount(deleteConfirmId)}
                className="px-4 py-2 text-xs font-bold text-white bg-rose-600 hover:bg-rose-700 rounded-xl shadow-sm"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
