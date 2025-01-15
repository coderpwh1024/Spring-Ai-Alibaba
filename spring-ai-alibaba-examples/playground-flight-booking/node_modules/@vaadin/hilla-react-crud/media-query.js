import { useEffect, useState } from "react";
function useMediaQuery(query) {
  const [matches, setMatches] = useState(() => window.matchMedia(query).matches);
  useEffect(() => {
    const listener = (e) => setMatches(e.matches);
    const mediaQuery = window.matchMedia(query);
    setMatches(mediaQuery.matches);
    mediaQuery.addEventListener("change", listener);
    return () => mediaQuery.removeEventListener("change", listener);
  }, [query]);
  return matches;
}
export {
  useMediaQuery
};
//# sourceMappingURL=media-query.js.map
