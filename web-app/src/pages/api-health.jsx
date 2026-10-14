
const API_BASE_URL = 'http://localhost:5000'
import Sidebar from "../components/sidebar";

async function fetchHealth(endpoint) {
    const startedAt = performance.now()

    try {
        const response = await fetch(
            `${API_BASE_URL}${endpoint}`,
            { cache: 'no-store' }
        )

        const latencyMs = Math.round(performance.now() - startedAt)
        const data = await response.json().catch(() => null)

        if (!response.ok) {
            return {
                healthy: false,
                status: data?.status ?? 'Unhealthy',
                latencyMs,
                error: data?.message ?? `HTTP ${response.status}`,
                data
            }
        }

        return {
            healthy: data?.status?.toLowerCase() === 'healthy',
            status: data?.status ?? 'Unknown',
            latencyMs,
            error: null,
            data
        }
    } catch (error) {
        return {
            healthy: false,
            status: 'Unreachable',
            latencyMs: Math.round(performance.now() - startedAt),
            error: error.message || 'Unable to reach the API',
            data: null
        }
    }
}

async function checkDatabaseHealth() {
    return fetchHealth('/api/Health/db_health')
}

async function checkServerHealth() {
    return fetchHealth('/api/Health/health')
}

async function checkNodesHealth() {
    return fetchHealth('/api/Health/nodes')
}







import { useCallback, useEffect, useState } from 'react'

function StatusPill({ service }) {
    if (!service) {
        return <span className="pill pill-neutral">Checking...</span>
    }

    if (service.healthy) {
        return <span className="pill pill-success">Healthy</span>
    }

    return (
        <span className="pill pill-danger">
            {service.status || 'Unhealthy'}
        </span>
    )
}

function formatLatency(ms) {
    return Number.isFinite(ms) ? `${ms} ms` : '—'
}

function formatTime(value) {
    if (!value) return '—'

    const date = new Date(value)

    return Number.isNaN(date.getTime())
        ? '—'
        : date.toLocaleString()
}

export default function HealthMonitor() {
    const [api, setApi] = useState(null)
    const [database, setDatabase] = useState(null)
    const [lastChecked, setLastChecked] = useState(null)
    const [loading, setLoading] = useState(true)
    const [refreshing, setRefreshing] = useState(false)
    const [nodes, setNodes] = useState(null)

    const refreshHealth = useCallback(async (manual = false) => {
        if (manual) {
            setRefreshing(true)
        } else {
            setLoading(true)
        }

        try {
            const [apiResult, dbResult, nodeResult] = await Promise.all([
                checkServerHealth(),
                checkDatabaseHealth(),
                checkNodesHealth()
            ])

            setApi(apiResult)
            setDatabase(dbResult)
            setNodes(nodeResult)
            setLastChecked(new Date())
        } finally {
            setLoading(false)
            setRefreshing(false)
        }
    }, [])

    useEffect(() => {
        refreshHealth()

        const intervalId = setInterval(() => {
            refreshHealth()
        }, 15000)

        return () => clearInterval(intervalId)
    }, [refreshHealth])

    const services = [api, database].filter(Boolean)
    const healthyCount = services.filter(s => s.healthy).length

    const overallStatus =
        services.length === 0
            ? 'Checking'
            : healthyCount === services.length
                ? 'Healthy'
                : healthyCount > 0
                    ? 'Degraded'
                    : 'Unhealthy'

    const overallClass =
        overallStatus === 'Healthy'
            ? 'text-success'
            : overallStatus === 'Degraded'
                ? 'text-warning'
                : overallStatus === 'Unhealthy'
                    ? 'text-danger'
                    : 'text-secondary'

    return (
    <div className="admin-layout">
        <Sidebar />

        <main className="admin-content">
            <header className="topbar">
                <div>
                    <div className="text-secondary small">
                        Workspace / Monitoring
                    </div>
                    <h1 className="h4 mb-0 mt-1">API health</h1>
                </div>

                <button
                    className="btn btn-outline-secondary btn-sm"
                    type="button"
                    onClick={() => refreshHealth(true)}
                    disabled={refreshing || loading}
                >
                    {refreshing || loading ? "Checking..." : "↻ Refresh"}
                </button>
            </header>

            <div className="content-wrap">
                {/* Page heading */}
                <div className="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
                    <div>
                        <p className="text-secondary small fw-semibold mb-1">
                            SERVICE MONITORING
                        </p>
                        <h2 className="h3 fw-bold mb-1">System health</h2>
                        <p className="text-secondary mb-0">
                            Live checks of your API and PostgreSQL database.
                        </p>
                    </div>

                    <span className="pill pill-neutral">
                        Auto-refresh: 15 seconds
                    </span>
                </div>

                {/* Overall health */}
                <div className="health-hero mb-4">
                    <div>
                        <div className="d-flex align-items-center gap-2 mb-2">
                            <span className="health-pulse"></span>
                            <span className={`fw-semibold small ${overallClass}`}>
                                SYSTEM {overallStatus.toUpperCase()}
                            </span>
                        </div>

                        <h2 className="h3 fw-bold mb-2">
                            {overallStatus === "Healthy"
                                ? "All configured checks are healthy"
                                : overallStatus === "Degraded"
                                    ? "Some services need attention"
                                    : overallStatus === "Unhealthy"
                                        ? "Health checks are failing"
                                        : "Checking services..."}
                        </h2>

                        <p className="text-secondary mb-0">
                            Last checked: {formatTime(lastChecked)}
                        </p>
                    </div>

                    <div className="health-score">
                        <div className="health-score-number">
                            {healthyCount}/{services.length || 2}
                        </div>
                        <div className="small text-secondary">
                            Healthy services
                        </div>
                    </div>
                </div>

                {/* Health statistics */}
                <div className="row g-0 mb-4">
                    <div className="col-sm-6 col-xl-3 panel">
                        <div className="stat-card">
                            <div className="stat-label">API status</div>
                            <div className="stat-number fs-4 mt-3">
                                <StatusPill service={api} />
                            </div>
                            <div className="stat-foot">
                                {api?.data?.environment
                                    ? `Environment: ${api.data.environment}`
                                    : "HTTP health endpoint"}
                            </div>
                        </div>
                    </div>

                    <div className="col-sm-6 col-xl-3 panel">
                        <div className="stat-card">
                            <div className="stat-label">Database</div>
                            <div className="stat-number fs-4 mt-3">
                                <StatusPill service={database} />
                            </div>
                            <div className="stat-foot">
                                {database?.data?.database ?? "PostgreSQL"}
                            </div>
                        </div>
                    </div>

                    <div className="col-sm-6 col-xl-3 panel">
                        <div className="stat-card">
                            <div className="stat-label">API response time</div>
                            <div className="stat-number fs-4 mt-3">
                                {formatLatency(api?.latencyMs)}
                            </div>
                            <div className="stat-foot">
                                Browser-measured HTTP latency
                            </div>
                        </div>
                    </div>

                    <div className="col-sm-6 col-xl-3 panel">
                        <div className="stat-card">
                            <div className="stat-label">API uptime</div>
                            <div className="stat-number fs-4 mt-3">
                                {Number.isFinite(api?.data?.uptimeSeconds)
                                    ? `${Math.floor(api.data.uptimeSeconds / 3600)}h ${Math.floor(
                                        (api.data.uptimeSeconds % 3600) / 60
                                    )}m`
                                    : "—"}
                            </div>
                            <div className="stat-foot">
                                Time since API process started
                            </div>
                        </div>
                    </div>
                </div>

                {/* Service components */}
                <section className="panel mb-4">
                    <div className="panel-heading">
                        <div>
                            <h3 className="h6 fw-bold mb-1">
                                Service components
                            </h3>
                            <p className="text-secondary small mb-0">
                                Live status of configured health endpoints
                            </p>
                        </div>
                    </div>

                    <div className="table-responsive">
                        <table className="table table-hover align-middle mb-0">
                            <thead>
                                <tr>
                                    <th>Component</th>
                                    <th>Purpose</th>
                                    <th>Endpoint</th>
                                    <th>Status</th>
                                    <th>Latency</th>
                                </tr>
                            </thead>

                            <tbody>
                                <tr>
                                    <td>
                                        <div className="fw-semibold">
                                            Voting API
                                        </div>
                                        <div className="small text-secondary">
                                            Main service
                                        </div>
                                    </td>
                                    <td>HTTP request handling</td>
                                    <td><code>/api/Health/health</code></td>
                                    <td><StatusPill service={api} /></td>
                                    <td>{formatLatency(api?.latencyMs)}</td>
                                </tr>

                                <tr>
                                    <td>
                                        <div className="fw-semibold">
                                            PostgreSQL
                                        </div>
                                        <div className="small text-secondary">
                                            Primary database
                                        </div>
                                    </td>
                                    <td>Database connectivity</td>
                                    <td><code>/api/Health/db_health</code></td>
                                    <td><StatusPill service={database} /></td>
                                    <td>{formatLatency(database?.latencyMs)}</td>
                                </tr>

                                {nodes?.data?.nodes?.map((node) => (
                                    <tr key={node.node}>
                                        <td>
                                            <div className="fw-semibold">
                                                {node.node}
                                            </div>
                                            <div className="small text-secondary">
                                                Blockchain service
                                            </div>
                                        </td>
                                        <td>Node availability</td>
                                        <td>
                                            <code>
                                                /api/Health/nodes/{node.node}
                                            </code>
                                        </td>
                                        <td>
                                            <span
                                                className={`pill ${
                                                    node.status === "Healthy"
                                                        ? "pill-success"
                                                        : "pill-danger"
                                                }`}
                                            >
                                                {node.status}
                                            </span>
                                        </td>
                                        <td>
                                            {Number.isFinite(node.latencyMs)
                                                ? `${node.latencyMs} ms`
                                                : "—"}
                                        </td>
                                    </tr>
                                ))}

                                {!nodes?.data?.nodes && (
                                    <tr>
                                        <td
                                            colSpan={5}
                                            className="text-secondary p-3"
                                        >
                                            {nodes?.error ??
                                                "Node health data unavailable"}
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </section>

                {/* Diagnostic details */}
                <section className="panel">
                    <div className="panel-heading">
                        <div>
                            <h3 className="h6 fw-bold mb-1">
                                Health details
                            </h3>
                            <p className="text-secondary small mb-0">
                                Diagnostics from the API response
                            </p>
                        </div>
                    </div>

                    <div className="p-4">
                        {api?.data ? (
                            <dl className="row mb-0">
                                <dt className="col-sm-4">API status</dt>
                                <dd className="col-sm-8">{api.status}</dd>

                                <dt className="col-sm-4">Environment</dt>
                                <dd className="col-sm-8">
                                    {api.data.environment ?? "Not supplied"}
                                </dd>

                                <dt className="col-sm-4">
                                    Server timestamp (UTC)
                                </dt>
                                <dd className="col-sm-8">
                                    {formatTime(api.data.timestamp)}
                                </dd>

                                <dt className="col-sm-4">API uptime</dt>
                                <dd className="col-sm-8">
                                    {api.data.uptimeSeconds ?? "Not supplied"} seconds
                                </dd>
                            </dl>
                        ) : (
                            <p className="text-secondary mb-0">
                                {loading
                                    ? "Loading health details..."
                                    : `API details unavailable. ${api?.error ?? ""}`}
                            </p>
                        )}

                        {database && !database.healthy && (
                            <div className="alert alert-danger mt-3 mb-0">
                                Database check failed:{" "}
                                {database.error ?? database.status}
                            </div>
                        )}
                    </div>
                </section>

                <footer className="page-footer">
                    <span>BallotAdmin · System monitoring</span>
                    <span>API, database and blockchain node health</span>
                </footer>
            </div>
        </main>
    </div>
);
}
