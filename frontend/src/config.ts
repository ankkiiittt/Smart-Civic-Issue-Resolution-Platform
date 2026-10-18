// Configuration for backend server URLs
// Reads environment variables when deployed (e.g. on Netlify), with localhost fallback for local development.

const rawServerUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// Strip any trailing slashes from the server URL
export const SERVER_URL = rawServerUrl.replace(/\/+$/, '');

// REST API Base URL
export const API_BASE_URL = `${SERVER_URL}/api`;

// WebSocket Broker URL (auto-switches ws:// or wss:// based on http/https)
export const WS_BROKER_URL = (() => {
  if (import.meta.env.VITE_WS_URL) {
    return import.meta.env.VITE_WS_URL;
  }
  const wsProtocol = SERVER_URL.startsWith('https://') ? 'wss://' : 'ws://';
  const host = SERVER_URL.replace(/^https?:\/\//, '');
  return `${wsProtocol}${host}/ws`;
})();

// SockJS fallback endpoint (uses standard HTTP/HTTPS protocol)
export const SOCKJS_URL = `${SERVER_URL}/ws`;

// Helper function to resolve photo and static resource URLs safely
export function getPhotoUrl(photoUrl: string | undefined | null): string {
  if (!photoUrl) return '';
  if (photoUrl.startsWith('http://') || photoUrl.startsWith('https://') || photoUrl.startsWith('data:')) {
    return photoUrl;
  }
  const cleanPath = photoUrl.startsWith('/') ? photoUrl : `/${photoUrl}`;
  return `${SERVER_URL}${cleanPath}`;
}

// Swagger UI OpenAPI Documentation URL
export const SWAGGER_URL = `${SERVER_URL}/swagger-ui/index.html`;
