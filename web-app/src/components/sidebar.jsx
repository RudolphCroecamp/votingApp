
import { NavLink, useNavigate } from "react-router";

export default function Sidebar() {
    const navigate = useNavigate();

    function handleSignOut() {
        sessionStorage.removeItem("JWTToken");
        navigate("/");
    }

    const links = [
        {
            to: "/dashboard",
            label: "Overview",
            icon: "▦",
        },
        {
            to: "/Election_Results",
            label: "Election Results",
            icon: "▦",
        },
        {
            to: "/manage_elections",
            label: "Manage Elections",
            icon: "▤",
        },
        {
            to: "/manage_parties",
            label: "Manage Parties",
            icon: "▤",
        },
        {
            to: "/Election_Voter_Management",
            label: "Election Voter Management",
            icon: "♙",
        },
        {
            to: "/voter",
            label: "Voters",
            icon: "◉",
        },
        {
            to: "/admins",
            label: "Administrators",
            icon: "♧",
        },
        {
            to: "/health",
            label: "API health",
            icon: "◉",
        },
    ];

    return (
        <aside className="sidebar">
            <NavLink
                className="brand brand-light text-decoration-none"
                to="/dashboard"
            >
                <span className="brand-mark">B</span>
                <span>
                    Ballot<span className="fw-light">Admin</span>
                </span>
            </NavLink>

            <div className="sidebar-label">WORKSPACE</div>

            <nav className="nav flex-column gap-1">
                {links.map((link) => (
                    <NavLink
                        key={link.to}
                        to={link.to}
                        className={({ isActive }) =>
                            `nav-link ${isActive ? "active" : ""}`
                        }
                    >
                        <span className="sidebar-icon">{link.icon}</span>
                        <span>{link.label}</span>
                    </NavLink>
                ))}
            </nav>

            <div className="sidebar-bottom">
                <div className="avatar">AD</div>

                <div className="flex-grow-1">
                    <div className="small fw-semibold text-white">
                        Admin Demo
                    </div>
                    <div className="small text-white-50">
                        Super administrator
                    </div>
                </div>

                <button
                    type="button"
                    className="sidebar-signout"
                    onClick={handleSignOut}
                    title="Sign out"
                    aria-label="Sign out"
                >
                    ↗
                </button>
            </div>
        </aside>
    );
}
