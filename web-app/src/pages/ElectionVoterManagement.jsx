
import { useCallback, useEffect, useMemo, useState } from "react";
import Sidebar from "../components/sidebar";
import "../styles/election-voter-management.css";
import "../styles/voter.css";

const API_BASE = "http://localhost:5000/api/Admin";
const DOCUMENTS_API = "http://localhost:5000/api/documents";

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

    return data;
}






export default function ElectionVoterManagement() {
    const [elections, setElections] = useState([]);
    const [selectedElectionId, setSelectedElectionId] = useState("");
    const [records, setRecords] = useState([]);
    const [search, setSearch] = useState("");
    const [selectedVoter, setSelectedVoter] = useState(null);
    const [selectedRequest, setSelectedRequest] = useState(null);
    const [idFrontUrl, setIdFrontUrl] = useState(null);
    const [idBackUrl, setIdBackUrl] = useState(null);
    const [loadingIdImages, setLoadingIdImages] = useState(false);
    const [idImageError, setIdImageError] = useState("");
    const [updatingAccess, setUpdatingAccess] = useState(false);

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

            setSelectedRequest((current) =>
                current && voterDetails(current).id === voterId
                    ? { ...current, status: nextStatus }
                    : current
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
            setSelectedRequest(record);
            setLoadingVoterDetails(true);
            setSelectedVoter(null);
            setError("");
            setSuccess("");

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



    async function toggleVoterAccess() {
        const voter = voterDetails(selectedVoter);
        if (typeof voter.allowed !== "boolean" || !voter.id) {
            setError("The voter response does not contain a valid access status or voter ID.");
            return;
        }

        const newStatus = !voter.allowed;
        const action = newStatus ? "restore" : "revoke";
        if (!window.confirm(`Are you sure you want to ${action} access for ${voter.firstname} ${voter.lastname}?`)) return;

        setUpdatingAccess(true);
        setError("");
        setSuccess("");
        try {
            await apiRequest(`${API_BASE}/Update_voter_access`, {
                method: "PATCH",
                body: JSON.stringify({ ID: voter.id, status: newStatus }),
            });
            setSelectedVoter((current) => current ? { ...current, allowed: newStatus } : current);
            setSuccess(`Access ${newStatus ? "restored" : "revoked"} for ${voter.firstname} ${voter.lastname}.`);
            await loadElectionVoters();
        } catch (err) {
            setError(err.message || "Could not update voter access.");
        } finally {
            setUpdatingAccess(false);
        }
    }

    useEffect(() => {
        let cancelled = false;
        let frontObjectUrl = null;
        let backObjectUrl = null;

        async function loadIdImages() {
            setIdFrontUrl(null);
            setIdBackUrl(null);
            setIdImageError("");

            const voterId = voterDetails(selectedVoter).id;
            if (!selectedVoter || !voterId) {
                setLoadingIdImages(false);
                return;
            }

            setLoadingIdImages(true);
            try {
                const token = sessionStorage.getItem("JWTToken");
                if (!token) throw new Error("Your admin session has expired. Please log in again.");

                const metadataResponse = await fetch(`${DOCUMENTS_API}/voters/${voterId}`, {
                    headers: { Accept: "application/json", Authorization: `Bearer ${token}` },
                });
                if (!metadataResponse.ok) {
                    throw new Error(`Failed to load document information (HTTP ${metadataResponse.status}).`);
                }

                const documents = await metadataResponse.json();
                if (cancelled) return;
                if (!Array.isArray(documents)) throw new Error("The document API did not return a list of documents.");

                const frontDocument = documents.find((doc) =>
                    String(doc.fileName ?? doc.FileName ?? "").toLowerCase() === "id_front");
                const backDocument = documents.find((doc) =>
                    String(doc.fileName ?? doc.FileName ?? "").toLowerCase() === "id_back");
                const frontFileId = frontDocument?.fileId ?? frontDocument?.FileId;
                const backFileId = backDocument?.fileId ?? backDocument?.FileId;
                if (!frontFileId || !backFileId) {
                    throw new Error("The front or back ID document could not be found in the document records.");
                }

                const fetchImage = async (fileId) => {
                    const response = await fetch(`${DOCUMENTS_API}/voters/${voterId}/${fileId}`, {
                        headers: { Authorization: `Bearer ${token}` },
                    });
                    if (!response.ok) throw new Error(`Failed to load document (HTTP ${response.status}).`);
                    const blob = await response.blob();
                    if (!blob.type.startsWith("image/")) throw new Error("An ID document was not returned as an image.");
                    return URL.createObjectURL(blob);
                };

                const results = await Promise.allSettled([fetchImage(frontFileId), fetchImage(backFileId)]);
                if (cancelled) {
                    results.forEach((result) => {
                        if (result.status === "fulfilled") URL.revokeObjectURL(result.value);
                    });
                    return;
                }
                if (results.some((result) => result.status === "rejected")) {
                    results.forEach((result) => {
                        if (result.status === "fulfilled") URL.revokeObjectURL(result.value);
                    });
                    throw results.find((result) => result.status === "rejected").reason;
                }

                frontObjectUrl = results[0].value;
                backObjectUrl = results[1].value;
                setIdFrontUrl(frontObjectUrl);
                setIdBackUrl(backObjectUrl);
            } catch (err) {
                if (!cancelled) setIdImageError(err.message || "Could not load the ID images.");
            } finally {
                if (!cancelled) setLoadingIdImages(false);
            }
        }

        loadIdImages();
        return () => {
            cancelled = true;
            if (frontObjectUrl) URL.revokeObjectURL(frontObjectUrl);
            if (backObjectUrl) URL.revokeObjectURL(backObjectUrl);
        };
    }, [selectedVoter?.voter_id, selectedVoter?.voterId]);

    const selectedElection = elections.find(
        (e) => e.election_id === selectedElectionId
    );

    return (
        <div className="admin-layout">
            <Sidebar />

            <main className="admin-content">
                <div className="evm-page">
                    <header className="evm-header panel p-3">
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
                                setSelectedRequest(null);
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
                                    </tr>
                                </thead>

                                <tbody>
                                    {loadingRecords ? (
                                        <tr>
                                            <td colSpan="4" className="evm-empty">
                                                Loading election voters...
                                            </td>
                                        </tr>
                                    ) : !selectedElectionId ? (
                                        <tr>
                                            <td colSpan="4" className="evm-empty">
                                                Select an election above.
                                            </td>
                                        </tr>
                                    ) : filteredRecords.length === 0 ? (
                                        <tr>
                                            <td colSpan="4" className="evm-empty">
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
                                                            {record.voted ? "Voted" : "Not voted"}
                                                        </span>
                                                    </td>

                                                    <td>
                                                        <button
                                                            type="button"
                                                            className="btn btn-outline-primary btn-sm"
                                                            onClick={() => handleViewVoter(record)}
                                                            disabled={loadingVoterDetails}
                                                        >
                                                            View Voter
                                                        </button>
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

                <footer className="page-footer">
                    <span>BallotAdmin · Election Voter Management</span>
                    <span>Secure access for authorised personal only</span>
                </footer>
            </main>

            {selectedRequest && (
                <div
                    className="evm-modal-backdrop voter-modal-overlay"
                    role="presentation"
                    onMouseDown={(event) => {
                        if (event.target === event.currentTarget) {
                            setSelectedVoter(null);
                            setSelectedRequest(null);
                        }
                    }}
                >
                    <section
                        className="evm-modal voter-modal"
                        role="dialog"
                        aria-modal="true"
                        aria-labelledby="evm-modal-title"
                    >
                        <header className="evm-modal-header voter-modal-header">
                            <div>
                                <span className="evm-eyebrow">VOTER PROFILE</span>
                                <h2 id="evm-modal-title">
                                    {voterDetails(selectedVoter ?? selectedRequest).firstname}{" "}
                                    {voterDetails(selectedVoter ?? selectedRequest).lastname}
                                </h2>
                            </div>
                            <button
                                type="button"
                                className="evm-modal-close"
                                onClick={() => {
                                    setSelectedVoter(null);
                                    setSelectedRequest(null);
                                }}
                                aria-label="Close modal"
                            >
                                ×
                            </button>
                        </header>

                        {!loadingVoterDetails && selectedVoter && (
                            <div className={`voter-access-banner ${voterDetails(selectedVoter).allowed === true
                                ? "access-enabled"
                                : voterDetails(selectedVoter).allowed === false
                                    ? "access-revoked"
                                    : "access-unknown"}`}>
                                <div className="voter-access-icon">
                                    {voterDetails(selectedVoter).allowed === true ? "✓" : voterDetails(selectedVoter).allowed === false ? "!" : "?"}
                                </div>
                                <div className="voter-access-message">
                                    <strong>
                                        {voterDetails(selectedVoter).allowed === true
                                            ? "Account access enabled"
                                            : voterDetails(selectedVoter).allowed === false
                                                ? "Account access revoked"
                                                : "Access status unknown"}
                                    </strong>
                                    <span>
                                        {voterDetails(selectedVoter).allowed === true
                                            ? "This voter currently has access to permitted services."
                                            : voterDetails(selectedVoter).allowed === false
                                                ? "This voter cannot access protected services."
                                                : "The current access status could not be determined."}
                                    </span>
                                </div>
                                <div className="voter-access-actions">
                                    <span className="voter-access-label">
                                        {voterDetails(selectedVoter).allowed === true ? "ACTIVE" : voterDetails(selectedVoter).allowed === false ? "REVOKED" : "UNKNOWN"}
                                    </span>
                                    <button
                                        type="button"
                                        className={`voter-access-action ${voterDetails(selectedVoter).allowed === true ? "revoke-access" : "allow-access"}`}
                                        onClick={toggleVoterAccess}
                                        disabled={updatingAccess || typeof voterDetails(selectedVoter).allowed !== "boolean"}
                                    >
                                        {updatingAccess ? "Updating..." : voterDetails(selectedVoter).allowed === true ? "Revoke access" : "Allow access"}
                                    </button>
                                </div>
                            </div>
                        )}

                        <div className="evm-modal-body voter-modal-body">
                            {loadingVoterDetails ? (
                                <p className="evm-empty">Loading voter information...</p>
                            ) : selectedVoter ? (
                                (() => {
                                    const voter = voterDetails(selectedVoter);
                                    const requestStatus = getStatus(selectedRequest.status);
                                    const requestKey = `${selectedRequest.election_id ?? selectedElectionId}:${selectedRequest.voter_id ?? voter.id}`;
                                    const updating = updatingKey === requestKey;

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
                                            <div>
                                                <span>Election request</span>
                                                <strong>{statusLabel(selectedRequest.status)}</strong>
                                            </div>
                                            <div>
                                                <span>Voting status</span>
                                                <strong>{selectedRequest.voted ? "Voted" : "Not voted"}</strong>
                                            </div>
                                        </div>
                                        <section className="voter-details-section mt-4">
                                            <h3>South African ID document</h3>
                                            <p className="voter-id-description">Front and back images associated with this voter.</p>
                                            {idImageError && <div className="voter-error" role="alert">{idImageError}</div>}
                                            <div className="voter-id-images">
                                                <div className="voter-id-image-card">
                                                    <h4>Front of ID</h4>
                                                    {idFrontUrl ? <img src={idFrontUrl} alt="Front of voter's ID document" /> :
                                                        <div className="voter-id-image-placeholder">{loadingIdImages ? "Loading ID front..." : "ID front image unavailable"}</div>}
                                                </div>
                                                <div className="voter-id-image-card">
                                                    <h4>Back of ID</h4>
                                                    {idBackUrl ? <img src={idBackUrl} alt="Back of voter's ID document" /> :
                                                        <div className="voter-id-image-placeholder">{loadingIdImages ? "Loading ID back..." : "ID back image unavailable"}</div>}
                                                </div>
                                            </div>
                                        </section>
                                        <div className="evm-modal-status-control mt-4 pt-3 border-top">
                                            <label htmlFor="modal-request-status" className="form-label">
                                                Update election request
                                            </label>
                                            <select
                                                id="modal-request-status"
                                                className="form-select"
                                                value={Number.isFinite(requestStatus) ? requestStatus : STATUS.PENDING}
                                                disabled={updating || Boolean(selectedRequest.voted)}
                                                onChange={(event) => updateRequest(selectedRequest, Number(event.target.value))}
                                            >
                                                <option value={STATUS.PENDING}>Pending</option>
                                                <option value={STATUS.APPROVED}>Approved</option>
                                                <option value={STATUS.REJECTED}>Rejected</option>
                                            </select>
                                            {selectedRequest.voted && (
                                                <small className="text-muted">
                                                    The request status cannot be changed because this voter has already voted.
                                                </small>
                                            )}
                                            {updating && <span className="evm-updating">Saving...</span>}
                                        </div>
                                    </>
                                    );
                                })()
                            ) : (
                                <p className="evm-empty">Voter information is unavailable.</p>
                            )}
                        </div>

                        <footer className="evm-modal-footer voter-modal-footer">
                            <button
                                type="button"
                                className="btn btn-secondary"
                                onClick={() => {
                                    setSelectedVoter(null);
                                    setSelectedRequest(null);
                                }}
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
