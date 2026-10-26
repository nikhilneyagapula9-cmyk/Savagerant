import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { Navigate, Route, Routes, useLocation, useNavigate } from "react-router-dom";
import { currentUser, seedPosts, stories } from "./data.js";
import { AuthPage, CreatePostModal, StoryViewer } from "./components/Modals.jsx";
import { MainLayout } from "./components/Layout.jsx";
import { FeedPage, MessagesPage, NotificationsPage, ProfilePage, ReelsPage, SearchPage } from "./pages/Pages.jsx";

/** @typedef {typeof seedPosts[number]} DevGramPost */
/** @typedef {Array<DevGramPost>} DevGramPosts */
/**
 * @typedef {Object} DevGramContext
 * @property {typeof currentUser} user
 * @property {import("react").Dispatch<import("react").SetStateAction<typeof currentUser>>} setUser
 * @property {DevGramPosts} posts
 * @property {import("react").Dispatch<import("react").SetStateAction<DevGramPosts>>} setPosts
 * @property {string[]} saved
 * @property {import("react").Dispatch<import("react").SetStateAction<string[]>>} setSaved
 * @property {string[]} liked
 * @property {import("react").Dispatch<import("react").SetStateAction<string[]>>} setLiked
 * @property {"dark" | "light"} theme
 * @property {import("react").Dispatch<import("react").SetStateAction<"dark" | "light">>} setTheme
 * @property {import("react").Dispatch<import("react").SetStateAction<boolean>>} setCreateOpen
 * @property {import("react").Dispatch<import("react").SetStateAction<typeof stories[number] | null>>} setActiveStory
 * @property {(message: string) => void} notify
 */
/** @type {import("react").Context<DevGramContext | null>} */
const AppContext = createContext(null);
/** @returns {DevGramContext} */
export function useDevGram() {
  const context = useContext(AppContext);
  if (!context) throw new Error("DevGram context is unavailable outside the application.");
  return context;
}

/**
 * @template T
 * @param {string} key
 * @param {T} fallback
 * @returns {T}
 */
function readStored(key, fallback) {
  try {
    const stored = localStorage.getItem(key);
    return stored ? JSON.parse(stored) : fallback;
  } catch {
    return fallback;
  }
}

function RequireAuth({ children }) {
  const location = useLocation();
  return localStorage.getItem("devgram-session")
    ? children
    : <Navigate to="/login" replace state={{ from: location.pathname }} />;
}

export default function App() {
  const navigate = useNavigate();
  const [user, setUser] = useState(/** @type {typeof currentUser} */ (readStored("devgram-user", currentUser)));
  const [posts, setPosts] = useState(/** @type {DevGramPosts} */ (readStored("devgram-posts", seedPosts)));
  const [saved, setSaved] = useState(/** @type {string[]} */ (readStored("devgram-saved", ["p2"])));
  const [liked, setLiked] = useState(/** @type {string[]} */ (readStored("devgram-liked", [])));
  const [theme, setTheme] = useState(/** @type {"dark" | "light"} */ (localStorage.getItem("devgram-theme") === "light" ? "light" : "dark"));
  const [createOpen, setCreateOpen] = useState(false);
  /** @type {typeof stories[number] | null} */
  const initialStory = null;
  const [activeStory, setActiveStory] = useState(initialStory);
  const [toast, setToast] = useState("");

  useEffect(() => localStorage.setItem("devgram-posts", JSON.stringify(posts)), [posts]);
  useEffect(() => localStorage.setItem("devgram-saved", JSON.stringify(saved)), [saved]);
  useEffect(() => localStorage.setItem("devgram-liked", JSON.stringify(liked)), [liked]);
  useEffect(() => {
    localStorage.setItem("devgram-theme", theme);
    document.documentElement.dataset.theme = theme;
  }, [theme]);
  useEffect(() => {
    if (!toast) return undefined;
    const timeout = window.setTimeout(() => setToast(""), 2600);
    return () => window.clearTimeout(timeout);
  }, [toast]);

  const value = useMemo(() => ({
    user, setUser, posts, setPosts, saved, setSaved, liked, setLiked,
    theme, setTheme, setCreateOpen, setActiveStory, notify: setToast,
  }), [user, posts, saved, liked, theme]);

  const logout = () => {
    localStorage.removeItem("devgram-session");
    navigate("/login");
  };

  return (
    <AppContext.Provider value={value}>
      <Routes>
        <Route path="/login" element={<AuthPage mode="login" />} />
        <Route path="/signup" element={<AuthPage mode="signup" />} />
        <Route path="/" element={<RequireAuth><MainLayout onLogout={logout} /></RequireAuth>}>
          <Route index element={<Navigate to="/feed" replace />} />
          <Route path="feed" element={<FeedPage />} />
          <Route path="reels" element={<ReelsPage />} />
          <Route path="profile" element={<ProfilePage />} />
          <Route path="search" element={<SearchPage />} />
          <Route path="messages" element={<MessagesPage />} />
          <Route path="notifications" element={<NotificationsPage />} />
        </Route>
        <Route path="*" element={<Navigate to={localStorage.getItem("devgram-session") ? "/feed" : "/login"} replace />} />
      </Routes>
      {createOpen && <CreatePostModal onClose={() => setCreateOpen(false)} />}
      {Boolean(activeStory) && <StoryViewer story={activeStory} onClose={() => setActiveStory(null)} />}
      {toast && <div className="toast" role="status">{toast}</div>}
    </AppContext.Provider>
  );
}
