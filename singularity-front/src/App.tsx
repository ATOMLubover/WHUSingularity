import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { ConfigProvider } from 'antd'
import { AuthProvider } from './contexts/AuthContext'
import { ProtectedRoute } from './components/AuthGuard'
import LoginPage from './pages/Login'
import RegisterPage from './pages/Register'

function Placeholder() {
  return <div style={{ padding: 40 }}>秒杀主页 — 待实现</div>
}

export default function App() {
  return (
    <ConfigProvider>
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/" element={<ProtectedRoute><Placeholder /></ProtectedRoute>} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </ConfigProvider>
  )
}
