/*
 * OBSOLETE PAGE COMPONENT - NO LONGER USED
 * 
 * This MyTicketsPage component has been replaced by EndUserTicketsPage.jsx during the UI restructuring.
 * 
 * Navigation flow changed from:
 * - Separate /my-tickets page (this component)
 * To:
 * - Unified /tickets page with tabs (EndUserTicketsPage.jsx)
 * 
 * EndUsers now access their tickets through:
 * - /tickets → EndUserTicketsPage with "Meine Tickets" and "Alle Tickets" tabs
 * 
 * The /my-tickets route has been removed from the sidebar navigation.
 * Only /my-tickets/:ticketId route remains active for ticket detail pages.
 * 
 * This file can be safely deleted.
 * MyTicketsPage.css is still used by EndUserTicketsPage.jsx and should NOT be deleted.
 * 
 * Kept as placeholder to document the architectural change.
 */

// Import kept for CSS dependency documentation
import './MyTicketsPage.css';

// Placeholder export to prevent import errors if accidentally imported
export default function ObsoleteMyTicketsPage() {
  console.warn('MyTicketsPage component is obsolete. EndUsers should use EndUserTicketsPage (/tickets) instead.');
  return null;
}
