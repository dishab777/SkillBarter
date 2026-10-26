import { useCallback } from "react";

export function useTilt() {
  const onPointerMove = useCallback(
    (event: React.PointerEvent<HTMLElement>) => {
      if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;
      if (window.matchMedia("(max-width: 760px)").matches) return;
      const bounds = event.currentTarget.getBoundingClientRect();
      const x = (event.clientX - bounds.left) / bounds.width - 0.5;
      const y = (event.clientY - bounds.top) / bounds.height - 0.5;
      event.currentTarget.style.setProperty("--tilt-x", `${-y * 5}deg`);
      event.currentTarget.style.setProperty("--tilt-y", `${x * 5}deg`);
    },
    [],
  );

  const onPointerLeave = useCallback((event: React.PointerEvent<HTMLElement>) => {
    event.currentTarget.style.setProperty("--tilt-x", "0deg");
    event.currentTarget.style.setProperty("--tilt-y", "0deg");
  }, []);

  return { onPointerMove, onPointerLeave };
}
