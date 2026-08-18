import {createAsyncThunk, createSlice} from "@reduxjs/toolkit";
import {authApi} from "../../services/AuthApi.js";

export const verifyOtpThunk = createAsyncThunk(
    "otpVerification/verifyOtp",
    async ({email, otp}, {rejectWithValue}) => {
        try {
            return await authApi.verifyOtp({email, otp});
        } catch (error) {
            return rejectWithValue(
                error?.message || "Invalid or expired OTP.",
            );
        }
    },
);

export const resendOtpThunk = createAsyncThunk(
    "otpVerification/resendOtp",
    async ({email}, {rejectWithValue}) => {
        try {
            return await authApi.resendOtp({email});
        } catch (error) {
            return rejectWithValue(
                error?.message || "Failed to resend OTP.",
            );
        }
    },
);

export const changePasswordThunk = createAsyncThunk(
    "otpVerification/changePassword",
    async ({resetToken, newPassword}, {rejectWithValue}) => {
        try {
            return await authApi.changePassword({
                resetToken,
                newPassword,
            });
        } catch (error) {
            return rejectWithValue(
                error?.message || "Failed to change password.",
            );
        }
    },
);

const initialState = {
    verificationError: null,
    resendError: null,
    passwordError: null,

    verifyingOtp: false,
    resendingOtp: false,
    changingPassword: false,

    otpVerified: false,
    resetToken: null,
    passwordChanged: false,
};

const otpVerificationSlice = createSlice({
    name: "otpVerification",
    initialState,

    reducers: {
        resetActivation: () => initialState,
    },

    extraReducers: (builder) => {
        builder

            // VERIFY OTP
            .addCase(verifyOtpThunk.pending, (state) => {
                state.verifyingOtp = true;
                state.verificationError = null;
            })

            .addCase(verifyOtpThunk.fulfilled, (state, action) => {
                state.verifyingOtp = false;
                state.otpVerified = true;
                state.resetToken = action.payload.resetToken;
                state.verificationError = null;
            })

            .addCase(verifyOtpThunk.rejected, (state, action) => {
                state.verifyingOtp = false;
                state.otpVerified = false;
                state.resetToken = null;

                state.verificationError =
                    action.payload;
            })

            .addCase(resendOtpThunk.pending, (state) => {
                state.resendingOtp = true;
                state.resendError = null;
            })

            .addCase(resendOtpThunk.fulfilled, (state) => {
                state.resendingOtp = false;
                state.resendError = null;
            })

            .addCase(resendOtpThunk.rejected, (state, action) => {
                state.resendingOtp = false;
                state.resendError = action.payload;
            })


            .addCase(changePasswordThunk.pending, (state) => {
                state.changingPassword = true;
                state.passwordError = null;
            })

            .addCase(changePasswordThunk.fulfilled, (state) => {
                state.changingPassword = false;
                state.passwordChanged = true;
                state.resetToken = null;
            })

            .addCase(changePasswordThunk.rejected, (state, action) => {
                state.changingPassword = false;
                state.passwordError = action.payload;
            });
    },
});

export const {
    resetActivation,
} = otpVerificationSlice.actions;

export default otpVerificationSlice.reducer;