/**
 * Component Loader
 * Dinamik olarak header ve footer yükler
 */
async function loadComponent(elementId, filePath) {
    console.log(`[Component Loader] Loading ${elementId} from ${filePath}`);
    try {
        const response = await fetch(filePath);
        console.log(`[Component Loader] ${elementId} response status:`, response.status);
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        const html = await response.text();
        console.log(`[Component Loader] ${elementId} HTML length:`, html.length);
        const element = document.getElementById(elementId);
        if (element) {
            element.innerHTML = html;
            console.log(`[Component Loader] ${elementId} loaded successfully`);
        } else {
            console.error(`[Component Loader] Element #${elementId} not found in DOM`);
        }
    } catch (error) {
        console.error(`[Component Loader] Failed to load ${elementId}:`, error);
    }
}

/**
 * Tüm komponentleri yükle (dashboard/diagnoses/profile/settings için)
 */
function loadAllComponents() {
    loadComponent('header', '../../assets/components/header.html');
    loadComponent('footer', '../../assets/components/footer.html');
}
