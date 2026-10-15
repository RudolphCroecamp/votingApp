
import { useCallback, useEffect, useMemo, useState } from "react";
import Sidebar from "../components/sidebar";

const ADMIN_API =
    import.meta.env.VITE_ADMIN_API_URL ??
    "http://localhost:5000/api/Admin";

const SIDEBAR_WIDTH = "250px";

// Include the JWT token in every API request.
async function apiRequest(url, options = {}) {
    const token = sessionStorage.getItem("JWTToken");

    const response = await fetch(url, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
            ...options.headers,
        },
    });

    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
        throw new Error(
            data.message || `Request failed (${response.status})`
        );
    }

    return data;
}

// Support the property names returned by different DTOs.
function getPartyId(party) {
    return party?.party_id ?? party?.partyId;
}

function getPartyName(party) {
    return (
        party?.name ??
        party?.party_name ??
        party?.partyName ??
        "Unnamed party"
    );
}

function getElectionId(election) {
    return election?.election_id ?? election?.electionId;
}

function getElectionName(election) {
    return (
        election?.election_name ??
        election?.name ??
        election?.title ??
        "Unnamed election"
    );
}

function getAssignedParty(item) {
    return item?.Party ?? item?.party ?? item;
}

function normalizeId(id) {
    return id == null ? "" : String(id).toLowerCase();
}

export default function ManageParties() {
    const [activeTab, setActiveTab] = useState("parties");

    const [parties, setParties] = useState([]);
    const [elections, setElections] = useState([]);
    const [assignedParties, setAssignedParties] = useState([]);

    const [partyForm, setPartyForm] = useState({
        name: "",
        abbreviation: "",
        description: "",
        allowed: true,
    });

    const [selectedElection, setSelectedElection] = useState("");
    const [selectedParty, setSelectedParty] = useState("");
    const [updatingPartyId, setUpdatingPartyId] = useState(null);

    const [loading, setLoading] = useState(true);
    const [loadingAssignments, setLoadingAssignments] = useState(false);
    const [savingParty, setSavingParty] = useState(false);
    const [assigningParty, setAssigningParty] = useState(false);

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    // Load registered parties.
    const loadParties = useCallback(async () => {
        const data = await apiRequest(`${ADMIN_API}/get_parties`);

        setParties(
            Array.isArray(data.parties) ? data.parties : []
        );
    }, []);

    // Load elections.
    const loadElections = useCallback(async () => {
        const data = await apiRequest(`${ADMIN_API}/get_elections`);

        const items = Array.isArray(data.elections)
            ? data.elections
            : Array.isArray(data)
                ? data
                : [];

        setElections(items);
    }, []);

    // Refresh the main page data.
    const loadInitialData = useCallback(async () => {
        setLoading(true);
        setError("");

        try {
            await Promise.all([
                loadParties(),
                loadElections(),
            ]);
        } catch (err) {
            setError(
                err.message || "Could not load management data."
            );
        } finally {
            setLoading(false);
        }
    }, [loadParties, loadElections]);

    useEffect(() => {
        loadInitialData();
    }, [loadInitialData]);

    // Load parties assigned to the selected election.
    const loadElectionParties = useCallback(async (electionId) => {
        if (!electionId) {
            setAssignedParties([]);
            setSelectedParty("");
            setLoadingAssignments(false);
            return;
        }

        setLoadingAssignments(true);
        setSelectedParty("");
        setError("");

        try {
            const data = await apiRequest(
                `${ADMIN_API}/get_election_parties/${electionId}`
            );

            setAssignedParties(
                Array.isArray(data.parties) ? data.parties : []
            );
        } catch (err) {
            setAssignedParties([]);
            setError(
                err.message || "Could not load election parties."
            );
        } finally {
            setLoadingAssignments(false);
        }
    }, []);

    useEffect(() => {
        loadElectionParties(selectedElection);
    }, [selectedElection, loadElectionParties]);

    // Exclude disabled parties and parties already assigned to this election.
    const availableParties = useMemo(() => {
        const assignedIds = new Set(
            assignedParties
                .map(getAssignedParty)
                .map(getPartyId)
                .filter(Boolean)
                .map(normalizeId)
        );

        return parties.filter((party) => {
            const partyId = getPartyId(party);

            return (
                party.allowed !== false &&
                Boolean(partyId) &&
                !assignedIds.has(normalizeId(partyId))
            );
        });
    }, [parties, assignedParties]);

    // Register a new party.
    async function handleCreateParty(event) {
        event.preventDefault();

        setError("");
        setSuccess("");

        const request = {
            name: partyForm.name.trim(),
            abbreviation: partyForm.abbreviation.trim(),
            description: partyForm.description.trim(),
            allowed: partyForm.allowed,
        };

        if (
            !request.name ||
            !request.abbreviation ||
            !request.description
        ) {
            setError("Please complete all required party fields.");
            return;
        }

        setSavingParty(true);

        try {
            await apiRequest(`${ADMIN_API}/new_party`, {
                method: "POST",
                body: JSON.stringify(request),
            });

            setSuccess("Party created successfully!");

            setPartyForm({
                name: "",
                abbreviation: "",
                description: "",
                allowed: true,
            });

            await loadParties();
        } catch (err) {
            setError(err.message || "Could not create party.");
        } finally {
            setSavingParty(false);
        }
    }

    // Assign a registered party to an election.
    async function handleAssignParty(event) {
        event.preventDefault();

        setError("");
        setSuccess("");

        if (!selectedElection || !selectedParty) {
            setError("Select both an election and a party.");
            return;
        }

        setAssigningParty(true);

        try {
            await apiRequest(
                `${ADMIN_API}/assign_party_to_election`,
                {
                    method: "POST",
                    body: JSON.stringify({
                        party_id: selectedParty,
                        election_id: selectedElection,
                    }),
                }
            );

            setSuccess(
                "Party assigned to election successfully."
            );

            await loadElectionParties(selectedElection);
        } catch (err) {
            setError(
                err.message || "Could not assign party."
            );
        } finally {
            setAssigningParty(false);
        }
    }

    // Enable or disable a registered party.
    async function handleTogglePartyStatus(party) {
        const partyId = getPartyId(party);

        if (!partyId || updatingPartyId !== null) {
            return;
        }

        const newStatus = !party.allowed;

        setError("");
        setSuccess("");
        setUpdatingPartyId(partyId);

        try {
            await apiRequest(`${ADMIN_API}/update_party_status`, {
                method: "PATCH",
                body: JSON.stringify({
                    ID: partyId,
                    status: newStatus,
                }),
            });

            setParties((previous) =>
                previous.map((item) =>
                    normalizeId(getPartyId(item)) ===
                        normalizeId(partyId)
                        ? { ...item, allowed: newStatus }
                        : item
                )
            );

            setSuccess(
                `${getPartyName(party)} has been ${newStatus ? "enabled" : "disabled"
                } successfully.`
            );
        } catch (err) {
            setError(
                err.message || "Could not update party status."
            );
        } finally {
            setUpdatingPartyId(null);
        }
    }

    return (
        <div className="d-flex">
            <Sidebar />

            <main
                className="flex-grow-1 min-vh-100"
                style={{
                    minWidth: 0,
                    marginLeft: SIDEBAR_WIDTH,
                }}
            >
                <div className="container-fluid py-4 px-3 px-lg-4">
                    {/* Page heading */}
                    <div className="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4 panel p-3">
                        <div>
                            <h2 className="fw-bold mb-1">
                                Manage Parties
                            </h2>
                            <p className="text-secondary mb-0">
                                Register political parties and manage
                                their participation in elections.
                            </p>
                        </div>

                        <button
                            type="button"
                            className="btn btn-outline-secondary"
                            onClick={loadInitialData}
                            disabled={loading}
                        >
                            <i className="bi bi-arrow-clockwise me-2" />
                            Refresh data
                        </button>
                    </div>

                    {/* Error message */}
                    {error && (
                        <div
                            className="alert alert-danger alert-dismissible fade show"
                            role="alert"
                        >
                            <strong>Error:</strong> {error}
                            <button
                                type="button"
                                className="btn-close"
                                aria-label="Dismiss error"
                                onClick={() => setError("")}
                            />
                        </div>
                    )}

                    {/* Success message */}
                    {success && (
                        <div
                            className="alert alert-success alert-dismissible fade show"
                            role="alert"
                        >
                            <i className="bi bi-check-circle me-2" />
                            {success}
                            <button
                                type="button"
                                className="btn-close"
                                aria-label="Dismiss message"
                                onClick={() => setSuccess("")}
                            />
                        </div>
                    )}

                    {/* Summary cards */}
                    <div className="row g-3 mb-4">
                        <div className="col-12 col-md-6">
                            <div className="card border-0 shadow-sm h-100">
                                <div className="card-body d-flex align-items-center gap-3">
                                    <div
                                        className="rounded-3 bg-primary-subtle text-primary d-flex align-items-center justify-content-center"
                                        style={{
                                            width: 52,
                                            height: 52,
                                            flexShrink: 0,
                                        }}
                                    >
                                        <i className="bi bi-people fs-4" />
                                    </div>

                                    <div>
                                        <p className="text-secondary small mb-1">
                                            Registered parties
                                        </p>
                                        <h3 className="fw-bold mb-0">
                                            {loading ? "—" : parties.length}
                                        </h3>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="col-12 col-md-6">
                            <div className="card border-0 shadow-sm h-100">
                                <div className="card-body d-flex align-items-center gap-3">
                                    <div
                                        className="rounded-3 bg-success-subtle text-success d-flex align-items-center justify-content-center"
                                        style={{
                                            width: 52,
                                            height: 52,
                                            flexShrink: 0,
                                        }}
                                    >
                                        <i className="bi bi-calendar-event fs-4" />
                                    </div>

                                    <div>
                                        <p className="text-secondary small mb-1">
                                            Available elections
                                        </p>
                                        <h3 className="fw-bold mb-0">
                                            {loading ? "—" : elections.length}
                                        </h3>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Main management card */}
                    <div className="card border-0 shadow-sm">
                        <div className="card-header bg-white border-bottom p-0">
                            <div className="nav nav-tabs border-0 px-3 pt-2">
                                <button
                                    type="button"
                                    className={`nav-link ${activeTab === "parties"
                                            ? "active"
                                            : ""
                                        }`}
                                    onClick={() => {
                                        setActiveTab("parties");
                                        setError("");
                                        setSuccess("");
                                    }}
                                >
                                    <i className="bi bi-people me-2" />
                                    All parties
                                </button>

                                <button
                                    type="button"
                                    className={`nav-link ${activeTab === "assignments"
                                            ? "active"
                                            : ""
                                        }`}
                                    onClick={() => {
                                        setActiveTab("assignments");
                                        setError("");
                                        setSuccess("");
                                    }}
                                >
                                    <i className="bi bi-diagram-3 me-2" />
                                    Election assignments
                                </button>
                            </div>
                        </div>

                        <div className="card-body p-3 p-lg-4">
                            {/* ALL PARTIES TAB */}
                            {activeTab === "parties" && (
                                <>
                                    <section className="mb-4">
                                        <h5 className="fw-bold mb-1">
                                            Register a party
                                        </h5>
                                        <p className="text-secondary small mb-4">
                                            Add the party's details. You can
                                            assign it to specific elections
                                            after registration.
                                        </p>

                                        <form
                                            onSubmit={handleCreateParty}
                                            className="row g-3"
                                        >
                                            <div className="col-12 col-md-6">
                                                <label
                                                    htmlFor="partyName"
                                                    className="form-label"
                                                >
                                                    Party name *
                                                </label>
                                                <input
                                                    id="partyName"
                                                    type="text"
                                                    className="form-control"
                                                    placeholder="e.g. Democratic Alliance"
                                                    value={partyForm.name}
                                                    onChange={(event) =>
                                                        setPartyForm((previous) => ({
                                                            ...previous,
                                                            name: event.target.value,
                                                        }))
                                                    }
                                                    maxLength={150}
                                                    required
                                                />
                                            </div>

                                            <div className="col-12 col-md-6">
                                                <label
                                                    htmlFor="partyAbbreviation"
                                                    className="form-label"
                                                >
                                                    Abbreviation *
                                                </label>
                                                <input
                                                    id="partyAbbreviation"
                                                    type="text"
                                                    className="form-control"
                                                    placeholder="e.g. DA"
                                                    value={partyForm.abbreviation}
                                                    onChange={(event) =>
                                                        setPartyForm((previous) => ({
                                                            ...previous,
                                                            abbreviation: event.target.value,
                                                        }))
                                                    }
                                                    maxLength={20}
                                                    required
                                                />
                                            </div>

                                            <div className="col-12">
                                                <label
                                                    htmlFor="partyDescription"
                                                    className="form-label"
                                                >
                                                    Description *
                                                </label>
                                                <textarea
                                                    id="partyDescription"
                                                    className="form-control"
                                                    rows={4}
                                                    placeholder="Enter a short description of the party..."
                                                    value={partyForm.description}
                                                    onChange={(event) =>
                                                        setPartyForm((previous) => ({
                                                            ...previous,
                                                            description: event.target.value,
                                                        }))
                                                    }
                                                    maxLength={500}
                                                    required
                                                />
                                                <div className="form-text text-end">
                                                    {partyForm.description.length}/500
                                                    {" "}characters
                                                </div>
                                            </div>

                                            <div className="col-12">
                                                <div className="form-check form-switch">
                                                    <input
                                                        id="partyAllowed"
                                                        type="checkbox"
                                                        className="form-check-input"
                                                        role="switch"
                                                        checked={partyForm.allowed}
                                                        onChange={(event) =>
                                                            setPartyForm((previous) => ({
                                                                ...previous,
                                                                allowed: event.target.checked,
                                                            }))
                                                        }
                                                    />
                                                    <label
                                                        htmlFor="partyAllowed"
                                                        className="form-check-label"
                                                    >
                                                        Allow this party to participate
                                                        in future elections
                                                    </label>
                                                </div>
                                                <div className="form-text">
                                                    Election assignments are managed
                                                    separately below.
                                                </div>
                                            </div>

                                            <div className="col-12 d-flex justify-content-end">
                                                <button
                                                    type="submit"
                                                    className="btn btn-primary"
                                                    disabled={savingParty || loading}
                                                >
                                                    {savingParty ? (
                                                        <>
                                                            <span
                                                                className="spinner-border spinner-border-sm me-2"
                                                                aria-hidden="true"
                                                            />
                                                            Creating...
                                                        </>
                                                    ) : (
                                                        <>
                                                            <i className="bi bi-plus-lg me-2" />
                                                            Add party
                                                        </>
                                                    )}
                                                </button>
                                            </div>
                                        </form>
                                    </section>

                                    <hr className="my-4" />

                                    {/* Registered parties table */}
                                    <section>
                                        <div className="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
                                            <h5 className="fw-bold mb-0">
                                                Registered parties
                                            </h5>
                                            <span className="badge text-bg-light">
                                                {parties.length} parties
                                            </span>
                                        </div>

                                        {loading ? (
                                            <div className="text-center py-5">
                                                <div
                                                    className="spinner-border text-primary"
                                                    role="status"
                                                >
                                                    <span className="visually-hidden">
                                                        Loading
                                                    </span>
                                                </div>
                                                <p className="text-secondary mt-2 mb-0">
                                                    Loading parties...
                                                </p>
                                            </div>
                                        ) : parties.length === 0 ? (
                                            <div className="text-center text-secondary py-5 border rounded">
                                                <i className="bi bi-people fs-1 d-block mb-2" />
                                                No parties registered yet.
                                            </div>
                                        ) : (
                                            <div className="table-responsive">
                                                <table
                                                    className="table table-hover align-middle mb-0"
                                                    style={{
                                                        minWidth: "850px",
                                                        tableLayout: "fixed",
                                                    }}
                                                >
                                                    <colgroup>
                                                        <col style={{ width: "28%" }} />
                                                        <col style={{ width: "26%" }} />
                                                        <col style={{ width: "13%" }} />
                                                        <col style={{ width: "13%" }} />
                                                        <col style={{ width: "20%" }} />
                                                    </colgroup>

                                                    <thead className="table-light">
                                                        <tr>
                                                            <th scope="col">
                                                                Party ID
                                                            </th>
                                                            <th scope="col">
                                                                Party name
                                                            </th>
                                                            <th scope="col">
                                                                Abbreviation
                                                            </th>
                                                            <th scope="col">
                                                                Status
                                                            </th>
                                                            <th
                                                                scope="col"
                                                                className="text-end"
                                                            >
                                                                Actions
                                                            </th>
                                                        </tr>
                                                    </thead>

                                                    <tbody>
                                                        {parties.map((party, index) => {
                                                            const partyId = getPartyId(party);
                                                            const isUpdating =
                                                                updatingPartyId !== null &&
                                                                normalizeId(updatingPartyId) ===
                                                                normalizeId(partyId);

                                                            return (
                                                                <tr key={partyId ?? index}>
                                                                    <td>
                                                                        <code
                                                                            className="text-break"
                                                                            style={{
                                                                                fontSize: "0.8rem",
                                                                                overflowWrap: "anywhere",
                                                                            }}
                                                                        >
                                                                            {partyId ?? "—"}
                                                                        </code>
                                                                    </td>

                                                                    <td
                                                                        className="fw-semibold text-break"
                                                                        style={{
                                                                            overflowWrap: "anywhere",
                                                                        }}
                                                                    >
                                                                        {getPartyName(party)}
                                                                    </td>

                                                                    <td className="text-break">
                                                                        {party.abbreviation || "—"}
                                                                    </td>

                                                                    <td>
                                                                        <span
                                                                            className={`badge ${party.allowed
                                                                                    ? "text-bg-success"
                                                                                    : "text-bg-secondary"
                                                                                }`}
                                                                        >
                                                                            {party.allowed
                                                                                ? "Enabled"
                                                                                : "Disabled"}
                                                                        </span>
                                                                    </td>

                                                                    <td className="text-end">
                                                                        <button
                                                                            type="button"
                                                                            className={`btn btn-sm ${party.allowed
                                                                                    ? "btn-outline-danger"
                                                                                    : "btn-outline-success"
                                                                                }`}
                                                                            onClick={() =>
                                                                                handleTogglePartyStatus(party)
                                                                            }
                                                                            disabled={
                                                                                updatingPartyId !== null ||
                                                                                !partyId
                                                                            }
                                                                        >
                                                                            {isUpdating ? (
                                                                                <>
                                                                                    <span
                                                                                        className="spinner-border spinner-border-sm me-1"
                                                                                        aria-hidden="true"
                                                                                    />
                                                                                    Updating...
                                                                                </>
                                                                            ) : (
                                                                                <>
                                                                                    <i
                                                                                        className={`bi ${party.allowed
                                                                                                ? "bi-slash-circle"
                                                                                                : "bi-check-circle"
                                                                                            } me-1`}
                                                                                    />
                                                                                    {party.allowed
                                                                                        ? "Disable"
                                                                                        : "Enable"}
                                                                                </>
                                                                            )}
                                                                        </button>
                                                                    </td>
                                                                </tr>
                                                            );
                                                        })}
                                                    </tbody>
                                                </table>
                                            </div>
                                        )}
                                    </section>
                                </>
                            )}

                            {/* ELECTION ASSIGNMENTS TAB */}
                            {activeTab === "assignments" && (
                                <>
                                    <h5 className="fw-bold mb-1">
                                        Assign parties to elections
                                    </h5>
                                    <p className="text-secondary small mb-4">
                                        Select an election to view its assigned
                                        parties and add eligible registered parties.
                                    </p>

                                    <div className="mb-4">
                                        <label
                                            htmlFor="electionSelect"
                                            className="form-label"
                                        >
                                            Select election
                                        </label>

                                        <select
                                            id="electionSelect"
                                            className="form-select"
                                            value={selectedElection}
                                            onChange={(event) =>
                                                setSelectedElection(event.target.value)
                                            }
                                            disabled={loading}
                                        >
                                            <option value="">
                                                Choose an election...
                                            </option>

                                            {elections.map((election, index) => (
                                                <option
                                                    key={
                                                        getElectionId(election) ?? index
                                                    }
                                                    value={
                                                        getElectionId(election) ?? ""
                                                    }
                                                >
                                                    {getElectionName(election)}
                                                </option>
                                            ))}
                                        </select>
                                    </div>

                                    {!selectedElection ? (
                                        <div className="text-center text-secondary py-5 border rounded">
                                            <i className="bi bi-calendar-event fs-1 d-block mb-2" />
                                            Select an election to manage its parties.
                                        </div>
                                    ) : (
                                        <>
                                            {/* Assign a party */}
                                            <div className="p-3 p-lg-4 bg-light rounded mb-4">
                                                <h6 className="fw-bold mb-1">
                                                    Add a party to this election
                                                </h6>
                                                <p className="text-secondary small mb-3">
                                                    Only eligible parties not already
                                                    assigned to this election are shown.
                                                </p>

                                                <form
                                                    onSubmit={handleAssignParty}
                                                    className="row g-3 align-items-end"
                                                >
                                                    <div className="col-12 col-md-8">
                                                        <label
                                                            htmlFor="partySelect"
                                                            className="form-label"
                                                        >
                                                            Available party
                                                        </label>

                                                        <select
                                                            id="partySelect"
                                                            className="form-select"
                                                            value={selectedParty}
                                                            onChange={(event) =>
                                                                setSelectedParty(event.target.value)
                                                            }
                                                            disabled={
                                                                loading ||
                                                                loadingAssignments ||
                                                                assigningParty ||
                                                                availableParties.length === 0
                                                            }
                                                        >
                                                            <option value="">
                                                                {availableParties.length > 0
                                                                    ? "Choose a party..."
                                                                    : "No eligible unassigned parties"}
                                                            </option>

                                                            {availableParties.map((party, index) => (
                                                                <option
                                                                    key={
                                                                        getPartyId(party) ?? index
                                                                    }
                                                                    value={getPartyId(party)}
                                                                >
                                                                    {getPartyName(party)}
                                                                    {party.abbreviation
                                                                        ? ` (${party.abbreviation})`
                                                                        : ""}
                                                                </option>
                                                            ))}
                                                        </select>
                                                    </div>

                                                    <div className="col-12 col-md-4">
                                                        <button
                                                            type="submit"
                                                            className="btn btn-primary w-100"
                                                            disabled={
                                                                assigningParty ||
                                                                loadingAssignments ||
                                                                !selectedParty
                                                            }
                                                        >
                                                            {assigningParty ? (
                                                                <>
                                                                    <span
                                                                        className="spinner-border spinner-border-sm me-2"
                                                                        aria-hidden="true"
                                                                    />
                                                                    Assigning...
                                                                </>
                                                            ) : (
                                                                <>
                                                                    <i className="bi bi-plus-lg me-2" />
                                                                    Assign party
                                                                </>
                                                            )}
                                                        </button>
                                                    </div>
                                                </form>
                                            </div>

                                            {/* Assigned parties table */}
                                            <div className="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
                                                <h5 className="fw-bold mb-0">
                                                    Assigned parties
                                                </h5>
                                                <span className="badge text-bg-light">
                                                    {assignedParties.length} assigned
                                                </span>
                                            </div>

                                            {loadingAssignments ? (
                                                <div className="text-center py-4">
                                                    <div
                                                        className="spinner-border text-primary"
                                                        role="status"
                                                    >
                                                        <span className="visually-hidden">
                                                            Loading
                                                        </span>
                                                    </div>
                                                    <p className="text-secondary mt-2 mb-0">
                                                        Loading assignments...
                                                    </p>
                                                </div>
                                            ) : assignedParties.length === 0 ? (
                                                <div className="text-center text-secondary py-5 border rounded">
                                                    <i className="bi bi-inbox fs-1 d-block mb-2" />
                                                    No parties have been assigned to
                                                    this election yet.
                                                </div>
                                            ) : (
                                                <div className="table-responsive">
                                                    <table
                                                        className="table table-hover align-middle mb-0"
                                                        style={{
                                                            minWidth: "600px",
                                                            tableLayout: "fixed",
                                                        }}
                                                    >
                                                        <colgroup>
                                                            <col style={{ width: "32%" }} />
                                                            <col style={{ width: "48%" }} />
                                                            <col style={{ width: "20%" }} />
                                                        </colgroup>

                                                        <thead className="table-light">
                                                            <tr>
                                                                <th scope="col">
                                                                    Party ID
                                                                </th>
                                                                <th scope="col">
                                                                    Party name
                                                                </th>
                                                                <th scope="col">
                                                                    Abbreviation
                                                                </th>
                                                            </tr>
                                                        </thead>

                                                        <tbody>
                                                            {assignedParties.map((item, index) => {
                                                                const party = getAssignedParty(item);
                                                                const partyId = getPartyId(party);

                                                                return (
                                                                    <tr key={partyId ?? index}>
                                                                        <td>
                                                                            <code
                                                                                className="text-break"
                                                                                style={{
                                                                                    fontSize: "0.8rem",
                                                                                    overflowWrap: "anywhere",
                                                                                }}
                                                                            >
                                                                                {partyId ?? "—"}
                                                                            </code>
                                                                        </td>

                                                                        <td
                                                                            className="fw-semibold text-break"
                                                                            style={{
                                                                                overflowWrap: "anywhere",
                                                                            }}
                                                                        >
                                                                            {getPartyName(party)}
                                                                        </td>

                                                                        <td className="text-break">
                                                                            {party?.abbreviation || "—"}
                                                                        </td>
                                                                    </tr>
                                                                );
                                                            })}
                                                        </tbody>
                                                    </table>
                                                </div>
                                            )}
                                        </>
                                    )}
                                </>
                            )}
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}
