/**
 * SkillBarter — Frontend Application Scripts
 * Vanilla JavaScript communicating with Java Servlets via Fetch API.
 */

const SkillBarter = {
  // Base API caller supporting GET, POST, PUT, DELETE
  async apiCall(url, method = 'GET', data = null) {
    const options = {
      method: method,
      headers: {
        'Accept': 'application/json'
      }
    };

    if (data) {
      options.headers['Content-Type'] = 'application/json';
      options.body = JSON.stringify(data);
    }

    try {
      const response = await fetch(url, options);
      const result = await response.json().catch(() => ({
        success: false,
        message: 'Non-JSON server response: ' + response.statusText
      }));

      return {
        status: response.status,
        ok: response.ok,
        data: result
      };
    } catch (err) {
      return {
        status: 0,
        ok: false,
        data: { success: false, message: 'Network connection error. Is the server running?' }
      };
    }
  },

  // Display alert messages (success, danger, info)
  showAlert(alertEl, message, type = 'info') {
    if (typeof alertEl === 'string') {
      alertEl = document.getElementById(alertEl);
    }
    if (!alertEl) return;

    alertEl.className = `alert alert-${type}`;
    alertEl.textContent = message;
    alertEl.style.display = 'block';

    if (type === 'success') {
      setTimeout(() => {
        if (alertEl) alertEl.style.display = 'none';
      }, 4000);
    }
  },

  // Clear alert message
  clearAlert(alertEl) {
    if (typeof alertEl === 'string') {
      alertEl = document.getElementById(alertEl);
    }
    if (alertEl) {
      alertEl.style.display = 'none';
      alertEl.textContent = '';
    }
  },

  // Check authentication status via /api/profile
  async checkAuth(redirectIfNotLoggedIn = true) {
    const res = await this.apiCall('api/profile');
    if (!res.ok || !res.data.success) {
      if (redirectIfNotLoggedIn) {
        window.location.href = 'login.html';
      }
      return null;
    }
    return res.data.data;
  },

  // Dynamic Navigation Bar Renderer
  renderNavbar(currentUser, activePage = '') {
    const nav = document.getElementById('mainNav');
    if (!nav) return;

    if (currentUser) {
      nav.innerHTML = `
        <a href="index.html" class="brand">
          <span class="brand-icon">SB</span> SkillBarter
        </a>
        <ul class="nav-links">
          <li><a href="dashboard.html" class="${activePage === 'dashboard' ? 'active' : ''}">Dashboard</a></li>
          <li><a href="skills.html" class="${activePage === 'skills' ? 'active' : ''}">My Skills</a></li>
          <li><a href="matches.html" class="${activePage === 'matches' ? 'active' : ''}">Matches</a></li>
          <li><span class="user-badge">${this.escapeHtml(currentUser.name)} (${currentUser.department || 'Student'})</span></li>
          <li><a href="javascript:void(0)" onclick="SkillBarter.logout()" class="btn btn-secondary btn-sm">Log Out</a></li>
        </ul>
      `;
    } else {
      nav.innerHTML = `
        <a href="index.html" class="brand">
          <span class="brand-icon">SB</span> SkillBarter
        </a>
        <ul class="nav-links">
          <li><a href="index.html" class="${activePage === 'home' ? 'active' : ''}">Home</a></li>
          <li><a href="login.html" class="${activePage === 'login' ? 'active' : ''}">Login</a></li>
          <li><a href="register.html" class="btn btn-primary btn-sm">Sign Up Free</a></li>
        </ul>
      `;
    }
  },

  // Logout handler
  async logout() {
    await this.apiCall('api/logout', 'POST');
    window.location.href = 'login.html';
  },

  // HTML sanitization helper to prevent XSS
  escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }
};
