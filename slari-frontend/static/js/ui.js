/* ============================================================
   SLARI — RESEARCH HUB
   ui.js: Shared UI functions — sidebar, toast, variety cards,
          and reusable page-section renderers
   ============================================================ */

/* ============================
   SIDEBAR RENDERER
   ============================ */
function renderSidebar() {
  const user = Auth.getUser() || { name: 'Guest', role: 'guest' };
  const navItems = NAV_CONFIG[user.role] || NAV_CONFIG.guest;
  const initials = user.name.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);

  /* Build nav links */
  const navHTML = navItems.map(item => `
    <a class="sidebar__link" href="${item.href}">
      <span class="sidebar__icon">${item.icon}</span>
      ${item.label}
    </a>
  `).join('');

  /* Build user chip */
  const userHTML = `
    <div class="user-chip">
      <div class="user-chip__avatar">${initials}</div>
      <div>
        <div class="user-chip__name">${user.name}</div>
        <div class="user-chip__role">${user.role}</div>
      </div>
    </div>
    <button class="sidebar__logout" onclick="Auth.logout()">↩ Logout / Switch Role</button>
  `;

  const navEl  = document.getElementById('sidebar-nav');
  const userEl = document.getElementById('sidebar-user');
  if (navEl)  navEl.innerHTML  = navHTML;
  if (userEl) userEl.innerHTML = userHTML;

  markActiveNav();
}

/* ============================
   SIDEBAR MOBILE TOGGLE
   ============================ */
function toggleSidebar() {
  document.getElementById('sidebar').classList.toggle('open');
  document.getElementById('sidebar-overlay').classList.toggle('open');
}
function closeSidebar() {
  document.getElementById('sidebar').classList.remove('open');
  document.getElementById('sidebar-overlay').classList.remove('open');
}

/* ============================
   TOAST NOTIFICATION
   ============================ */
let _toastTimer = null;
function showToast(message, type = 'info') {
  const el = document.getElementById('toast');
  if (!el) return;
  el.textContent = message;
  el.className = `toast toast--${type}`;
  el.style.display = 'flex';
  clearTimeout(_toastTimer);
  _toastTimer = setTimeout(() => { el.style.display = 'none'; }, 3400);
}

/* ============================
   VARIETY CARD HTML
   ============================ */
function varietyCardHTML(v, opts = {}) {
  const isBookmarked = Bookmarks.has(v.id);
  const user = Auth.getUser();
  const canBookmark = user && user.role !== 'guest';
  const { detailPage = 'reseach-list.html' } = opts;

  return `
    <div class="variety-card" onclick="openVarietyModal(${v.id})" role="button" tabindex="0"
         onkeydown="if(event.key==='Enter')openVarietyModal(${v.id})">
      <div class="variety-card__img" style="background:${getCropGradient(v.crop)}">
        <span>${v.emoji}</span>
        <span class="variety-card__crop-badge">${v.crop}</span>
        <span class="variety-card__status-badge status--${v.status}">${v.status}</span>
      </div>
      <div class="variety-card__body">
        <div class="variety-card__header">
          <div>
            <div class="variety-card__name">${v.name}</div>
            <div class="variety-card__researcher">${v.researcher}</div>
          </div>
          ${canBookmark ? `
            <button class="bookmark-btn ${isBookmarked ? 'bookmarked' : ''}"
                    title="${isBookmarked ? 'Remove bookmark' : 'Bookmark this variety'}"
                    onclick="handleBookmark(event, ${v.id})">${isBookmarked ? '🔖' : '🔲'}</button>
          ` : ''}
        </div>
        <div class="variety-card__tags">
          ${v.tags.map(t => `<span class="tag tag--green">${t}</span>`).join('')}
          <span class="tag tag--blue">📍 ${v.region}</span>
        </div>
        <div class="variety-card__stats">
          <div class="v-stat">
            <div class="v-stat__val">${v.yield} ${v.yieldUnit}</div>
            <div class="v-stat__lbl">Yield</div>
          </div>
          <div class="v-stat">
            <div class="v-stat__val">${v.duration}</div>
            <div class="v-stat__lbl">Duration</div>
          </div>
          <div class="v-stat">
            <div class="v-stat__val">⭐ ${v.rating}</div>
            <div class="v-stat__lbl">Rating</div>
          </div>
        </div>
      </div>
    </div>
  `;
}

/* ============================
   BOOKMARK HANDLER
   ============================ */
function handleBookmark(event, id) {
  event.stopPropagation(); // prevent card click
  const added = Bookmarks.toggle(id);
  showToast(added ? '🔖 Variety saved to bookmarks!' : 'Removed from bookmarks.');
  // Re-render bookmark button in place
  const btn = event.currentTarget;
  btn.className = `bookmark-btn ${added ? 'bookmarked' : ''}`;
  btn.textContent = added ? '🔖' : '🔲';
}

/* ============================
   EMPTY STATE HTML
   ============================ */
function emptyStateHTML(icon, title, desc, actionHTML = '') {
  return `
    <div class="empty-state">
      <div class="empty-state__icon">${icon}</div>
      <h3 class="empty-state__title">${title}</h3>
      <p class="empty-state__desc">${desc}</p>
      ${actionHTML ? `<div style="margin-top:18px;">${actionHTML}</div>` : ''}
    </div>
  `;
}

/* ============================
   STATS ROW HTML
   ============================ */
function statsRowHTML(stats) {
  return `
    <div class="stats-row">
      ${stats.map(s => `
        <div class="stat-card">
          <div class="stat-card__label">${s.label}</div>
          <div class="stat-card__value" ${s.color ? `style="color:${s.color}"` : ''}>${s.value}</div>
          ${s.sub ? `<div class="stat-card__sub">${s.sub}</div>` : ''}
        </div>
      `).join('')}
    </div>
  `;
}

/* ============================
   PAGE HEADER HTML
   ============================ */
function pageHeaderHTML(title, subtitle, actionHTML = '') {
  return `
    <div class="page-header ${actionHTML ? 'page-header--row' : ''}">
      <div>
        <h2 class="page-header__title">${title}</h2>
        ${subtitle ? `<p class="page-header__sub">${subtitle}</p>` : ''}
      </div>
      ${actionHTML ? `<div>${actionHTML}</div>` : ''}
    </div>
  `;
}

/* ============================
   TABS COMPONENT
   ============================ */
function renderTabs(tabs, activeIdx, containerId) {
  const container = document.getElementById(containerId + '-tabs');
  if (!container) return;
  container.innerHTML = tabs.map((label, i) => `
    <button class="tab-btn ${i === activeIdx ? 'active' : ''}"
            onclick="switchTab('${containerId}', ${i})">${label}</button>
  `).join('');
}

function switchTab(containerId, idx) {
  const container = document.getElementById(containerId);
  if (!container) return;
  container.querySelectorAll('.tab-btn').forEach((btn, i) =>
    btn.classList.toggle('active', i === idx)
  );
  container.querySelectorAll('.tab-panel').forEach((panel, i) =>
    panel.classList.toggle('active', i === idx)
  );
}

/* ============================
   SEARCH + FILTER BAR HTML
   ============================ */
function searchFilterHTML(fs) {
  return `
    <div class="search-bar">
      <input class="search-input" type="text"
             placeholder="🔍 Search by name, crop type, or keyword..."
             value="${fs.query}"
             oninput="onSearchInput(this.value)">
    </div>
    <div class="filter-row">
      <span class="filter-label">Filter:</span>
      <select class="filter-select" onchange="onFilterChange('cropType', this.value)">
        ${CROP_TYPES.map(c =>
          `<option value="${c === 'All Crops' ? '' : c}" ${fs.cropType === c || (fs.cropType === '' && c === 'All Crops') ? 'selected' : ''}>${c}</option>`
        ).join('')}
      </select>
      <select class="filter-select" onchange="onFilterChange('region', this.value)">
        ${REGIONS.map(r =>
          `<option value="${r === 'All Regions' ? '' : r}" ${fs.region === r || (fs.region === '' && r === 'All Regions') ? 'selected' : ''}>${r}</option>`
        ).join('')}
      </select>
      <select class="filter-select" onchange="onFilterChange('season', this.value)">
        ${SEASONS.map(s =>
          `<option value="${s === 'All Seasons' ? '' : s}" ${fs.season === s || (fs.season === '' && s === 'All Seasons') ? 'selected' : ''}>${s}</option>`
        ).join('')}
      </select>
      ${fs.hasActive() ? `
        <button class="btn btn--ghost btn--sm" onclick="clearFilters()">✕ Clear filters</button>
      ` : ''}
    </div>
  `;
}

/* ============================
   GLOBAL PAGE INIT
   (Called by every page on load)
   ============================ */
function initPage(options = {}) {
  const { title, requireAuth = false, requireRole = null } = options;

  // Auth guards
  if (requireAuth) Auth.requireAuth();
  if (requireRole) Auth.requireRole(...requireRole);

  // Set topbar title
  if (title) {
    const el = document.getElementById('page-title');
    if (el) el.textContent = title;
  }

  // Render sidebar
  renderSidebar();

  // Apply role-based element visibility
  applyRoleVisibility();
}
