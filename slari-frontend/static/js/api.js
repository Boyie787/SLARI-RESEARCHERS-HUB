/* ============================================================
   SLARI — RESEARCH HUB
   api.js: Data layer — all mock data + data access functions
   In production, replace these with real fetch() API calls
   ============================================================ */

/* ============================
   VARIETIES DATABASE
   ============================ */
const VARIETIES = [
  {
    id: 1, name: 'SLARI-CASS-001', crop: 'Cassava', emoji: '🌿',
    yield: '24', yieldUnit: 't/ha', duration: '12 months',
    resistance: 'CMD, CBSD', region: 'Southern', season: 'Bimodal',
    researcher: 'Dr. Amara Koroma', institute: 'SLARI Njala',
    status: 'published', year: 2023, views: 312, bookmarks: 54,
    description: 'High-yielding cassava variety with strong resistance to Cassava Mosaic Disease. Best suited for humid lowland zones in the Southern Province.',
    insights: [
      'Best for high rainfall areas',
      'Matures in 12 months',
      'Resistant to Cassava Mosaic Disease',
      'Suited for processing into gari and fufu',
    ],
    tags: ['High Yield', 'Disease Resistant', 'Processing'],
    soilType: 'Loamy, Sandy loam', rainfall: '1200–2000mm', altitude: '0–800m',
    rating: 4.5, ratingCount: 38,
  },
  {
    id: 2, name: 'NERICA-L-14', crop: 'Rice', emoji: '🌾',
    yield: '5.2', yieldUnit: 't/ha', duration: '105 days',
    resistance: 'Blast, RYMV', region: 'Northern', season: 'Dry & Wet',
    researcher: 'Dr. Fatmata Bangura', institute: 'SLARI Rokupr',
    status: 'published', year: 2022, views: 489, bookmarks: 87,
    description: 'Upland NERICA rice variety adapted to dryland farming in the northern savannah belt. Excellent performance under low rainfall conditions.',
    insights: [
      'Performs well in low rainfall zones',
      'Short maturity — only 105 days',
      'Ideal for upland dryland farming',
      'Two crops possible per season',
    ],
    tags: ['Short Season', 'Drought Tolerant', 'Upland'],
    soilType: 'Sandy loam, Laterite', rainfall: '700–1400mm', altitude: '0–600m',
    rating: 4.7, ratingCount: 61,
  },
  {
    id: 3, name: 'SAMMAZ-15', crop: 'Maize', emoji: '🌽',
    yield: '4.8', yieldUnit: 't/ha', duration: '90 days',
    resistance: 'Streak virus, Downy mildew', region: 'Northern', season: 'Wet',
    researcher: 'Dr. Ibrahim Sesay', institute: 'SLARI Teko',
    status: 'published', year: 2023, views: 201, bookmarks: 33,
    description: 'Open pollinated maize variety developed for savannah agroecology zones with good drought tolerance after establishment.',
    insights: [
      'Drought tolerant after establishment',
      'Matures in just 90 days',
      'High protein content',
      'Good for intercropping with legumes',
    ],
    tags: ['Drought Tolerant', 'Open Pollinated', 'High Protein'],
    soilType: 'Well-drained loam', rainfall: '900–1600mm', altitude: '100–1000m',
    rating: 4.2, ratingCount: 22,
  },
  {
    id: 4, name: 'SARI-SPT-3', crop: 'Sweet Potato', emoji: '🍠',
    yield: '18', yieldUnit: 't/ha', duration: '4 months',
    resistance: 'SPCSV, weevil-tolerant', region: 'All Zones', season: 'Bimodal',
    researcher: 'Dr. Mariama Conteh', institute: 'SLARI Kabala',
    status: 'published', year: 2024, views: 155, bookmarks: 28,
    description: 'Orange-fleshed sweet potato rich in Vitamin A. Excellent nutritional profile and market value. Suitable across multiple agroecologies.',
    insights: [
      'Rich in Vitamin A (orange-fleshed)',
      'Excellent for child nutrition programs',
      'Suitable across all regions',
      'High market demand and good income',
    ],
    tags: ['Vitamin A Rich', 'Nutrition', 'All Regions'],
    soilType: 'Sandy loam to loam', rainfall: '800–2000mm', altitude: '0–1200m',
    rating: 4.8, ratingCount: 44,
  },
  {
    id: 5, name: 'SLARI-GNT-2', crop: 'Groundnut', emoji: '🥜',
    yield: '1.8', yieldUnit: 't/ha', duration: '110 days',
    resistance: 'Leaf spot, Rosette', region: 'Northern', season: 'Wet',
    researcher: 'Dr. Ibrahim Sesay', institute: 'SLARI Teko',
    status: 'published', year: 2022, views: 98, bookmarks: 19,
    description: 'Spanish-type groundnut variety with good shelling percentage and oil content. Suited to the Northern Province farming systems.',
    insights: [
      'High oil content (48%)',
      'Good for confectionery and export',
      'Fixes nitrogen — improves soil',
      'Suited to Northern Province soils',
    ],
    tags: ['High Oil', 'Nitrogen Fixing', 'Northern Zones'],
    soilType: 'Sandy to sandy loam', rainfall: '700–1200mm', altitude: '0–500m',
    rating: 4.0, ratingCount: 15,
  },
  {
    id: 6, name: 'SORGHUM-SL-01', crop: 'Sorghum', emoji: '🌾',
    yield: '3.2', yieldUnit: 't/ha', duration: '120 days',
    resistance: 'Anthracnose, Head mold', region: 'Northern', season: 'Wet',
    researcher: 'Dr. Fatmata Bangura', institute: 'SLARI Rokupr',
    status: 'pending', year: 2024, views: 34, bookmarks: 7,
    description: 'Dual-purpose sorghum variety for food and fodder in semi-arid northern regions. Strong performance under moisture stress.',
    insights: [
      'Dual purpose: food and animal fodder',
      'Withstands dry season moisture stress',
      'Ideal for northern dry zones',
      'Good for local brewing industries',
    ],
    tags: ['Dual Purpose', 'Semi-Arid', 'Fodder'],
    soilType: 'Sandy to clay loam', rainfall: '600–1200mm', altitude: '0–600m',
    rating: 3.8, ratingCount: 9,
  },
  {
    id: 7, name: 'COWPEA-SL-04', crop: 'Cowpea', emoji: '🫘',
    yield: '1.2', yieldUnit: 't/ha', duration: '75 days',
    resistance: 'Striga, Aphid-tolerant', region: 'All Zones', season: 'Bimodal',
    researcher: 'Dr. Amara Koroma', institute: 'SLARI Njala',
    status: 'published', year: 2023, views: 167, bookmarks: 31,
    description: 'Early maturing cowpea variety ideal for intercropping with maize. High protein content and excellent leaf quality for vegetables.',
    insights: [
      'Matures in just 75 days',
      'Excellent for intercropping with maize',
      'Young leaves used as nutritious vegetable',
      'High protein for household nutrition',
    ],
    tags: ['Early Maturing', 'Intercrop', 'High Protein'],
    soilType: 'Any well-drained soil', rainfall: '600–2000mm', altitude: '0–1500m',
    rating: 4.3, ratingCount: 27,
  },
  {
    id: 8, name: 'SLARI-YAM-02', crop: 'Yam', emoji: '🍫',
    yield: '20', yieldUnit: 't/ha', duration: '8 months',
    resistance: 'Yam mosaic virus', region: 'Southern', season: 'Wet',
    researcher: 'Dr. Mariama Conteh', institute: 'SLARI Kabala',
    status: 'draft', year: 2024, views: 12, bookmarks: 3,
    description: 'White Guinea yam with large tubers and good storability. Selected for humid forest zone farming systems in Southern Province.',
    insights: [
      'Large tuber size — premium quality',
      'Good storability up to 6 months',
      'Suited to humid forest farming zones',
      'Commands premium price at market',
    ],
    tags: ['Large Tuber', 'Good Storage', 'Southern Zone'],
    soilType: 'Deep loamy soil', rainfall: '1500–2500mm', altitude: '0–600m',
    rating: 4.1, ratingCount: 8,
  },
];

/* ============================
   FARMERS DATABASE
   ============================ */
const FARMERS = [
  { id: 1, name: 'Sorie Kamara',   location: 'Tonkolili District', crops: ['Rice', 'Cassava'],                registered: '2024-01-15' },
  { id: 2, name: 'Aminata Koroma', location: 'Kenema District',   crops: ['Sweet Potato', 'Yam'],            registered: '2024-02-28' },
  { id: 3, name: 'Mohamed Turay',  location: 'Bombali District',  crops: ['Maize', 'Groundnut', 'Sorghum'], registered: '2024-03-10' },
  { id: 4, name: 'Isata Bangura',  location: 'Bo District',       crops: ['Cassava', 'Cowpea'],              registered: '2024-04-05' },
  { id: 5, name: 'Foday Conteh',   location: 'Pujehun District',  crops: ['Rice', 'Sweet Potato'],           registered: '2024-05-12' },
];

/* ============================
   FILTER LOOKUP ARRAYS
   ============================ */
const CROP_TYPES = [
  'All Crops', 'Cassava', 'Rice', 'Maize',
  'Sweet Potato', 'Groundnut', 'Sorghum', 'Cowpea', 'Yam',
];
const REGIONS = [
  'All Regions', 'Northern', 'Southern', 'Eastern', 'Western Area',
];
const SEASONS = [
  'All Seasons', 'Wet', 'Dry & Wet', 'Bimodal',
];

/* ============================
   DATA ACCESS FUNCTIONS
   (Replace bodies with fetch() calls in production)
   ============================ */

/** Returns all varieties combined with any session-submitted ones */
function getAllVarieties() {
  const submitted = JSON.parse(sessionStorage.getItem('submitted_varieties') || '[]');
  return [...VARIETIES, ...submitted];
}

/** Returns only published varieties */
function getPublishedVarieties() {
  return getAllVarieties().filter(v => v.status === 'published');
}

/** Find a variety by id */
function getVarietyById(id) {
  return getAllVarieties().find(v => v.id === Number(id)) || null;
}

/** Update a variety's status (admin action) */
function updateVarietyStatus(id, status) {
  // Try in session storage first
  const submitted = JSON.parse(sessionStorage.getItem('submitted_varieties') || '[]');
  const idx = submitted.findIndex(v => v.id === Number(id));
  if (idx !== -1) {
    submitted[idx].status = status;
    sessionStorage.setItem('submitted_varieties', JSON.stringify(submitted));
    return;
  }
  // Try in main array
  const v = VARIETIES.find(v => v.id === Number(id));
  if (v) v.status = status;
}

/** Submit a new variety (researcher action) */
function submitVariety(data) {
  const submitted = JSON.parse(sessionStorage.getItem('submitted_varieties') || '[]');
  const newVariety = {
    ...data,
    id: Date.now(),
    status: 'pending',
    views: 0,
    bookmarks: 0,
    rating: 0,
    ratingCount: 0,
  };
  submitted.push(newVariety);
  sessionStorage.setItem('submitted_varieties', JSON.stringify(submitted));
  return newVariety;
}

/** Update a variety's rating */
function rateVariety(id, score) {
  const v = getAllVarieties().find(v => v.id === Number(id));
  if (!v) return;
  v.rating = Math.round(((v.rating * v.ratingCount) + score) / (v.ratingCount + 1) * 10) / 10;
  v.ratingCount++;
}

/** Get all farmers */
function getAllFarmers() {
  return FARMERS;
}

/** Get platform summary stats */
function getPlatformStats() {
  const all = getAllVarieties();
  return {
    totalVarieties:  all.length,
    published:       all.filter(v => v.status === 'published').length,
    pending:         all.filter(v => v.status === 'pending').length,
    totalViews:      all.reduce((s, v) => s + (v.views || 0), 0),
    totalBookmarks:  all.reduce((s, v) => s + (v.bookmarks || 0), 0),
    cropTypes:       [...new Set(all.map(v => v.crop))].length,
    researchers:     8,
    farmersRegistered: 156,
    extensionOfficers: 12,
  };
}

/** Background gradient per crop type (used for card images) */
function getCropGradient(crop) {
  const map = {
    Cassava:       'linear-gradient(135deg,#a8d9b8,#5ab87a)',
    Rice:          'linear-gradient(135deg,#faecd4,#e8a824)',
    Maize:         'linear-gradient(135deg,#fdf6e8,#d4911c)',
    'Sweet Potato':'linear-gradient(135deg,#faecd4,#e86a1c)',
    Groundnut:     'linear-gradient(135deg,#faecd4,#c49a6c)',
    Sorghum:       'linear-gradient(135deg,#f4e8d4,#c47a1c)',
    Cowpea:        'linear-gradient(135deg,#d4ede4,#3a9960)',
    Yam:           'linear-gradient(135deg,#ede8d4,#8b6914)',
  };
  return map[crop] || 'linear-gradient(135deg,#a8d9b8,#3a9960)';
}
