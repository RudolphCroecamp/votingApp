
import { useCallback, useEffect, useMemo, useState } from "react";
import Sidebar from "../components/sidebar";
import "../styles/ElectionResults.css";

const ADMIN_API = import.meta.env.VITE_ADMIN_API_URL ?? "http://localhost:5000/api/Admin";

function getToken() {
    return sessionStorage.getItem("JWTToken");
}

function getHeaders() {
    const token = getToken();

    return {
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };
}

function getElectionName(election) {
    return (
        election.election_name ??
        election.name ??
        election.title ??
        "Untitled election"
    );
}

export default function ElectionResults() {
    const [elections, setElections] = useState([]);
    const [electionId, setElectionId] = useState("");
    const [results, setResults] = useState([]);

    const [loadingElections, setLoadingElections] = useState(true);
    const [loadingResults, setLoadingResults] = useState(false);
    const [error, setError] = useState("");
    const [notice, setNotice] = useState("");

    const [blockchainStatus, setBlockchainStatus] = useState("");
    const [checkingBlockchain, setCheckingBlockchain] = useState(false);
    const [blockchainValid, setBlockchainValid] = useState(null);
    const [blockchainError, setBlockchainError] = useState("");

    const selectedElection = elections.find(
        (election) => election.election_id === electionId
    );

    const formatNumber = (value) =>
        new Intl.NumberFormat("en-ZA").format(value);

    const formatPercentage = (party) => {
        const percentage = Number(party.percentage);

        if (party.percentage != null && Number.isFinite(percentage)) {
            return `${percentage.toFixed(2)}%`;
        }

        const total = results.reduce(
            (sum, item) => sum + Number(item.votes || 0),
            0
        );

        return total > 0
            ? `${((Number(party.votes || 0) / total) * 100).toFixed(2)}%`
            : "0.00%";
    };


    async function checkBlockchain() {
        try {
            setCheckingBlockchain(true);
            setBlockchainValid(null);
            setBlockchainError("");

            const response = await fetch(
                `${ADMIN_API}/isBlockchainValid`,
                {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${sessionStorage.getItem("JWTToken")}`
                    }
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message || "Blockchain verification failed."
                );
            }

            setBlockchainValid(data.blockchain);
        } catch (error) {
            setBlockchainError(
                error.message || "Unable to verify the blockchain."
            );
        } finally {
            setCheckingBlockchain(false);
        }
    }

    // Load the available elections.
    useEffect(() => {
        let cancelled = false;

        async function loadElections() {
            setLoadingElections(true);
            setError("");

            try {
                const response = await fetch(
                    `${ADMIN_API}/get_elections`,
                    { headers: getHeaders() }
                );

                const data = await response.json();

                if (!response.ok) {
                    throw new Error(
                        data.message || "Failed to load elections."
                    );
                }

                const list = Array.isArray(data.elections)
                    ? data.elections
                    : [];

                if (!cancelled) {
                    setElections(list);
                }
            } catch (err) {
                if (!cancelled) {
                    setError(err.message || "Failed to load elections.");
                }
            } finally {
                if (!cancelled) {
                    setLoadingElections(false);
                }
            }
        }

        loadElections();

        return () => {
            cancelled = true;
        };
    }, []);

    // Load the selected election's results.
    const loadResults = useCallback(async (id) => {
        if (!id) {
            setResults([]);
            return;
        }

        setLoadingResults(true);
        setError("");
        setNotice("");
        setResults([]);

        try {
            const response = await fetch(
                `${ADMIN_API}/GetElectionResults/${encodeURIComponent(id)}`,
                { headers: getHeaders() }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message || "Failed to load election results."
                );
            }

            if (!Array.isArray(data.results)) {
                throw new Error(
                    "The server returned an unexpected results format."
                );
            }

            const sortedResults = [...data.results].sort(
                (a, b) => Number(b.votes || 0) - Number(a.votes || 0)
            );

            setResults(sortedResults);
            setNotice(data.message || "Results loaded successfully.");
        } catch (err) {
            setError(err.message || "Failed to load results.");
        } finally {
            setLoadingResults(false);
        }
    }, []);

    function handleElectionChange(event) {
        const id = event.target.value;
        setElectionId(id);
        setResults([]);
        setNotice("");
        setError("");

        if (id) {
            loadResults(id);
        }
    }

    const totalVotes = useMemo(
        () =>
            results.reduce(
                (total, party) => total + Number(party.votes || 0),
                0
            ),
        [results]
    );

    const highestVotes = results.length
        ? Math.max(...results.map((party) => Number(party.votes || 0)))
        : 0;

    // A tie produces multiple winners.
    const winners =
        highestVotes > 0
            ? results.filter(
                (party) => Number(party.votes || 0) === highestVotes
            )
            : [];

    const hasResults = results.length > 0;

    return (
        <div className="admin-layout">
            <Sidebar />

            <main className="admin-main election-results-page">
                <div className="election-results-container">
                    <header className="page-header">
                        <div className="blockchain-check">
                            <div className="blockchain-check-info">
                                <h3>Blockchain Integrity</h3>
                                <p>Verify the integrity of the election blockchain.</p>
                            </div>

                            <button
                                type="button"
                                onClick={checkBlockchain}
                                disabled={checkingBlockchain}
                            >
                                {checkingBlockchain ? "Checking..." : "Check Blockchain"}
                            </button>

                            {blockchainValid !== null && (
                                <div
                                    className={`blockchain-status ${blockchainValid ? "valid" : "invalid"
                                        }`}
                                    role="status"
                                >
                                    <span className="blockchain-status-icon">
                                        {blockchainValid ? "✓" : "!"}
                                    </span>

                                    <div>
                                        <strong>
                                            {blockchainValid
                                                ? "Blockchain Valid"
                                                : "Blockchain Invalid"}
                                        </strong>
                                        <p>
                                            {blockchainValid
                                                ? "The blockchain integrity check passed."
                                                : "The blockchain integrity check failed. Investigate before relying on these results."}
                                        </p>
                                    </div>
                                </div>
                            )}

                            {blockchainError && (
                                <div className="blockchain-status invalid" role="alert">
                                    <span className="blockchain-status-icon">!</span>
                                    <div>
                                        <strong>Verification Error</strong>
                                        <p>{blockchainError}</p>
                                    </div>
                                </div>
                            )}
                        </div>
                        <div>
                            <p className="page-eyebrow">ADMINISTRATION</p>
                            <h1>Election Results</h1>
                            <p className="page-description">
                                View election outcomes, party performance,
                                and vote distribution.
                            </p>
                        </div>
                    </header>

                    <section className="results-filter-card">
                        <div className="filter-heading">
                            <div>
                                <h2>Select Election</h2>
                                <p>
                                    Choose an election to view its results.
                                </p>
                            </div>
                        </div>

                        <label htmlFor="election-select">
                            Election
                        </label>

                        <select
                            id="election-select"
                            value={electionId}
                            onChange={handleElectionChange}
                            disabled={loadingElections}
                        >
                            <option value="">
                                {loadingElections
                                    ? "Loading elections..."
                                    : "Select an election"}
                            </option>

                            {elections.map((election) => (
                                <option
                                    key={election.election_id}
                                    value={election.election_id}
                                >
                                    {getElectionName(election)}
                                </option>
                            ))}
                        </select>

                        {!loadingElections && elections.length === 0 && (
                            <p className="results-muted">
                                No elections are available.
                            </p>
                        )}

                        {selectedElection && (
                            <div className="selected-election-info">
                                <span className="info-label">
                                    Election ID
                                </span>
                                <span className="election-id">
                                    {selectedElection.election_id}
                                </span>
                            </div>
                        )}
                    </section>

                    {error && (
                        <div className="results-alert error" role="alert">
                            {error}
                        </div>
                    )}

                    {notice && !error && hasResults && (
                        <div className="results-alert success" role="status">
                            {notice}
                        </div>
                    )}

                    {loadingResults && (
                        <div className="results-loading" role="status">
                            Loading election results...
                        </div>
                    )}

                    {!loadingResults && electionId && !error && (
                        <>
                            {hasResults ? (
                                <>
                                    <section className="results-summary">
                                        <article className="summary-card">
                                            <div className="summary-icon votes-icon">
                                                <span>V</span>
                                            </div>
                                            <div>
                                                <p>Total Votes</p>
                                                <h2>
                                                    {formatNumber(totalVotes)}
                                                </h2>
                                            </div>
                                        </article>

                                        <article className="summary-card">
                                            <div className="summary-icon parties-icon">
                                                <span>P</span>
                                            </div>
                                            <div>
                                                <p>Participating Parties</p>
                                                <h2>
                                                    {formatNumber(results.length)}
                                                </h2>
                                            </div>
                                        </article>

                                        <article className="summary-card">
                                            <div className="summary-icon winner-icon">
                                                <span>★</span>
                                            </div>
                                            <div>
                                                <p>
                                                    {winners.length > 1
                                                        ? "Joint Winners"
                                                        : "Winning Party"}
                                                </p>
                                                <h2 className="winner-name">
                                                    {winners.length > 0
                                                        ? winners
                                                            .map(
                                                                (party) =>
                                                                    party.party_name
                                                            )
                                                            .join(", ")
                                                        : "No votes recorded"}
                                                </h2>
                                            </div>
                                        </article>
                                    </section>

                                    <section className="results-section">
                                        <div className="section-heading">
                                            <div>
                                                <h2>Vote Distribution</h2>
                                                <p>
                                                    Comparison of votes received
                                                    by each party.
                                                </p>
                                            </div>
                                        </div>

                                        <div className="distribution-list">
                                            {results.map((party) => {
                                                const isWinner =
                                                    winners.includes(party);

                                                const percentage =
                                                    Number(party.percentage);

                                                const barWidth =
                                                    party.percentage != null &&
                                                        Number.isFinite(percentage)
                                                        ? Math.min(
                                                            100,
                                                            Math.max(
                                                                0,
                                                                percentage
                                                            )
                                                        )
                                                        : totalVotes > 0
                                                            ? (Number(
                                                                party.votes || 0
                                                            ) /
                                                                totalVotes) *
                                                            100
                                                            : 0;

                                                return (
                                                    <div
                                                        className="distribution-item"
                                                        key={party.party_id}
                                                    >
                                                        <div className="distribution-labels">
                                                            <div className="party-label">
                                                                <span className="party-name">
                                                                    {party.party_name}
                                                                </span>
                                                                {isWinner && (
                                                                    <span className="winner-badge">
                                                                        {winners.length >
                                                                            1
                                                                            ? "Joint winner"
                                                                            : "Winner"}
                                                                    </span>
                                                                )}
                                                            </div>

                                                            <div className="party-stats">
                                                                <strong>
                                                                    {formatNumber(
                                                                        Number(
                                                                            party.votes ||
                                                                            0
                                                                        )
                                                                    )}
                                                                </strong>
                                                                <span>
                                                                    {formatPercentage(
                                                                        party
                                                                    )}
                                                                </span>
                                                            </div>
                                                        </div>

                                                        <div
                                                            className="progress-track"
                                                            role="progressbar"
                                                            aria-label={`${party.party_name} vote share`}
                                                            aria-valuenow={barWidth}
                                                            aria-valuemin={0}
                                                            aria-valuemax={100}
                                                        >
                                                            <div
                                                                className={`progress-fill ${isWinner
                                                                    ? "winner-fill"
                                                                    : ""
                                                                    }`}
                                                                style={{
                                                                    width: `${barWidth}%`,
                                                                }}
                                                            />
                                                        </div>
                                                    </div>
                                                );
                                            })}
                                        </div>
                                    </section>

                                    <section className="results-section">
                                        <div className="section-heading">
                                            <div>
                                                <h2>Detailed Results</h2>
                                                <p>
                                                    Parties ranked by votes
                                                    received.
                                                </p>
                                            </div>
                                        </div>

                                        <div className="results-table-wrapper">
                                            <table className="results-table">
                                                <thead>
                                                    <tr>
                                                        <th scope="col">
                                                            #
                                                        </th>
                                                        <th scope="col">
                                                            Party
                                                        </th>
                                                        <th
                                                            scope="col"
                                                            className="numeric-cell"
                                                        >
                                                            Votes
                                                        </th>
                                                        <th
                                                            scope="col"
                                                            className="numeric-cell"
                                                        >
                                                            Percentage
                                                        </th>
                                                        <th scope="col">
                                                            Outcome
                                                        </th>
                                                    </tr>
                                                </thead>

                                                <tbody>
                                                    {results.map(
                                                        (party, index) => {
                                                            const isWinner =
                                                                winners.includes(
                                                                    party
                                                                );

                                                            return (
                                                                <tr
                                                                    key={
                                                                        party.party_id
                                                                    }
                                                                >
                                                                    <td>
                                                                        {index +
                                                                            1}
                                                                    </td>
                                                                    <td className="table-party-name">
                                                                        {
                                                                            party.party_name
                                                                        }
                                                                    </td>
                                                                    <td className="numeric-cell">
                                                                        {formatNumber(
                                                                            Number(
                                                                                party.votes ||
                                                                                0
                                                                            )
                                                                        )}
                                                                    </td>
                                                                    <td className="numeric-cell">
                                                                        {formatPercentage(
                                                                            party
                                                                        )}
                                                                    </td>
                                                                    <td>
                                                                        {isWinner ? (
                                                                            <span className="winner-badge">
                                                                                {winners.length >
                                                                                    1
                                                                                    ? "Joint winner"
                                                                                    : "Winner"}
                                                                            </span>
                                                                        ) : (
                                                                            <span className="outcome-muted">
                                                                                —
                                                                            </span>
                                                                        )}
                                                                    </td>
                                                                </tr>
                                                            );
                                                        }
                                                    )}
                                                </tbody>

                                                <tfoot>
                                                    <tr>
                                                        <th
                                                            colSpan={2}
                                                            scope="row"
                                                        >
                                                            Total
                                                        </th>
                                                        <th className="numeric-cell">
                                                            {formatNumber(
                                                                totalVotes
                                                            )}
                                                        </th>
                                                        <th className="numeric-cell">
                                                            {totalVotes > 0
                                                                ? "100.00%"
                                                                : "0.00%"}
                                                        </th>
                                                        <th />
                                                    </tr>
                                                </tfoot>
                                            </table>
                                        </div>
                                    </section>
                                </>
                            ) : (
                                <div className="results-empty">
                                    <h2>No Results Available</h2>
                                    <p>
                                        No party results were returned for this
                                        election. There may not be any recorded
                                        votes yet.
                                    </p>
                                </div>
                            )}
                        </>
                    )}
                </div>
            </main>
        </div>
    );
}
