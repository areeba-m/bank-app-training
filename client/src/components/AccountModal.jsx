import {useState, useEffect} from 'react';
import {X, User, Lock, Mail, MapPin, DollarSign, CheckCircle2} from 'lucide-react';
import PropTypes from "prop-types";

export const AccountModal = ({isOpen, onClose, onSave, accountToEdit = null}) => {

    const isEdit = !!accountToEdit;

    const [formData, setFormData] = useState({
        id: '', password: '', name: '', email: '', address: '', initialBalance: '0.00'
    });

    const [error, setError] = useState('');
    const [submitting, setSubmitting] = useState(false);


    useEffect(() => {
        if (!isOpen) return;

        const updateForm = () => {
            if (accountToEdit) {
                setFormData({
                    id: accountToEdit.id || '',
                    password: accountToEdit.password || '',
                    name: accountToEdit.name || '',
                    email: accountToEdit.email || '',
                    address: accountToEdit.address || '',
                    initialBalance:
                        accountToEdit.balance !== undefined
                            ? accountToEdit.balance.toString()
                            : '0.00'
                });
            } else {
                setFormData({
                    id: '',
                    password: '',
                    name: '',
                    email: '',
                    address: '',
                    initialBalance: '1000.00'
                });
            }

            setError('');
        };

        updateForm();

    }, [accountToEdit, isOpen]);


    if (!isOpen) return null;


    const handleSubmit = async (e) => {

        e.preventDefault();

        setError('');


        if (!formData.name.trim() || !formData.email.trim()) {

            setError('Name and Email are required.');
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


    return (

        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">

            <div className="bg-white rounded-2xl max-w-lg w-full shadow-2xl border border-burgundy-100 overflow-hidden">


                <div
                    className="bg-gradient-to-r from-burgundy-800 to-burgundy-950 px-6 py-4 flex items-center justify-between text-white">

                    <div className="flex items-center gap-2.5">

                        <div className="w-8 h-8 rounded-lg bg-burgundy-700/80 flex items-center justify-center">
                            <User className="w-4 h-4 text-burgundy-100"/>
                        </div>


                        <div>

                            <h3 className="font-bold text-base">
                                {isEdit ? `Edit Account (${accountToEdit.id})` : 'Create New Account'}
                            </h3>

                            <p className="text-xs text-burgundy-200">
                                {isEdit ? 'Update account details' : 'Add new customer account'}
                            </p>

                        </div>

                    </div>


                    <button
                        onClick={onClose}
                        className="p-1 rounded-lg hover:bg-white/10"
                    >
                        <X className="w-5 h-5"/>
                    </button>

                </div>


                <form onSubmit={handleSubmit} className="p-6 space-y-4">


                    {error && (

                        <div className="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-xl">
                            {error}
                        </div>

                    )}


                    {isEdit && (

                        <div>

                            <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                                Account ID
                            </label>


                            <input
                                value={formData.id}
                                disabled
                                className="w-full px-3 py-2 text-sm border rounded-xl bg-slate-100 text-slate-500"
                            />

                        </div>

                    )}


                    <div>

                        <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                            Full Name *
                        </label>


                        <input

                            type="text"
                            value={formData.name}

                            onChange={(e) => setFormData({
                                ...formData, name: e.target.value
                            })}

                            className="w-full px-3 py-2 text-sm border rounded-xl"
                            required

                        />

                    </div>


                    <div>

                        <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                            Email Address *
                        </label>


                        <div className="relative">

                            <input

                                type="email"
                                disabled={isEdit}

                                value={formData.email}

                                onChange={(e) => setFormData({
                                    ...formData, email: e.target.value
                                })}

                                className="w-full pl-9 pr-3 py-2 text-sm border rounded-xl disabled:bg-slate-100 disabled:text-slate-500"

                                required

                            />


                            <Mail className="absolute left-3 top-2.5 w-4 h-4 text-slate-400"/>


                        </div>


                    </div>


                    <div>

                        <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                            Password *
                        </label>


                        <div className="relative">


                            <input

                                type="text"

                                disabled={isEdit}

                                value={formData.password}

                                onChange={(e) => setFormData({
                                    ...formData, password: e.target.value
                                })}


                                className="w-full pl-9 pr-3 py-2 text-sm border rounded-xl disabled:bg-slate-100 disabled:text-slate-500"

                                required={!isEdit}

                            />


                            <Lock className="absolute left-3 top-2.5 w-4 h-4 text-slate-400"/>


                        </div>


                    </div>


                    <div>

                        <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                            Address
                        </label>


                        <div className="relative">


                            <input

                                value={formData.address}

                                onChange={(e) => setFormData({
                                    ...formData, address: e.target.value
                                })}


                                className="w-full pl-9 pr-3 py-2 text-sm border rounded-xl"

                            />


                            <MapPin className="absolute left-3 top-2.5 w-4 h-4 text-slate-400"/>


                        </div>


                    </div>


                    {!isEdit && (

                        <div>


                            <label className="block text-xs font-bold text-slate-700 uppercase mb-1">
                                Opening Deposit ($)
                            </label>


                            <div className="relative">


                                <input

                                    type="number"

                                    value={formData.initialBalance}

                                    onChange={(e) => setFormData({
                                        ...formData, initialBalance: e.target.value
                                    })}


                                    className="w-full pl-9 pr-3 py-2 text-sm border rounded-xl"

                                />


                                <DollarSign className="absolute left-3 top-2.5 w-4 h-4 text-slate-400"/>


                            </div>


                        </div>

                    )}
                    <div className="pt-4 flex justify-end gap-3 border-t">


                        <button

                            type="button"

                            onClick={onClose}

                            className="px-4 py-2 text-xs font-bold rounded-xl hover:bg-slate-100"

                        >

                            Cancel

                        </button>


                        <button

                            type="submit"

                            disabled={submitting}

                            className="px-5 py-2 text-xs font-bold text-white bg-gradient-to-r from-burgundy-700 to-burgundy-900 rounded-xl disabled:opacity-50 flex items-center gap-2"

                        >

                            <CheckCircle2 className="w-4 h-4"/>

                            {submitting ? 'Saving...' : isEdit ? 'Save Changes' : 'Create Account'}

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
    accountToEdit: PropTypes.object,
};