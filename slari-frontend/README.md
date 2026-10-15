# 🌱 SLARI — Agricultural Research Hub

> Sierra Leone Agricultural Research Institute  
> Web-based crop variety dissemination platform

---

## Project Structure

```
RESEARCH HUB/
├── static/
│   ├── css/
│   │   ├── main.css          # CSS variables, reset, base typography
│   │   ├── layout.css        # Sidebar, topbar, hero, page wrappers, responsive
│   │   └── component.css     # Buttons, cards, forms, tables, modals, badges, toast
│   ├── images/               # Crop images, logos (add your own)
│   └── js/
│       ├── api.js            # Data layer — VARIETIES, FARMERS + access functions
│       ├── app.js            # Auth, bookmarks, nav config, filter state, page helpers
│       ├── ui.js             # Sidebar render, toast, variety card HTML, page init
│       └── modal.js          # Modal open/close + variety detail modal
├── templates/
│   ├── fragments/
│   │   ├── nabar.html        # Sidebar/navigation fragment (reference)
│   │   ├── footer.html       # Footer fragment (reference)
│   │   └── modal.html        # Modal overlay fragment (reference)
│   ├── layout/
│   │   └── base.html         # Master layout template — copy this to make new pages
│   └── pages/
│       ├── index.html        # Home / Landing page
│       ├── login.html        # Login (role selector + credentials)
│       ├── register.html     # New user registration
│       ├── reseach-list.html # Browse varieties + bookmarks + crop insights
│       └── dashboard.html    # Role-based dashboard:
│                             #   Researcher → My Varieties | Analytics | Pending | Upload
│                             #   Admin      → Pending | All Varieties | Users | Stats
│                             #   Extension  → Recommend | Sent | My Farmers
│                             #   Farmer     → Bookmarks | Insights
└── README.md
```

---

## Pages & Their Purpose

| Page | URL | Who uses it |
|------|-----|-------------|
| `index.html` | `/` | Everyone — hero, featured varieties, how it works |
| `login.html` | `/login` | All roles — sign in with role selector |
| `register.html` | `/register` | New users — role-specific registration form |
| `reseach-list.html` | `/varieties` | Farmers & public — browse, filter, bookmark, insights |
| `dashboard.html` | `/dashboard` | Researcher / Admin / Extension / Farmer — role-aware |

---

## JavaScript Architecture

| File | Responsibility |
|------|---------------|
| `api.js` | All data. **Replace mock arrays with `fetch()` calls in production.** |
| `app.js` | `Auth` (localStorage), `Bookmarks`, `Recommendations`, `NAV_CONFIG`, `FilterState` |
| `ui.js` | `renderSidebar()`, `showToast()`, `varietyCardHTML()`, `initPage()` |
| `modal.js` | `Modal.open()`, `Modal.close()`, `openVarietyModal()` |

**Script load order on every page (mandatory):**
```html
<script src="../../static/js/api.js"></script>
<script src="../../static/js/app.js"></script>
<script src="../../static/js/ui.js"></script>
<script src="../../static/js/modal.js"></script>
```

---

## User Roles

| Role | Access |
|------|--------|
| `guest` | Browse published varieties only |
| `farmer` | Browse + bookmark + insights + dashboard |
| `researcher` | Upload varieties + dashboard + analytics |
| `extension` | Recommend varieties to farmers |
| `admin` | Approve/reject submissions + manage users |

---

## Adding a New Page

1. Copy `templates/layout/base.html` to `templates/pages/your-page.html`
2. Update `<title>`, `id="page-title"`, and topbar content
3. Fill `<div id="page-content">` with your HTML
4. Add your JS logic inside the `<script>` block at the bottom
5. Call `initPage({ title: 'Your Page Title', requireAuth: true })` on load
6. Add the page link to the appropriate role in `NAV_CONFIG` inside `app.js`

---

## Moving to Production

1. **Backend:** Replace `api.js` mock data with `fetch()` calls to a REST API (Node.js/Express or Python/Django)
2. **Database:** Use the schema in the build guide — PostgreSQL recommended
3. **Auth:** Replace `localStorage` login with JWT tokens + HTTP-only cookies
4. **Files:** Connect `upload-zone` to AWS S3 or Cloudinary for images/PDFs
5. **Deploy:** Host on Render.com, Railway.app, or any VPS

---

## How to Run Locally

Open with **VS Code Live Server** extension (right-click `index.html` → Open with Live Server).  
Or use any local server — Python's built-in works:

```bash
cd templates/pages
python -m http.server 8080
# Then open http://localhost:8080/index.html
```

---

*Built for SLARI — Sierra Leone Agricultural Research Institute*
