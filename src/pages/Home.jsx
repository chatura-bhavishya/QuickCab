import React from 'react';
import { Link } from 'react-router-dom';

export default function Home() {
  return (
    <>
      <div className="hero">
        <h1>Welcome to QuickCab</h1>
        <p>Book a cab in seconds. Cars, bikes & autos at your fingertips.</p>
        <Link to="/user/register" className="btn">Get Started</Link>
      </div>

      <div className="card">
        <h2>Quick Access</h2>
        <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
          <Link to="/user/login" className="btn">User Login</Link>
          <Link to="/user/register" className="btn btn-dark">User Register</Link>
          <Link to="/driver/login" className="btn">Driver Login</Link>
          <Link to="/driver/register" className="btn btn-dark">Driver Register</Link>
        </div>
      </div>
    </>
  );
}
