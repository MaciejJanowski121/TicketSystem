import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import Sidebar from './components/Sidebar'
import HomePage from './pages/HomePage'
import WikiPage from './pages/WikiPage'
import RegisterPage from './pages/RegisterPage'
import LoginPage from './pages/LoginPage'
import ChangePasswordPage from './pages/ChangePasswordPage'
import CreateTicketPage from './pages/CreateTicketPage'
import MyTicketDetailPage from './pages/MyTicketDetailPage'
import TicketsOverviewPage from './pages/TicketsOverviewPage'
import EndUserTicketsPage from './pages/EndUserTicketsPage'
import TicketDetailsReadOnlyPage from './pages/TicketDetailsReadOnlyPage'
import SupportTicketsPage from './pages/SupportTicketsPage'
import UserManagementPage from './pages/UserManagementPage'
import { getCurrentUser } from './utils/auth'
import './App.css'

// Role-based wrapper component for tickets page
function TicketsPageWrapper() {
  const currentUser = getCurrentUser();

  if (currentUser?.role === 'ENDUSER') {
    return <EndUserTicketsPage />;
  } else if (currentUser?.role === 'SUPPORTUSER' || currentUser?.role === 'ADMINUSER') {
    return <SupportTicketsPage />;
  } else {
    return <TicketsOverviewPage />;
  }
}

function App() {
  return (
    <Router>
      <div className="app">
        <Sidebar />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/wiki" element={<WikiPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/change-password" element={<ChangePasswordPage />} />
            <Route path="/tickets" element={<TicketsPageWrapper />} />
            <Route path="/tickets/:ticketId" element={<TicketDetailsReadOnlyPage />} />
            <Route path="/tickets/new" element={<CreateTicketPage />} />
            <Route path="/my-tickets/:ticketId" element={<MyTicketDetailPage />} />
            <Route path="/admin/users" element={<UserManagementPage />} />
          </Routes>
        </main>
      </div>
    </Router>
  )
}

export default App
