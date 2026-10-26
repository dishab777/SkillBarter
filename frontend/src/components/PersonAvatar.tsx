import { initials } from "../utils/formatters";

const colors = ["#d4e0a0", "#f3c7a3", "#b5d6ca", "#e7c6ce", "#c9d1eb", "#e8dba8"];

export function PersonAvatar({
  name,
  size = "medium",
}: {
  name: string;
  size?: "small" | "medium" | "large";
}) {
  const colorIndex = name.split("").reduce((sum, letter) => sum + letter.charCodeAt(0), 0) % colors.length;

  return (
    <span
      className={`person-avatar person-avatar--${size}`}
      style={{ backgroundColor: colors[colorIndex] }}
      aria-label={name}
      role="img"
    >
      {initials(name)}
    </span>
  );
}
