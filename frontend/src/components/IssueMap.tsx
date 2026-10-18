import React, { useEffect, useRef } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import type { Issue } from '../services/api';

// Fix for default Leaflet icon paths in bundlers
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconUrl: markerIcon,
  iconRetinaUrl: markerIcon2x,
  shadowUrl: markerShadow,
});

interface IssueMapProps {
  issues: Issue[];
  center: [number, number]; // [lat, lng]
  zoom?: number;
  onMapClick?: (lat: number, lng: number) => void;
  selectedLocation?: [number, number] | null;
  onSelectIssue?: (issueId: number) => void;
}

export const IssueMap: React.FC<IssueMapProps> = ({
  issues,
  center,
  zoom = 13,
  onMapClick,
  selectedLocation,
  onSelectIssue,
}) => {
  const mapContainerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<L.Map | null>(null);
  const markersLayerRef = useRef<L.LayerGroup | null>(null);
  const tempMarkerRef = useRef<L.Marker | null>(null);

  // Status-colored SVGs for premium custom map markers
  const getMarkerIcon = (status: string) => {
    let color = '#0ea5e9'; // Info / Reported
    if (status === 'ACKNOWLEDGED') color = '#f59e0b';
    if (status === 'IN_PROGRESS') color = '#6366f1';
    if (status === 'RESOLVED') color = '#10b981';

    const svgIcon = `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="${color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="width:30px; height:30px; filter: drop-shadow(0px 2px 6px rgba(0,0,0,0.5));">
        <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
        <circle cx="12" cy="10" r="3" fill="${color}"></circle>
      </svg>
    `;

    return L.divIcon({
      html: svgIcon,
      className: 'custom-map-marker',
      iconSize: [30, 30],
      iconAnchor: [15, 30],
      popupAnchor: [0, -30],
    });
  };

  // Store onMapClick in a ref to avoid stale closures in the map's click event handler
  const onMapClickRef = useRef(onMapClick);
  useEffect(() => {
    onMapClickRef.current = onMapClick;
  }, [onMapClick]);

  // 1. Initialize Map
  useEffect(() => {
    if (!mapContainerRef.current) return;

    // Create map instance
    const map = L.map(mapContainerRef.current, {
      center: center,
      zoom: zoom,
      zoomControl: true,
    });

    // OpenStreetMap tile layer (100% free, no API key or watermark required)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
      maxZoom: 19,
      className: 'dark-tiles',
    }).addTo(map);

    // Group for dynamic issue markers
    const markersLayer = L.layerGroup().addTo(map);
    markersLayerRef.current = markersLayer;
    mapRef.current = map;

    // Trigger size recalculation to prevent grey/unloaded tile areas
    setTimeout(() => {
      map.invalidateSize();
    }, 250);

    // Map click registration for reporting coordinates
    map.on('click', (e: L.LeafletMouseEvent) => {
      if (onMapClickRef.current) {
        onMapClickRef.current(e.latlng.lat, e.latlng.lng);
      }
    });

    return () => {
      map.remove();
      mapRef.current = null;
    };
  }, []);

  // 2. Fly to center if it changes programmatically
  useEffect(() => {
    if (mapRef.current) {
      mapRef.current.flyTo(center, zoom, { duration: 1.5 });
    }
  }, [center, zoom]);

  // 3. Render Dynamic Markers on Issues list update
  useEffect(() => {
    const map = mapRef.current;
    const markersLayer = markersLayerRef.current;
    if (!map || !markersLayer) return;

    map.invalidateSize();

    // Clear previous markers
    markersLayer.clearLayers();

    issues.forEach((issue) => {
      const marker = L.marker([issue.latitude, issue.longitude], {
        icon: getMarkerIcon(issue.status),
      });

      // Bind detailed status info in popup
      const popupContent = document.createElement('div');
      popupContent.style.padding = '8px';
      popupContent.style.minWidth = '180px';
      popupContent.innerHTML = `
        <h4 style="margin: 0 0 6px 0; font-family: var(--font-heading); font-size: 14px; font-weight:600;">${issue.title}</h4>
        <div style="font-size: 12px; color: var(--text-secondary); margin-bottom: 8px;">
          Category: <strong style="color:var(--text-primary);">${issue.category.name}</strong><br/>
          Status: <strong style="color:var(--text-primary);">${issue.status}</strong>
        </div>
        <button id="popup-btn-${issue.id}" style="width: 100%; border: none; background: linear-gradient(135deg, var(--accent-primary) 0%, var(--accent-secondary) 100%); color: white; padding: 6px 12px; border-radius: 4px; font-weight:600; font-size: 11px; cursor: pointer; text-align:center;">
          View Details
        </button>
      `;

      marker.bindPopup(popupContent);
      markersLayer.addLayer(marker);

      marker.on('popupopen', () => {
        const btn = document.getElementById(`popup-btn-${issue.id}`);
        if (btn && onSelectIssue) {
          btn.onclick = () => {
            onSelectIssue(issue.id);
            marker.closePopup();
          };
        }
      });
    });
  }, [issues, onSelectIssue]);

  // 4. Render Temp Marker for current selected coordinate (e.g. reporting)
  useEffect(() => {
    const map = mapRef.current;
    if (!map) return;


    if (selectedLocation) {
      if (tempMarkerRef.current) {
        // Update marker position dynamically instead of recreating (prevents snap/flicker)
        const currentLatLng = tempMarkerRef.current.getLatLng();
        if (currentLatLng.lat !== selectedLocation[0] || currentLatLng.lng !== selectedLocation[1]) {
          tempMarkerRef.current.setLatLng(selectedLocation);
        }
      } else {
        const redPin = L.divIcon({
          html: `
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="width:36px; height:36px; filter: drop-shadow(0px 2px 8px rgba(239,68,68,0.7));">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
              <circle cx="12" cy="10" r="3" fill="#ef4444"></circle>
            </svg>
          `,
          className: 'temp-marker',
          iconSize: [36, 36],
          iconAnchor: [18, 36],
        });

        const marker = L.marker(selectedLocation, { 
          icon: redPin,
          draggable: true 
        }).addTo(map);

        // Update coordinates in parent form on drag end
        marker.on('dragend', () => {
          const position = marker.getLatLng();
          if (onMapClick) {
            onMapClick(position.lat, position.lng);
          }
        });

        tempMarkerRef.current = marker;
      }
      map.panTo(selectedLocation);
    } else {
      if (tempMarkerRef.current) {
        tempMarkerRef.current.remove();
        tempMarkerRef.current = null;
      }
    }
  }, [selectedLocation]);

  return (
    <div 
      ref={mapContainerRef} 
      className="glass-panel"
      style={{ 
        width: '100%', 
        height: '100%', 
        minHeight: '400px', 
        borderRadius: 'var(--radius-md)', 
        overflow: 'hidden', 
        zIndex: 1 
      }} 
    />
  );
};
