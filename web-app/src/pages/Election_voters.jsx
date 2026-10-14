import { useEffect, useMemo, useState } from 'react'
import Sidebar from "../components/sidebar";
const PAGE_SIZE = 10

async function GetElections() {
    const token = sessionStorage.getItem('JWTToken')

    if (!token) {
        throw new Error('You are not authenticated. Please log in again.')
    }

    const response = await fetch(
        'http://localhost:5000/api/Admin/get_elections',
        {
            method: 'GET',
            headers: {
                Authorization: `Bearer ${token}`
            }
        }
    )

    const data = await response.json().catch(() => null)

    if (!response.ok) {
        throw new Error(data?.message ?? 'Failed to load elections')
    }

    const elections = Array.isArray(data)
        ? data
        : data?.elections

    if (!Array.isArray(elections)) {
        throw new Error('The API returned an unexpected elections format.')
    }

    return elections
}

async function GetElectionVoters(election_id) {
    const token = sessionStorage.getItem('JWTToken')

    if (!token) {
        throw new Error('You are not authenticated. Please log in again.')
    }

    const response = await fetch(
        `http://localhost:5000/api/Admin/GetElectionVoters/${encodeURIComponent(election_id)}`,
        {
            method: 'GET',
            headers: {
                Authorization: `Bearer ${token}`
            }
        }
    )

    const data = await response.json().catch(() => null)

    if (!response.ok) {
        throw new Error(
            data?.message ?? `Request failed: ${response.status}`
        )
    }

    const voters = Array.isArray(data)
        ? data
        : data?.election_voters ?? data?.electionVoters

    if (!Array.isArray(voters)) {
        throw new Error('The API returned an unexpected voter format.')
    }

    return voters
}

export default function Voters() {
    const [voters, setVoters] = useState([])
    const [elections, setElections] = useState([])
    const [selectedElectionId, setSelectedElectionId] = useState('')

    const [search, setSearch] = useState('')
    const [page, setPage] = useState(1)

    const [loadingElections, setLoadingElections] = useState(true)
    const [loading, setLoading] = useState(false)

    const [electionError, setElectionError] = useState('')
    const [error, setError] = useState('')

    // Increment this to retry loading voters for the current election.
    const [retryCount, setRetryCount] = useState(0)

    // Load available elections when the page opens.
    useEffect(() => {
        let cancelled = false

        async function loadElections() {
            setLoadingElections(true)
            setElectionError('')

            try {
                const result = await GetElections()

                if (!cancelled) {
                    setElections(result)
                }
            } catch (err) {
                if (!cancelled) {
                    setElectionError(
                        err.message || 'Failed to load elections.'
                    )
                }
            } finally {
                if (!cancelled) {
                    setLoadingElections(false)
                }
            }
        }

        loadElections()

        return () => {
            cancelled = true
        }
    }, [])

    // Load voters whenever the selected election changes.
    useEffect(() => {
        let cancelled = false

        if (elections.length > 0 && !selectedElectionId) {
            setSelectedElectionId(elections[0].election_id);
        }

        if (!selectedElectionId) {
            setVoters([])
            setError('')
            setLoading(false)
            setPage(1)
            return
        }

        async function loadVoters() {
            setLoading(true)
            setError('')
            setVoters([])
            setPage(1)

            try {
                const result = await GetElectionVoters(selectedElectionId)

                if (!cancelled) {
                    setVoters(result)
                }
            } catch (err) {
                if (!cancelled) {
                    setError(
                        err.message || 'Failed to load election voters.'
                    )
                }
            } finally {
                if (!cancelled) {
                    setLoading(false)
                }
            }
        }

        loadVoters()

        return () => {
            cancelled = true
        }

    }, [elections, selectedElectionId, retryCount])

    const filteredVoters = useMemo(() => {
        const query = search.trim().toLowerCase()

        if (!query) return voters

        return voters.filter(voter =>
            String(voter.voter_id ?? voter.voterId ?? '')
                .toLowerCase()
                .includes(query)
        )
    }, [voters, search])

    const totalPages = Math.max(
        1,
        Math.ceil(filteredVoters.length / PAGE_SIZE)
    )

    const currentPage = Math.min(page, totalPages)

    const pageVoters = filteredVoters.slice(
        (currentPage - 1) * PAGE_SIZE,
        currentPage * PAGE_SIZE
    )

    const votedCount = voters.filter(
        voter => Boolean(voter.voted ?? voter.has_voted ?? voter.hasVoted)
    ).length

    const notVotedCount = voters.length - votedCount

    function formatId(value) {
        return value ?? '—'
    }

    
return (
    <div className="admin-layout">
        <Sidebar />

        <main className="admin-content">
            <header className="topbar">
                <div>
                    <div className="text-secondary small">
                        Workspace / Voter register
                    </div>
                    <h1 className="h4 mb-0 mt-1">Election voters</h1>
                </div>

                <div className="avatar avatar-sm">AD</div>
            </header>

            <div className="content-wrap">
                {/* Page heading */}
                <div className="mb-4">
                    <p className="text-secondary small fw-semibold mb-1">
                        VOTER MANAGEMENT
                    </p>
                    <h2 className="h3 fw-bold mb-1">Voter register</h2>
                    <p className="text-secondary mb-0">
                        View voter participation in a selected election.
                    </p>
                </div>

                {/* Election selector */}
                <div className="mb-4">
                    <label
                        htmlFor="electionSelect"
                        className="form-label fw-semibold"
                    >
                        Select election
                    </label>

                    <select
                        id="electionSelect"
                        className="form-select"
                        value={selectedElectionId}
                        disabled={loadingElections}
                        onChange={(event) => {
                            setSelectedElectionId(event.target.value);
                            setSearch("");
                            setPage(1);
                        }}
                    >
                        <option value="">Select an election</option>

                        {Object.entries(
                            elections.reduce((groups, election) => {
                                const type = election.election_type || "Other";

                                if (!groups[type]) {
                                    groups[type] = [];
                                }

                                groups[type].push(election);
                                return groups;
                            }, {})
                        ).map(([type, groupedElections]) => (
                            <optgroup key={type} label={type}>
                                {groupedElections.map((election) => (
                                    <option
                                        key={election.election_id}
                                        value={election.election_id}
                                    >
                                        {election.election_name}
                                    </option>
                                ))}
                            </optgroup>
                        ))}
                    </select>

                    {loadingElections && (
                        <div className="text-secondary small mt-2">
                            Loading elections...
                        </div>
                    )}

                    {electionError && (
                        <div className="alert alert-danger mt-2 mb-0">
                            <div>{electionError}</div>
                            <button
                                type="button"
                                className="btn btn-sm btn-outline-danger mt-2"
                                onClick={() => setRetryCount((count) => count + 1)}
                            >
                                Retry loading elections
                            </button>
                        </div>
                    )}
                </div>

                {/* Statistics */}
                <div className="row g-3 mb-4">
                    <div className="col-md-4">
                        <div className="stat-card compact">
                            <div className="stat-label">Total records</div>
                            <div className="stat-number">
                                {voters.length}
                            </div>
                        </div>
                    </div>

                    <div className="col-md-4">
                        <div className="stat-card compact">
                            <div className="stat-label">Voted</div>
                            <div className="stat-number">{votedCount}</div>
                        </div>
                    </div>

                    <div className="col-md-4">
                        <div className="stat-card compact">
                            <div className="stat-label">Not yet voted</div>
                            <div className="stat-number">{notVotedCount}</div>
                        </div>
                    </div>
                </div>

                {/* Voter table */}
                <section className="panel">
                    <div className="panel-heading flex-wrap">
                        <div>
                            <h3 className="h6 fw-bold mb-1">
                                All voter records
                            </h3>
                            <p className="small text-secondary mb-0">
                                {filteredVoters.length} matching records
                            </p>
                        </div>

                        <input
                            className="form-control form-control-sm search-input"
                            type="search"
                            placeholder="Search by voter ID..."
                            aria-label="Search voters"
                            value={search}
                            disabled={!selectedElectionId || loading}
                            onChange={(event) => {
                                setSearch(event.target.value);
                                setPage(1);
                            }}
                        />
                    </div>

                    {loading && (
                        <div className="p-4 text-center">
                            <div
                                className="spinner-border spinner-border-sm me-2"
                                role="status"
                                aria-label="Loading"
                            />
                            Loading election voters...
                        </div>
                    )}

                    {!loading && error && (
                        <div className="alert alert-danger m-3" role="alert">
                            {error}
                            <button
                                className="btn btn-sm btn-outline-danger ms-3"
                                onClick={() => setRetryCount((count) => count + 1)}
                                type="button"
                            >
                                Try again
                            </button>
                        </div>
                    )}

                    {!loading && !error && !selectedElectionId && (
                        <div className="p-4 text-center text-secondary">
                            Select an election to view its voters.
                        </div>
                    )}

                    {!loading && !error && selectedElectionId && (
                        <>
                            <div className="table-responsive">
                                <table className="table table-hover align-middle mb-0">
                                    <thead>
                                        <tr>
                                            <th>Voter ID</th>
                                            <th>Election ID</th>
                                            <th>Participation</th>
                                        </tr>
                                    </thead>

                                    <tbody>
                                        {pageVoters.map((voter) => {
                                            const hasVoted = Boolean(
                                                voter.voted ??
                                                voter.has_voted ??
                                                voter.hasVoted
                                            );

                                            const voterId =
                                                voter.voter_id ?? voter.voterId;

                                            const electionId =
                                                voter.election_id ??
                                                voter.electionId ??
                                                selectedElectionId;

                                            return (
                                                <tr
                                                    key={`${electionId}-${voterId}`}
                                                >
                                                    <td className="font-monospace small">
                                                        {formatId(voterId)}
                                                    </td>

                                                    <td className="font-monospace small">
                                                        {formatId(electionId)}
                                                    </td>

                                                    <td>
                                                        <span
                                                            className={`pill ${
                                                                hasVoted
                                                                    ? "pill-success"
                                                                    : "pill-neutral"
                                                            }`}
                                                        >
                                                            {hasVoted
                                                                ? "Voted"
                                                                : "Not yet voted"}
                                                        </span>
                                                    </td>
                                                </tr>
                                            );
                                        })}

                                        {pageVoters.length === 0 && (
                                            <tr>
                                                <td
                                                    colSpan={3}
                                                    className="text-center text-secondary p-4"
                                                >
                                                    No voter records found.
                                                </td>
                                            </tr>
                                        )}
                                    </tbody>
                                </table>
                            </div>

                            {/* Pagination */}
                            <div className="panel-footer d-flex flex-wrap justify-content-between align-items-center gap-2 p-3 border-top">
                                <span className="small text-secondary">
                                    Showing{" "}
                                    {filteredVoters.length === 0
                                        ? 0
                                        : (currentPage - 1) * PAGE_SIZE + 1}
                                    {"–"}
                                    {Math.min(
                                        currentPage * PAGE_SIZE,
                                        filteredVoters.length
                                    )}
                                    {" of "}
                                    {filteredVoters.length} records
                                </span>

                                <nav aria-label="Voter table pagination">
                                    <ul className="pagination pagination-sm mb-0">
                                        <li
                                            className={`page-item ${
                                                currentPage <= 1 ? "disabled" : ""
                                            }`}
                                        >
                                            <button
                                                className="page-link"
                                                disabled={currentPage <= 1}
                                                onClick={() =>
                                                    setPage((page) =>
                                                        Math.max(1, page - 1)
                                                    )
                                                }
                                                type="button"
                                            >
                                                Previous
                                            </button>
                                        </li>

                                        <li className="page-item active">
                                            <span className="page-link">
                                                {currentPage} / {totalPages}
                                            </span>
                                        </li>

                                        <li
                                            className={`page-item ${
                                                currentPage >= totalPages
                                                    ? "disabled"
                                                    : ""
                                            }`}
                                        >
                                            <button
                                                className="page-link"
                                                disabled={
                                                    currentPage >= totalPages
                                                }
                                                onClick={() =>
                                                    setPage((page) =>
                                                        Math.min(
                                                            totalPages,
                                                            page + 1
                                                        )
                                                    )
                                                }
                                                type="button"
                                            >
                                                Next
                                            </button>
                                        </li>
                                    </ul>
                                </nav>
                            </div>
                        </>
                    )}
                </section>

                <footer className="page-footer">
                    <span>BallotAdmin · Voter management</span>
                    <span>Voter participation by election</span>
                </footer>
            </div>
        </main>
    </div>
)}