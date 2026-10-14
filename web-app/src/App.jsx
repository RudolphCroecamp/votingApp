import { Routes, Route, Link } from 'react-router'

import Login from './pages/Login.jsx'
import Dashboard from './pages/Dashboard.jsx'
import Election_voters from './pages/Election_voters.jsx'
import HealthMonitor from './pages/api-health.jsx'
import AdminDash from './pages/admins.jsx'
import Manage_Elections from './pages/Manage_Elections.jsx';
import Voter from './pages/voter.jsx';
import ElectionVoterManager from './pages/ElectionVoterManagement.jsx';

export default function App() {
  return (
    <div>
      {/* <nav>
        <Link to="/">Login</Link>{' | '}
        <Link to="/Dashboard">Dashboard</Link>{' | '}
        <Link to="/Election_voters">Election_voters</Link>{' | '}
        <Link to="/health">Health Monitor</Link>
        <Link to="/admins">Admin Dashboard</Link>
        <Link to="/manage_elections">Admin Dashboard</Link>
      </nav> */}

      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/Dashboard" element={<Dashboard />} />
        <Route path="/manage_elections" element={<Manage_Elections />} />
        <Route path="/view_Election_voters" element={<Election_voters />} />
        <Route path="/Election_voters" element={<ElectionVoterManager />} />
        <Route path="/admins" element={<AdminDash />} />
        <Route path="/Voter" element={<Voter />} />
        <Route path="/health" element={<HealthMonitor />} />
      </Routes>
    </div>
  )
}