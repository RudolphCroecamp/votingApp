import { Routes, Route, Link } from 'react-router'

import Login from './pages/Login.jsx'
import Dashboard from './pages/Dashboard.jsx'

import HealthMonitor from './pages/api-health.jsx'
import AdminDash from './pages/admins.jsx'
import Manage_Elections from './pages/Manage_Elections.jsx';
import Voter from './pages/voter.jsx';
import ElectionVoterManager from './pages/ElectionVoterManagement.jsx';
import ManageParties from "./pages/ManageParties.jsx";
import Election_Results from "./pages/ElectionResults.jsx"

export default function App() {
  return (
    <div>
      {/* <nav>
        <Link to="/">Login </Link>{' | '}
        <Link to="/Dashboard"> Dashboard</Link>{' | '}

        <Link to="/manage_elections"> manage_elections </Link>{' | '}
        <Link to="/Election_Voter_Management"> Election_Voter_Management </Link>{' | '}

        <Link to="/health"> health </Link>{' | '}

        <Link to="/admins"> admins </Link>{' | '}

        <Link to="/voter"> Voter </Link>{' | '}

        
      </nav> */}

      <Routes>manage_elections
        <Route path="/" element={<Login />} />

        <Route path="/Dashboard" element={<Dashboard />} />

        <Route path="/Election_Results" element={<Election_Results />} />

        <Route path="/manage_elections" element={<Manage_Elections />} />
        <Route path="/Election_Voter_Management" element={<ElectionVoterManager />} />

        <Route path="/manage_parties" element={<ManageParties />} />

        <Route path="/admins" element={<AdminDash />} />

        <Route path="/Voter" element={<Voter />} />

        <Route path="/health" element={<HealthMonitor />} />
      </Routes>
    </div>
  )
}