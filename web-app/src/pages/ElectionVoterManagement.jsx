
import { useCallback, useEffect, useMemo, useState } from "react";
import Sidebar from "../components/sidebar";
import "../styles/election-voter-management.css";

const API_BASE = "http://localhost:5000/api/Admin";

const ENDPOINTS = {
    elections: `${API_BASE}/get_elections`,
    electionVoters: `${API_BASE}/GetElectionVoters`,
    updateStatus: `${API_BASE}/AssignVoterToElection`,
    voterById: `${API_BASE}/GetVoterByID`,
};

const STATUS = {
    PENDING: 0,
    APPROVED: 1,
    REJECTED: 2,
};

function getStatus(value) {
    if (typeof value === "number") return value;

    switch (String(value ?? "").toLowerCase()) {
        case "pending":
            return STATUS.PENDING;
        case "approved":
            return STATUS.APPROVED;
        case "rejected":
            return STATUS.REJECTED;
        default:
            return Number(value);
    }
}

function statusLabel(value) {
    switch (getStatus(value)) {
        case STATUS.PENDING:
            return "Pending";
        case STATUS.APPROVED:
            return "Approved";
        case STATUS.REJECTED:
            return "Rejected";
        default:
            return "Unknown";
    }
}

function readArray(data, ...keys) {
    if (Array.isArray(data)) return data;

    for (const key of keys) {
        if (Array.isArray(data?.[key])) return data[key];
    }

    return [];
}

function voterDetails(record) {
    // Supports either a nested Voter object or a flattened DTO.
    const voter = record?.voter ?? record?.Voter ?? record;

    return {
        id: voter?.voter_id ?? record?.voter_id ?? "",
        firstname: voter?.firstname ?? record?.firstname ?? "",
        lastname: voter?.lastname ?? record?.lastname ?? "",
        province: voter?.province ?? record?.province ?? "",
        city: voter?.city ?? record?.city ?? "",
        street: voter?.street ?? record?.street ?? "",
        postal: voter?.postal ?? record?.postal ?? "",
        allowed: voter?.allowed ?? record?.allowed,
        role: voter?.role ?? record?.role ?? "Voter",
        created_at: voter?.created_at ?? record?.created_at,
    };
}

function formatDate(value) {
    if (!value) return "—";

    const date = new Date(value);
    return Number.isNaN(date.getTime())
        ? "—"
        : date.toLocaleDateString();
}

async function apiRequest(url, options = {}) {
    const token = sessionStorage.getItem("JWTToken");

    if (!token) {
        throw new Error("Your session has expired. Please log in again.");
    }

    const response = await fetch(url, {
        ...options,
        headers: {
            Accept: "application/json",
            ...(options.body ? { "Content-Type": "application/json" } : {}),
            Authorization: `Bearer ${token}`,
            ...options.headers,
        },
    });

    const text = await response.text();
    let data = null;

    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            data = { message: text };
        }
    }

    if (!response.ok) {
        throw new Error(
            data?.message ??
            data?.title ??
            `Request failed (HTTP ${response.status}).`
        );
    }

    console.log(data);

    return data;
}






export default function ElectionVoterManagement() {
    const [elections, setElections] = useState([]);
    const [selectedElectionId, setSelectedElectionId] = useState("");
    const [records, setRecords] = useState([]);
    const [search, setSearch] = useState("");
    const [selectedVoter, setSelectedVoter] = useState(null);

    const [loadingElections, setLoadingElections] = useState(true);
    const [loadingRecords, setLoadingRecords] = useState(false);
    const [updatingKey, setUpdatingKey] = useState("");
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    const loadElections = useCallback(async () => {
        setLoadingElections(true);
        setError("");

        try {
            const data = await apiRequest(ENDPOINTS.elections);
            const list = readArray(data, "elections", "data", "results");

            setElections(list);

            setSelectedElectionId((current) => {
                if (current && list.some(
                    (e) => e.election_id === current
                )) {
                    return current;
                }

                return list[0]?.election_id ?? "";
            });
        } catch (err) {
            setError(err.message || "Could not load elections.");
        } finally {
            setLoadingElections(false);
        }
    }, []);

    const loadElectionVoters = useCallback(async () => {
        if (!selectedElectionId) {
            setRecords([]);
            return;
        }

        setLoadingRecords(true);
        setError("");
        setSuccess("");

        try {
            const url = new URL(ENDPOINTS.electionVoters);
            url.searchParams.set("election_id", selectedElectionId);
            url.searchParams.set("limit", "10");
            url.searchParams.set("skip", "0");

            const data = await apiRequest(url.toString());
            const list = readArray(
                data,
                "electionVoters",
                "election_voters",
                "records",
                "data"
            );

            setRecords(list);
        } catch (err) {
            setRecords([]);
            setError(err.message || "Could not load election voters.");
        } finally {
            setLoadingRecords(false);
        }
    }, [selectedElectionId]);

    useEffect(() => {
        loadElections();
    }, [loadElections]);

    useEffect(() => {
        loadElectionVoters();
    }, [loadElectionVoters]);

    const groupedElections = useMemo(() => {
        return elections.reduce((groups, election) => {
            const type = election.election_type || "Other";
            if (!groups[type]) groups[type] = [];
            groups[type].push(election);
            return groups;
        }, {});
    }, [elections]);

    const filteredRecords = useMemo(() => {
        const term = search.trim().toLowerCase();

        if (!term) return records;

        return records.filter((record) => {
            const voter = voterDetails(record);
            const values = [
                voter.id,
                voter.firstname,
                voter.lastname,
                voter.province,
                voter.city,
                voter.postal,
                statusLabel(record.status),
            ];

            return values.some((value) =>
                String(value ?? "").toLowerCase().includes(term)
            );
        });
    }, [records, search]);

    const counts = useMemo(() => ({
        total: records.length,
        pending: records.filter(
            (r) => getStatus(r.status) === STATUS.PENDING
        ).length,
        approved: records.filter(
            (r) => getStatus(r.status) === STATUS.APPROVED
        ).length,
        rejected: records.filter(
            (r) => getStatus(r.status) === STATUS.REJECTED
        ).length,
    }), [records]);

    async function updateRequest(record, nextStatus) {
        const voter = voterDetails(record);
        const electionId = record.election_id ?? selectedElectionId;
        const voterId = record.voter_id ?? voter.id;
        const key = `${electionId}:${voterId}`;

        if (!electionId || !voterId) {
            setError("The election or voter ID is missing from this record.");
            return;
        }

        const action = statusLabel(nextStatus).toLowerCase();
        const confirmed = window.confirm(
            `Are you sure you want to set ${voter.firstname} ${voter.lastname}'s request to ${action}?`
        );

        if (!confirmed) return;

        setUpdatingKey(key);
        setError("");
        setSuccess("");

        try {
            await apiRequest(ENDPOINTS.updateStatus, {
                method: "PATCH",
                body: JSON.stringify({
                    election_id: electionId,
                    voter_id: voterId,
                    status: nextStatus,
                }),
            });

            setSuccess(
                `Request ${action} successfully for ${voter.firstname} ${voter.lastname}.`
            );

            await loadElectionVoters();
        } catch (err) {
            setError(err.message || "Could not update the request.");
        } finally {
            setUpdatingKey("");
        }
    }

    const [loadingVoterDetails, setLoadingVoterDetails] = useState(false);

    const handleViewVoter = async (record) => {
        try {
            setLoadingVoterDetails(true);
            setSelectedVoter(null);

            const voterId = record.voter_id ?? record.Voter?.voter_id;

            if (!voterId) {
                throw new Error("Voter ID could not be found.");
            }

            const data = await apiRequest(
                `${ENDPOINTS.voterById}/${voterId}`
            );

            if (!data?.voter) {
                throw new Error("Voter details were not returned by the API.");
            }

            // Populate the existing voter details modal with fresh API data.
            setSelectedVoter(data.voter);
        } catch (err) {
            console.error("Failed to fetch voter details:", err);
            setError(err.message || "Failed to load voter details.");
        } finally {
            setLoadingVoterDetails(false);
        }
    };



    const selectedElection = elections.find(
        (e) => e.election_id === selectedElectionId
    );

    return (
        <div className="admin-layout">
            <Sidebar />

            <main className="admin-content">
                <div className="evm-page">
                    <header className="evm-header">
                        <div>
                            <span className="evm-eyebrow">
                                ELECTION ADMINISTRATION
                            </span>
                            <h1>Election Voter Management</h1>
                            <p>
                                Review voter requests and manage eligibility
                                for each election.
                            </p>
                        </div>

                        <button
                            type="button"
                            className="btn btn-outline-primary"
                            onClick={() => {
                                loadElections();
                                loadElectionVoters();
                            }}
                            disabled={loadingElections || loadingRecords}
                        >
                            ↻ Refresh
                        </button>
                    </header>

                    {error && (
                        <div className="alert alert-danger" role="alert">
                            {error}
                            <button
                                type="button"
                                className="evm-alert-close"
                                onClick={() => setError("")}
                                aria-label="Dismiss error"
                            >
                                ×
                            </button>
                        </div>
                    )}

                    {success && (
                        <div className="alert alert-success" role="status">
                            {success}
                            <button
                                type="button"
                                className="evm-alert-close"
                                onClick={() => setSuccess("")}
                                aria-label="Dismiss message"
                            >
                                ×
                            </button>
                        </div>
                    )}

                    <section className="evm-election-panel">
                        <label htmlFor="election-select">
                            Select election
                        </label>

                        <select
                            id="election-select"
                            className="form-select"
                            value={selectedElectionId}
                            onChange={(event) => {
                                setSelectedElectionId(event.target.value);
                                setSearch("");
                                setSelectedVoter(null);
                            }}
                            disabled={loadingElections || elections.length === 0}
                        >
                            <option value="">
                                {loadingElections
                                    ? "Loading elections..."
                                    : elections.length === 0
                                        ? "No elections available"
                                        : "Choose an election"}
                            </option>

                            {Object.entries(groupedElections).map(
                                ([type, items]) => (
                                    <optgroup key={type} label={type}>
                                        {items.map((election) => (
                                            <option
                                                key={election.election_id}
                                                value={election.election_id}
                                            >
                                                {election.election_name}
                                            </option>
                                        ))}
                                    </optgroup>
                                )
                            )}
                        </select>

                        {selectedElection && (
                            <div className="evm-election-meta">
                                <span>
                                    <strong>Type:</strong>{" "}
                                    {selectedElection.election_type}
                                </span>
                                <span>
                                    <strong>Start:</strong>{" "}
                                    {formatDate(selectedElection.start_date)}
                                </span>
                                <span>
                                    <strong>End:</strong>{" "}
                                    {formatDate(selectedElection.end_date)}
                                </span>
                            </div>
                        )}
                    </section>

                    <section className="evm-stats">
                        <div className="evm-stat-card">
                            <span>Total requests</span>
                            <strong>{counts.total}</strong>
                        </div>
                        <div className="evm-stat-card">
                            <span>Pending</span>
                            <strong>{counts.pending}</strong>
                        </div>
                        <div className="evm-stat-card">
                            <span>Approved</span>
                            <strong>{counts.approved}</strong>
                        </div>
                        <div className="evm-stat-card">
                            <span>Rejected</span>
                            <strong>{counts.rejected}</strong>
                        </div>
                    </section>

                    <section className="evm-table-panel">
                        <div className="evm-table-heading">
                            <div>
                                <h2>Election voter requests</h2>
                                <p>
                                    {selectedElection
                                        ? selectedElection.election_name
                                        : "Select an election to view requests"}
                                </p>
                            </div>

                            <input
                                type="search"
                                className="form-control evm-search"
                                placeholder="Search voter or status..."
                                aria-label="Search election voter requests"
                                value={search}
                                onChange={(event) => setSearch(event.target.value)}
                                disabled={!selectedElectionId || loadingRecords}
                            />
                        </div>

                        <div className="evm-table-scroll">
                            <table className="table evm-table align-middle">
                                <thead>
                                    <tr>
                                        <th>Voter</th>
                                        <th>Request status</th>
                                        <th>Voting status</th>
                                        <th>Voter details</th>
                                        <th>Update request</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {loadingRecords ? (
                                        <tr>
                                            <td colSpan="6" className="evm-empty">
                                                Loading election voters...
                                            </td>
                                        </tr>
                                    ) : !selectedElectionId ? (
                                        <tr>
                                            <td colSpan="6" className="evm-empty">
                                                Select an election above.
                                            </td>
                                        </tr>
                                    ) : filteredRecords.length === 0 ? (
                                        <tr>
                                            <td colSpan="6" className="evm-empty">
                                                {search
                                                    ? "No requests match your search."
                                                    : "No voters have requested or been assigned to this election."}
                                            </td>
                                        </tr>
                                    ) : (
                                        filteredRecords.map((record) => {
                                            const voter = voterDetails(record);
                                            const status = getStatus(record.status);
                                            const key = `${record.election_id ?? selectedElectionId}:${record.voter_id ?? voter.id}`;
                                            const updating = updatingKey === key;

                                            return (
                                                <tr key={key}>
                                                    <td>
                                                        <div className="evm-voter-name">
                                                            {voter.firstname} {voter.lastname}
                                                        </div>
                                                        <small className="text-muted">
                                                            {voter.id}
                                                        </small>
                                                    </td>

                                                    <td>
                                                        <span className={`evm-status evm-status-${statusLabel(status).toLowerCase()}`}>
                                                            {statusLabel(status)}
                                                        </span>
                                                    </td>

                                                    <td>
                                                        <span className={`evm-voted ${record.voted ? "evm-voted-yes" : ""}`}>
                                                            {record.voted? "Voted" : "Not voted"}
                                                        </span>
                                                    </td>

                                                    <td>
                                                        <button
                                                            type="button"
                                                            className="btn btn-outline-primary btn-sm"
                                                            onClick={() => handleViewVoter(record)}
                                                            disabled={loadingVoterDetails}
                                                        >
                                                            {loadingVoterDetails ? "Loading..." : "View Voter"}
                                                        </button>
                                                    </td>

                                                    <td>
                                                        <div className="evm-actions">
                                                            <select
                                                                className="form-select form-select-sm"
                                                                aria-label={`Update status for ${voter.firstname} ${voter.lastname}`}
                                                                value={Number.isFinite(status) ? status : STATUS.PENDING}
                                                                disabled={updating || Boolean(record.voted)}
                                                                onChange={(event) =>updateRequest(record, Number(event.target.value))}
                                                            >
                                                                <option value={STATUS.PENDING}>Pending</option>
                                                                <option value={STATUS.APPROVED}>Approve</option>
                                                                <option value={STATUS.REJECTED}>Reject</option>
                                                            </select>

                                                            {updating && (
                                                                <span className="evm-updating">
                                                                    Saving...
                                                                </span>
                                                            )}
                                                        </div>
                                                    </td>
                                                </tr>
                                            );
                                        })
                                    )}
                                </tbody>
                            </table>
                        </div>

                        <footer className="evm-table-footer">
                            Showing {filteredRecords.length} of {records.length} requests
                        </footer>
                    </section>
                </div>
            </main>

            {selectedVoter && (
                <div
                    className="evm-modal-backdrop"
                    role="presentation"
                    onMouseDown={(event) => {
                        if (event.target === event.currentTarget) {
                            setSelectedVoter(null);
                        }
                    }}
                >
                    <section
                        className="evm-modal"
                        role="dialog"
                        aria-modal="true"
                        aria-labelledby="evm-modal-title"
                    >
                        <header className="evm-modal-header">
                            <div>
                                <span className="evm-eyebrow">VOTER PROFILE</span>
                                <h2 id="evm-modal-title">
                                    {voterDetails(selectedVoter).firstname}{" "}
                                    {voterDetails(selectedVoter).lastname}
                                </h2>
                            </div>
                            <button
                                type="button"
                                className="evm-modal-close"
                                onClick={() => setSelectedVoter(null)}
                                aria-label="Close modal"
                            >
                                ×
                            </button>
                        </header>

                        <div className="evm-modal-body">
                            {(() => {
                                const voter = voterDetails(selectedVoter);

                                return (
                                    <>
                                        <div className="evm-detail-grid">
                                            <div>
                                                <span>Voter ID</span>
                                                <strong>{voter.id || "—"}</strong>
                                            </div>
                                            <div>
                                                <span>First name</span>
                                                <strong>{voter.firstname || "—"}</strong>
                                            </div>
                                            <div>
                                                <span>Last name</span>
                                                <strong>{voter.lastname || "—"}</strong>
                                            </div>
                                            <div>
                                                <span>Province</span>
                                                <strong>{voter.province || "—"}</strong>
                                            </div>
                                            <div>
                                                <span>City</span>
                                                <strong>{voter.city || "—"}</strong>
                                            </div>
                                            <div>
                                                <span>Street</span>
                                                <strong>{voter.street || "—"}</strong>
                                            </div>
                                            <div>
                                                <span>Postal code</span>
                                                <strong>{voter.postal || "—"}</strong>
                                            </div>
                                            <div>
                                                <span>Account role</span>
                                                <strong>{voter.role}</strong>
                                            </div>
                                            <div>
                                                <span>Account access</span>
                                                <strong>
                                                    {voter.allowed === true
                                                        ? "Allowed"
                                                        : voter.allowed === false
                                                            ? "Revoked"
                                                            : "Unknown"}
                                                </strong>
                                            </div>
                                            <div>
                                                <span>Account created</span>
                                                <strong>{formatDate(voter.created_at)}</strong>
                                            </div>
                                            {/* <div>
                                                <span>Election request</span>
                                                <strong>{statusLabel(selectedVoter.status)}</strong>
                                            </div>
                                            <div>
                                                <span>Has voted</span>
                                                <strong>{selectedVoter.voted ? "Yes" : "No"}</strong>
                                            </div> */}
                                        </div>
                                    </>
                                );
                            })()}
                        </div>

                        <footer className="evm-modal-footer">
                            <button
                                type="button"
                                className="btn btn-secondary"
                                onClick={() => setSelectedVoter(null)}
                            >
                                Close
                            </button>
                        </footer>
                    </section>
                </div>
            )}
        </div>
    );
}
