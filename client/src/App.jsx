import {BrowserRouter, Routes, Route} from 'react-router-dom';
import {Navbar} from './components/Navbar';
import {LandingPage} from './pages/LandingPage';
import {LoginPage} from './pages/LoginPage';

export function AppRoutes() {
    return (
        <div className="min-h-screen bg-[#FAF9FA] text-slate-800 flex flex-col font-sans">
            <Navbar/>
            <Routes>
                <Route path="/" element={<LandingPage/>}/>
                <Route path="/login" element={<LoginPage/>}/>
            </Routes>
        </div>
    );
}

export default function App() {
    return (
        <BrowserRouter>
            <AppRoutes/>
        </BrowserRouter>
    );
}