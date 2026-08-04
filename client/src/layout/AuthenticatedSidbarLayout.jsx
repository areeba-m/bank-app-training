import React from 'react';
import { useSelector } from 'react-redux';
import {Sidebar} from '../components/Sidebar.jsx';

const AuthenticatedLayout = ({ children }) => {
    const { isAuthenticated } = useSelector((state) => state.auth);

    return (
        <div className="flex min-h-[calc(100vh-4rem)]">
            {isAuthenticated && <Sidebar />}

            <main className="flex-1 p-4 sm:p-6 lg:p-8 max-w-7xl mx-auto w-full">
                {children}
            </main>
        </div>
    );
};

export default AuthenticatedLayout;