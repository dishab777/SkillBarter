import { useEffect, useState } from "react";

export function useParallax(factor = 0.08): number {
  const [offset, setOffset] = useState(0);

  useEffect(() => {
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;
    let frame = 0;
    const update = () => {
      window.cancelAnimationFrame(frame);
      frame = window.requestAnimationFrame(() => setOffset(Math.min(window.scrollY * factor, 36)));
    };
    window.addEventListener("scroll", update, { passive: true });
    return () => {
      window.removeEventListener("scroll", update);
      window.cancelAnimationFrame(frame);
    };
  }, [factor]);

  return offset;
}
