import {useEffect, useRef, useState} from "react";
import {
    ArrowLeft,
    CheckCircle2,
    KeyRound,
    Lock,
    Eye,
    EyeOff,
    Mail,
    ShieldCheck,
} from "lucide-react";
import {useDispatch, useSelector} from "react-redux";
import {useNavigate, useSearchParams} from "react-router-dom";
import {
    resendOtpThunk,
    verifyOtpThunk,
    changePasswordThunk,
    resetActivation,
} from "../redux/slices/OtpVerificationSlice.js";

export const OtpVerificationPage = () => {
    const dispatch = useDispatch();
    const navigate = useNavigate();

    const [searchParams] = useSearchParams();

    const email = searchParams.get("email");

    const {
        verificationError,
        resendError,
        resetToken,
        passwordError,
    } = useSelector((state) => state.otpVerification);

    const [otp, setOtp] = useState(["", "", "", "", "", ""]);
    const [error, setError] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [resending, setResending] = useState(false);

    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);

    const inputRefs = useRef([]);

    useEffect(() => {
        if (!resetToken) {
            inputRefs.current[0]?.focus();
        }
    }, [resetToken]);

    if (!email) {
        return (
            <div className="min-h-screen flex items-center justify-center p-4 bg-slate-100">
                <div className="w-full max-w-md bg-white rounded-3xl shadow-2xl border border-red-100 p-8 text-center">
                    <div className="mx-auto w-14 h-14 rounded-2xl bg-red-50 flex items-center justify-center mb-5">
                        <Mail className="w-7 h-7 text-red-600"/>
                    </div>

                    <h1 className="text-xl font-bold text-slate-900">
                        Invalid Activation Link
                    </h1>

                    <p className="mt-2 text-sm text-slate-500">
                        This activation link is missing the required email
                        address.
                    </p>

                    <button
                        type="button"
                        onClick={() => navigate("/login")}
                        className="mt-6 w-full py-3 rounded-xl text-sm font-bold text-white bg-gradient-to-r from-burgundy-700 to-burgundy-900"
                    >
                        Go to Login
                    </button>
                </div>
            </div>
        );
    }


    const handleChange = (value, index) => {
        if (!/^\d*$/.test(value)) {
            return;
        }

        const newOtp = [...otp];

        newOtp[index] = value.slice(-1);

        setOtp(newOtp);
        setError("");

        if (value && index < 5) {
            inputRefs.current[index + 1]?.focus();
        }
    };

    const handleKeyDown = (e, index) => {
        if (
            e.key === "Backspace" &&
            !otp[index] &&
            index > 0
        ) {
            inputRefs.current[index - 1]?.focus();
        }
    };

    const handlePaste = (e) => {
        e.preventDefault();

        const pastedData = e.clipboardData
            .getData("text")
            .replace(/\D/g, "")
            .slice(0, 6);

        if (!pastedData) {
            return;
        }

        const newOtp = ["", "", "", "", "", ""];

        pastedData.split("").forEach((digit, index) => {
            newOtp[index] = digit;
        });

        setOtp(newOtp);
        setError("");

        const nextIndex = Math.min(
            pastedData.length,
            5,
        );

        inputRefs.current[nextIndex]?.focus();
    };

    const handleVerify = async (e) => {
        e.preventDefault();

        setError("");

        const otpCode = otp.join("");

        if (otpCode.length !== 6) {
            setError(
                "Please enter the complete 6-digit verification code.",
            );
            return;
        }

        setSubmitting(true);

        try {
            await dispatch(
                verifyOtpThunk({
                    email,
                    otp: otpCode,
                }),
            ).unwrap();
        } catch (err) {
            setError(
                err ||
                verificationError ||
                "Invalid or expired verification code.",
            );
        } finally {
            setSubmitting(false);
        }
    };

    const handleResend = async () => {
        setError("");
        setResending(true);

        try {
            await dispatch(
                resendOtpThunk({
                    email,
                }),
            ).unwrap();

            setOtp(["", "", "", "", "", ""]);

            inputRefs.current[0]?.focus();
        } catch (err) {
            setError(
                err ||
                resendError ||
                "Failed to resend verification code.",
            );
        } finally {
            setResending(false);
        }
    };

    const handlePasswordChange = async (e) => {
        e.preventDefault();

        setError("");

        if (!newPassword) {
            setError("Password is required.");
            return;
        }

        if (newPassword.length < 8) {
            setError(
                "Password must be at least 8 characters long.",
            );
            return;
        }

        if (newPassword !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }

        if (!resetToken) {
            setError(
                "Your password reset session has expired. Please verify your email again.",
            );
            return;
        }

        setSubmitting(true);

        try {
            await dispatch(
                changePasswordThunk({
                    resetToken,
                    newPassword,
                }),
            ).unwrap();

            dispatch(resetActivation());

            navigate("/login");
        } catch (err) {
            setError(
                err ||
                passwordError ||
                "Failed to change password.",
            );
        } finally {
            setSubmitting(false);
        }
    };

    if (resetToken) {
        return (
            <div className="min-h-screen flex items-center justify-center p-4 bg-slate-100">
                <div
                    className="w-full max-w-md bg-white rounded-3xl shadow-2xl border border-burgundy-100 overflow-hidden">

                    <div
                        className="bg-gradient-to-r from-burgundy-800 to-burgundy-950 px-8 py-7 text-center text-white">
                        <div
                            className="mx-auto w-14 h-14 rounded-2xl bg-burgundy-700/70 flex items-center justify-center mb-4">
                            <ShieldCheck className="w-7 h-7 text-burgundy-100"/>
                        </div>

                        <h1 className="text-xl font-bold">
                            Set Your Password
                        </h1>

                        <p className="mt-1 text-xs text-burgundy-200">
                            Secure your new account
                        </p>
                    </div>

                    <form
                        onSubmit={handlePasswordChange}
                        className="p-8"
                        autoComplete="off"
                    >
                        <div className="text-center mb-7">
                            <div className="flex justify-center mb-4">
                                <div className="w-11 h-11 rounded-xl bg-burgundy-50 flex items-center justify-center">
                                    <Lock className="w-5 h-5 text-burgundy-700"/>
                                </div>
                            </div>

                            <h2 className="text-lg font-bold text-slate-800">
                                Create Your Password
                            </h2>

                            <p className="mt-2 text-sm text-slate-500 leading-6">
                                Your email has been verified. Set a
                                password for your account.
                            </p>

                            <p className="text-sm font-semibold text-burgundy-800 break-all">
                                {email}
                            </p>
                        </div>

                        <div className="mb-4">
                            <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                                New Password
                            </label>

                            <div className="relative">
                                <input
                                    type={
                                        showPassword
                                            ? "text"
                                            : "password"
                                    }
                                    value={newPassword}
                                    onChange={(e) => {
                                        setNewPassword(
                                            e.target.value,
                                        );
                                        setError("");
                                    }}
                                    autoComplete="new-password"
                                    placeholder="Enter your new password"
                                    className="w-full pl-3 pr-10 py-3 text-sm border border-slate-200 rounded-xl outline-none focus:border-burgundy-700 focus:ring-4 focus:ring-burgundy-100"
                                />

                                <button
                                    type="button"
                                    onClick={() =>
                                        setShowPassword(
                                            (prev) => !prev,
                                        )
                                    }
                                    className="absolute right-3 top-3 text-slate-400 hover:text-slate-600"
                                    aria-label={
                                        showPassword
                                            ? "Hide password"
                                            : "Show password"
                                    }
                                >
                                    {showPassword ? (
                                        <EyeOff className="w-4 h-4"/>
                                    ) : (
                                        <Eye className="w-4 h-4"/>
                                    )}
                                </button>
                            </div>
                        </div>
                        <div className="mb-5">
                            <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                                Confirm Password
                            </label>

                            <div className="relative">
                                <input
                                    type={
                                        showConfirmPassword
                                            ? "text"
                                            : "password"
                                    }
                                    value={confirmPassword}
                                    onChange={(e) => {
                                        setConfirmPassword(
                                            e.target.value,
                                        );
                                        setError("");
                                    }}
                                    autoComplete="new-password"
                                    placeholder="Confirm your password"
                                    className="w-full pl-3 pr-10 py-3 text-sm border border-slate-200 rounded-xl outline-none focus:border-burgundy-700 focus:ring-4 focus:ring-burgundy-100"
                                />

                                <button
                                    type="button"
                                    onClick={() =>
                                        setShowConfirmPassword(
                                            (prev) => !prev,
                                        )
                                    }
                                    className="absolute right-3 top-3 text-slate-400 hover:text-slate-600"
                                    aria-label={
                                        showConfirmPassword
                                            ? "Hide password"
                                            : "Show password"
                                    }
                                >
                                    {showConfirmPassword ? (
                                        <EyeOff className="w-4 h-4"/>
                                    ) : (
                                        <Eye className="w-4 h-4"/>
                                    )}
                                </button>
                            </div>
                        </div>

                        {error && (
                            <div
                                className="mb-5 p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-xl text-center">
                                {error}
                            </div>
                        )}

                        <button
                            type="submit"
                            disabled={submitting}
                            className="w-full py-3 rounded-xl text-sm font-bold text-white bg-gradient-to-r from-burgundy-700 to-burgundy-900 hover:opacity-95 disabled:opacity-50 transition flex items-center justify-center gap-2"
                        >
                            <CheckCircle2 className="w-4 h-4"/>

                            {submitting
                                ? "Saving Password..."
                                : "Set Password"}
                        </button>
                    </form>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen flex items-center justify-center p-4 bg-slate-100">
            <div className="w-full max-w-md bg-white rounded-3xl shadow-2xl border border-burgundy-100 overflow-hidden">

                <div className="bg-gradient-to-r from-burgundy-800 to-burgundy-950 px-8 py-7 text-center text-white">
                    <div
                        className="mx-auto w-14 h-14 rounded-2xl bg-burgundy-700/70 flex items-center justify-center mb-4">
                        <ShieldCheck className="w-7 h-7 text-burgundy-100"/>
                    </div>

                    <h1 className="text-xl font-bold">
                        Activate Your Account
                    </h1>

                    <p className="mt-1 text-xs text-burgundy-200">
                        Secure verification required
                    </p>
                </div>

                <form
                    onSubmit={handleVerify}
                    className="p-8"
                    autoComplete="off"
                >
                    <div className="text-center mb-7">
                        <div className="flex justify-center mb-4">
                            <div className="w-11 h-11 rounded-xl bg-burgundy-50 flex items-center justify-center">
                                <Mail className="w-5 h-5 text-burgundy-700"/>
                            </div>
                        </div>

                        <h2 className="text-lg font-bold text-slate-800">
                            Enter Verification Code
                        </h2>

                        <p className="mt-2 text-sm text-slate-500 leading-6">
                            We have sent a 6-digit verification code to
                        </p>

                        <p className="text-sm font-semibold text-burgundy-800 break-all">
                            {email}
                        </p>
                    </div>

                    <div
                        className="flex justify-between gap-2 mb-5"
                        onPaste={handlePaste}
                    >
                        {otp.map((digit, index) => (
                            <input
                                key={index}
                                ref={(element) => {
                                    inputRefs.current[index] =
                                        element;
                                }}
                                type="text"
                                inputMode="numeric"
                                maxLength={1}
                                value={digit}
                                onChange={(e) =>
                                    handleChange(
                                        e.target.value,
                                        index,
                                    )
                                }
                                onKeyDown={(e) =>
                                    handleKeyDown(e, index)
                                }
                                aria-label={`OTP digit ${index + 1}`}
                                className="w-full aspect-square max-w-12 text-center text-xl font-bold text-burgundy-900 border-2 border-slate-200 rounded-xl outline-none focus:border-burgundy-700 focus:ring-4 focus:ring-burgundy-100 transition"
                            />
                        ))}
                    </div>
                    {error && (
                        <div
                            className="mb-5 p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-xl text-center">
                            {error}
                        </div>
                    )}

                    <button
                        type="submit"
                        disabled={submitting}
                        className="w-full py-3 rounded-xl text-sm font-bold text-white bg-gradient-to-r from-burgundy-700 to-burgundy-900 hover:opacity-95 disabled:opacity-50 transition flex items-center justify-center gap-2"
                    >
                        <KeyRound className="w-4 h-4"/>

                        {submitting
                            ? "Verifying..."
                            : "Verify Email"}
                    </button>
                    <div className="mt-6 pt-5 border-t border-slate-100 text-center">
                        <p className="text-xs text-slate-500">
                            Did not receive the code?
                        </p>

                        <button
                            type="button"
                            onClick={handleResend}
                            disabled={resending}
                            className="mt-2 text-xs font-bold text-burgundy-700 hover:text-burgundy-900 disabled:opacity-50"
                        >
                            {resending
                                ? "Sending..."
                                : "Resend Verification Code"}
                        </button>
                    </div>
                    <button
                        type="button"
                        onClick={() => navigate("/login")}
                        disabled={submitting || resending}
                        className="mt-5 w-full flex items-center justify-center gap-2 text-xs font-semibold text-slate-500 hover:text-slate-800 disabled:opacity-50"
                    >
                        <ArrowLeft className="w-4 h-4"/>
                        Back to Login
                    </button>
                </form>
            </div>
        </div>
    );
};