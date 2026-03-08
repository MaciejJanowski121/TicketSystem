/*
 * OBSOLETE COMPONENT - NO LONGER USED
 * 
 * This Navbar component has been replaced by Sidebar.jsx during the UI restructuring.
 * 
 * Navigation approach changed from:
 * - Horizontal navbar at the top (this component)
 * To:
 * - Vertical sidebar on the left (Sidebar.jsx)
 * 
 * This component referenced obsolete routes:
 * - /my-tickets (replaced by unified /tickets page with tabs for EndUsers)
 * - /support/tickets (replaced by unified /tickets page with tabs for Support/Admin)
 * 
 * This file can be safely deleted along with Navbar.css.
 * Kept as placeholder to document the architectural change.
 */

// Placeholder export to prevent import errors if accidentally imported
export default function ObsoleteNavbar() {
  console.warn('Navbar component is obsolete. Use Sidebar component instead.');
  return null;
}
