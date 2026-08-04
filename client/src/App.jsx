import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Navbar } from './components/Navbar';
import { LandingPage } from './pages/LandingPage';

export function AppRoutes() {
  return (
      <div className="min-h-screen bg-[#FAF9FA] text-slate-800 flex flex-col font-sans">
        <Navbar />
        <Routes>
          <Route path="/" element={<LandingPage />} />
        </Routes>
      </div>
  );
}

export default function App() {
  return (
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
  );
}