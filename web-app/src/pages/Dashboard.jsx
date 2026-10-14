import { Link } from "react-router";
import Sidebar from "../components/sidebar";



export default function AdminDash() {
    return (
        <div className="admin-layout">
            <Sidebar />

            <main className="admin-content">
                <header className="topbar d-flex justify-content-between align-items-center mb-4">
                    <div>
                        <div className="text-secondary small">
                            Workspace / Overview
                        </div>
                        <h1 className="h4 mb-0 mt-1">Overview</h1>
                    </div>

                    <div className="d-flex align-items-center gap-3">
                        <span className="status-dot"></span>
                        <span className="small text-secondary d-none d-sm-inline">
                            System status: operational
                        </span>
                        <div className="avatar avatar-sm">AD</div>
                    </div>
                </header>

                <div className="content-wrap">
                    {/* Welcome panel */}
                    <div className="welcome-panel mb-4">
                        <div>
                            <span className="eyebrow text-info">
                                ADMIN CONSOLE
                            </span>
                            <h2 className="h3 fw-bold mt-2 mb-2">
                                Good morning, Admin.
                            </h2>
                            <p className="mb-0 text-white-50">
                                Here’s a snapshot of your election service.
                            </p>
                        </div>

                        <div className="welcome-icon">▦</div>
                    </div>

                    {/* Statistics */}
                    <div className="row g-3 mb-4">
                        <div className="col-sm-6 col-xl-3">
                            <div className="stat-card">
                                <div className="stat-top">
                                    <span className="stat-label">
                                        Registered voters
                                    </span>
                                    <span className="stat-icon blue">♙</span>
                                </div>
                                <div className="stat-number">24,581</div>
                                <div className="stat-foot">
                                    <span className="text-success">↑ 4.8%</span>
                                    <span>from last month</span>
                                </div>
                            </div>
                        </div>

                        <div className="col-sm-6 col-xl-3">
                            <div className="stat-card">
                                <div className="stat-top">
                                    <span className="stat-label">
                                        Voter participation
                                    </span>
                                    <span className="stat-icon green">✓</span>
                                </div>
                                <div className="stat-number">68.4%</div>
                                <div className="stat-foot">
                                    <span>Across active elections</span>
                                </div>
                            </div>
                        </div>

                        <div className="col-sm-6 col-xl-3">
                            <div className="stat-card">
                                <div className="stat-top">
                                    <span className="stat-label">
                                        Active elections
                                    </span>
                                    <span className="stat-icon violet">▣</span>
                                </div>
                                <div className="stat-number">3</div>
                                <div className="stat-foot">
                                    <span>1 election closing soon</span>
                                </div>
                            </div>
                        </div>

                        <div className="col-sm-6 col-xl-3">
                            <div className="stat-card">
                                <div className="stat-top">
                                    <span className="stat-label">
                                        API availability
                                    </span>
                                    <span className="stat-icon green">◉</span>
                                </div>
                                <div className="stat-number">99.98%</div>
                                <div className="stat-foot">
                                    <span className="text-success">
                                        All services responding
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Recent records and service health */}
                    <div className="row g-4">
                        <div className="col-xl-7">
                            <section className="panel h-100">
                                <div className="panel-heading">
                                    <div>
                                        <h3 className="h6 fw-bold mb-1">
                                            Recent voter records
                                        </h3>
                                        <p className="text-secondary small mb-0">
                                            Latest entries in the voter register
                                        </p>
                                    </div>

                                    <Link
                                        className="btn btn-sm btn-outline-secondary"
                                        to="/Election_voters"
                                    >
                                        View all
                                    </Link>
                                </div>

                                <div className="table-responsive">
                                    <table className="table align-middle mb-0">
                                        <thead>
                                            <tr>
                                                <th>Voter</th>
                                                <th>Election</th>
                                                <th>Status</th>
                                            </tr>
                                        </thead>

                                        <tbody>
                                            <tr>
                                                <td>
                                                    <div className="fw-semibold">
                                                        Thabo Mokoena
                                                    </div>
                                                    <div className="small text-secondary">
                                                        VTR-002481
                                                    </div>
                                                </td>
                                                <td>National Election</td>
                                                <td>
                                                    <span className="pill pill-success">
                                                        Eligible
                                                    </span>
                                                </td>
                                            </tr>

                                            <tr>
                                                <td>
                                                    <div className="fw-semibold">
                                                        Amara Jacobs
                                                    </div>
                                                    <div className="small text-secondary">
                                                        VTR-002480
                                                    </div>
                                                </td>
                                                <td>Municipal Election</td>
                                                <td>
                                                    <span className="pill pill-success">
                                                        Eligible
                                                    </span>
                                                </td>
                                            </tr>

                                            <tr>
                                                <td>
                                                    <div className="fw-semibold">
                                                        Liam Nkosi
                                                    </div>
                                                    <div className="small text-secondary">
                                                        VTR-002479
                                                    </div>
                                                </td>
                                                <td>National Election</td>
                                                <td>
                                                    <span className="pill pill-warning">
                                                        Review
                                                    </span>
                                                </td>
                                            </tr>

                                            <tr>
                                                <td>
                                                    <div className="fw-semibold">
                                                        Naledi Dlamini
                                                    </div>
                                                    <div className="small text-secondary">
                                                        VTR-002478
                                                    </div>
                                                </td>
                                                <td>Campus Election</td>
                                                <td>
                                                    <span className="pill pill-success">
                                                        Eligible
                                                    </span>
                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </div>
                            </section>
                        </div>

                        <div className="col-xl-5">
                            {/* Service health */}
                            <section className="panel mb-4">
                                <div className="panel-heading">
                                    <div>
                                        <h3 className="h6 fw-bold mb-1">
                                            Service health
                                        </h3>
                                        <p className="text-secondary small mb-0">
                                            Illustrative status indicators
                                        </p>
                                    </div>

                                    <Link
                                        className="small text-decoration-none"
                                        to="/health"
                                    >
                                        Details →
                                    </Link>
                                </div>

                                <div className="service-row">
                                    <span className="service-symbol">API</span>
                                    <div className="flex-grow-1">
                                        <div className="fw-semibold small">
                                            Voting API
                                        </div>
                                        <div className="text-secondary small">
                                            Primary application
                                        </div>
                                    </div>
                                    <span className="pill pill-success">
                                        Operational
                                    </span>
                                </div>

                                <div className="service-row">
                                    <span className="service-symbol">DB</span>
                                    <div className="flex-grow-1">
                                        <div className="fw-semibold small">
                                            PostgreSQL
                                        </div>
                                        <div className="text-secondary small">
                                            Main database
                                        </div>
                                    </div>
                                    <span className="pill pill-success">
                                        Connected
                                    </span>
                                </div>

                                <div className="service-row">
                                    <span className="service-symbol">BC</span>
                                    <div className="flex-grow-1">
                                        <div className="fw-semibold small">
                                            Blockchain nodes
                                        </div>
                                        <div className="text-secondary small">
                                            4 configured nodes
                                        </div>
                                    </div>
                                    <span className="pill pill-warning">
                                        Demo data
                                    </span>
                                </div>
                            </section>

                            {/* Quick actions */}
                            <section className="panel p-3">
                                <h3 className="h6 fw-bold mb-3">
                                    Quick actions
                                </h3>

                                <Link
                                    className="quick-action"
                                    to="/Election_voters"
                                >
                                    <span className="quick-icon">♙</span>
                                    <span>
                                        <strong>Browse voter register</strong>
                                        <small>Review voter records</small>
                                    </span>
                                    <span className="ms-auto">→</span>
                                </Link>

                                <Link
                                    className="quick-action"
                                    to="/admins"
                                >
                                    <span className="quick-icon">♧</span>
                                    <span>
                                        <strong>Manage administrators</strong>
                                        <small>Invite authorised admins</small>
                                    </span>
                                    <span className="ms-auto">→</span>
                                </Link>
                            </section>
                        </div>
                    </div>

                    <footer className="page-footer">
                        <span>BallotAdmin · Static UI prototype</span>
                        <span>
                            Sample data only — connect to your API before production.
                        </span>
                    </footer>
                </div>
            </main>
        </div>
    );
}