/* ============================================================
   SLARI — RESEARCH HUB
   app.js: Application state, auth helpers, navigation config,
           page initialisation, filter state management
   ============================================================ */

/* ============================
   AUTH — persisted in localStorage
   ============================ */
const Auth = {
  /** Save user after login */
  login(name, role) {
    localStorage.setItem('slari_user', JSON.stringify({ name, role }));
  },

  /** Clear user on logout */
  logout() {
    localStorage.removeItem('slari_user');
    localStorage.removeItem('slari_bookmarks');
    window.location.href = 'login.html';
  },

  /** Get current logged-in user or null */
  getUser() {
    try {
      return JSON.parse(localStorage.getItem('slari_user')) || null;
    } catch { return null; }
  },

  /** True if a user is logged in */
  isLoggedIn() {
    return !!this.getUser();
  },

  /** Redirect to login if not authenticated */
  requireAuth() {
    if (!this.isLoggedIn()) {
      window.location.href = 'login.html';
    }
  },

  /** Redirect if the current role is not in the allowed list */
  requireRole(...roles) {
    const user = this.getUser();
    if (!user || !roles.includes(user.role)) {
      window.location.href = 'index.html';
    }
  },
};

/* ============================
   BOOKMARKS — persisted in localStorage
   ============================ */
const Bookmarks = {
  getAll() {
    try { return JSON.parse(localStorage.getItem('slari_bookmarks')) || []; }
    catch { return []; }
  },
  has(id) { return this.getAll().includes(Number(id)); },
  toggle(id) {
    const list = this.getAll();
    const numId = Number(id);
    const idx = list.indexOf(numId);
    if (idx === -1) list.push(numId);
    else list.splice(idx, 1);
    localStorage.setItem('slari_bookmarks', JSON.stringify(list));
    return idx === -1; // true = added, false = removed
  },
};

/* ============================
   RECOMMENDATIONS — persisted in sessionStorage
   ============================ */
const Recommendations = {
  getAll() {
    try { return JSON.parse(sessionStorage.getItem('slari_recommendations')) || []; }
    catch { return []; }
  },
  add(rec) {
    const list = this.getAll();
    list.unshift({ ...rec, date: new Date().toLocaleDateString() });
    sessionStorage.setItem('slari_recommendations', JSON.stringify(list));
  },
};

/* ============================
   NAVIGATION CONFIG (per role)
   ============================ */
const NAV_CONFIG = {
  guest: [
    { icon: '🏠', label: 'Home',            href: 'index.html' },
    { icon: '🔍', label: 'Browse Varieties', href: 'reseach-list.html' },
  ],
  farmer: [
    { icon: '🏠', label: 'Home',            href: 'index.html' },
    { icon: '🔍', label: 'Browse Varieties', href: 'reseach-list.html' },
    { icon: '🔖', label: 'My Bookmarks',     href: 'reseach-list.html?view=bookmarks' },
    { icon: '📊', label: 'Crop Insights',    href: 'reseach-list.html?view=insights' },
    { icon: '📋', label: 'My Dashboard',     href: 'dashboard.html' },
  ],
  researcher: [
    { icon: '🏠', label: 'Home',            href: 'index.html' },
    { icon: '📋', label: 'My Dashboard',     href: 'dashboard.html' },
    { icon: '➕', label: 'Upload Variety',   href: 'dashboard.html?view=upload' },
    { icon: '🔍', label: 'Browse All',       href: 'reseach-list.html' },
  ],
  admin: [
    { icon: '🏠', label: 'Home',            href: 'index.html' },
    { icon: '🛡️', label: 'Admin Panel',      href: 'dashboard.html' },
    { icon: '🔍', label: 'Browse All',       href: 'reseach-list.html' },
    { icon: '👥', label: 'Manage Users',     href: 'dashboard.html?view=users' },
  ],
  extension: [
    { icon: '🏠', label: 'Home',            href: 'index.html' },
    { icon: '🔍', label: 'Browse Varieties', href: 'reseach-list.html' },
    { icon: '📤', label: 'Recommend',        href: 'dashboard.html' },
    { icon: '👨‍🌾', label: 'My Farmers',     href: 'dashboard.html?view=farmers' },
  ],
};

/* ============================
   FILTER STATE
   (shared between research-list page and URL params)
   ============================ */
const FilterState = {
  query:    '',
  cropType: '',
  region:   '',
  season:   '',

  /** Read initial values from URL params */
  fromURL() {
    const p = new URLSearchParams(window.location.search);
    this.query    = p.get('q')    || '';
    this.cropType = p.get('crop') || '';
    this.region   = p.get('region') || '';
    this.season   = p.get('season') || '';
  },

  /** Apply all active filters to a varieties array */
  apply(varieties) {
    let list = [...varieties];
    if (this.cropType) list = list.filter(v => v.crop === this.cropType);
    if (this.region)   list = list.filter(v => v.region === this.region || v.region === 'All Zones');
    if (this.season)   list = list.filter(v => v.season === this.season || v.season === 'Bimodal');
    if (this.query) {
      const q = this.query.toLowerCase();
      list = list.filter(v =>
        v.name.toLowerCase().includes(q) ||
        v.crop.toLowerCase().includes(q) ||
        (v.description || '').toLowerCase().includes(q)
      );
    }
    return list;
  },

  hasActive() {
    return !!(this.query || this.cropType || this.region || this.season);
  },

  clear() {
    this.query = this.cropType = this.region = this.season = '';
  },
};

/* ============================
   PAGE HELPERS
   ============================ */

/** Read a URL query param */
function getParam(key) {
  return new URLSearchParams(window.location.search).get(key) || '';
}

/** Mark the current page's link as active in the sidebar */
function markActiveNav() {
  const current = window.location.pathname.split('/').pop();
  const search  = window.location.search;
  document.querySelectorAll('.sidebar__link').forEach(link => {
    const href = link.getAttribute('href') || '';
    const isActive =
      href === current ||
      href === current + search ||
      (href.startsWith(current) && href.includes(search) && search);
    link.classList.toggle('active', isActive);
  });
}

/** Show/hide elements based on user role */
function applyRoleVisibility() {
  const user = Auth.getUser();
  const role = user ? user.role : 'guest';
  document.querySelectorAll('[data-role]').forEach(el => {
    const allowed = el.dataset.role.split(',').map(s => s.trim());
    el.style.display = allowed.includes(role) ? '' : 'none';
  });
}
