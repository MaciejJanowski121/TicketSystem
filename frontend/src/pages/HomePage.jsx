import { useState } from 'react';
import './HomePage.css';

function HomePage() {
  const [isHelpExpanded, setIsHelpExpanded] = useState(false);

  const toggleHelp = () => {
    setIsHelpExpanded(!isHelpExpanded);
  };

  return (
    <div className="page">
      <div className="container">
        <div className="home-hero">
          <h1>Willkommen im TicketSystem</h1>
          <p className="hero-description">
            Unser TicketSystem ist eine umfassende Plattform zur Optimierung 
            des Kundensupports und der Problemverwaltung. Ob Sie einen Fehler melden, 
            eine Funktion anfordern oder technische Unterstützung benötigen – unser System 
            macht es einfach, Ihre Anliegen effizient zu verfolgen und zu lösen.
          </p>
          <p className="hero-subtitle">
            Beginnen Sie, indem Sie ein Konto erstellen, um Tickets einzureichen, deren Fortschritt 
            zu verfolgen und direkt mit unserem Support-Team zu kommunizieren.
          </p>
        </div>

        {/* Help & Wiki Section - Moved up for better visibility */}
        <div className="help-section">
          <div className="help-header" onClick={toggleHelp}>
            <h2>Hilfe & Erklärung – Ticket-System</h2>
            <span className={`help-toggle ${isHelpExpanded ? 'expanded' : ''}`}>
              {isHelpExpanded ? '−' : '+'}
            </span>
          </div>

          {isHelpExpanded && (
            <div className="help-content">
              <div className="help-grid">
                <div className="help-card">
                  <h3>🎯 Zweck des Ticket-Systems</h3>
                  <p>
                    Das Ticket-System dient zur effizienten Verwaltung und Bearbeitung von 
                    Support-Anfragen. Es ermöglicht eine strukturierte Kommunikation zwischen 
                    Anwendern und Support-Team und stellt sicher, dass keine Anfrage verloren geht.
                  </p>
                </div>

                <div className="help-card">
                  <h3>👥 Benutzerrollen</h3>
                  <ul>
                    <li><strong>End-Anwender:</strong> Können Tickets erstellen, eigene Tickets einsehen und kommentieren</li>
                    <li><strong>Support-Anwender:</strong> Können Tickets zuweisen, bearbeiten, schließen und kommentieren</li>
                    <li><strong>Admin-Anwender:</strong> Haben alle Rechte plus Benutzerverwaltung und Ticket-Löschung</li>
                  </ul>
                </div>

                <div className="help-card">
                  <h3>🔄 Ticket-Workflow</h3>
                  <div className="workflow-steps">
                    <div className="workflow-step">
                      <span className="step-number">1</span>
                      <span className="step-text"><strong>Erstellen:</strong> End-Anwender erstellt ein neues Ticket</span>
                    </div>
                    <div className="workflow-step">
                      <span className="step-number">2</span>
                      <span className="step-text"><strong>Zuweisen:</strong> Support-Anwender übernimmt das Ticket</span>
                    </div>
                    <div className="workflow-step">
                      <span className="step-number">3</span>
                      <span className="step-text"><strong>Bearbeitung:</strong> Ticket wird aktiv bearbeitet</span>
                    </div>
                    <div className="workflow-step">
                      <span className="step-number">4</span>
                      <span className="step-text"><strong>Schließen:</strong> Problem wird gelöst und Ticket geschlossen</span>
                    </div>
                  </div>
                </div>

                <div className="help-card">
                  <h3>📊 Ticket-Status</h3>
                  <div className="status-list">
                    <div className="status-item">
                      <span className="status-badge unassigned">Nicht zugeordnet</span>
                      <span>Ticket wurde erstellt, aber noch nicht zugewiesen</span>
                    </div>
                    <div className="status-item">
                      <span className="status-badge in-progress">In Bearbeitung</span>
                      <span>Ticket wird aktiv von einem Support-Mitarbeiter bearbeitet</span>
                    </div>
                    <div className="status-item">
                      <span className="status-badge closed">Abgeschlossen</span>
                      <span>Problem wurde gelöst und Ticket geschlossen</span>
                    </div>
                  </div>
                </div>

                <div className="help-card">
                  <h3>💬 Kommunikation</h3>
                  <p>
                    <strong>Kommentare</strong> ermöglichen die Kommunikation zwischen Anwendern und Support-Team:
                  </p>
                  <ul>
                    <li>End-Anwender können ihre eigenen Tickets kommentieren</li>
                    <li>Support-Anwender können zugewiesene Tickets kommentieren</li>
                    <li>Admin-Anwender können alle Tickets kommentieren</li>
                    <li>Beim Schließen eines Tickets ist ein abschließender Kommentar vom Support erforderlich</li>
                  </ul>
                </div>

                <div className="help-card">
                  <h3>🚀 Erste Schritte</h3>
                  <ol>
                    <li>Registrieren Sie sich oder melden Sie sich an</li>
                    <li>Erstellen Sie ein neues Ticket mit einer aussagekräftigen Beschreibung</li>
                    <li>Wählen Sie die passende Kategorie aus</li>
                    <li>Verfolgen Sie den Status Ihres Tickets</li>
                    <li>Kommunizieren Sie über Kommentare mit dem Support-Team</li>
                  </ol>
                </div>
              </div>
            </div>
          )}
        </div>

        <div className="features-grid">
          <div className="card feature-card">
            <h3>Einfache Ticket-Erstellung</h3>
            <p>Reichen Sie Support-Anfragen schnell mit unserem intuitiven Ticket-Erstellungssystem ein.</p>
          </div>

          <div className="card feature-card">
            <h3>Echtzeit-Verfolgung</h3>
            <p>Verfolgen Sie den Fortschritt Ihrer Tickets und erhalten Sie Updates in Echtzeit.</p>
          </div>

          <div className="card feature-card">
            <h3>Sichere Kommunikation</h3>
            <p>Kommunizieren Sie sicher mit unserem Support-Team über verschlüsselte Kanäle.</p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default HomePage;
