import { useSelector } from "react-redux";
import { CheckCircle2, Key, Mail, MapPin, User } from "lucide-react";

export const ProfilePage = () => {
  const { user } = useSelector((state) => state.user);

  return (
    <div className="max-w-3xl space-y-5">
      <div className="bg-white p-6 rounded-3xl border border-burgundy-100/80 shadow-xs flex items-center gap-4">
        <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-burgundy-700 to-burgundy-900 text-white flex items-center justify-center font-extrabold text-xl shadow-burgundy-glow">
          {user?.name?.charAt(0)}
        </div>
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-extrabold text-slate-900">
              {user?.name}
            </h1>
            <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-burgundy-50 text-burgundy-800 border border-burgundy-200">
              {user?.role}
            </span>
          </div>
          <p className="text-xs text-slate-400 font-mono mt-0.5">
            ID: {user?.id}
          </p>
        </div>
      </div>
      <div className="bg-white rounded-3xl p-6 border border-burgundy-100/80 shadow-xs space-y-5">
        <div className="flex items-center justify-between border-b border-slate-100 pb-3">
          <h2 className="text-sm font-extrabold text-slate-900">
            Account Attributes
          </h2>
          <span className="text-xs font-bold text-emerald-600 flex items-center gap-1">
            <CheckCircle2 className="w-4 h-4" /> Verified
          </span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
          <div className="p-3.5 bg-sand-50/70 rounded-2xl border border-slate-200 space-y-0.5">
            <div className="flex items-center gap-1.5 text-slate-400 font-bold uppercase tracking-wider text-[9px]">
              <User className="w-3.5 h-3.5 text-burgundy-700" />
              <span>ID</span>
            </div>
            <p className="font-mono font-bold text-slate-900 text-xs">
              {user?.id}
            </p>
          </div>

          <div className="p-3.5 bg-sand-50/70 rounded-2xl border border-slate-200 space-y-0.5">
            <div className="flex items-center gap-1.5 text-slate-400 font-bold uppercase tracking-wider text-[9px]">
              <Key className="w-3.5 h-3.5 text-burgundy-700" />
              <span>PASSWORD</span>
            </div>
            <p className="font-mono font-bold text-slate-900 text-xs">
              •••••••• ({user?.password})
            </p>
          </div>

          <div className="p-3.5 bg-sand-50/70 rounded-2xl border border-slate-200 space-y-0.5">
            <div className="flex items-center gap-1.5 text-slate-400 font-bold uppercase tracking-wider text-[9px]">
              <User className="w-3.5 h-3.5 text-burgundy-700" />
              <span>NAME</span>
            </div>
            <p className="font-bold text-slate-900 text-xs">{user?.name}</p>
          </div>

          <div className="p-3.5 bg-sand-50/70 rounded-2xl border border-slate-200 space-y-0.5">
            <div className="flex items-center gap-1.5 text-slate-400 font-bold uppercase tracking-wider text-[9px]">
              <Mail className="w-3.5 h-3.5 text-burgundy-700" />
              <span>EMAIL</span>
            </div>
            <p className="font-bold text-slate-900 text-xs">{user?.email}</p>
          </div>

          <div className="sm:col-span-2 p-3.5 bg-sand-50/70 rounded-2xl border border-slate-200 space-y-0.5">
            <div className="flex items-center gap-1.5 text-slate-400 font-bold uppercase tracking-wider text-[9px]">
              <MapPin className="w-3.5 h-3.5 text-burgundy-700" />
              <span>ADDRESS</span>
            </div>
            <p className="font-bold text-slate-900 text-xs">
              {user?.address || "No address registered"}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
