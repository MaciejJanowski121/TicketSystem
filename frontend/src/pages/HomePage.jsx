import './HomePage.css';

function HomePage() {

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
