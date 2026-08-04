import { Link } from 'react-router-dom';
import { Building2, ShieldCheck, Lock, Users, ArrowRight, ChevronRight, CreditCard, Sparkles } from 'lucide-react';

export const LandingPage = () => {
    return (
        <div className="min-h-[calc(100vh-4rem)] flex flex-col bg-[#FAF9FA]">
            <section className="relative overflow-hidden pt-12 pb-16 lg:pt-16 lg:pb-24">
                <div className="absolute top-0 right-1/4 w-96 h-96 bg-burgundy-100/40 rounded-full blur-3xl -z-10"></div>
                <div className="absolute bottom-0 left-1/3 w-80 h-80 bg-sand-200/50 rounded-full blur-3xl -z-10"></div>
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
                        <div className="lg:col-span-7 space-y-6 text-left">

                            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-burgundy-50 border border-burgundy-200/80 text-burgundy-800 text-xs font-bold tracking-wide uppercase shadow-xs">
                                <ShieldCheck className="w-4 h-4 text-burgundy-700" />
                                Enterprise Banking Portal
                            </div>

                            <h1 className="text-4xl sm:text-5xl font-extrabold text-slate-900 tracking-tight leading-[1.15]">
                                Modern Banking Built for <span className="bg-gradient-to-r from-burgundy-700 via-burgundy-800 to-burgundy-950 bg-clip-text text-transparent">Security & Simplicity</span>
                            </h1>

                            <p className="text-base text-slate-600 max-w-xl leading-relaxed">
                                Manage accounts, track transactions, and monitor balances with a secure and intuitive banking platform.
                            </p>
                            <div className="pt-2 flex flex-wrap items-center gap-3">
                                <Link
                                    to="/login"
                                    className="px-6 py-3 bg-gradient-to-r from-burgundy-700 via-burgundy-800 to-burgundy-900 hover:from-burgundy-800 hover:to-burgundy-950 text-white font-bold rounded-xl shadow-burgundy-lg hover:shadow-burgundy-glow transition-all duration-200 flex items-center gap-2 text-xs"
                                >
                                    Access Banking Portal
                                    <ArrowRight className="w-4 h-4" />
                                </Link>
                            </div>

                        </div>
                        <div className="lg:col-span-5 relative">
                            <div className="bg-white p-6 rounded-3xl shadow-xl border border-burgundy-100/80 relative z-10">
                                <div className="bg-gradient-to-br from-burgundy-900 via-burgundy-800 to-burgundy-950 text-white p-6 rounded-2xl shadow-burgundy-lg relative overflow-hidden aspect-[1.586/1] flex flex-col justify-between">
                                    <div className="absolute -right-6 -bottom-6 opacity-10">
                                        <Building2 className="w-44 h-44 text-white" />
                                    </div>
                                    <div className="flex justify-between items-start relative z-10">
                                        <div>
                                            <p className="text-[9px] uppercase font-bold tracking-widest text-burgundy-200">AURA PREMIER</p>
                                            <p className="text-xs font-bold text-white tracking-wide mt-0.5">Digital Account</p>
                                        </div>
                                        <CreditCard className="w-6 h-6 text-burgundy-200" />
                                    </div>
                                    <div className="w-10 h-7 rounded-md bg-gradient-to-r from-amber-300 via-yellow-400 to-amber-500 border border-amber-200/50 shadow-xs relative z-10 my-auto"></div>
                                    <div className="relative z-10 space-y-2">
                                        <p className="font-mono text-sm tracking-widest text-burgundy-100 font-semibold">
                                            •••• •••• •••• 8842
                                        </p>
                                        <div className="flex justify-between items-end text-[10px] text-burgundy-200 font-mono">
                                            <span>MEMBER: ELECTED HOLDER</span>
                                            <span className="font-bold text-white bg-white/10 px-2 py-0.5 rounded">STATUS: CR</span>
                                        </div>
                                    </div>

                                </div>
                            </div>
                        </div>

                    </div>
                </div>
            </section>
            <section className="py-12 bg-white border-t border-burgundy-100/80">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">

                    <div className="text-center max-w-xl mx-auto mb-8">
                        <h2 className="text-xl font-extrabold text-burgundy-900 tracking-tight">
                            Banking Made Simple & Secure
                        </h2>
                        <p className="text-xs text-slate-500 mt-1">
                            Powerful features designed to provide a seamless digital banking experience.
                        </p>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-5">

                        <div className="p-5 bg-sand-50/70 rounded-2xl border border-burgundy-200/80 shadow-xs space-y-3">
                            <div className="p-2 bg-burgundy-700 text-white rounded-lg w-fit">
                                <ShieldCheck className="w-5 h-5" />
                            </div>

                            <h3 className="font-bold text-slate-900 text-sm">
                                Secure Banking
                            </h3>

                            <p className="text-xs text-slate-500 leading-relaxed">
                                Protected authentication and role-based access control ensure your financial data stays secure.
                            </p>
                        </div>


                        <div className="p-5 bg-sand-50/70 rounded-2xl border border-burgundy-200/80 shadow-xs space-y-3">
                            <div className="p-2 bg-burgundy-700 text-white rounded-lg w-fit">
                                <CreditCard className="w-5 h-5" />
                            </div>

                            <h3 className="font-bold text-slate-900 text-sm">
                                Smart Transactions
                            </h3>

                            <p className="text-xs text-slate-500 leading-relaxed">
                                Create, track, and manage transactions with real-time balance updates and DB/CR indicators.
                            </p>
                        </div>


                        <div className="p-5 bg-sand-50/70 rounded-2xl border border-burgundy-200/80 shadow-xs space-y-3">
                            <div className="p-2 bg-burgundy-700 text-white rounded-lg w-fit">
                                <Sparkles className="w-5 h-5" />
                            </div>

                            <h3 className="font-bold text-slate-900 text-sm">
                                Modern Experience
                            </h3>

                            <p className="text-xs text-slate-500 leading-relaxed">
                                Enjoy a responsive banking interface built with modern technologies and intuitive workflows.
                            </p>
                        </div>

                    </div>
                </div>
            </section>
            <footer className="mt-auto py-6 bg-burgundy-950 text-burgundy-300 text-[11px] text-center border-t border-burgundy-900">
                AURA BANKING SYSTEM
            </footer>

        </div>
    );
};