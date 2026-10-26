import { useState } from "react";
import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import {
  Bell, ChevronDown, Code2, Compass, Home, LogOut, Menu, MessageCircle,
  Moon, PlusSquare, Search, Settings, Sun, Video, X,
} from "lucide-react";
import { currentUser } from "../data.js";
import { useDevGram } from "../App.jsx";

const links = [
  { to: "/feed", label: "Home", icon: Home },
  { to: "/search", label: "Explore", icon: Compass },
  { to: "/reels", label: "Reels", icon: Video },
  { to: "/messages", label: "Messages", icon: MessageCircle, badge: "2" },
  { to: "/notifications", label: "Notifications", icon: Bell, badge: "3" },
];

function Avatar({ initials, color = "lavender", size = "md", ring = false }) {
  return <span className={`avatar avatar-${color} avatar-${size}${ring ? " avatar-ring" : ""}`}>{initials}</span>;
}

export { Avatar };

export function MainLayout({ onLogout }) {
  const { user, setCreateOpen, theme, setTheme } = useDevGram();
  const [moreOpen, setMoreOpen] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const pageTitle = location.pathname === "/feed" ? "Home" : location.pathname.slice(1);

  return (
    <div className="app-shell">
      <aside className={`left-sidebar${mobileOpen ? " mobile-open" : ""}`}>
        <div className="brand-row">
          <NavLink to="/feed" className="brand"><span className="brand-glyph"><Code2 size={19} /></span>devgram</NavLink>
          <button className="icon-button mobile-close" onClick={() => setMobileOpen(false)} aria-label="Close menu"><X size={20} /></button>
        </div>
        <nav className="primary-nav" aria-label="Main navigation">
          {links.map(({ to, label, icon: Icon, badge }) => (
            <NavLink key={to} to={to} onClick={() => setMobileOpen(false)} className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}>
              <span className="nav-icon"><Icon size={21} strokeWidth={1.8} /></span>
              <span>{label}</span>{badge && <span className="nav-badge">{badge}</span>}
            </NavLink>
          ))}
          <button className="nav-link" onClick={() => { setCreateOpen(true); setMobileOpen(false); }}>
            <span className="nav-icon"><PlusSquare size={21} strokeWidth={1.8} /></span><span>Create</span>
          </button>
          <NavLink to="/profile" onClick={() => setMobileOpen(false)} className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}>
            <Avatar initials={user.avatar} color={user.color} size="nav" /><span>Profile</span>
          </NavLink>
        </nav>
        <div className="sidebar-bottom">
          {moreOpen && <div className="more-menu">
            <button onClick={() => { setTheme(theme === "dark" ? "light" : "dark"); setMoreOpen(false); }}>
              {theme === "dark" ? <Sun size={17} /> : <Moon size={17} />} Switch appearance
            </button>
            <button onClick={() => { navigate("/profile"); setMoreOpen(false); }}><Settings size={17} /> Settings</button>
            <button onClick={onLogout}><LogOut size={17} /> Log out</button>
          </div>}
          <button className={`nav-link more-toggle${moreOpen ? " active" : ""}`} onClick={() => setMoreOpen(!moreOpen)}>
            <span className="nav-icon"><Menu size={21} strokeWidth={1.8} /></span><span>More</span><ChevronDown className="more-chevron" size={15} />
          </button>
        </div>
      </aside>

      <header className="mobile-header">
        <button className="icon-button" onClick={() => setMobileOpen(true)} aria-label="Open menu"><Menu size={21} /></button>
        <NavLink to="/feed" className="brand"><span className="brand-glyph"><Code2 size={17} /></span>devgram</NavLink>
        <button className="icon-button" onClick={() => setCreateOpen(true)} aria-label="Create post"><PlusSquare size={21} /></button>
      </header>

      <main className="main-area">
        <div className="mobile-page-title">{pageTitle}</div>
        <Outlet />
      </main>

      <aside className="right-sidebar">
        <div className="switch-profile">
          <NavLink to="/profile" className="user-line">
            <Avatar initials={user.avatar} color={user.color} size="lg" />
            <span className="user-line-copy"><strong>{user.username}</strong><span>{user.name}</span></span>
          </NavLink>
          <button className="text-action" onClick={onLogout}>Switch</button>
        </div>
        <section className="suggestions">
          <div className="section-head"><span>Suggested for you</span><button className="subtle-action">See all</button></div>
          {[
            ["tom.codes", "Tom Rivera", "TR", "blue"],
            ["nina.platform", "Nina Alvarez", "NA", "mint"],
            ["buildwithjo", "Jo Williams", "JW", "gold"],
            ["marco.devops", "Marco Silva", "MS", "peach"],
          ].map(([handle, name, initials, color]) => (
            <div className="suggestion-row" key={handle}>
              <Avatar initials={initials} color={color} />
              <div className="suggestion-copy"><strong>{handle}</strong><span>{name} · suggested</span></div>
              <button className="text-action" onClick={(e) => { e.currentTarget.textContent = "Following"; }}>Follow</button>
            </div>
          ))}
        </section>
        <div className="sidebar-note">
          <p>About · Help · Press · API · Jobs · Privacy · Terms · Locations</p>
          <span>© 2025 DEVGRAM FROM THE BUILDERS</span>
        </div>
        <div className="theme-shortcut">
          <button className="icon-button" onClick={() => setTheme(theme === "dark" ? "light" : "dark")} aria-label="Toggle theme">
            {theme === "dark" ? <Sun size={16} /> : <Moon size={16} />}
          </button><span>{theme === "dark" ? "Dark mode" : "Light mode"}</span>
        </div>
      </aside>
      {mobileOpen && <button className="mobile-scrim" onClick={() => setMobileOpen(false)} aria-label="Close navigation" />}
      <nav className="mobile-bottom-nav" aria-label="Mobile navigation">
        <NavLink to="/feed" aria-label="Home" className={({ isActive }) => isActive ? "active" : ""}><Home size={22} /></NavLink>
        <NavLink to="/search" aria-label="Explore" className={({ isActive }) => isActive ? "active" : ""}><Search size={22} /></NavLink>
        <button aria-label="Create post" onClick={() => setCreateOpen(true)}><PlusSquare size={22} /></button>
        <NavLink to="/reels" aria-label="Reels" className={({ isActive }) => isActive ? "active" : ""}><Video size={22} /></NavLink>
        <NavLink to="/profile" aria-label="Profile" className={({ isActive }) => isActive ? "active" : ""}><Avatar initials={currentUser.avatar} size="nav" /></NavLink>
      </nav>
    </div>
  );
}
