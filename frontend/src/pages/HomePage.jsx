import './HomePage.css';

function HomePage() {
  return (
    <div className="page">
      <div className="container">
        <div className="home-hero">
          <h1>Welcome to TicketSystem</h1>
          <p className="hero-description">
            Our TicketSystem is a comprehensive platform designed to streamline 
            customer support and issue management. Whether you need to report a bug, 
            request a feature, or get technical assistance, our system makes it easy 
            to track and resolve your concerns efficiently.
          </p>
          <p className="hero-subtitle">
            Get started by creating an account to submit tickets, track their progress, 
            and communicate directly with our support team.
          </p>
        </div>

        <div className="features-grid">
          <div className="card feature-card">
            <h3>Easy Ticket Creation</h3>
            <p>Submit support requests quickly with our intuitive ticket creation system.</p>
          </div>

          <div className="card feature-card">
            <h3>Real-time Tracking</h3>
            <p>Monitor the progress of your tickets and receive updates in real-time.</p>
          </div>

          <div className="card feature-card">
            <h3>Secure Communication</h3>
            <p>Communicate securely with our support team through encrypted channels.</p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default HomePage;
