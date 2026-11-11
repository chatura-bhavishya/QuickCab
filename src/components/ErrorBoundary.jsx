import { Component } from 'react';

/**
 * Catches rendering errors anywhere below it and shows a friendly message
 * instead of a blank page. (Error boundaries must be class components.)
 */
export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error, info) {
    // Logged for developers only; nothing sensitive is shown to the user.
    console.error('Unexpected UI error', error, info?.componentStack);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="card" role="alert">
          <h2>Something went wrong</h2>
          <p className="muted">Please reload the page. If the problem continues, try again later.</p>
          <button className="btn" style={{ marginTop: 12 }} onClick={() => window.location.reload()}>
            Reload
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}
