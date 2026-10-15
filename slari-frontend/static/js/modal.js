/* ============================================================
   SLARI — RESEARCH HUB
   modal.js: Modal open/close logic + variety detail modal HTML
   ============================================================ */

/* ============================
   MODAL CONTROLLER
   ============================ */
const Modal = {
  /** Open the modal overlay with given HTML content */
  open(contentHTML) {
    let overlay = document.getElementById('modal-overlay');
    if (!overlay) {
      overlay = document.createElement('div');
      overlay.id = 'modal-overlay';
      overlay.className = 'modal-overlay';
      overlay.addEventListener('click', (e) => {
        if (e.target === overlay) Modal.close();
      });
      document.body.appendChild(overlay);
    }
    overlay.innerHTML = contentHTML;
    overlay.style.display = 'flex';
    document.body.style.overflow = 'hidden';
  },

  /** Close / hide the modal */
  close() {
    const overlay = document.getElementById('modal-overlay');
    if (overlay) overlay.style.display = 'none';
    document.body.style.overflow = '';
  },
};

/* Keyboard close */
document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') Modal.close();
});

/* ============================
   VARIETY DETAIL MODAL
   ============================ */
function openVarietyModal(id) {
  const v = getVarietyById(id);
  if (!v) return;

  const user         = Auth.getUser();
  const isBookmarked = Bookmarks.has(v.id);
  const canBookmark  = user && user.role !== 'guest';
  const isAdmin      = user && user.role === 'admin';
  const isResearcher = user && user.role === 'researcher';

  Modal.open(`
    <div class="modal" role="dialog" aria-modal="true" aria-labelledby="modal-title">

      <div class="modal__header">
        <div>
          <h2 class="modal__title" id="modal-title">${v.name}</h2>
          <p class="modal__meta">${v.crop} · ${v.institute || 'SLARI'} · ${v.year}</p>
        </div>
        <button class="modal__close" onclick="Modal.close()" aria-label="Close">✕</button>
      </div>

      <div class="modal__body">
        <div class="detail-grid">

          <!-- LEFT COLUMN -->
          <div>
            <div class="detail-img" style="background:${getCropGradient(v.crop)}">${v.emoji}</div>

            <div class="variety-card__tags" style="margin-top:14px;">
              ${v.tags.map(t => `<span class="tag tag--green">${t}</span>`).join('')}
              <span class="tag tag--blue">📍 ${v.region}</span>
              <span class="tag tag--amber">🌧 ${v.season}</span>
              <span class="badge badge--${v.status === 'published' ? 'green' : v.status === 'pending' ? 'yellow' : 'gray'}"
                    style="margin-left:2px;">${v.status}</span>
            </div>

            <p style="font-size:13px;margin-top:14px;line-height:1.65;color:var(--text-2);">
              ${v.description}
            </p>

            <div class="insights-box">
              <div class="insights-box__title">🌟 Farmer Insights</div>
              ${v.insights.map(i => `
                <div class="insight-item">
                  <span class="insight-item__dot"></span>
                  ${i}
                </div>
              `).join('')}
            </div>
          </div>

          <!-- RIGHT COLUMN -->
          <div>
            <table class="detail-table">
              <tr><td>Yield</td>              <td>${v.yield} ${v.yieldUnit}</td></tr>
              <tr><td>Growth Duration</td>    <td>${v.duration}</td></tr>
              <tr><td>Disease Resistance</td> <td>${v.resistance}</td></tr>
              <tr><td>Region</td>             <td>${v.region}</td></tr>
              <tr><td>Cropping Season</td>    <td>${v.season}</td></tr>
              <tr><td>Soil Type</td>          <td>${v.soilType}</td></tr>
              <tr><td>Rainfall Needed</td>    <td>${v.rainfall}</td></tr>
              <tr><td>Altitude</td>           <td>${v.altitude}</td></tr>
              <tr><td>Researcher</td>         <td>${v.researcher}</td></tr>
              <tr><td>Institute</td>          <td>${v.institute || 'SLARI'}</td></tr>
              <tr><td>Year</td>               <td>${v.year}</td></tr>
            </table>

            <!-- Rating box -->
            <div style="margin-top:18px;padding:14px;background:var(--green-50);border-radius:var(--radius);">
              <div style="font-size:11px;font-weight:700;color:var(--text-3);text-transform:uppercase;letter-spacing:.5px;margin-bottom:8px;">
                Farmer Rating
              </div>
              <div class="rating-row">
                <div id="modal-stars">
                  ${[1,2,3,4,5].map(s => `
                    <button class="star ${s <= Math.round(v.rating) ? 'filled' : ''}"
                            onclick="submitRating(${v.id}, ${s})"
                            aria-label="Rate ${s} star${s>1?'s':''}">★</button>
                  `).join('')}
                </div>
                <span class="rating-score" id="modal-rating-score">${v.rating}</span>
                <span class="rating-count">(${v.ratingCount} reviews)</span>
              </div>
            </div>

            <!-- Extension contact -->
            <div style="margin-top:14px;padding:12px 14px;background:#e8f4fd;border-radius:var(--radius);">
              <div style="font-size:11px;font-weight:700;color:#0c5460;text-transform:uppercase;margin-bottom:4px;">
                📞 Get Help Planting
              </div>
              <div style="font-size:13px;color:#1558a0;line-height:1.5;">
                Contact your district extension office for planting advice and seed access.
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="modal__footer">
        ${canBookmark ? `
          <button class="btn btn--secondary btn--sm" id="modal-bookmark-btn"
                  onclick="handleModalBookmark(${v.id})">
            ${isBookmarked ? '🔖 Bookmarked' : '🔲 Save Variety'}
          </button>
        ` : ''}
        <button class="btn btn--ghost btn--sm"
                onclick="showToast('PDF download coming in the next release.','info')">
          📥 Download PDF
        </button>
        ${isAdmin && v.status === 'pending' ? `
          <button class="btn btn--primary btn--sm"
                  onclick="adminApprove(${v.id})">✅ Approve & Publish</button>
          <button class="btn btn--danger btn--sm"
                  onclick="adminReject(${v.id})">✕ Reject</button>
        ` : ''}
        ${isResearcher ? `
          <button class="btn btn--primary btn--sm"
                  onclick="showToast('Edit mode coming soon!','info')">✏️ Edit</button>
        ` : ''}
      </div>
    </div>
  `);
}

/* ============================
   MODAL ACTIONS
   ============================ */
function handleModalBookmark(id) {
  const added = Bookmarks.toggle(id);
  const btn   = document.getElementById('modal-bookmark-btn');
  if (btn) btn.textContent = added ? '🔖 Bookmarked' : '🔲 Save Variety';
  showToast(added ? '🔖 Variety saved to bookmarks!' : 'Removed from bookmarks.');
}

function submitRating(id, score) {
  rateVariety(id, score);
  const v = getVarietyById(id);
  if (!v) return;
  // Update stars in modal without reopening
  const starsEl = document.getElementById('modal-stars');
  const scoreEl = document.getElementById('modal-rating-score');
  if (starsEl) {
    starsEl.innerHTML = [1,2,3,4,5].map(s => `
      <button class="star ${s <= Math.round(v.rating) ? 'filled' : ''}"
              onclick="submitRating(${id}, ${s})">★</button>
    `).join('');
  }
  if (scoreEl) scoreEl.textContent = v.rating;
  showToast(`⭐ Thanks! You rated ${v.name} ${score}/5.`);
}

/* Admin actions called from inside the modal */
function adminApprove(id) {
  updateVarietyStatus(id, 'published');
  Modal.close();
  showToast('✅ Variety published successfully!');
  if (typeof renderDashboard === 'function') renderDashboard();
}
function adminReject(id) {
  updateVarietyStatus(id, 'draft');
  Modal.close();
  showToast('Submission returned to researcher as draft.');
  if (typeof renderDashboard === 'function') renderDashboard();
}
