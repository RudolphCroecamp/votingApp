
import { useCallback, useEffect, useState } from "react";
import Sidebar from "../components/sidebar";

const API_URL = "http://localhost:5000/api/Admin";

const emptyElection = {
    election_name: "",
    election_type: "",
    start_date: "",
    end_date: "",
};

function formatDate(value) {
    if (!value) return "";
    return String(value).slice(0, 10);
}

function getErrorMessage(data, fallback) {
    return data?.message || data?.title || fallback;
}

export default function Elections() {
    const [elections, setElections] = useState([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [editingId, setEditingId] = useState(null);
    const [form, setForm] = useState(emptyElection);
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [statusChanges, setStatusChanges] = useState({});

    const token = sessionStorage.getItem("JWTToken");

    const authHeaders = {
        Authorization: `Bearer ${token}`,
    };

    const loadElections = useCallback(async () => {
        setLoading(true);
        setError("");

        try {
            const response = await fetch(
                `${API_URL}/get_elections`,
                { headers: authHeaders }
            );

            const data = await response.json().catch(() => null);

            if (!response.ok) {
                throw new Error(
                    getErrorMessage(data, "Failed to load elections.")
                );
            }

            // Supports APIs returning either an array or { elections: [...] }.
            const list = Array.isArray(data)
                ? data
                : data?.elections ?? [];

            setElections(list);
        } catch (err) {
            setError(err.message || "Could not connect to the API.");
        } finally {
            setLoading(false);
        }
    }, [token]);

    useEffect(() => {
        loadElections();
    }, [loadElections]);

    function handleFormChange(event) {
        const { name, value } = event.target;

        setForm((current) => ({
            ...current,
            [name]: value,
        }));
    }

    function startEditing(election) {
        setEditingId(election.election_id);

        setForm({
            election_name: election.election_name ?? "",
            election_type: election.election_type ?? "",
            start_date: formatDate(election.start_date),
            end_date: formatDate(election.end_date),
        });

        setMessage("");
        setError("");
        window.scrollTo({ top: 0, behavior: "smooth" });
    }

    function resetForm() {
        setEditingId(null);
        setForm(emptyElection);
    }

    async function handleSubmit(event) {
        event.preventDefault();
        setSaving(true);
        setMessage("");
        setError("");

        try {
            const isEditing = editingId !== null;

            const url = isEditing
                ? `${API_URL}/updateElection${editingId}`
                : `${API_URL}/new_election`;

            const method = isEditing ? "PUT" : "POST";

            // Keep this payload aligned with your C# DTO properties.
            const payload = {
                election_name: form.election_name.trim(),
                election_type: form.election_type,
                start_date: form.start_date,
                end_date: form.end_date,
            };

            const response = await fetch(url, {
                method,
                headers: {
                    ...authHeaders,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(payload),
            });

            const data = await response.json().catch(() => null);

            if (!response.ok) {
                throw new Error(
                    getErrorMessage(
                        data,
                        isEditing
                            ? "Failed to update election."
                            : "Failed to create election."
                    )
                );
            }

            setMessage(
                data?.message ||
                (isEditing
                    ? "Election updated successfully."
                    : "Election created successfully.")
            );

            resetForm();
            await loadElections();
        } catch (err) {
            setError(err.message || "The request failed.");
        } finally {
            setSaving(false);
        }
    }

    async function updateStatus(election) {
        const newStatus = statusChanges[election.election_id];

        if (newStatus === undefined) return;

        setMessage("");
        setError("");

        try {
            const response = await fetch(
                `${API_URL}/update_election_status`,
                {
                    method: "PATCH",
                    headers: {
                        ...authHeaders,
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        ID: election.election_id,
                        status: newStatus,
                    }),
                }
            );

            const data = await response.json().catch(() => null);

            if (!response.ok) {
                throw new Error(
                    getErrorMessage(data, "Failed to update election status.")
                );
            }

            setMessage(data?.message || "Election status updated.");

            setStatusChanges((current) => {
                const next = { ...current };
                delete next[election.election_id];
                return next;
            });

            await loadElections();
        } catch (err) {
            setError(err.message || "The status update failed.");
        }
    }

    function getStatus(election) {
        return election.isActive === true;
    }


    function statusClass(isActive) {
        return isActive
            ? "bg-success"
            : "bg-secondary";
    }


    


    return (
        <div className="admin-layout">
            <Sidebar />

            <main className="admin-content">
                <div className="container-fluid p-0">
                    <div className="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
                        <div>
                            <h2 className="fw-bold mb-1">Election Management</h2>
                            <p className="text-secondary mb-0">
                                Create elections, edit their details, and manage their status.
                            </p>
                        </div>

                        <button
                            className="btn btn-outline-secondary"
                            onClick={loadElections}
                            disabled={loading}
                        >
                            <i className="bi bi-arrow-clockwise me-2"></i>
                            Refresh
                        </button>
                    </div>

                    {message && (
                        <div className="alert alert-success alert-dismissible" role="alert">
                            {message}
                            <button
                                type="button"
                                className="btn-close"
                                aria-label="Close"
                                onClick={() => setMessage("")}
                            />
                        </div>
                    )}

                    {error && (
                        <div className="alert alert-danger alert-dismissible" role="alert">
                            {error}
                            <button
                                type="button"
                                className="btn-close"
                                aria-label="Close"
                                onClick={() => setError("")}
                            />
                        </div>
                    )}

                    {/* Create / edit form */}
                    <div className="card border-0 shadow-sm mb-4">
                        <div className="card-header bg-white py-3">
                            <h5 className="fw-semibold mb-0">
                                {editingId ? "Edit Election" : "Create Election"}
                            </h5>
                        </div>

                        <div className="card-body p-4">
                            <form onSubmit={handleSubmit}>
                                <div className="row g-3">
                                    <div className="col-md-6">
                                        <label className="form-label">Election Name</label>
                                        <input
                                            className="form-control"
                                            name="election_name"
                                            value={form.election_name}
                                            onChange={handleFormChange}
                                            required
                                            maxLength={200}
                                        />
                                    </div>

                                    <div className="col-md-6">
                                        <label className="form-label">Election Type</label>
                                        <select
                                            className="form-select"
                                            name="election_type"
                                            value={form.election_type}
                                            onChange={handleFormChange}
                                            required
                                        >
                                            <option value="">Select a type</option>
                                            <option value="National">National</option>
                                            <option value="Provincial">Provincial</option>
                                            <option value="Municipal">Municipal</option>
                                            <option value="Other">Other</option>
                                        </select>
                                    </div>

                                    <div className="col-md-6">
                                        <label className="form-label">Start Date</label>
                                        <input
                                            type="date"
                                            className="form-control"
                                            name="start_date"
                                            value={form.start_date}
                                            onChange={handleFormChange}
                                            required
                                        />
                                    </div>

                                    <div className="col-md-6">
                                        <label className="form-label">End Date</label>
                                        <input
                                            type="date"
                                            className="form-control"
                                            name="end_date"
                                            value={form.end_date}
                                            onChange={handleFormChange}
                                            min={form.start_date || undefined}
                                            required
                                        />
                                    </div>
                                </div>

                                <div className="d-flex gap-2 mt-4">
                                    <button
                                        type="submit"
                                        className="btn btn-primary"
                                        disabled={saving}
                                    >
                                        {saving
                                            ? "Saving..."
                                            : editingId
                                                ? "Save Changes"
                                                : "Create Election"}
                                    </button>

                                    {editingId && (
                                        <button
                                            type="button"
                                            className="btn btn-outline-secondary"
                                            onClick={resetForm}
                                            disabled={saving}
                                        >
                                            Cancel Edit
                                        </button>
                                    )}
                                </div>
                            </form>
                        </div>
                    </div>

                    {/* Elections table */}
                    <div className="card border-0 shadow-sm">
                        <div className="card-header bg-white py-3 d-flex justify-content-between align-items-center">
                            <h5 className="fw-semibold mb-0">All Elections</h5>
                            <span className="badge text-bg-light border">
                                {elections.length} elections
                            </span>
                        </div>

                        <div className="table-responsive">
                            <table className="table table-hover align-middle mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th className="ps-3">Election</th>
                                        <th>Type</th>
                                        <th>Start Date</th>
                                        <th>End Date</th>
                                        <th>Status</th>
                                        <th>Change Status</th>
                                        <th className="text-end pe-3">Actions</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {loading ? (
                                        <tr>
                                            <td colSpan="7" className="text-center py-5">
                                                <div
                                                    className="spinner-border spinner-border-sm me-2"
                                                    role="status"
                                                />
                                                Loading elections...
                                            </td>
                                        </tr>
                                    ) : elections.length === 0 ? (
                                        <tr>
                                            <td colSpan="7" className="text-center text-secondary py-5">
                                                No elections found.
                                            </td>
                                        </tr>
                                    ) : (
                                        elections.map((election) => {
                                            const status = getStatus(election);

                                            return (
                                                <tr key={election.election_id}>
                                                    <td className="ps-3 fw-semibold">
                                                        {election.election_name}
                                                    </td>
                                                    <td>{election.election_type || "—"}</td>
                                                    <td>{formatDate(election.start_date) || "—"}</td>
                                                    <td>{formatDate(election.end_date) || "—"}</td>
                                                    <td>
                                                        <span className={`badge ${statusClass(getStatus(election))}`}>
                                                            {getStatus(election) ? "Active" : "Inactive"}
                                                        </span>
                                                    </td>
                                                    <td>
                                                        <div className="d-flex gap-2">
                                                            <select
                                                                className="form-select form-select-sm"
                                                                value={
                                                                    statusChanges[election.election_id] ??
                                                                    getStatus(election)
                                                                }
                                                                onChange={(event) =>
                                                                    setStatusChanges((current) => ({
                                                                        ...current,
                                                                        [election.election_id]:
                                                                            event.target.value === "true",
                                                                    }))
                                                                }
                                                                aria-label={`Status for ${election.election_name}`}
                                                            >
                                                                <option value="true">Active</option>
                                                                <option value="false">Inactive</option>
                                                            </select>

                                                            <button
                                                                className="btn btn-sm btn-outline-primary"
                                                                onClick={() => updateStatus(election)}
                                                                disabled={
                                                                    statusChanges[election.election_id] === undefined ||
                                                                    statusChanges[election.election_id] === getStatus(election)
                                                                }
                                                            >
                                                                Update
                                                            </button>
                                                        </div>
                                                    </td>
                                                    <td className="text-end pe-3">
                                                        <button
                                                            className="btn btn-sm btn-outline-secondary"
                                                            onClick={() => startEditing(election)}
                                                        >
                                                            <i className="bi bi-pencil me-1"></i>
                                                            Edit
                                                        </button>
                                                    </td>
                                                </tr>
                                            );
                                        })
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                <footer className="page-footer">
                    <span>BallotAdmin · Election Management</span>
                    <span>Secure access for authorised personal only</span>
                </footer>
            </main>
        </div>
    );
}
