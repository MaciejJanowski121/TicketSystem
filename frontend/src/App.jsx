import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import Navbar from './components/Navbar'
import HomePage from './pages/HomePage'
import RegisterPage from './pages/RegisterPage'
import LoginPage from './pages/LoginPage'
import ChangePasswordPage from './pages/ChangePasswordPage'
import CreateTicketPage from './pages/CreateTicketPage'
import MyTicketsPage from './pages/MyTicketsPage'
import MyTicketDetailPage from './pages/MyTicketDetailPage'
import './App.css'

function App() {
  return (
    <Router>
      <div className="app">
        <Navbar />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/change-password" element={<ChangePasswordPage />} />
            <Route path="/tickets/new" element={<CreateTicketPage />} />
            <Route path="/my-tickets" element={<MyTicketsPage />} />
            <Route path="/my-tickets/:ticketId" element={<MyTicketDetailPage />} />
          </Routes>
        </main>
      </div>
    </Router>
  )
}

export default App
