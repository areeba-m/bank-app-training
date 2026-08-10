import { useState, useEffect } from 'react';
import {
    X,
    User,
    Lock,
    Mail,
    MapPin,
    CheckCircle2,
    Eye,
    EyeOff
} from 'lucide-react';
import PropTypes from 'prop-types';

export const AccountModal = ({
    isOpen,
    onClose,
    onSave,
    accountToEdit = null
}) => {

    const isEdit = !!accountToEdit;

    const emptyForm = {
        password: '',
        name: '',
        email: '',
        address: ''
    };

    const [formData, setFormData] = useState(emptyForm);
    const [error, setError] = useState('');
    const [submitting, setSubmitting] = useState(false);
    const [showPassword, setShowPassword] = useState(false);

    useEffect(() => {
        if (!isOpen) {
            return;
        }

        if (accountToEdit) {
            // EDIT MODE
            // Only load fields that can actually be edited.
            setFormData({
                password: '',
                name: accountToEdit.name || '',
                email: accountToEdit.email || '',
                address: accountToEdit.address || ''
            });
        } else {
            // CREATE MODE
            // Completely empty form.
            setFormData({
                password: '',
                name: '',
                email: '',
                address: ''
            });
        }

        setError('');
        setShowPassword(false);

    }, [isOpen, accountToEdit]);

    if (!isOpen) {
        return null;
    }

    const handleSubmit = async (e) => {
        e.preventDefault();

        setError('');

        if (!formData.name.trim()) {
            setError('Name is required.');
            return;
        }

        if (!formData.email.trim()) {
            setError('Email is required.');
            return;
        }

        if (!isEdit && !formData.password.trim()) {
            setError('Password is required for new account creation.');
            return;
        }

        setSubmitting(true);

        try {
            await onSave(formData);
            onClose();
        } catch (err) {
            setError(err.message || 'Failed to save account');
        } finally {
            setSubmitting(false);
        }
    };

    const handleClose = () => {
        // Clear the form when closing.
        setFormData(emptyForm);
        setError('');
        setShowPassword(false);
        onClose();
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">

            <div className="bg-white rounded-2xl max-w-lg w-full shadow-2xl border border-burgundy-100 overflow-hidden">

                {/* Header */}
                <div className="bg-gradient-to-r from-burgundy-800 to-burgundy-950 px-6 py-4 flex items-center justify-between text-white">

                    <div className="flex items-center gap-2.5">

                        <div className="w-8 h-8 rounded-lg bg-burgundy-700/80 flex items-center justify-center">
                            <User className="w-4 h-4 text-burgundy-100" />
                        </div>

                        <div>
                            <h3 className="font-bold text-base">
                                {isEdit
                                    ? `Edit Account (${accountToEdit.id})`
                                    : 'Create New Account'}
                            </h3>

                            <p className="text-xs text-burgundy-200">
                                {isEdit
                                    ? 'Update account details'
                                    : 'Add new customer account'}
                            </p>
                        </div>

                    </div>

                    <button
                        type="button"
                        onClick={handleClose}
                        className="p-1 rounded-lg hover:bg-white/10"
                    >
                        <X className="w-5 h-5" />
                    </button>

                </div>

                <form
                    onSubmit={handleSubmit}
                    className="p-6 space-y-4"
                    autoComplete="off"
                >

                    {/* Error */}
                    {error && (
                        <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-xl">
                            {error}
                        </div>
                    )}

                    {/* Account ID - Edit only */}
                    {isEdit && (
                        <div>
                            <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                                Account ID
                            </label>

                            <input
                                value={accountToEdit.id}
                                disabled
                                autoComplete="off"
                                className="w-full px-3 py-2 text-sm border rounded-xl bg-slate-100 text-slate-500"
                            />
                        </div>
                    )}

                    {/* Full Name */}
                    <div>
                        <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                            Full Name *
                        </label>

                        <input
                            type="text"
                            name="new-account-name"
                            autoComplete="off"
                            value={formData.name}
                            onChange={(e) =>
                                setFormData({
                                    ...formData,
                                    name: e.target.value
                                })
                            }
                            className="w-full px-3 py-2 text-sm border rounded-xl"
                            required
                        />
                    </div>

                    {/* Email */}
                    <div>
                        <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                            Email Address *
                        </label>

                        <div className="relative">

                            <input
                                type="email"
                                name="new-account-email"
                                autoComplete="off"
                                disabled={isEdit}
                                value={formData.email}
                                onChange={(e) =>
                                    setFormData({
                                        ...formData,
                                        email: e.target.value
                                    })
                                }
                                className="w-full pl-9 pr-3 py-2 text-sm border rounded-xl disabled:bg-slate-100 disabled:text-slate-500"
                                required
                            />

                            <Mail className="absolute left-3 top-2.5 w-4 h-4 text-slate-400" />

                        </div>
                    </div>

                    {/* Password */}
                    <div>
                        <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                            {isEdit ? 'Password' : 'Password *'}
                        </label>

                        <div className="relative">

                            <input
                                type={showPassword ? 'text' : 'password'}
                                name="new-account-password"
                                autoComplete="new-password"
                                disabled={isEdit}
                                value={formData.password}
                                onChange={(e) =>
                                    setFormData({
                                        ...formData,
                                        password: e.target.value
                                    })
                                }
                                className="w-full pl-9 pr-10 py-2 text-sm border rounded-xl disabled:bg-slate-100 disabled:text-slate-500"
                                required={!isEdit}
                                placeholder={
                                    isEdit
                                        ? 'Password cannot be changed here'
                                        : 'Enter password'
                                }
                            />

                            <Lock className="absolute left-3 top-2.5 w-4 h-4 text-slate-400" />

                            {!isEdit && (
                                <button
                                    type="button"
                                    onClick={() =>
                                        setShowPassword((prev) => !prev)
                                    }
                                    className="absolute right-3 top-2.5 text-slate-400 hover:text-slate-600"
                                    aria-label={
                                        showPassword
                                            ? 'Hide password'
                                            : 'Show password'
                                    }
                                >
                                    {showPassword ? (
                                        <EyeOff className="w-4 h-4" />
                                    ) : (
                                        <Eye className="w-4 h-4" />
                                    )}
                                </button>
                            )}

                        </div>
                    </div>

                    {/* Address */}
                    <div>
                        <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                            Address *
                        </label>

                        <div className="relative">

                            <input
                                type="text"
                                name="new-account-address"
                                autoComplete="off"
                                value={formData.address}
                                onChange={(e) =>
                                    setFormData({
                                        ...formData,
                                        address: e.target.value
                                    })
                                }
                                className="w-full pl-9 pr-3 py-2 text-sm border rounded-xl"
                            />

                            <MapPin className="absolute left-3 top-2.5 w-4 h-4 text-slate-400" />

                        </div>
                    </div>

                    {/* Buttons */}
                    <div className="pt-4 flex justify-end gap-3 border-t">

                        <button
                            type="button"
                            onClick={handleClose}
                            className="px-4 py-2 text-xs font-bold rounded-xl hover:bg-slate-100"
                        >
                            Cancel
                        </button>

                        <button
                            type="submit"
                            disabled={submitting}
                            className="px-5 py-2 text-xs font-bold text-white bg-gradient-to-r from-burgundy-700 to-burgundy-900 rounded-xl disabled:opacity-50 flex items-center gap-2"
                        >
                            <CheckCircle2 className="w-4 h-4" />

                            {submitting
                                ? 'Saving...'
                                : isEdit
                                    ? 'Save Changes'
                                    : 'Create Account'}
                        </button>

                    </div>

                </form>
            </div>
        </div>
    );
};

AccountModal.propTypes = {
    isOpen: PropTypes.bool.isRequired,
    onClose: PropTypes.func.isRequired,
    onSave: PropTypes.func.isRequired,
    accountToEdit: PropTypes.object
};