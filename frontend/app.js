// AiContentGenerator — frontend logic

const API_BASE = "http://localhost:8080/api/v1";

/* =========================================================
   STORAGE
========================================================= */

const Storage = {
  get token() {
    return localStorage.getItem("token");
  },

  set token(value) {
    if (value) {
      localStorage.setItem("token", value);
    } else {
      localStorage.removeItem("token");
    }
  },

  get refreshToken() {
    return localStorage.getItem("refreshToken");
  },

  set refreshToken(value) {
    if (value) {
      localStorage.setItem("refreshToken", value);
    } else {
      localStorage.removeItem("refreshToken");
    }
  },

  get user() {
    try {
      return JSON.parse(localStorage.getItem("user") || "null");
    } catch {
      return null;
    }
  },

  set user(value) {
    if (value) {
      localStorage.setItem("user", JSON.stringify(value));
    } else {
      localStorage.removeItem("user");
    }
  }
};

/* =========================================================
   HELPERS
========================================================= */

function showAlert(el, msg, type = "error") {
  if (!el) return;

  el.textContent = msg;
  el.className = `alert show alert-${type}`;
}

function clearAlert(el) {
  if (!el) return;

  el.className = "alert";
}

function redirectToLogin() {
  window.location.href = "login.html";
}

function requireAuth() {
  if (!Storage.token) {
    redirectToLogin();
  }
}

function safeJSON(text) {
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

function escapeHtml(str) {
  return String(str ?? "").replace(/[&<>"']/g, (char) => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#39;"
  }[char]));
}

function formatTime(time) {
  if (!time) return "";

  const d = new Date(time);

  if (isNaN(d)) {
    return String(time);
  }

  return d.toLocaleString();
}

function setLoading(btn, loading, text = "Loading...") {
  if (!btn) return;

  btn.disabled = loading;

  if (loading) {
    btn.dataset.original = btn.innerHTML;
    btn.innerHTML = `<span class="spinner"></span> ${text}`;
  } else {
    btn.innerHTML = btn.dataset.original || "Submit";
  }
}

function unwrapResponse(data) {
  if (!data) return null;

  // Handles ApiResponse<T>
  if (data.data !== undefined) {
    return data.data;
  }

  return data;
}

/* =========================================================
   API
========================================================= */

async function api(path, { method = "GET", body, auth = true } = {}) {

  const headers = {
    "Content-Type": "application/json"
  };

  if (auth) {
    const token = Storage.token;

    if (!token) {
      redirectToLogin();
      throw new Error("No token found");
    }

    headers["Authorization"] = `Bearer ${token}`;
  }

  let response;

  try {
    response = await fetch(`${API_BASE}${path}`, {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined
    });
  } catch (err) {
    throw new Error("Cannot connect to backend server");
  }

  if (response.status === 401 && auth) {
    Storage.token = null;
    Storage.refreshToken = null;
    Storage.user = null;

    redirectToLogin();

    throw new Error("Session expired");
  }

  const text = await response.text();

  const data = text ? safeJSON(text) : null;

  if (!response.ok) {
    const message =
      data?.message ||
      data?.error ||
      `Request failed (${response.status})`;

    throw new Error(message);
  }

  return data;
}

/* =========================================================
   AUTH
========================================================= */

async function login(email, password) {

  const response = await api("/auth/login", {
    method: "POST",
    body: {
      email,
      password
    },
    auth: false
  });

  console.log("LOGIN RESPONSE:", response);

  // Supports ApiResponse<AuthResponse>
  const data = unwrapResponse(response);

  const token =
    data?.accessToken ||
    data?.token ||
    data?.jwt;

  if (!token) {
    console.error("Missing token:", response);
    throw new Error("No token returned by server");
  }

  Storage.token = token;

  if (data.refreshToken) {
    Storage.refreshToken = data.refreshToken;
  }

  Storage.user = {
    email: data.email || email,
    username:
      data.username ||
      email.split("@")[0]
  };

  window.location.href = "dashboard.html";
}

async function register(username, email, password) {

  await api("/auth/register", {
    method: "POST",
    body: {
      username,
      email,
      password
    },
    auth: false
  });

  window.location.href = "login.html?registered=1";
}

function logout() {
  Storage.token = null;
  Storage.refreshToken = null;
  Storage.user = null;

  redirectToLogin();
}

/* =========================================================
   CONTENT
========================================================= */

async function generateContent(prompt) {

  return api("/content/create", {
    method: "POST",
    body: {
      prompt
    }
  });
}

async function fetchHistory() {
  return api("/content");
}

function extractText(data) {

  const payload = unwrapResponse(data);

  if (!payload) return "";

  if (typeof payload === "string") {
    return payload;
  }

  return (
    payload.result ||
    payload.content ||
    payload.text ||
    payload.generated ||
    payload.output ||
    JSON.stringify(payload, null, 2)
  );
}

/* =========================================================
   LOGIN PAGE
========================================================= */

function initLoginPage() {

  if (Storage.token) {
    window.location.href = "dashboard.html";
    return;
  }

  const form = document.getElementById("login-form");
  const alertEl = document.getElementById("alert");

  const params = new URLSearchParams(window.location.search);

  if (params.get("registered")) {
    showAlert(
      alertEl,
      "Account created successfully. Please sign in.",
      "success"
    );
  }

  form.addEventListener("submit", async (e) => {

    e.preventDefault();

    clearAlert(alertEl);

    const email = form.email.value.trim();
    const password = form.password.value;

    if (!email || !password) {
      return showAlert(alertEl, "Please fill in all fields.");
    }

    const btn = form.querySelector("button[type=submit]");

    setLoading(btn, true, "Signing in...");

    try {
      await login(email, password);
    } catch (err) {
      showAlert(alertEl, err.message);
      setLoading(btn, false);
    }
  });
}

/* =========================================================
   REGISTER PAGE
========================================================= */

function initRegisterPage() {

  if (Storage.token) {
    window.location.href = "dashboard.html";
    return;
  }

  const form = document.getElementById("register-form");
  const alertEl = document.getElementById("alert");

  form.addEventListener("submit", async (e) => {

    e.preventDefault();

    clearAlert(alertEl);

    const username = form.username.value.trim();
    const email = form.email.value.trim();
    const password = form.password.value;

    if (!username || !email || !password) {
      return showAlert(alertEl, "Please fill in all fields.");
    }

    if (password.length < 6) {
      return showAlert(
        alertEl,
        "Password must be at least 6 characters."
      );
    }

    const btn = form.querySelector("button[type=submit]");

    setLoading(btn, true, "Creating account...");

    try {
      await register(username, email, password);
    } catch (err) {
      showAlert(alertEl, err.message);
      setLoading(btn, false);
    }
  });
}

/* =========================================================
   DASHBOARD PAGE
========================================================= */

function initDashboardPage() {

  requireAuth();

  const user = Storage.user || {};

  const emailEl = document.getElementById("user-email");
  const nameEl = document.getElementById("user-name");
  const avatarEl = document.getElementById("user-avatar");

  const displayName =
    user.username ||
    (user.email
      ? user.email.split("@")[0]
      : "User");

  if (nameEl) {
    nameEl.textContent = displayName;
  }

  if (emailEl) {
    emailEl.textContent = user.email || "";
  }

  if (avatarEl) {
    avatarEl.textContent =
      (displayName[0] || "U").toUpperCase();
  }

  // NAVIGATION

  document
    .querySelectorAll(".nav-item[data-view]")
    .forEach((btn) => {
      btn.addEventListener("click", () => {
        switchView(btn.dataset.view);
      });
    });

  document
    .getElementById("logout-btn")
    .addEventListener("click", logout);

  // GENERATE FORM

  const form = document.getElementById("generate-form");

  const resultBox = document.getElementById("result-box");

  const alertEl = document.getElementById("generate-alert");

  form.addEventListener("submit", async (e) => {

    e.preventDefault();

    clearAlert(alertEl);

    const prompt = form.prompt.value.trim();

    if (!prompt) {
      return showAlert(alertEl, "Please enter a prompt.");
    }

    const btn = form.querySelector("button[type=submit]");

    setLoading(btn, true, "Generating...");

    resultBox.className = "result-box";

    resultBox.innerHTML =
      '<div class="spinner spinner-lg"></div>';

    try {

      const response = await generateContent(prompt);

      const text = extractText(response);

      resultBox.textContent =
        text || "(No content returned)";

      loadHistory().catch(() => {});

    } catch (err) {

      resultBox.className = "result-box empty";

      resultBox.textContent = "";

      showAlert(alertEl, err.message);

    } finally {

      setLoading(btn, false);
    }
  });

  loadHistory();
}

/* =========================================================
   HISTORY
========================================================= */

async function loadHistory() {

  const list = document.getElementById("history-list");

  const countEl = document.getElementById("stat-total");

  const latestEl = document.getElementById("stat-latest");

  if (list) {
    list.innerHTML =
      '<div class="spinner spinner-lg"></div>';
  }

  try {

    const response = await fetchHistory();

    const payload = unwrapResponse(response);

    const items = Array.isArray(payload)
      ? payload
      : payload?.items || payload?.content || [];

    if (countEl) {
      countEl.textContent = items.length;
    }

    if (latestEl) {
      latestEl.textContent = items[0]
        ? formatTime(
            items[0].createdAt ||
            items[0].timestamp
          )
        : "—";
    }

    if (!list) return;

    if (!items.length) {

      list.innerHTML = `
        <div class="empty-state">
          <h3>No content yet</h3>
          <p>Generate your first AI content.</p>
        </div>
      `;

      return;
    }

    list.innerHTML = items.map((item) => `

      <div class="history-item">

        <div class="history-prompt">
          ${escapeHtml(item.prompt || "Untitled")}
        </div>

        <div class="history-result">
          ${escapeHtml(extractText(item))}
        </div>

        <div class="history-time">
          ${formatTime(item.createdAt || item.timestamp)}
        </div>

      </div>

    `).join("");

  } catch (err) {

    if (list) {
      list.innerHTML = `
        <div class="empty-state">
          <h3>Failed to load history</h3>
          <p>${escapeHtml(err.message)}</p>
        </div>
      `;
    }
  }
}

/* =========================================================
   VIEW SWITCHING
========================================================= */

function switchView(name) {

  document
    .querySelectorAll(".view")
    .forEach((view) => {
      view.classList.toggle(
        "active",
        view.dataset.view === name
      );
    });

  document
    .querySelectorAll(".nav-item[data-view]")
    .forEach((btn) => {
      btn.classList.toggle(
        "active",
        btn.dataset.view === name
      );
    });

  if (name === "history") {
    loadHistory();
  }
}

/* =========================================================
   EXPORT
========================================================= */

window.AiApp = {
  initLoginPage,
  initRegisterPage,
  initDashboardPage
};