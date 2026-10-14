import { useState } from 'react'
import { useNavigate } from 'react-router'

export default function Login() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')

  const navigate = useNavigate()


  async function handleLogin(event) {
    event.preventDefault()
    
    const response = await fetch(
      "http://localhost:5000/api/Auth/AdminLogin",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          id_number : username, //9001015009087
          password : password //0.123456,-0.234567,0.345678,-0.456789,0.56789,-0.678901,0.789012,-0.890123
        })
      }
    );

    const data = await response.json();

    if (response.ok) {
      sessionStorage.setItem("JWTToken", data.token)

      navigate('/Dashboard', { replace: true })
    }else{
      throw new Error(data.message);
    }

    console.log(data);


    // window.location.href = "/dashboard"

  }




  return (
    <div className="login-page">
      <main className="container min-vh-100 d-flex align-items-center justify-content-center py-5">
        <div className="login-shell row g-0 shadow-lg overflow-hidden">

          <section className="col-lg-6 login-intro p-5 d-none d-lg-flex flex-column justify-content-between">
            <div className="brand brand-light">
              <span className="brand-mark">B</span>
              <span>Ballot<span className="fw-light">Admin</span></span>
            </div>

            <div>
              <span className="eyebrow text-info">ELECTION OPERATIONS</span>
              <h1 className="display-5 fw-semibold mt-3">
                Trust starts with a secure process.
              </h1>
              <p className="text-white-50 mt-3">
                A central workspace for voter records, service health,
                and administrator access.
              </p>
            </div>

            <p className="small text-white-50 mb-0">
              Secure administration · South Africa
            </p>
          </section>

          <section className="col-lg-6 bg-white p-4 p-md-5 d-flex align-items-center">
            <div className="w-100">
              <span className="eyebrow">ADMINISTRATOR PORTAL</span>
              <h2 className="h2 fw-bold mt-2">Welcome back</h2>

              <p className="text-secondary mb-4">
                Sign in with your authorised administrator account.
              </p>

              <form onSubmit={handleLogin}>
                <div className="mb-3">
                  <label htmlFor="username" className="form-label">
                    Username
                  </label>

                  <input
                    id="username"
                    type="text"
                    className="form-control form-control-lg"
                    placeholder="Username"
                    value={username}
                    onChange={(event) => setUsername(event.target.value)}
                    required
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="password" className="form-label">
                    Password
                  </label>

                  <input
                    id="password"
                    type="password"
                    className="form-control form-control-lg"
                    placeholder="Enter your password"
                    value={password}
                    onChange={(event) => setPassword(event.target.value)}
                    required
                  />
                </div>

                <button
                  type="submit"
                  className="btn btn-primary btn-lg w-100"
                >
                  Sign in
                </button>
              </form>

              <div className="alert alert-light border small mt-4 mb-0">
                <strong>Restricted access.</strong>
                {' '}Public administrator registration is disabled.
                Contact an existing authorised admin for an account.
              </div>

              <p className="text-secondary small mt-4 mb-0">
                © 2026 BallotAdmin · Administrative access only
              </p>
            </div>
          </section>

        </div>
      </main>
    </div>)
}


