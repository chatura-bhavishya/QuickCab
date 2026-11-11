// Lets non-button elements (cards, stars) be activated with Enter or Space.
const ACTIVATION_KEYS = ['Enter', ' '];

export function onActivateKey(handler) {
  return (e) => {
    if (ACTIVATION_KEYS.includes(e.key)) {
      e.preventDefault();
      handler();
    }
  };
}
