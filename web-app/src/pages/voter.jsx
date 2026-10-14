
import { useCallback, useEffect, useMemo, useState } from "react";
import Sidebar from "../components/sidebar";
import "../styles/voter.css";

const API_BASE = "http://localhost:5000/api/Admin";
const PAGE_SIZE = 10;

export default function VoterDirectory() {
    const [voters, setVoters] = useState([]);
    const [page, setPage] = useState(1);
    const [totalVoters, setTotalVoters] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [search, setSearch] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [updatingId, setUpdatingId] = useState(null);

    const fetchVoters = useCallback(async () => {
        setLoading(true);
        setError("");

        try {
            const token = sessionStorage.getItem("JWTToken");

            if (!token) {
                throw new Error("Your session has expired. Please log in again.");
            }

            const response = await fetch(
                `${API_BASE}/GetAllVoters?page=${page}&limit=${PAGE_SIZE}`,
                {
                    method: "GET",
                    headers: {
                        Accept: "application/json",
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    throw new Error(
                        "You are not authorised to view voter records."
                    );
                }

                throw new Error(
                    `Failed to load voters (HTTP ${response.status}).`
                );
            }

            const data = await response.json();
            const result = data.voters;

            const voterList = Array.isArray(result?.voters)
                ? result.voters
                : [];

            const count = Number(result?.voterCount ?? voterList.length);

            setVoters(voterList);
            setTotalVoters(count);
            setTotalPages(Math.ceil(count / PAGE_SIZE));
        } catch (err) {
            setError(err.message || "An unexpected error occurred.");
            setVoters([]);
            setTotalVoters(0);
            setTotalPages(0);
        } finally {
            setLoading(false);
        }
    }, [page]);

    useEffect(() => {
        fetchVoters();
    }, [fetchVoters]);

    const filteredVoters = useMemo(() => {
        const term = search.trim().toLowerCase();

        if (!term) return voters;

        return voters.filter((voter) =>
            [
                voter.voter_id,
                voter.firstname,
                voter.lastname,
                voter.province,
                voter.city,
                voter.street,
                voter.postal,
            ].some((value) =>
                String(value ?? "").toLowerCase().includes(term)
            )
        );
    }, [voters, search]);

    async function toggleVoterAccess(voter) {
        const currentlyAllowed = voter.allowed;

        if (typeof currentlyAllowed !== "boolean") {
            setError(
                "The voter response does not contain a valid allowed status."
            );
            return;
        }

        const newStatus = !currentlyAllowed;
        const action = newStatus ? "restore" : "revoke";

        const confirmed = window.confirm(
            `Are you sure you want to ${action} access for ` +
            `${voter.firstname} ${voter.lastname}?`
        );

        if (!confirmed) return;

        setUpdatingId(voter.voter_id);
        setError("");
        setSuccess("");

        try {
            const token = sessionStorage.getItem("JWTToken");

            if (!token) {
                throw new Error("Your session has expired. Please log in again.");
            }

            const response = await fetch(
                `${API_BASE}/Update_voter_access`,
                {
                    method: "PATCH",
                    headers: {
                        Accept: "application/json",
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`,
                    },
                    body: JSON.stringify({
                        ID: voter.voter_id,
                        status: newStatus,
                    }),
                }
            );

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    throw new Error(
                        "You are not authorised to change voter access."
                    );
                }

                const message = await response.text();

                throw new Error(
                    message || `Failed to update access (HTTP ${response.status}).`
                );
            }

            setSuccess(
                `Access ${newStatus ? "restored" : "revoked"} for ` +
                `${voter.firstname} ${voter.lastname}.`
            );

            // Reload the current page to retrieve the updated status.
            await fetchVoters();
        } catch (err) {
            setError(err.message || "Could not update voter access.");
        } finally {
            setUpdatingId(null);
        }
    }

    function changePage(nextPage) {
        if (nextPage < 1 || nextPage > totalPages) return;

        setPage(nextPage);
        setSearch("");
        setSuccess("");
    }

    return (
        <div className="admin-layout">
            <Sidebar />

            <main className="admin-content">
                <div className="voter-directory">
                    <header className="voter-page-header">
                        <div>
                            <span className="voter-eyebrow">
                                VOTER MANAGEMENT
                            </span>
                            <h1>Voter Directory</h1>
                            <p>
                                View registered voters and manage account access.
                            </p>
                        </div>

                        <button
                            type="button"
                            className="voter-refresh-btn"
                            onClick={fetchVoters}
                            disabled={loading || updatingId !== null}
                        >
                            {loading ? "Loading..." : "↻ Refresh"}
                        </button>
                    </header>

                    <section className="voter-summary">
                        <div className="voter-summary-icon">♙</div>
                        <div>
                            <p>Total registered voters</p>
                            <h2>{totalVoters.toLocaleString()}</h2>
                        </div>
                    </section>

                    <section className="voter-table-panel">
                        <div className="voter-table-heading">
                            <div>
                                <h2>Registered voters</h2>
                                <p>
                                    {loading
                                        ? "Loading voter records..."
                                        : `Page ${page} of ${Math.max(totalPages, 1)}`}
                                </p>
                            </div>

                            <input
                                type="search"
                                className="voter-search"
                                placeholder="Search this page..."
                                aria-label="Search voters on this page"
                                value={search}
                                onChange={(event) =>
                                    setSearch(event.target.value)
                                }
                                disabled={loading || voters.length === 0}
                            />
                        </div>

                        {error && (
                            <div className="voter-error" role="alert">
                                <span>{error}</span>
                                <button
                                    type="button"
                                    onClick={fetchVoters}
                                    disabled={loading}
                                >
                                    Try again
                                </button>
                            </div>
                        )}

                        {success && (
                            <div className="alert alert-success mx-3" role="status">
                                {success}
                            </div>
                        )}

                        <div className="voter-table-scroll">
                            <table className="voter-table">
                                <thead>
                                    <tr>
                                        <th>Voter ID</th>
                                        <th>First name</th>
                                        <th>Last name</th>
                                        <th>Province</th>
                                        <th>City</th>
                                        <th>Street</th>
                                        <th>Postal code</th>
                                        <th>Access status</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {loading ? (
                                        <tr>
                                            <td colSpan="9" className="voter-empty">
                                                Loading voters...
                                            </td>
                                        </tr>
                                    ) : error && voters.length === 0 ? (
                                        <tr>
                                            <td colSpan="9" className="voter-empty">
                                                Unable to load voter records.
                                            </td>
                                        </tr>
                                    ) : filteredVoters.length === 0 ? (
                                        <tr>
                                            <td colSpan="9" className="voter-empty">
                                                {search
                                                    ? "No voters match your search on this page."
                                                    : "No voter records found."}
                                            </td>
                                        </tr>
                                    ) : (
                                        filteredVoters.map((voter) => {
                                            const isAllowed =
                                                typeof voter.allowed === "boolean"
                                                    ? voter.allowed
                                                    : null;

                                            const isUpdating =
                                                updatingId === voter.voter_id;

                                            return (
                                                <tr key={voter.voter_id}>
                                                    <td>
                                                        <span className="voter-id">
                                                            {voter.voter_id}
                                                        </span>
                                                    </td>

                                                    <td className="voter-name">
                                                        {voter.firstname}
                                                    </td>

                                                    <td className="voter-name">
                                                        {voter.lastname}
                                                    </td>

                                                    <td>{voter.province}</td>
                                                    <td>{voter.city}</td>
                                                    <td>{voter.street || "—"}</td>
                                                    <td>{voter.postal}</td>

                                                    <td>
                                                        <span
                                                            className={`pill ${isAllowed === true
                                                                    ? "pill-success"
                                                                    : isAllowed === false
                                                                        ? "pill-neutral"
                                                                        : "pill-warning"
                                                                }`}
                                                        >
                                                            {isAllowed === true
                                                                ? "Allowed"
                                                                : isAllowed === false
                                                                    ? "Revoked"
                                                                    : "Unknown"}
                                                        </span>
                                                    </td>

                                                    <td>
                                                        <button
                                                            type="button"
                                                            className={`btn btn-sm ${isAllowed
                                                                    ? "btn-outline-danger"
                                                                    : "btn-outline-success"
                                                                }`}
                                                            disabled={
                                                                isUpdating ||
                                                                isAllowed === null ||
                                                                updatingId !== null
                                                            }
                                                            onClick={() =>
                                                                toggleVoterAccess(voter)
                                                            }
                                                        >
                                                            {isUpdating
                                                                ? "Updating..."
                                                                : isAllowed
                                                                    ? "Revoke access"
                                                                    : "Restore access"}
                                                        </button>
                                                    </td>
                                                </tr>
                                            );
                                        })
                                    )}
                                </tbody>
                            </table>
                        </div>

                        <footer className="voter-pagination">
                            <span>
                                Showing {filteredVoters.length} of {voters.length}{" "}
                                voters on this page
                            </span>

                            <div className="voter-pagination-controls">
                                <button
                                    type="button"
                                    onClick={() => changePage(page - 1)}
                                    disabled={loading || page <= 1}
                                >
                                    ← Previous
                                </button>

                                <span className="voter-page-number">
                                    {page}
                                </span>

                                <button
                                    type="button"
                                    onClick={() => changePage(page + 1)}
                                    disabled={
                                        loading ||
                                        totalPages === 0 ||
                                        page >= totalPages
                                    }
                                >
                                    Next →
                                </button>
                            </div>
                        </footer>
                    </section>

                    <footer className="page-footer">
                        <span>BallotAdmin · Voter Management</span>
                        <span>Secure access for authorised personal only</span>
                    </footer>
                </div>
            </main>
        </div>
    );
}
