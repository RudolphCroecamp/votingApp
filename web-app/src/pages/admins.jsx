
import { useCallback, useEffect, useState } from "react";
import Sidebar from "../components/sidebar";

// Change these paths to match your ASP.NET Core controller routes.
const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL || "http://localhost:5000";

const API = {
    getAdmins: "/api/Admin/GetAllAdmins",
    createAdmin: "/api/Admin/NewAdmin",
    updateAdmin: (id) => `/api/Admin/UpdateAdmin/${id}`,
};

const emptyNewAdmin = {
    id_number: "",
    password: "",
    firstname: "",
    lastname: "",
    province: "",
    city: "",
    postal: "",
};

const emptyUpdateAdmin = {
    firstname: "",
    lastname: "",
    allowed: true,
    role: "Admin",
    province: "",
    city: "",
    postal: "",
};

async function apiRequest(path, options = {}) {
    const token = sessionStorage.getItem("JWTToken");

    const response = await fetch(`${API_BASE_URL}${path}`, {
        ...options,
        headers: {
            ...(options.body ? { "Content-Type": "application/json" } : {}),
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
            ...options.headers,
        },
    });

    if (!response.ok) {
        const message = await response.text();
        throw new Error(
            message || `Request failed with status ${response.status}`
        );
    }

    if (response.status === 204) return null;

    const contentType = response.headers.get("content-type") || "";
    return contentType.includes("application/json")
        ? response.json()
        : null;
}

function getErrorMessage(error) {
    return error instanceof Error ? error.message : "An unexpected error occurred.";
}

function getAdminList(data) {
    // Supports either a direct array or a wrapped API response.
    if (Array.isArray(data)) return data;
    if (Array.isArray(data?.admins)) return data.admins;
    if (Array.isArray(data?.data)) return data.data;
    return [];
}

function formatDate(date) {
    if (!date) return "—";

    const parsed = new Date(date);
    if (Number.isNaN(parsed.getTime())) return "—";

    return parsed.toLocaleString("en-ZA", {
        dateStyle: "medium",
        timeStyle: "short",
    });
}

export default function AdminDash() {
    const [admins, setAdmins] = useState([]);
    const [search, setSearch] = useState("");

    const [showAddForm, setShowAddForm] = useState(false);
    const [newAdmin, setNewAdmin] = useState(emptyNewAdmin);

    const [editingAdmin, setEditingAdmin] = useState(null);
    const [editForm, setEditForm] = useState(emptyUpdateAdmin);

    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    const loadAdmins = useCallback(async () => {
        setLoading(true);
        setError("");

        try {
            const data = await apiRequest(API.getAdmins);
            setAdmins(getAdminList(data));
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadAdmins();
    }, [loadAdmins]);

    const filteredAdmins = admins.filter((admin) =>
        [
            admin.firstname,
            admin.lastname,
            admin.role,
            admin.user_id,
        ]
            .filter(Boolean)
            .join(" ")
            .toLowerCase()
            .includes(search.toLowerCase())
    );

    function updateNewAdmin(event) {
        const { name, value } = event.target;

        setNewAdmin((previous) => ({
            ...previous,
            [name]: value,
        }));
    }

    function updateEditForm(event) {
        const { name, value, type, checked } = event.target;

        setEditForm((previous) => ({
            ...previous,
            [name]: type === "checkbox" ? checked : value,
        }));
    }

    async function handleCreateAdmin(event) {
        event.preventDefault();
        setSaving(true);
        setError("");
        setSuccess("");

        try {
            const payload = {
                ...newAdmin,
                postal: Number(newAdmin.postal),
            };

            await apiRequest(API.createAdmin, {
                method: "POST",
                body: JSON.stringify(payload),
            });

            setNewAdmin(emptyNewAdmin);
            setShowAddForm(false);
            setSuccess("Administrator created successfully.");

            await loadAdmins();
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setSaving(false);
        }
    }

    function startEditing(admin) {
        setEditingAdmin(admin);

        setEditForm({
            firstname: admin.firstname ?? "",
            lastname: admin.lastname ?? "",
            allowed: admin.allowed ?? false,
            role: admin.role ?? "Admin",
            province: admin.province ?? "",
            city: admin.city ?? "",
            postal: String(admin.postal ?? ""),
        });

        setError("");
        setSuccess("");
    }

    async function handleUpdateAdmin(event) {
        event.preventDefault();

        if (!editingAdmin) return;

        setSaving(true);
        setError("");
        setSuccess("");

        try {
            const payload = {
                firstname: editForm.firstname.trim(),
                lastname: editForm.lastname.trim(),
                allowed: editForm.allowed,
                role: editForm.role,
                province: editForm.province.trim(),
                city: editForm.city.trim(),
                postal: Number(editForm.postal),
            };

            await apiRequest(API.updateAdmin(editingAdmin.user_id), {
                method: "PUT",
                body: JSON.stringify(payload),
            });

            setEditingAdmin(null);
            setSuccess("Administrator updated successfully.");

            await loadAdmins();
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setSaving(false);
        }
    }

    return (
        <div className="admin-layout">
            <Sidebar />

            <main className="admin-content">
                <header className="topbar">
                    <div>
                        <div className="text-secondary small">
                            Workspace / Access control
                        </div>
                        <h1 className="h4 mb-0 mt-1">Administrators</h1>
                    </div>

                    <div className="avatar avatar-sm" title="Current administrator">
                        AD
                    </div>
                </header>

                <div className="content-wrap">
                    <div className="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
                        <div>
                            <p className="text-secondary small fw-semibold mb-1">
                                ACCESS CONTROL
                            </p>

                            <h2 className="h3 fw-bold mb-1">
                                Administrator management
                            </h2>

                            <p className="text-secondary mb-0">
                                Create accounts, update details, and manage access.
                            </p>
                        </div>

                        <button
                            className="btn btn-primary"
                            type="button"
                            onClick={() => {
                                setShowAddForm((value) => !value);
                                setEditingAdmin(null);
                                setError("");
                            }}
                        >
                            {showAddForm ? "− Hide form" : "＋ Add administrator"}
                        </button>
                    </div>

                    {error && (
                        <div className="alert alert-danger" role="alert">
                            <strong>Request failed:</strong> {error}
                            <button
                                type="button"
                                className="btn btn-sm btn-outline-danger ms-3"
                                onClick={loadAdmins}
                            >
                                Retry
                            </button>
                        </div>
                    )}

                    {success && (
                        <div className="alert alert-success" role="status">
                            {success}
                        </div>
                    )}

                    <div className="alert alert-primary border-0 d-flex gap-3 align-items-start admin-notice">
                        <span className="fs-4">ⓘ</span>
                        <div>
                            <strong>Protected administrator access</strong>
                            <div className="small mt-1">
                                Administrator creation and modification must be
                                authorized by your ASP.NET Core API.
                            </div>
                        </div>
                    </div>

                    {showAddForm && (
                        <section className="panel mb-4">
                            <div className="panel-heading">
                                <div>
                                    <h3 className="h6 fw-bold mb-1">
                                        Add an administrator
                                    </h3>
                                    <p className="text-secondary small mb-0">
                                        Create an account using your newAdminDTO.
                                    </p>
                                </div>
                                <span className="pill pill-neutral">Admin only</span>
                            </div>

                            <form className="p-4" onSubmit={handleCreateAdmin}>
                                <div className="row g-3">
                                    <div className="col-md-6">
                                        <label className="form-label" htmlFor="id_number">
                                            ID number
                                        </label>
                                        <input
                                            id="id_number"
                                            name="id_number"
                                            className="form-control"
                                            value={newAdmin.id_number}
                                            onChange={updateNewAdmin}
                                            required
                                        />
                                    </div>

                                    <div className="col-md-6">
                                        <label className="form-label" htmlFor="password">
                                            Initial password
                                        </label>
                                        <input
                                            id="password"
                                            name="password"
                                            type="password"
                                            className="form-control"
                                            autoComplete="new-password"
                                            value={newAdmin.password}
                                            onChange={updateNewAdmin}
                                            minLength={12}
                                            required
                                        />
                                    </div>

                                    <div className="col-md-6">
                                        <label className="form-label" htmlFor="firstname">
                                            First name
                                        </label>
                                        <input
                                            id="firstname"
                                            name="firstname"
                                            className="form-control"
                                            value={newAdmin.firstname}
                                            onChange={updateNewAdmin}
                                            required
                                        />
                                    </div>

                                    <div className="col-md-6">
                                        <label className="form-label" htmlFor="lastname">
                                            Last name
                                        </label>
                                        <input
                                            id="lastname"
                                            name="lastname"
                                            className="form-control"
                                            value={newAdmin.lastname}
                                            onChange={updateNewAdmin}
                                            required
                                        />
                                    </div>

                                    <div className="col-md-6">
                                        <label className="form-label" htmlFor="province">
                                            Province
                                        </label>
                                        <select
                                            id="province"
                                            name="province"
                                            className="form-select"
                                            value={newAdmin.province}
                                            onChange={updateNewAdmin}
                                            required
                                        >
                                            <option value="">Select province</option>
                                            <option>Eastern Cape</option>
                                            <option>Free State</option>
                                            <option>Gauteng</option>
                                            <option>KwaZulu-Natal</option>
                                            <option>Limpopo</option>
                                            <option>Mpumalanga</option>
                                            <option>Northern Cape</option>
                                            <option>North West</option>
                                            <option>Western Cape</option>
                                        </select>
                                    </div>

                                    <div className="col-md-6">
                                        <label className="form-label" htmlFor="city">
                                            City
                                        </label>
                                        <input
                                            id="city"
                                            name="city"
                                            className="form-control"
                                            value={newAdmin.city}
                                            onChange={updateNewAdmin}
                                            required
                                        />
                                    </div>

                                    <div className="col-md-6">
                                        <label className="form-label" htmlFor="postal">
                                            Postal code
                                        </label>
                                        <input
                                            id="postal"
                                            name="postal"
                                            type="number"
                                            className="form-control"
                                            value={newAdmin.postal}
                                            onChange={updateNewAdmin}
                                            min="0"
                                            max="9999"
                                            required
                                        />
                                    </div>

                                    <div className="col-12 d-flex justify-content-end gap-2 mt-4">
                                        <button
                                            className="btn btn-light"
                                            type="button"
                                            onClick={() => setNewAdmin(emptyNewAdmin)}
                                        >
                                            Clear
                                        </button>

                                        <button
                                            className="btn btn-primary"
                                            type="submit"
                                            disabled={saving}
                                        >
                                            {saving ? "Creating..." : "Create administrator"}
                                        </button>
                                    </div>
                                </div>
                            </form>
                        </section>
                    )}

                    {editingAdmin && (
                        <section className="panel mb-4">
                            <div className="panel-heading">
                                <div>
                                    <h3 className="h6 fw-bold mb-1">
                                        Edit administrator
                                    </h3>
                                    <p className="text-secondary small mb-0">
                                        {editingAdmin.firstname} {editingAdmin.lastname}
                                    </p>
                                </div>

                                <button
                                    className="btn btn-sm btn-light"
                                    type="button"
                                    onClick={() => setEditingAdmin(null)}
                                >
                                    Cancel
                                </button>
                            </div>

                            <form className="p-4" onSubmit={handleUpdateAdmin}>
                                <div className="row g-3">
                                    <div className="col-md-6">
                                        <label className="form-label" htmlFor="edit-firstname">
                                            First name
                                        </label>
                                        <input
                                            id="edit-firstname"
                                            name="firstname"
                                            className="form-control"
                                            value={editForm.firstname}
                                            onChange={updateEditForm}
                                            required
                                        />
                                    </div>

                                    <div className="col-md-6">
                                        <label className="form-label" htmlFor="edit-lastname">
                                            Last name
                                        </label>
                                        <input
                                            id="edit-lastname"
                                            name="lastname"
                                            className="form-control"
                                            value={editForm.lastname}
                                            onChange={updateEditForm}
                                            required
                                        />
                                    </div>

                                    <div className="col-md-6">
                                        <label className="form-label" htmlFor="edit-province">
                                            Province
                                        </label>
                                        <select
                                            id="edit-province"
                                            name="province"
                                            className="form-select"
                                            value={editForm.province}
                                            onChange={updateEditForm}
                                            required
                                        >
                                            <option value="">Select province</option>
                                            <option>Eastern Cape</option>
                                            <option>Free State</option>
                                            <option>Gauteng</option>
                                            <option>KwaZulu-Natal</option>
                                            <option>Limpopo</option>
                                            <option>Mpumalanga</option>
                                            <option>Northern Cape</option>
                                            <option>North West</option>
                                            <option>Western Cape</option>
                                        </select>
                                    </div>

                                    <div className="col-md-6">
                                        <label className="form-label" htmlFor="edit-city">
                                            City
                                        </label>
                                        <input
                                            id="edit-city"
                                            name="city"
                                            className="form-control"
                                            value={editForm.city}
                                            onChange={updateEditForm}
                                            required
                                        />
                                    </div>

                                    <div className="col-md-6">
                                        <label className="form-label" htmlFor="edit-postal">
                                            Postal code
                                        </label>
                                        <input
                                            id="edit-postal"
                                            name="postal"
                                            type="number"
                                            className="form-control"
                                            value={editForm.postal}
                                            onChange={updateEditForm}
                                            min="0"
                                            max="9999"
                                            required
                                        />
                                    </div>

                                    <div className="col-md-6">
                                        <label className="form-label" htmlFor="edit-role">
                                            Role
                                        </label>
                                        <select
                                            id="edit-role"
                                            name="role"
                                            className="form-select"
                                            value={editForm.role}
                                            onChange={updateEditForm}
                                            required
                                        >
                                            <option value="Admin">Admin</option>
                                            <option value="Voter">Voter</option>
                                        </select>
                                    </div>

                                    <div className="col-12">
                                        <div className="form-check form-switch">
                                            <input
                                                className="form-check-input"
                                                type="checkbox"
                                                id="edit-allowed"
                                                name="allowed"
                                                checked={editForm.allowed}
                                                onChange={updateEditForm}
                                            />
                                            <label
                                                className="form-check-label"
                                                htmlFor="edit-allowed"
                                            >
                                                Account allowed to access the system
                                            </label>
                                        </div>
                                    </div>

                                    <div className="col-12 d-flex justify-content-end gap-2 mt-4">
                                        <button
                                            className="btn btn-light"
                                            type="button"
                                            onClick={() => setEditingAdmin(null)}
                                        >
                                            Cancel
                                        </button>

                                        <button
                                            className="btn btn-primary"
                                            type="submit"
                                            disabled={saving}
                                        >
                                            {saving ? "Saving..." : "Save changes"}
                                        </button>
                                    </div>
                                </div>
                            </form>
                        </section>
                    )}

                    <section className="panel">
                        <div className="panel-heading">
                            <div>
                                <h3 className="h6 fw-bold mb-1">
                                    Current administrators
                                </h3>
                                <p className="text-secondary small mb-0">
                                    {filteredAdmins.length} accounts
                                </p>
                            </div>

                            <input
                                className="form-control form-control-sm search-input"
                                type="search"
                                placeholder="Search admins..."
                                aria-label="Search administrators"
                                value={search}
                                onChange={(event) => setSearch(event.target.value)}
                            />
                        </div>

                        <div className="table-responsive">
                            <table className="table table-hover align-middle mb-0">
                                <thead>
                                    <tr>
                                        <th>Administrator</th>
                                        <th>Role</th>
                                        <th>Created</th>
                                        <th>Status</th>
                                        <th className="text-end">Actions</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {loading ? (
                                        <tr>
                                            <td colSpan="5" className="text-center py-5">
                                                <div
                                                    className="spinner-border spinner-border-sm me-2"
                                                    role="status"
                                                />
                                                Loading administrators...
                                            </td>
                                        </tr>
                                    ) : filteredAdmins.length === 0 ? (
                                        <tr>
                                            <td
                                                colSpan="5"
                                                className="text-center text-secondary py-5"
                                            >
                                                No administrators found.
                                            </td>
                                        </tr>
                                    ) : (
                                        filteredAdmins.map((admin) => (
                                            <tr key={admin.user_id}>
                                                <td>
                                                    <div className="d-flex align-items-center gap-2">
                                                        <div className="avatar avatar-table">
                                                            {`${admin.firstname?.[0] ?? ""}${admin.lastname?.[0] ?? ""}`.toUpperCase()}
                                                        </div>

                                                        <div>
                                                            <div className="fw-semibold">
                                                                {admin.firstname} {admin.lastname}
                                                            </div>
                                                            <div className="small text-secondary">
                                                                {admin.user_id}
                                                            </div>
                                                        </div>
                                                    </div>
                                                </td>

                                                <td>{admin.role}</td>

                                                <td>{formatDate(admin.created_at)}</td>

                                                <td>
                                                    <span
                                                        className={
                                                            admin.allowed
                                                                ? "pill pill-success"
                                                                : "pill pill-neutral"
                                                        }
                                                    >
                                                        {admin.allowed ? "Allowed" : "Disabled"}
                                                    </span>
                                                </td>

                                                <td className="text-end">
                                                    <button
                                                        className="btn btn-sm btn-outline-primary"
                                                        type="button"
                                                        onClick={() => startEditing(admin)}
                                                    >
                                                        Edit
                                                    </button>
                                                </td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </table>
                        </div>

                        <div className="px-4 py-3 border-top text-secondary small">
                            Administrator details are retrieved from your API.
                        </div>
                    </section>

                    <footer className="page-footer">
                        <span>BallotAdmin · Administrator management</span>
                        <span>Secure access for authorised administrators</span>
                    </footer>
                </div>
            </main>
        </div>
    );
}
