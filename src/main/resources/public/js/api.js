import { appState } from './state.js';

export async function api(path, options = {}) {
    const headers = new Headers(options.headers || {});
    if (appState.token) headers.set('Authorization', `Bearer ${appState.token}`);
    const isFormData = typeof FormData !== 'undefined' && options.body instanceof FormData;
    if (options.body && !isFormData && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');

    const response = await fetch(path, {
        credentials: 'same-origin',
        ...options,
        headers,
    });

    const contentType = response.headers.get('content-type') || '';
    const payload = contentType.includes('application/json') ? await response.json() : await response.text();

    if (!response.ok) {
        const message = typeof payload === 'string' ? payload : payload?.message || response.statusText;
        throw new Error(message || 'Request failed');
    }

    return payload;
}