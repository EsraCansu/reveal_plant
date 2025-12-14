/**
 * Component Loader
 * Dinamik olarak header ve footer yükler
 */
async function loadComponent(elementId, filePath) {
    try {
        const response = await fetch(filePath);
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        const html = await response.text();
        const element = document.getElementById(elementId);
        if (element) {
            element.innerHTML = html;
        }
    } catch (error) {
        console.error(`Component loading failed: ${filePath}`, error);
    }
}

/**
 * Tüm komponentleri yükle (dashboard/diagnoses/profile/settings için)
 */
function loadAllComponents() {
    loadComponent('header', '../../assets/components/header.html');
    loadComponent('footer', '../../assets/components/footer.html');
}
