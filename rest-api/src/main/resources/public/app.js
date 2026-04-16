// ============================================================
// VARIABLES GLOBALES
// ============================================================
const searchBtn = document.getElementById("searchBtn");
const keywordInput = document.getElementById("keyword");
const resultsDiv = document.getElementById("results");

const compareResultsDiv = document.getElementById("compareResults");
const clearCompareBtn = document.getElementById("clearCompareBtn");

// Outils (éligibilité + résultats)
const toolCourseId = document.getElementById("toolCourseId");
const toolCycle = document.getElementById("toolCycle");
const toolCompleted = document.getElementById("toolCompleted");
const btnToolEligibility = document.getElementById("btnToolEligibility");
const btnToolResults = document.getElementById("btnToolResults");
const toolOutput = document.getElementById("toolOutput");

// Si ton frontend est servi par le backend Javalin, laisse "".
// Sinon mets "http://localhost:8070"
const API_BASE = "";

// Liste globale pour stocker les IDs des cours à comparer
let compareList = [];
let cardSeq = 0; // garantit des IDs DOM uniques
const cardDetailState = {};
let toastHideTimeout = null;
let scheduleUiContext = { courseId: "", semester: "A25" };
const semesterMeta = {
    A25: { label: "Automne", accent: "#F7931A" },
    H25: { label: "Hiver", accent: "#22d3ee" },
    E25: { label: "Été", accent: "#a78bfa" }
};

// ============================================================
// CU#10, CU#11, CU#12: GESTION DES ENSEMBLES
// ============================================================

// Structure des ensembles (max 2)
let ensembles = {};
let ensembleCount = 0;

// Initialisation au chargement de la page
document.addEventListener("DOMContentLoaded", function() {
    initLandingGate();
    initEnsemblesSection();
    initCourseUiComponents();
});

function initLandingGate() {
    const landing = document.getElementById("landingGate");
    const appShell = document.getElementById("appShell");
    const enterBtn = document.getElementById("enterAppBtn");
    if (!landing || !appShell || !enterBtn) return;

    document.body.classList.add("landing-active");

    const revealApp = () => {
        landing.style.display = "none";
        appShell.classList.remove("is-hidden");
        document.body.classList.remove("landing-active");
    };

    enterBtn.addEventListener("click", revealApp);

    const brandLink = document.getElementById("brandLink");
    if (brandLink) {
        brandLink.addEventListener("click", (e) => {
            e.preventDefault();
            appShell.classList.add("is-hidden");
            landing.style.display = "";
            document.body.classList.add("landing-active");
        });
    }
}

function initCourseUiComponents() {
    ensureToastContainer();
    ensureScheduleUiModal();
}

function ensureToastContainer() {
    if (document.getElementById("toast-container")) return;
    const toastContainer = document.createElement("div");
    toastContainer.id = "toast-container";
    document.body.appendChild(toastContainer);
}

function showToast(message) {
    ensureToastContainer();
    const container = document.getElementById("toast-container");
    if (!container) return;

    const toast = document.createElement("div");
    toast.className = "app-toast";
    toast.innerHTML = `<span class="app-toast-check">✓</span><span>${escapeHtml(message)}</span>`;

    container.innerHTML = "";
    container.appendChild(toast);
    requestAnimationFrame(() => toast.classList.add("show"));

    if (toastHideTimeout) clearTimeout(toastHideTimeout);
    toastHideTimeout = setTimeout(() => {
        toast.classList.remove("show");
        setTimeout(() => {
            if (container.contains(toast)) container.removeChild(toast);
        }, 220);
    }, 2200);
}

/**
 * Initialise les événements de la section Ensembles
 */
function initEnsemblesSection() {
    const btnAjouter = document.getElementById("btnAjouterEnsemble");
    const btnComparer = document.getElementById("btnComparerEnsembles");
    const btnEffacer = document.getElementById("btnEffacerHoraires");
    
    if (btnAjouter) {
        btnAjouter.onclick = ajouterEnsemble;
    }
    if (btnComparer) {
        btnComparer.onclick = comparerEnsembles;
    }
    if (btnEffacer) {
        btnEffacer.onclick = effacerTousHoraires;
    }
}

/**
 * Ajoute un nouvel ensemble (max 2)
 */
function ajouterEnsemble() {
    if (ensembleCount >= 2) {
        alert("Vous pouvez créer au maximum 2 ensembles.");
        return;
    }
    
    ensembleCount++;
    const ensembleNum = ensembleCount;
    
    // Initialiser l'ensemble
    ensembles[ensembleNum] = {
        trimestre: 'H25',
        cours: [],
        horaireSaved: false,
        horaireGenere: false,
        horaireData: {}
    };
    
    // Cacher le message vide
    const emptyState = document.getElementById("ensemblesEmptyState");
    if (emptyState) emptyState.style.display = "none";
    
    // Créer le sous-panneau
    const container = document.getElementById("ensemblesContainer");
    const subpanel = document.createElement("div");
    subpanel.id = `ensemble-${ensembleNum}`;
    subpanel.className = "ensemble-subpanel";

    subpanel.innerHTML = `
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:14px; border-bottom:4px solid #000; padding-bottom:10px;">
            <h3 style="font-size:1.1rem; font-weight:900; margin:0; text-transform:uppercase; letter-spacing:-0.02em;">Ensemble ${ensembleNum}</h3>
            <button onclick="supprimerEnsemble(${ensembleNum})" class="btn-clear">Supprimer</button>
        </div>

        <div style="margin-bottom:14px;">
            <label style="font-size:0.72rem; font-weight:900; display:block; margin-bottom:6px; text-transform:uppercase; letter-spacing:0.12em;">Trimestre</label>
            <select id="trimestre-${ensembleNum}" onchange="changerTrimestre(${ensembleNum})" style="width:100%;">
                <option value="H25">Hiver 2025 (H25)</option>
                <option value="A24">Automne 2024 (A24)</option>
                <option value="E24">Ete 2024 (E24)</option>
            </select>
        </div>

        <div style="margin-bottom:14px;">
            <label style="font-size:0.72rem; font-weight:900; display:block; margin-bottom:6px; text-transform:uppercase; letter-spacing:0.12em;">Cours (max 6)</label>
            <div id="cours-list-${ensembleNum}" style="min-height:48px; background:var(--neo-bg); border:4px solid #000; padding:10px;">
                <div class="empty-state" style="font-size:0.82rem; padding:8px; border-style:dashed;">Ajoutez des cours depuis la recherche</div>
            </div>
        </div>

        <div style="display:flex; flex-direction:column; gap:8px;">
            <button id="btn-generer-${ensembleNum}" onclick="genererHoraire(${ensembleNum})" class="btn btn-primary" style="width:100%;" disabled>
                Generer horaire
            </button>
            <button id="btn-enregistrer-${ensembleNum}" onclick="enregistrerHoraire(${ensembleNum})" class="btn btn-outline" style="width:100%; background:var(--neo-secondary);" disabled>
                Enregistrer
            </button>
        </div>

        <div id="horaire-zone-${ensembleNum}" style="display:none; margin-top:16px;">
            <h4 style="font-size:0.95rem; font-weight:900; margin-bottom:10px; text-transform:uppercase;">Horaire genere</h4>
            <div id="horaire-content-${ensembleNum}"></div>
        </div>

        <div id="conflits-zone-${ensembleNum}" style="display:none; margin-top:16px;">
            <h4 style="font-size:0.95rem; font-weight:900; margin-bottom:10px; text-transform:uppercase;">Conflits d'horaire</h4>
            <div id="conflits-content-${ensembleNum}"></div>
        </div>
    `;
    
    container.appendChild(subpanel);
    
    // Mettre à jour le bouton d'ajout
    if (ensembleCount >= 2) {
        document.getElementById("btnAjouterEnsemble").disabled = true;
        document.getElementById("btnAjouterEnsemble").style.opacity = "0.5";
    }
    
    updateComparerButton();
}

/**
 * Change le trimestre d'un ensemble
 */
function changerTrimestre(ensembleNum) {
    const select = document.getElementById(`trimestre-${ensembleNum}`);
    if (select && ensembles[ensembleNum]) {
        ensembles[ensembleNum].trimestre = select.value;
        ensembles[ensembleNum].horaireGenere = false;
        ensembles[ensembleNum].horaireSaved = false;
        ensembles[ensembleNum].horaireData = {};
        
        // Réinitialiser l'affichage
        document.getElementById(`horaire-zone-${ensembleNum}`).style.display = "none";
        document.getElementById(`conflits-zone-${ensembleNum}`).style.display = "none";
        document.getElementById(`btn-enregistrer-${ensembleNum}`).disabled = true;
        
        updateComparerButton();
    }
}

/**
 * Supprime un ensemble
 */
function supprimerEnsemble(ensembleNum) {
    const subpanel = document.getElementById(`ensemble-${ensembleNum}`);
    if (subpanel) {
        subpanel.remove();
    }
    
    delete ensembles[ensembleNum];
    ensembleCount--;
    
    // Réactiver le bouton d'ajout
    const btnAjouter = document.getElementById("btnAjouterEnsemble");
    if (btnAjouter) {
        btnAjouter.disabled = false;
        btnAjouter.style.opacity = "1";
    }
    
    // Afficher message vide si plus d'ensembles
    if (ensembleCount === 0) {
        const emptyState = document.getElementById("ensemblesEmptyState");
        if (emptyState) emptyState.style.display = "block";
    }
    
    updateComparerButton();
}

/**
 * Ajoute un cours à un ensemble
 */
function ajouterCoursAEnsemble(courseId, courseName, ensembleNum) {
    if (!ensembles[ensembleNum]) {
        alert("Ensemble non trouvé. Veuillez d'abord créer un ensemble.");
        return;
    }
    
    if (ensembles[ensembleNum].cours.length >= 6) {
        alert("Un ensemble peut contenir au maximum 6 cours.");
        return;
    }
    
    // Vérifier si le cours est déjà dans l'ensemble
    if (ensembles[ensembleNum].cours.find(c => c.courseId === courseId)) {
        alert("Ce cours est déjà dans l'ensemble.");
        return;
    }
    
    // Ajouter le cours
    ensembles[ensembleNum].cours.push({
        courseId: courseId,
        name: courseName,
        horaires: [],
        selectedHoraire: null
    });
    
    // Mettre à jour l'affichage
    updateCoursListDisplay(ensembleNum);
    
    // Activer le bouton de génération si au moins 1 cours
    const btnGenerer = document.getElementById(`btn-generer-${ensembleNum}`);
    if (btnGenerer) {
        btnGenerer.disabled = false;
    }
    
    // Réinitialiser l'horaire si déjà généré
    ensembles[ensembleNum].horaireGenere = false;
    ensembles[ensembleNum].horaireSaved = false;
    ensembles[ensembleNum].horaireData = {};
    document.getElementById(`horaire-zone-${ensembleNum}`).style.display = "none";
    document.getElementById(`conflits-zone-${ensembleNum}`).style.display = "none";
    document.getElementById(`btn-enregistrer-${ensembleNum}`).disabled = true;
    
    updateComparerButton();
    
    // Scroll vers la section ensembles
    document.getElementById("ensemblesSection").scrollIntoView({ behavior: "smooth" });
}

/**
 * Retire un cours d'un ensemble
 */
function retirerCoursDeEnsemble(courseId, ensembleNum) {
    if (!ensembles[ensembleNum]) return;
    
    ensembles[ensembleNum].cours = ensembles[ensembleNum].cours.filter(c => c.courseId !== courseId);
    
    updateCoursListDisplay(ensembleNum);
    
    // Désactiver le bouton de génération si plus de cours
    if (ensembles[ensembleNum].cours.length === 0) {
        document.getElementById(`btn-generer-${ensembleNum}`).disabled = true;
    }
    
    // Réinitialiser
    ensembles[ensembleNum].horaireGenere = false;
    ensembles[ensembleNum].horaireSaved = false;
    ensembles[ensembleNum].horaireData = {};
    document.getElementById(`horaire-zone-${ensembleNum}`).style.display = "none";
    document.getElementById(`conflits-zone-${ensembleNum}`).style.display = "none";
    document.getElementById(`btn-enregistrer-${ensembleNum}`).disabled = true;
    
    updateComparerButton();
}

/**
 * Met à jour l'affichage de la liste des cours d'un ensemble
 */
function updateCoursListDisplay(ensembleNum) {
    const container = document.getElementById(`cours-list-${ensembleNum}`);
    if (!container || !ensembles[ensembleNum]) return;
    
    const cours = ensembles[ensembleNum].cours;
    
    if (cours.length === 0) {
        container.innerHTML = '<div class="empty-state" style="font-size:0.82rem; padding:8px; border-style:dashed;">Ajoutez des cours depuis la recherche</div>';
        return;
    }

    let html = '';
    cours.forEach(c => {
        html += `
            <div style="display:flex; justify-content:space-between; align-items:center; background:var(--neo-white); padding:8px 12px; margin-bottom:6px; border:2px solid #000; font-size:0.85rem; font-weight:700;">
                <span><strong style="font-weight:900;">${escapeHtml(c.courseId)}</strong> ${escapeHtml(c.name || '')}</span>
                <button onclick="retirerCoursDeEnsemble('${c.courseId}', ${ensembleNum})" style="background:var(--neo-accent); border:2px solid #000; color:#000; cursor:pointer; font-size:0.85rem; font-weight:900; padding:2px 8px; box-shadow:2px 2px 0px 0px #000; transition:transform 100ms ease-out,box-shadow 100ms ease-out;" onmouseover="this.style.transform='translate(2px,2px)';this.style.boxShadow='none'" onmouseout="this.style.transform='';this.style.boxShadow='2px 2px 0px 0px #000'">&times;</button>
            </div>
        `;
    });
    
    container.innerHTML = html;
}

/**
 * Génère l'horaire pour un ensemble (CU#10) - utilise CU#4
 */
async function genererHoraire(ensembleNum) {
    if (!ensembles[ensembleNum] || ensembles[ensembleNum].cours.length === 0) {
        alert("Ajoutez au moins un cours à l'ensemble.");
        return;
    }
    
    const btnGenerer = document.getElementById(`btn-generer-${ensembleNum}`);
    const horaireZone = document.getElementById(`horaire-zone-${ensembleNum}`);
    const horaireContent = document.getElementById(`horaire-content-${ensembleNum}`);
    const conflitsZone = document.getElementById(`conflits-zone-${ensembleNum}`);
    const conflitsContent = document.getElementById(`conflits-content-${ensembleNum}`);
    
    btnGenerer.disabled = true;
    btnGenerer.textContent = "Chargement...";
    horaireContent.innerHTML = '<div class="loader"></div>';
    horaireZone.style.display = "block";
    
    const ensemble = ensembles[ensembleNum];
    const trimestre = ensemble.trimestre;
    
    try {
        // Récupérer l'horaire de chaque cours via l'endpoint CU#4
        const horairesCombines = {};
        const allEntries = [];
        
        for (const cours of ensemble.cours) {
            const resp = await fetch(`${API_BASE}/courses/${encodeURIComponent(cours.courseId)}/schedule?semester=${encodeURIComponent(trimestre)}`);
            const data = await safeJson(resp);
            
            if (resp.ok && data.sections && data.sections.length > 0) {
                horairesCombines[cours.courseId] = [];
                
                // Extraire les entrées d'horaire
                data.sections.forEach(section => {
                    if (section.activities) {
                        section.activities.forEach(activity => {
                            if (activity.entries) {
                                activity.entries.forEach(entry => {
                                    const horaireEntry = {
                                        courseId: cours.courseId,
                                        jour: entry.day || "N/A",
                                        heureDebut: entry.startTime || "",
                                        heureFin: entry.endTime || "",
                                        local: entry.room || "N/A",
                                        typeActivite: activity.type || "Cours",
                                        section: section.sectionCode || "A"
                                    };
                                    horairesCombines[cours.courseId].push(horaireEntry);
                                    allEntries.push(horaireEntry);
                                });
                            }
                        });
                    }
                });
            } else {
                horairesCombines[cours.courseId] = [];
            }
        }
        
        // Stocker les données d'horaire
        ensemble.horaireData = horairesCombines;
        
        // Afficher l'horaire combiné
        displayHoraireCombine(ensembleNum, horairesCombines);
        
        // Détecter les conflits (CU#11)
        detecterConflits(ensembleNum, allEntries, conflitsZone, conflitsContent);
        
        ensemble.horaireGenere = true;
        
        // Activer le bouton d'enregistrement
        document.getElementById(`btn-enregistrer-${ensembleNum}`).disabled = false;
        
    } catch (e) {
        console.error(e);
        horaireContent.innerHTML = `<div style="background:var(--neo-accent); padding:10px; font-weight:700; border:4px solid #000;">Erreur: ${e.message}</div>`;
    } finally {
        btnGenerer.disabled = false;
        btnGenerer.textContent = "Generer horaire";
    }
}

/**
 * Affiche l'horaire combiné d'un ensemble
 */
function displayHoraireCombine(ensembleNum, horaires) {
    const container = document.getElementById(`horaire-content-${ensembleNum}`);
    
    if (!horaires || Object.keys(horaires).length === 0) {
        container.innerHTML = '<div style="color: #666; padding: 10px;">Aucun horaire disponible pour ce trimestre.</div>';
        return;
    }
    
    // Mapper les jours en français
    const dayNames = {
        monday: "Lundi", tuesday: "Mardi", wednesday: "Mercredi", thursday: "Jeudi", friday: "Vendredi",
        lundi: "Lundi", mardi: "Mardi", mercredi: "Mercredi", jeudi: "Jeudi", vendredi: "Vendredi"
    };
    
    let html = '<div style="overflow-x:auto;"><table style="width:100%; border-collapse:collapse; font-size:0.82rem; border:4px solid #000;">';
    html += `
        <thead>
            <tr style="border-bottom:4px solid #000; background:var(--neo-secondary);">
                <th style="padding:8px 10px; text-align:left; font-weight:900; font-size:0.72rem; text-transform:uppercase; letter-spacing:0.06em;">Cours</th>
                <th style="padding:8px 10px; text-align:left; font-weight:900; font-size:0.72rem; text-transform:uppercase; letter-spacing:0.06em;">Jour</th>
                <th style="padding:8px 10px; text-align:left; font-weight:900; font-size:0.72rem; text-transform:uppercase; letter-spacing:0.06em;">Heure</th>
                <th style="padding:8px 10px; text-align:left; font-weight:900; font-size:0.72rem; text-transform:uppercase; letter-spacing:0.06em;">Local</th>
                <th style="padding:8px 10px; text-align:left; font-weight:900; font-size:0.72rem; text-transform:uppercase; letter-spacing:0.06em;">Type</th>
            </tr>
        </thead>
        <tbody>
    `;

    let rowIndex = 0;
    let hasAnySchedule = false;

    for (const [courseId, horaireList] of Object.entries(horaires)) {
        if (horaireList && horaireList.length > 0) {
            hasAnySchedule = true;
            horaireList.forEach(h => {
                const jour = dayNames[h.jour?.toLowerCase()] || h.jour || "N/A";
                const debut = h.heureDebut || "N/A";
                const fin = h.heureFin || "N/A";
                const local = h.local || "N/A";
                const type = h.typeActivite || "Cours";

                html += `
                    <tr style="border-bottom:2px solid #000;">
                        <td style="padding:8px 10px; font-weight:900;">${courseId}</td>
                        <td style="padding:8px 10px; font-weight:700;">${jour}</td>
                        <td style="padding:8px 10px; font-weight:700; font-size:0.82rem;">${debut} - ${fin}</td>
                        <td style="padding:8px 10px; font-weight:700;">${local}</td>
                        <td style="padding:8px 10px; font-weight:700;">${type}</td>
                    </tr>
                `;
                rowIndex++;
            });
        } else {
            html += `
                <tr style="border-bottom:2px solid #000;">
                    <td style="padding:8px 10px; font-weight:700;" colspan="5">${courseId}: Aucun horaire disponible</td>
                </tr>
            `;
        }
    }

    html += '</tbody></table></div>';

    if (!hasAnySchedule) {
        html = '<div style="background:var(--neo-secondary); padding:14px; font-size:0.85rem; font-weight:700; border:4px solid #000; box-shadow:4px 4px 0px 0px #000;">Aucun horaire trouve pour ce trimestre.</div>';
    }
    
    container.innerHTML = html;
}

/**
 * Détecte les conflits d'horaire (CU#11)
 */
function detecterConflits(ensembleNum, allEntries, conflitsZone, conflitsContent) {
    const conflits = [];
    
    // Mapper les jours pour normalisation
    const normalizeDay = (day) => {
        const dayMap = {
            monday: "lundi", tuesday: "mardi", wednesday: "mercredi", 
            thursday: "jeudi", friday: "vendredi",
            lundi: "lundi", mardi: "mardi", mercredi: "mercredi", 
            jeudi: "jeudi", vendredi: "vendredi"
        };
        return dayMap[day?.toLowerCase()] || day?.toLowerCase() || "";
    };
    
    // Convertir heure en minutes
    const timeToMinutes = (time) => {
        if (!time) return 0;
        const parts = time.split(":");
        return parseInt(parts[0]) * 60 + (parseInt(parts[1]) || 0);
    };
    
    // Vérifier les chevauchements
    for (let i = 0; i < allEntries.length; i++) {
        for (let j = i + 1; j < allEntries.length; j++) {
            const e1 = allEntries[i];
            const e2 = allEntries[j];
            
            // IMPORTANT: Ne pas détecter de conflit si les deux entrées sont du même cours
            // (ex: IFT2255 TH vs IFT2255 Intra ne devrait pas être un conflit)
            if (e1.courseId === e2.courseId) {
                continue; // Skip - même cours, pas de conflit possible
            }
            
            // Même jour?
            if (normalizeDay(e1.jour) === normalizeDay(e2.jour) && normalizeDay(e1.jour) !== "") {
                const start1 = timeToMinutes(e1.heureDebut);
                const end1 = timeToMinutes(e1.heureFin);
                const start2 = timeToMinutes(e2.heureDebut);
                const end2 = timeToMinutes(e2.heureFin);
                
                // Chevauchement?
                if (start1 < end2 && start2 < end1) {
                    conflits.push({
                        cours1: e1.courseId,
                        cours2: e2.courseId,
                        jour: e1.jour,
                        heure1: `${e1.heureDebut}-${e1.heureFin}`,
                        heure2: `${e2.heureDebut}-${e2.heureFin}`,
                        chevauchement: Math.min(end1, end2) - Math.max(start1, start2)
                    });
                }
            }
        }
    }
    
    // Stocker les conflits dans l'ensemble
    ensembles[ensembleNum].conflits = conflits;
    
    conflitsZone.style.display = "block";
    
    if (conflits.length === 0) {
        conflitsContent.innerHTML = `
            <div style="background:#d4edda; padding:12px; font-size:0.85rem; font-weight:700; border:4px solid #000; box-shadow:4px 4px 0px 0px #000;">
                Aucun conflit detecte. Horaire compatible.
            </div>
        `;
    } else {
        let html = `
            <div style="background:var(--neo-accent); padding:12px; margin-bottom:10px; font-size:0.85rem; font-weight:900; border:4px solid #000; box-shadow:4px 4px 0px 0px #000; text-transform:uppercase;">
                ${conflits.length} conflit(s) detecte(s)
            </div>
        `;

        html += '<div style="max-height:150px; overflow-y:auto; display:flex; flex-direction:column; gap:6px;">';
        conflits.forEach((c) => {
            const dayNames = {
                monday: "Lundi", tuesday: "Mardi", wednesday: "Mercredi", thursday: "Jeudi", friday: "Vendredi",
                lundi: "Lundi", mardi: "Mardi", mercredi: "Mercredi", jeudi: "Jeudi", vendredi: "Vendredi"
            };
            const jourFr = dayNames[c.jour?.toLowerCase()] || c.jour;

            html += `
                <div style="background:var(--neo-secondary); padding:8px 12px; font-size:0.82rem; font-weight:700; border:2px solid #000;">
                    <strong style="font-weight:900;">${c.cours1}</strong> / <strong style="font-weight:900;">${c.cours2}</strong> &mdash;
                    ${jourFr}: ${c.heure1} vs ${c.heure2} (${c.chevauchement} min)
                </div>
            `;
        });
        html += '</div>';

        conflitsContent.innerHTML = html;
    }
}

/**
 * Enregistre l'horaire d'un ensemble
 */
function enregistrerHoraire(ensembleNum) {
    if (!ensembles[ensembleNum] || !ensembles[ensembleNum].horaireGenere) {
        alert("Veuillez d'abord générer l'horaire.");
        return;
    }
    
    ensembles[ensembleNum].horaireSaved = true;
    
    // Feedback visuel
    const btn = document.getElementById(`btn-enregistrer-${ensembleNum}`);
    btn.textContent = "Horaire enregistre";
    btn.disabled = true;
    btn.style.background = "#d4edda";
    
    // Mettre à jour le bouton de comparaison
    updateComparerButton();
    
    alert(`✅ Horaire de l'Ensemble ${ensembleNum} enregistré!`);
}

/**
 * Met à jour l'état du bouton "Comparer Ensembles"
 */
function updateComparerButton() {
    const btnComparer = document.getElementById("btnComparerEnsembles");
    const btnEffacer = document.getElementById("btnEffacerHoraires");
    
    // Compter les ensembles avec horaire enregistré
    let savedCount = 0;
    for (const key in ensembles) {
        if (ensembles[key].horaireSaved) {
            savedCount++;
        }
    }
    
    if (savedCount >= 2) {
        btnComparer.style.display = "inline-block";
        btnComparer.disabled = false;
    } else {
        btnComparer.style.display = "none";
    }
    
    if (savedCount >= 1) {
        btnEffacer.style.display = "inline-block";
    } else {
        btnEffacer.style.display = "none";
    }
}

/**
 * Compare les ensembles (CU#12)
 */
function comparerEnsembles() {
    const savedEnsembles = Object.entries(ensembles).filter(([_, e]) => e.horaireSaved);
    
    if (savedEnsembles.length < 2) {
        alert("Veuillez enregistrer au moins 2 horaires avant de comparer.");
        return;
    }
    
    const resultZone = document.getElementById("comparaisonResultat");
    const resultContent = document.getElementById("comparaisonContent");
    
    resultZone.style.display = "block";
    
    const [ens1Entry, ens2Entry] = savedEnsembles;
    const ens1 = ens1Entry[1];
    const ens2 = ens2Entry[1];
    const ens1Num = ens1Entry[0];
    const ens2Num = ens2Entry[0];
    
    // Calculer les métriques
    const credits1 = ens1.cours.length * 3; // Estimation: 3 crédits par cours
    const credits2 = ens2.cours.length * 3;
    const conflits1 = ens1.conflits ? ens1.conflits.length : 0;
    const conflits2 = ens2.conflits ? ens2.conflits.length : 0;
    const nbCours1 = ens1.cours.length;
    const nbCours2 = ens2.cours.length;
    
    // Score simple: moins de conflits = meilleur
    const score1 = 100 - (conflits1 * 20);
    const score2 = 100 - (conflits2 * 20);
    
    const meilleur = score1 > score2 ? `Ensemble ${ens1Num}` : (score2 > score1 ? `Ensemble ${ens2Num}` : "Égalité");
    const raison = conflits1 < conflits2 ? "Moins de conflits d'horaire" : 
                   (conflits2 < conflits1 ? "Moins de conflits d'horaire" : "Scores équivalents");
    
    // Afficher les deux horaires côte à côte + les métriques
    let html = `
        <div style="display:flex; flex-wrap:wrap; gap:12px; margin-bottom:20px;">
            <div style="flex:1; min-width:280px; background:var(--neo-bg); padding:16px; border:4px solid #000; box-shadow:4px 4px 0px 0px #000;">
                <h4 style="font-size:0.95rem; font-weight:900; margin:0 0 10px; text-transform:uppercase;">Ensemble ${ens1Num} (${ens1.trimestre})</h4>
                <div style="font-size:0.82rem; font-weight:700;">${renderHoraireCompact(ens1.horaireData)}</div>
            </div>
            <div style="flex:1; min-width:280px; background:var(--neo-bg); padding:16px; border:4px solid #000; box-shadow:4px 4px 0px 0px #000;">
                <h4 style="font-size:0.95rem; font-weight:900; margin:0 0 10px; text-transform:uppercase;">Ensemble ${ens2Num} (${ens2.trimestre})</h4>
                <div style="font-size:0.82rem; font-weight:700;">${renderHoraireCompact(ens2.horaireData)}</div>
            </div>
        </div>

        <div style="border:4px solid #000; overflow:hidden;">
            <table style="width:100%; border-collapse:collapse; font-size:0.85rem;">
                <thead>
                    <tr style="border-bottom:4px solid #000; background:var(--neo-secondary);">
                        <th style="padding:10px 14px; text-align:left; font-size:0.72rem; font-weight:900; text-transform:uppercase; letter-spacing:0.06em;">Critere</th>
                        <th style="padding:10px 14px; text-align:center; font-size:0.72rem; font-weight:900; text-transform:uppercase; letter-spacing:0.06em;">Ensemble ${ens1Num}</th>
                        <th style="padding:10px 14px; text-align:center; font-size:0.72rem; font-weight:900; text-transform:uppercase; letter-spacing:0.06em;">Ensemble ${ens2Num}</th>
                    </tr>
                </thead>
                <tbody>
                    <tr style="border-bottom:2px solid #000;">
                        <td style="padding:10px 14px; font-weight:900;">Nombre de cours</td>
                        <td style="padding:10px 14px; text-align:center; font-weight:700;">${nbCours1}</td>
                        <td style="padding:10px 14px; text-align:center; font-weight:700;">${nbCours2}</td>
                    </tr>
                    <tr style="border-bottom:2px solid #000;">
                        <td style="padding:10px 14px; font-weight:900;">Credits estimes</td>
                        <td style="padding:10px 14px; text-align:center; font-weight:700;">${credits1}</td>
                        <td style="padding:10px 14px; text-align:center; font-weight:700;">${credits2}</td>
                    </tr>
                    <tr style="border-bottom:2px solid #000;">
                        <td style="padding:10px 14px; font-weight:900;">Conflits</td>
                        <td style="padding:10px 14px; text-align:center; font-weight:900; ${conflits1 > 0 ? 'background:var(--neo-accent);' : 'background:#d4edda;'}">${conflits1}</td>
                        <td style="padding:10px 14px; text-align:center; font-weight:900; ${conflits2 > 0 ? 'background:var(--neo-accent);' : 'background:#d4edda;'}">${conflits2}</td>
                    </tr>
                    <tr>
                        <td style="padding:10px 14px; font-weight:900;">Score</td>
                        <td style="padding:10px 14px; text-align:center; font-weight:900; font-size:1.1rem;">${score1}</td>
                        <td style="padding:10px 14px; text-align:center; font-weight:900; font-size:1.1rem;">${score2}</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div style="margin-top:16px; background:var(--neo-secondary); padding:14px 18px; text-align:center; border:4px solid #000; box-shadow:4px 4px 0px 0px #000;">
            <strong style="font-weight:900; text-transform:uppercase;">Recommandation :</strong> ${meilleur}<br>
            <span style="font-size:0.85rem; font-weight:700;">${raison}</span>
        </div>
    `;
    
    resultContent.innerHTML = html;
    
    // Scroller vers les résultats
    resultZone.scrollIntoView({ behavior: "smooth" });
}

/**
 * Rend un horaire de façon compacte pour la comparaison
 */
function renderHoraireCompact(horaireData) {
    if (!horaireData || Object.keys(horaireData).length === 0) {
        return '<div style="font-size:0.82rem; font-weight:700;">Aucun horaire</div>';
    }

    const dayNames = {
        monday: "Lun", tuesday: "Mar", wednesday: "Mer", thursday: "Jeu", friday: "Ven",
        lundi: "Lun", mardi: "Mar", mercredi: "Mer", jeudi: "Jeu", vendredi: "Ven"
    };

    let html = '';
    for (const [courseId, horaireList] of Object.entries(horaireData)) {
        html += `<div style="margin-bottom:8px; padding:6px 8px; border:2px solid #000; background:var(--neo-white);"><strong style="font-size:0.82rem; font-weight:900;">${courseId}</strong><br>`;
        if (horaireList && horaireList.length > 0) {
            horaireList.forEach(h => {
                const jour = dayNames[h.jour?.toLowerCase()] || h.jour || "?";
                html += `<span style="margin-left:10px; font-size:0.8rem; font-weight:700;">${jour} ${h.heureDebut}-${h.heureFin}</span><br>`;
            });
        } else {
            html += `<span style="margin-left:10px; font-size:0.8rem; font-weight:700;">Pas d'horaire</span>`;
        }
        html += '</div>';
    }
    return html;
}

/**
 * Efface tous les horaires et réinitialise
 */
function effacerTousHoraires() {
    if (!confirm("Êtes-vous sûr de vouloir effacer tous les horaires enregistrés?")) {
        return;
    }
    
    // Réinitialiser chaque ensemble
    for (const key in ensembles) {
        ensembles[key].horaireGenere = false;
        ensembles[key].horaireSaved = false;
        ensembles[key].horaireData = {};
        ensembles[key].conflits = [];
        
        // Réinitialiser l'affichage
        const horaireZone = document.getElementById(`horaire-zone-${key}`);
        const conflitsZone = document.getElementById(`conflits-zone-${key}`);
        const btnEnregistrer = document.getElementById(`btn-enregistrer-${key}`);
        
        if (horaireZone) horaireZone.style.display = "none";
        if (conflitsZone) conflitsZone.style.display = "none";
        if (btnEnregistrer) {
            btnEnregistrer.disabled = true;
            btnEnregistrer.textContent = "Enregistrer";
            btnEnregistrer.style.background = "var(--neo-secondary)";
        }
    }
    
    // Cacher la comparaison
    document.getElementById("comparaisonResultat").style.display = "none";
    
    updateComparerButton();
    
    alert("✅ Tous les horaires ont été effacés.");
}

// Exposer les fonctions globalement
window.supprimerEnsemble = supprimerEnsemble;
window.changerTrimestre = changerTrimestre;
window.retirerCoursDeEnsemble = retirerCoursDeEnsemble;
window.genererHoraire = genererHoraire;
window.enregistrerHoraire = enregistrerHoraire;
window.ajouterCoursAEnsemble = ajouterCoursAEnsemble;

// ============================================================
// AVIS ÉTUDIANTS
// ============================================================
const reviewsCache = new Map();

async function fetchReviews(courseId) {
    const cid = (courseId ?? "").trim().toUpperCase();
    if (!cid) return [];

    if (reviewsCache.has(cid)) return reviewsCache.get(cid);

    const r = await fetch(`${API_BASE}/reviews/${encodeURIComponent(cid)}`);
    if (!r.ok) {
        reviewsCache.set(cid, []);
        return [];
    }

    const data = await safeJson(r);
    const arr = Array.isArray(data) ? data : [];
    reviewsCache.set(cid, arr);
    return arr;
}

function aggregateDistinctByAuthor(reviews) {
    const byAuthor = new Map();
    for (const rev of reviews) {
        const author = (rev?.author ?? "").trim();
        if (!author) continue;
        byAuthor.set(author.toLowerCase(), rev);
    }

    const distinct = Array.from(byAuthor.values());
    const n = distinct.length;

    const avg = n === 0 ? 0 : distinct.reduce((s, r) => s + (Number(r?.difficulty) || 0), 0) / n;

    const last = distinct.slice(-5).reverse();
    return { n, avg, last };
}

function escapeHtml(str) {
    return (str ?? "")
        .toString()
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

async function renderReviewsIn(containerEl, courseId) {
    if (!containerEl) return;
    containerEl.innerHTML = `<div style="margin-top:10px;"><div class="loader"></div></div>`;

    try {
        const reviews = await fetchReviews(courseId);
        const agg = aggregateDistinctByAuthor(reviews);

        if (agg.n < 5) {
            containerEl.innerHTML = `
                <div style="margin-top:10px; font-size:0.82rem; font-weight:700;">
                    Pas assez d'avis pour afficher (${agg.n}/5 minimum).
                </div>
            `;
            return;
        }

        const avgTxt = agg.avg.toFixed(1);
        const itemsHtml = agg.last
            .map((r) => {
                const a = r.author ?? "Anonyme";
                const d = r.difficulty ?? "?";
                const c = (r.comment ?? "").toString();
                return `<li style="margin:5px 0;font-size:0.82rem;font-weight:700;"><strong style="font-weight:900;">${escapeHtml(a)}</strong> (${escapeHtml(d)}/10) — ${escapeHtml(c)}</li>`;
            })
            .join("");

        containerEl.innerHTML = `
            <div style="margin-top:12px; padding-top:10px; border-top:4px solid #000;">
                <div style="font-weight:900; margin-bottom:6px; font-size:0.85rem; text-transform:uppercase; letter-spacing:0.04em;">Avis etudiants</div>
                <div style="margin-bottom:8px; font-size:0.85rem; font-weight:700;">
                    Difficulte moyenne : <strong style="font-weight:900;">${avgTxt}/10</strong>
                    (${agg.n} avis)
                </div>
                <ul style="margin:0; padding-left:16px;">${itemsHtml}</ul>
            </div>
        `;
    } catch (e) {
        console.error(e);
        containerEl.innerHTML = `<div style="margin-top:10px; font-size:0.88rem; font-weight:700; background:var(--neo-accent); padding:8px; border:2px solid #000;">Impossible de charger les avis.</div>`;
    }
}

// ============================================================
// RECHERCHE
// ============================================================
keywordInput.addEventListener("keypress", (e) => {
    if (e.key === "Enter") searchBtn.click();
});

searchBtn.onclick = async () => {
    const kw = keywordInput.value.trim();
    resultsDiv.innerHTML = '<div class="loader"></div>';

    if (kw === "") {
        resultsDiv.innerHTML = "<div class='empty-state'>Veuillez entrer un mot-clé.</div>";
        return;
    }

    const isSigle = /^[A-Za-z]{3}\d{4}$/i.test(kw);
    const isProgramCode = /^\d{6}$/.test(kw);
    const isProgramSemester = /^(\d{6})\s+([A-Za-z]\d{2})$/i.test(kw);

    try {
        if (isProgramSemester) {
            const matches = kw.match(/^(\d{6})\s+([A-Za-z]\d{2})$/i);
            const prog = matches[1];
            const sem = matches[2].toUpperCase();

            const r = await fetch(`${API_BASE}/semesters/${encodeURIComponent(sem)}/courses?program=${encodeURIComponent(prog)}`);
            if (!r.ok) {
                const err = await safeJson(r);
                resultsDiv.innerHTML = `<div class='empty-state'>Erreur: ${err.error?.message || "Introuvable"}</div>`;
                return;
            }

            const courses = await r.json();
            resultsDiv.innerHTML = "";

            if (!Array.isArray(courses) || courses.length === 0) {
                resultsDiv.innerHTML = "<div class='empty-state'>Aucun cours trouvé pour ce programme et ce semestre.</div>";
                return;
            }

            courses.forEach((c) => resultsDiv.insertAdjacentHTML("beforeend", renderCard(c, false)));
            return;
        }

        if (isSigle) {
            const r = await fetch(`${API_BASE}/courses/${encodeURIComponent(kw)}`);
            if (!r.ok) throw new Error();
            const course = await r.json();
            resultsDiv.innerHTML = renderCard(course, false);
            return;
        }

        if (isProgramCode) {
            const r = await fetch(`${API_BASE}/programs/courses?program=${encodeURIComponent(kw)}`);
            if (!r.ok) throw new Error();

            const courses = await r.json();
            resultsDiv.innerHTML = "";

            if (!Array.isArray(courses) || courses.length === 0) {
                resultsDiv.innerHTML = "<div class='empty-state'>Aucun cours pour ce programme.</div>";
                return;
            }

            courses.forEach((c) => resultsDiv.insertAdjacentHTML("beforeend", renderCard(c, false)));
            return;
        }

        let r = await fetch(`${API_BASE}/courses/search?q=${encodeURIComponent(kw)}`);
        if (r.ok) {
            const courses = await r.json();
            if (Array.isArray(courses) && courses.length > 0) {
                resultsDiv.innerHTML = "";
                courses.forEach((c) => resultsDiv.insertAdjacentHTML("beforeend", renderCard(c, false)));
                return;
            }
        }

        r = await fetch(`${API_BASE}/programs/search?q=${encodeURIComponent(kw)}`);
        if (!r.ok) throw new Error();

        const programs = await r.json();
        resultsDiv.innerHTML = "";

        if (!Array.isArray(programs) || programs.length === 0) {
            resultsDiv.innerHTML = "<div class='empty-state'>Aucun résultat trouvé.</div>";
            return;
        }

        programs.forEach((p) => {
            resultsDiv.insertAdjacentHTML("beforeend", `
                <div class="card">
                    <div class="card-title">${p.name}</div>
                    <div style="font-size:0.88rem; font-weight:700;">Programme ${p.id}</div>
                    <button class="btn-card-action" onclick="loadProgramCourses('${p.id}')" style="width:100%;margin-top:8px;background:var(--neo-secondary);">Voir les cours du programme</button>
                </div>
            `);
        });
    } catch (err) {
        console.error(err);
        resultsDiv.innerHTML = "<div class='empty-state'>Erreur serveur.</div>";
    }
};

window.loadProgramCourses = async function (programId) {
    resultsDiv.innerHTML = '<div class="loader"></div>';

    try {
        const r = await fetch(`${API_BASE}/programs/courses?program=${encodeURIComponent(programId)}`);
        if (!r.ok) throw new Error();

        const courses = await r.json();
        resultsDiv.innerHTML = "";

        courses.forEach((c) => resultsDiv.insertAdjacentHTML("beforeend", renderCard(c, false)));
    } catch (e) {
        console.error(e);
        resultsDiv.innerHTML = "<div class='empty-state'>Impossible de charger les cours.</div>";
    }
};

// ============================================================
// OUTILS : ÉLIGIBILITÉ + RÉSULTATS
// ============================================================
btnToolEligibility.onclick = async () => {
    const id = toolCourseId.value.trim();
    const cycle = parseInt(toolCycle.value, 10);
    const completed = parseCompletedCourses(toolCompleted.value);

    if (!id) return showTool("❌ Course ID requis.");

    try {
        const resp = await fetch(`${API_BASE}/courses/${encodeURIComponent(id)}/eligibility`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ completed_courses: completed, cycle }),
        });

        const data = await safeJson(resp);
        if (!resp.ok) return showTool({ status: resp.status, error: data });

        showTool({ status: resp.status, raw: data });
    } catch (e) {
        showTool("Erreur réseau: " + e.message);
    }
};

btnToolResults.onclick = async () => {
    const id = toolCourseId.value.trim();
    if (!id) return showTool("❌ Course ID requis.");

    try {
        const resp = await fetch(`${API_BASE}/courses/${encodeURIComponent(id)}/results`);
        const data = await safeJson(resp);

        if (!resp.ok) return showTool({ status: resp.status, error: data });
        showTool({ status: resp.status, raw: data });
    } catch (e) {
        showTool("Erreur réseau: " + e.message);
    }
};

window.prefillTools = function (courseId) {
    toolCourseId.value = courseId;
    toolCourseId.scrollIntoView({ behavior: "smooth", block: "center" });
};

window.checkEligibilityFromCard = function (courseId) {
    const key = `eligibility-${courseId}`;
    cardDetailState[key] = !cardDetailState[key];
    refreshCardPanels(courseId);
};

window.loadResultsFromCard = function (courseId) {
    const key = `results-${courseId}`;
    cardDetailState[key] = !cardDetailState[key];
    refreshCardPanels(courseId);
};

function createMockEligibilityData(courseId) {
    const base = normalizeCourseId(courseId).slice(-2);
    return {
        courseId: normalizeCourseId(courseId),
        statut: Number(base) % 2 === 0 ? "ELIGIBLE" : "A_VERIFIER",
        prerequisValides: ["IFT1005", "MAT1919"],
        prerequisManquants: Number(base) % 2 === 0 ? [] : ["IFT2105"],
        cycle: "1er cycle",
        scoreCompat: `${72 + (Number(base) % 20)}%`
    };
}

function createMockResultsData(courseId) {
    const seed = normalizeCourseId(courseId).slice(-2);
    return {
        courseId: normalizeCourseId(courseId),
        moyenne: `${2.7 + ((Number(seed) % 10) / 10)}/4.3`,
        mediane: `${2.5 + ((Number(seed) % 8) / 10)}/4.3`,
        participants: 90 + (Number(seed) % 80),
        trimestres: `A23-H25 (${4 + (Number(seed) % 4)} sessions)`,
        difficulte: `${55 + (Number(seed) % 30)}/100`,
        recommandation: Number(seed) % 2 === 0 ? "Stable" : "Surveiller charge TP"
    };
}

function renderMonospacePanel(title, payload) {
    const rows = Object.entries(payload)
        .map(([key, value]) => {
            const normalizedKey = key
                .replace(/([A-Z])/g, " $1")
                .replace(/_/g, " ")
                .toLowerCase()
                .replace(/^./, (s) => s.toUpperCase());
            const formattedValue = Array.isArray(value)
                ? value.join(", ")
                : value;
            return `<div class="card-action-row"><span class="card-action-key">${escapeHtml(normalizedKey)}</span><span class="card-action-sep">:</span><span class="card-action-value">${escapeHtml(String(formattedValue))}</span></div>`;
        })
        .join("");

    return `
        <div class="card-action-panel">
            <div class="card-action-title">${title}</div>
            <div class="card-action-content">${rows}</div>
        </div>
    `;
}

function refreshCardPanels(courseId) {
    const eligibilityBox = document.getElementById(`card-eligibility-${courseId}`);
    const resultsBox = document.getElementById(`card-results-${courseId}`);
    if (!eligibilityBox || !resultsBox) return;

    const showEligibility = !!cardDetailState[`eligibility-${courseId}`];
    const showResults = !!cardDetailState[`results-${courseId}`];

    eligibilityBox.style.display = showEligibility ? "block" : "none";
    resultsBox.style.display = showResults ? "block" : "none";

    if (showEligibility) {
        eligibilityBox.innerHTML = renderMonospacePanel("Eligibilite (mock)", createMockEligibilityData(courseId));
    } else {
        eligibilityBox.innerHTML = "";
    }
    if (showResults) {
        resultsBox.innerHTML = renderMonospacePanel("Resultats (mock)", createMockResultsData(courseId));
    } else {
        resultsBox.innerHTML = "";
    }
}

function ensureScheduleUiModal() {
    if (document.getElementById("scheduleUiModal")) return;

    const modal = document.createElement("div");
    modal.id = "scheduleUiModal";
    modal.className = "schedule-ui-modal";
    modal.innerHTML = `
        <div class="schedule-ui-panel">
            <div class="schedule-ui-header">
                <div>
                    <h3 id="scheduleUiTitle">Horaire tactique</h3>
                    <p class="schedule-ui-subtitle">Choisissez un trimestre pour afficher une grille synthetique.</p>
                </div>
                <button class="schedule-ui-close" onclick="closeScheduleUiModal()">&times;</button>
            </div>
            <div class="schedule-semesters" id="scheduleSemesters">
                <button class="schedule-semester-btn" data-semester="A25">Automne</button>
                <button class="schedule-semester-btn" data-semester="H25">Hiver</button>
                <button class="schedule-semester-btn" data-semester="E25">Ete</button>
            </div>
            <div id="scheduleGridContainer"></div>
        </div>
    `;
    document.body.appendChild(modal);

    modal.addEventListener("click", (evt) => {
        if (evt.target === modal) closeScheduleUiModal();
    });

    modal.querySelectorAll(".schedule-semester-btn").forEach((btn) => {
        btn.addEventListener("click", () => {
            renderSchedulePreview(scheduleUiContext.courseId, btn.dataset.semester);
        });
    });
}

function getFakeScheduleRows(courseId, semester) {
    const rowsBySemester = {
        A25: [
            { day: "LUN", block: "08:30-10:00", room: "B-421", type: "TH", chain: "NODE-A1" },
            { day: "MER", block: "13:00-14:30", room: "A-315", type: "LAB", chain: "NODE-A2" },
            { day: "VEN", block: "10:00-11:30", room: "Z-210", type: "TP", chain: "NODE-A3" }
        ],
        H25: [
            { day: "MAR", block: "09:30-11:00", room: "P-114", type: "TH", chain: "NODE-H1" },
            { day: "JEU", block: "15:30-17:00", room: "M-410", type: "LAB", chain: "NODE-H2" },
            { day: "VEN", block: "12:30-14:00", room: "Q-212", type: "TP", chain: "NODE-H3" }
        ],
        E25: [
            { day: "LUN", block: "17:30-19:00", room: "C-120", type: "TH", chain: "NODE-E1" },
            { day: "MER", block: "17:30-19:00", room: "C-120", type: "LAB", chain: "NODE-E2" },
            { day: "SAM", block: "09:00-12:00", room: "L-001", type: "INT", chain: "NODE-E3" }
        ]
    };

    return (rowsBySemester[semester] || rowsBySemester.A25).map((row) => ({
        ...row,
        hash: `${courseId}-${semester}-${row.chain}`
    }));
}

function renderSchedulePreview(courseId, semester) {
    scheduleUiContext = { courseId, semester };
    const title = document.getElementById("scheduleUiTitle");
    const gridContainer = document.getElementById("scheduleGridContainer");
    const semesterButtons = document.querySelectorAll(".schedule-semester-btn");

    semesterButtons.forEach((btn) => {
        const active = btn.dataset.semester === semester;
        btn.classList.toggle("active", active);
    });

    const meta = semesterMeta[semester] || semesterMeta.A25;
    if (title) {
        title.textContent = `Horaire tactique - ${courseId} [${meta.label}]`;
    }
    if (!gridContainer) return;

    const rows = getFakeScheduleRows(courseId, semester);
    gridContainer.innerHTML = `
        <div class="schedule-cyber-grid">
            <div class="schedule-cyber-head">JOUR</div>
            <div class="schedule-cyber-head">PLAGE</div>
            <div class="schedule-cyber-head">LOCAL</div>
            <div class="schedule-cyber-head">TYPE</div>
            <div class="schedule-cyber-head">HASH LEDGER</div>
            ${rows.map((row) => `
                <div class="schedule-cyber-cell">${row.day}</div>
                <div class="schedule-cyber-cell">${row.block}</div>
                <div class="schedule-cyber-cell">${row.room}</div>
                <div class="schedule-cyber-cell">${row.type}</div>
                <div class="schedule-cyber-cell hash">${row.hash}</div>
            `).join("")}
        </div>
    `;
}

window.closeScheduleUiModal = function () {
    const modal = document.getElementById("scheduleUiModal");
    if (modal) modal.classList.remove("show");
};

// ============================================================
// HORAIRE - CU#4 (teammate's implementation)
// ============================================================
window.showScheduleFromCard = async function (courseId) {
    ensureScheduleUiModal();
    const modal = document.getElementById("scheduleUiModal");
    if (!modal) return;

    renderSchedulePreview(courseId, "A25");
    modal.classList.add("show");
};

async function showSchedule(courseId, semester) {
    const scheduleModalId = `schedule-modal-${courseId}-${semester}`;
    let modal = document.getElementById(scheduleModalId);
    
    if (!modal) {
        modal = document.createElement("div");
        modal.id = scheduleModalId;
        modal.className = "schedule-modal";
        modal.innerHTML = `
            <div class="schedule-modal-content">
                <div class="schedule-modal-header">
                    <h3>Horaire — ${courseId} (${semester})</h3>
                    <button class="schedule-modal-close" onclick="closeScheduleModal('${scheduleModalId}')">&times;</button>
                </div>
                <div class="schedule-modal-body" id="${scheduleModalId}-body">
                    <div class="loader"></div>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
    }
    
    modal.style.display = "block";
    const bodyEl = document.getElementById(`${scheduleModalId}-body`);

    try {
        const resp = await fetch(`${API_BASE}/courses/${encodeURIComponent(courseId)}/schedule?semester=${encodeURIComponent(semester)}`);
        const schedule = await safeJson(resp);

        if (!resp.ok) {
            bodyEl.innerHTML = `<div class='empty-state'>❌ ${schedule.error?.message || schedule.error || "Erreur lors du chargement de l'horaire"}</div>`;
            return;
        }

        if (!schedule?.sections?.length) {
            bodyEl.innerHTML = `<div class='empty-state'>Aucun horaire disponible pour ${courseId} au trimestre ${semester}.</div>`;
            return;
        }

        bodyEl.innerHTML = renderSchedule(schedule);
    } catch (e) {
        bodyEl.innerHTML = `<div class='empty-state'>Erreur réseau: ${e.message}</div>`;
    }
}

function renderSchedule(schedule) {
    const dayNames = {
        monday: "Lundi", tuesday: "Mardi", wednesday: "Mercredi", thursday: "Jeudi", friday: "Vendredi",
        lundi: "Lundi", mardi: "Mardi", mercredi: "Mercredi", jeudi: "Jeudi", vendredi: "Vendredi"
    };

    let html = `<div class="schedule-info">
        <p><strong>Cours:</strong> ${schedule.courseId || "N/A"}</p>
        <p><strong>Trimestre:</strong> ${schedule.semester || "N/A"}</p>
    </div>`;

    schedule.sections.forEach((section) => {
        html += `<div class="schedule-section">
            <h4 class="schedule-section-title">Section ${section.sectionCode || "N/A"}</h4>`;

        if (!section.activities?.length) {
            html += `<p class="schedule-empty">Aucune activité planifiée.</p>`;
        } else {
            section.activities.forEach((activity) => {
                html += `<div class="schedule-activity">
                    <div class="schedule-activity-type"><strong>${activity.type || "Activité"}</strong></div>`;

                if (!activity.entries?.length) {
                    html += `<p class="schedule-empty">Aucun horaire défini.</p>`;
                } else {
                    html += `<div class="schedule-entries">`;
                    activity.entries.forEach((entry) => {
                        const dayFr = dayNames[entry.day?.toLowerCase()] || entry.day || "N/A";
                        html += `<div class="schedule-entry">
                            <span class="schedule-day">${dayFr}</span>
                            <span class="schedule-time">${entry.startTime || ""} - ${entry.endTime || ""}</span>
                            ${entry.room ? `<span class="schedule-room">${entry.room}</span>` : ""}
                        </div>`;
                    });
                    html += `</div>`;
                }
                html += `</div>`;
            });
        }
        html += `</div>`;
    });

    return html;
}

window.closeScheduleModal = function (modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = "none";
    }
};

// Fermer la modale en cliquant à l'extérieur
window.onclick = function (event) {
    if (event.target.classList.contains("schedule-modal")) {
        event.target.style.display = "none";
    }
};

// ============================================================
// COMPARAISON DE COURS (corrigé : affiche aussi les résultats)
// ============================================================

// Ajoute un cours à la liste de comparaison
window.addToCompare = function (id) {
    if (compareList.includes(id)) {
        showToast("Cours déjà présent dans l'onglet comparaison");
        return;
    }
    compareList.push(id);
    updateComparePanel();
    showToast("Cours ajouté à l'onglet comparaison");
    const compareSection = document.getElementById("section-compare");
    if (compareSection) {
        compareSection.scrollIntoView({ behavior: "smooth" });
    }
};

// Retire un cours de la comparaison
window.removeFromCompare = function (id) {
    compareList = compareList.filter((x) => x !== id);
    updateComparePanel();
};

// Vider la comparaison
clearCompareBtn.onclick = () => {
    compareList = [];
    updateComparePanel();
};

// Normalise un sigle de cours (ex: "ift2255" → "IFT2255")
function normalizeCourseId(id) {
    return (id ?? "").toString().trim().toUpperCase();
}

// Affiche proprement une valeur dans un tableau
function displayCell(v) {
    if (v === null || v === undefined || v === "") return "N/A";
    return v;
}

// Construit le tableau comparatif des résultats académiques
function renderResultsComparison(data, id1, id2) {
    const stats = data.resultsStats || {};
    const r1 = stats[id1];
    const r2 = stats[id2];

    return `
        <div class="compare-summary">
            <h3 style="font-size:1rem; font-weight:900; text-transform:uppercase; margin-bottom:0.5rem;">Resultats academiques</h3>
            <div style="border:4px solid #000; overflow:hidden;">
                <table style="width:100%;border-collapse:collapse;font-size:0.85rem;">
                    <thead>
                        <tr style="border-bottom:4px solid #000; background:var(--neo-secondary);">
                            <th style="padding:8px 12px;text-align:left;font-size:0.72rem;font-weight:900;text-transform:uppercase;letter-spacing:0.06em;">Critere</th>
                            <th style="padding:8px 12px;text-align:center;font-size:0.72rem;font-weight:900;text-transform:uppercase;letter-spacing:0.06em;">${id1}</th>
                            <th style="padding:8px 12px;text-align:center;font-size:0.72rem;font-weight:900;text-transform:uppercase;letter-spacing:0.06em;">${id2}</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr style="border-bottom:2px solid #000;"><td style="padding:8px 12px;font-weight:900;">Moyenne</td><td style="padding:8px 12px;text-align:center;font-weight:700;">${displayCell(r1?.moyenne)}</td><td style="padding:8px 12px;text-align:center;font-weight:700;">${displayCell(r2?.moyenne)}</td></tr>
                        <tr style="border-bottom:2px solid #000;"><td style="padding:8px 12px;font-weight:900;">Score</td><td style="padding:8px 12px;text-align:center;font-weight:700;">${displayCell(r1?.score)}</td><td style="padding:8px 12px;text-align:center;font-weight:700;">${displayCell(r2?.score)}</td></tr>
                        <tr style="border-bottom:2px solid #000;"><td style="padding:8px 12px;font-weight:900;">Participants</td><td style="padding:8px 12px;text-align:center;font-weight:700;">${displayCell(r1?.participants)}</td><td style="padding:8px 12px;text-align:center;font-weight:700;">${displayCell(r2?.participants)}</td></tr>
                        <tr style="border-bottom:2px solid #000;"><td style="padding:8px 12px;font-weight:900;">Trimestres</td><td style="padding:8px 12px;text-align:center;font-weight:700;">${displayCell(r1?.trimestres)}</td><td style="padding:8px 12px;text-align:center;font-weight:700;">${displayCell(r2?.trimestres)}</td></tr>
                        <tr><td style="padding:8px 12px;font-weight:900;">Indice difficulte</td><td style="padding:8px 12px;text-align:center;font-weight:700;">${displayCell(r1?.difficultyIndex)}</td><td style="padding:8px 12px;text-align:center;font-weight:700;">${displayCell(r2?.difficultyIndex)}</td></tr>
                    </tbody>
                </table>
            </div>
        </div>
    `;
}

// Met à jour le panneau de comparaison
async function updateComparePanel() {
    document
        .querySelectorAll(".compare-summary, .compare-total, .compare-hint")
        .forEach(e => e.remove());

    if (compareList.length === 0) {
        compareResultsDiv.innerHTML =
            "<div class='empty-state'>Ajoutez des cours pour les comparer.</div>";
        clearCompareBtn.style.display = "none";
        return;
    }

    clearCompareBtn.style.display = "block";
    compareResultsDiv.innerHTML = '<div class="loader"></div>';

    try {
        // Cas : un seul cours
        if (compareList.length === 1) {
            const id = compareList[0];
            const r = await fetch(`${API_BASE}/courses/${encodeURIComponent(id)}`);
            const course = await safeJson(r);

            compareResultsDiv.innerHTML = "";
            compareResultsDiv.insertAdjacentHTML(
                "beforeend",
                renderCard(course, true)
            );
            return;
        }

        // Cas : comparaison de 2 cours
        const query = compareList.map(id => "id=" + encodeURIComponent(id)).join("&");
        const r = await fetch(`${API_BASE}/courses/compare?${query}`);
        const data = await safeJson(r);

        compareResultsDiv.innerHTML = "";

        const ids = compareList.map(normalizeCourseId);
        const id1 = ids[0];
        const id2 = ids[1];

        // Afficher les cartes
        (data.courses || []).forEach(c => {
            compareResultsDiv.insertAdjacentHTML(
                "beforeend",
                renderCard(c, true)
            );
        });

        // Afficher le tableau des résultats académiques
        compareResultsDiv.insertAdjacentHTML(
            "beforeend",
            renderResultsComparison(data, id1, id2)
        );

    } catch (e) {
        console.error(e);
        compareResultsDiv.innerHTML =
            "<div class='empty-state'>Erreur lors de la comparaison.</div>";
    }
}


// ============================================================
// UTILITAIRES
// ============================================================
window.toggleDetails = async function (id) {
    const el = document.getElementById(id);
    const btn = document.getElementById("btn-" + id);
    if (!el || !btn) return;

    const open = el.style.display === "block";
    el.style.display = open ? "none" : "block";
    btn.textContent = open ? "Voir details" : "Masquer";

    if (!open) {
        const courseId = el.getAttribute("data-course-id");
        const reviewsBoxId = el.getAttribute("data-reviews-box-id");
        if (courseId && reviewsBoxId) {
            const box = document.getElementById(reviewsBoxId);
            if (box && box.getAttribute("data-loaded") !== "true") {
                await renderReviewsIn(box, courseId);
                box.setAttribute("data-loaded", "true");
            }
        }
    }
};

function formatPrerequisites(c) {
    const p = c.prerequisite_courses;
    if (!p || p.length === 0) return "Aucun";
    if (Array.isArray(p)) return p.join(", ");
    return p;
}

function formatSessions(c) {
    if (!c.available_terms) return "Non spécifié";
    const map = { autumn: "Automne", winter: "Hiver", summer: "Été", spring: "Printemps" };
    return (
        Object.entries(c.available_terms)
            .filter(([_, v]) => v === true)
            .map(([k]) => map[k] || k)
            .join(", ") || "Aucune session"
    );
}

function parseCompletedCourses(text) {
    if (!text) return [];
    return text.replace(/\n/g, ",").split(",").map((s) => s.trim()).filter(Boolean).map((s) => s.toUpperCase());
}

async function safeJson(resp) {
    const txt = await resp.text();
    try {
        return JSON.parse(txt);
    } catch {
        return { raw: txt };
    }
}

function showTool(payload) {
    toolOutput.classList.remove("ok", "bad", "warn");

    if (typeof payload === "string") {
        toolOutput.textContent = payload;
        toolOutput.classList.add("warn");
        return;
    }

    if (payload && payload.error) {
        toolOutput.classList.add("bad");
        const msg = payload.error?.error?.message || payload.error?.message || JSON.stringify(payload.error);
        toolOutput.textContent = `❌ Erreur (${payload.status ?? ""})\n${msg}`;
        return;
    }

    const raw = payload.raw ?? payload;

    if (raw && typeof raw.eligible === "boolean") {
        const ok = raw.eligible;
        const cycleOk = raw.cycle_ok ?? raw.cycleOk ?? null;
        const missing = raw.missing_prerequisites ?? raw.missingPrerequisites ?? [];

        toolOutput.classList.add(ok ? "ok" : "bad");

        const lines = [];
        lines.push(ok ? "✅ Résultat : ÉLIGIBLE" : "❌ Résultat : NON ÉLIGIBLE");
        lines.push("");

        if (raw.course_id) lines.push(`📌 Cours : ${raw.course_id}`);
        if (typeof cycleOk === "boolean") lines.push(`🎓 Cycle : ${cycleOk ? "OK" : "Non approprié"}`);
        if (raw.message) lines.push(`💬 ${raw.message}`);

        if (Array.isArray(missing) && missing.length > 0) {
            lines.push("");
            lines.push("📚 Prérequis manquants :");
            missing.forEach((p) => lines.push(` • ${p}`));
        }

        toolOutput.textContent = lines.join("\n");
        return;
    }

    if (raw && (raw.sigle || raw.score || raw.moyenne || raw.participants || raw.trimestres)) {
        toolOutput.classList.add("ok");

        const lines = [];
        lines.push("📊 Résultats académiques");
        lines.push("");

        if (raw.sigle) lines.push(`📌 Sigle : ${raw.sigle}`);
        if (raw.nom) lines.push(`📖 Nom : ${raw.nom}`);
        if (raw.moyenne) lines.push(`🅰️ Moyenne : ${raw.moyenne}`);
        if (raw.score !== undefined) lines.push(`⭐ Score : ${raw.score}`);
        if (raw.participants !== undefined) lines.push(`👥 Participants : ${raw.participants}`);
        if (raw.trimestres !== undefined) lines.push(`🗓️ Trimestres : ${raw.trimestres}`);

        if (raw.error) {
            toolOutput.classList.remove("ok");
            toolOutput.classList.add("bad");
            lines.push("");
            lines.push("❌ Erreur :");
            lines.push(raw.error);
        }

        toolOutput.textContent = lines.join("\n");
        return;
    }

    toolOutput.classList.add("warn");
    toolOutput.textContent = "Réponse reçue, mais format inconnu.";
}

/**
 * Génère le dropdown pour ajouter à un ensemble
 */
function generateEnsembleDropdown(courseId, courseName) {
    const availableEnsembles = Object.entries(ensembles).filter(([_, e]) => e.cours.length < 6);

    if (availableEnsembles.length === 0) {
        return `<button class="btn-card-action" onclick="alert('Creez d\\'abord un ensemble dans l\\'onglet Ensembles.')" style="opacity:0.45;cursor:not-allowed;margin-top:6px;width:100%;">Ajouter a un ensemble</button>`;
    }

    let options = availableEnsembles.map(([num, e]) => {
        const coursCount = e.cours.length;
        return `<option value="${num}">Ensemble ${num} (${coursCount}/6)</option>`;
    }).join("");

    const selectId = `ensemble-select-${courseId}-${cardSeq}`;

    return `
        <div style="display:flex; gap:0; margin-top:6px; border:4px solid #000; box-shadow:2px 2px 0px 0px #000;">
            <select id="${selectId}" style="flex:1; font-size:0.8rem; border:none; border-right:4px solid #000; box-shadow:none;">
                <option value="">Ajouter a un ensemble...</option>
                ${options}
            </select>
            <button class="btn-card-action" onclick="addCourseFromDropdown('${courseId}', '${escapeHtml(courseName).replace(/'/g, "\\'")}', '${selectId}')" style="border:none;box-shadow:none;min-width:42px;">+</button>
        </div>
    `;
}

window.addCourseFromDropdown = function(courseId, courseName, selectId) {
    const select = document.getElementById(selectId);
    if (!select || !select.value) {
        alert("Veuillez sélectionner un ensemble.");
        return;
    }
    
    const ensembleNum = parseInt(select.value);
    ajouterCoursAEnsemble(courseId, courseName, ensembleNum);
    
    select.value = "";
};

function renderCard(c, isComparisonView) {
    const courseId = (c.id ?? c._id ?? "UNKNOWN").toString();
    const courseName = c.name || "";
    const uniqueDescId = `desc-${courseId}-${++cardSeq}`;
    const reviewsBoxId = `reviews-box-${courseId}-${cardSeq}`;

    return `
        <div class="card">
            <div class="card-header">
                <span class="card-sigle">${courseId}</span>
                <span class="card-credits">${c.credits ?? 0} cr.</span>
            </div>

            <div class="card-title">${courseName}</div>

            <div style="font-size:0.82rem; font-weight:700;">
                Sessions : ${formatSessions(c)}
            </div>

            <button id="btn-${uniqueDescId}"
                    class="btn-card-action"
                    onclick="toggleDetails('${uniqueDescId}')"
                    style="width:100%;margin-top:6px;">
                Voir details
            </button>

            <div id="${uniqueDescId}" style="display:none;"
                 data-course-id="${courseId}"
                 data-reviews-box-id="${reviewsBoxId}">
                <div class="card-description">${c.description ?? "Aucune description disponible."}</div>
                <div id="${reviewsBoxId}"></div>
            </div>

            <div class="card-prereq">
                <strong>Prerequis</strong> ${formatPrerequisites(c)}
            </div>

            ${!isComparisonView ? `
                <div class="card-btn-grid">
                    <button class="btn-card-action" onclick="addToCompare('${courseId}')">Comparer</button>
                    <button class="btn-card-action" onclick="checkEligibilityFromCard('${courseId}')">Eligibilite</button>
                    <button class="btn-card-action" onclick="loadResultsFromCard('${courseId}')">Resultats</button>
                    <button class="btn-card-action" onclick="showScheduleFromCard('${courseId}')">Horaire</button>
                </div>
                <div id="card-eligibility-${courseId}" style="display:none;"></div>
                <div id="card-results-${courseId}" style="display:none;"></div>
                ${generateEnsembleDropdown(courseId, courseName)}
            ` : `
                <button class="btn-card-action" onclick="removeFromCompare('${courseId}')" style="width:100%;margin-top:6px;background:var(--neo-accent);">Retirer</button>
            `}
        </div>
    `;
}