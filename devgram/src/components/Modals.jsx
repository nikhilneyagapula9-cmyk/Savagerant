import { useRef, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import {
  ArrowLeft, ArrowRight, Check, Code2, ImagePlus, LockKeyhole, Mail, X,
} from "lucide-react";
import { currentUser, seedPosts, stories } from "../data.js";
import { Avatar } from "./Layout.jsx";
import { useDevGram } from "../App.jsx";

export function AuthPage({ mode }) {
  const isSignup = mode === "signup";
  const [values, setValues] = useState({ name: "", email: "", password: "" });
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { setUser, notify } = useDevGram();
  const destination = location.state?.from || "/feed";

  const submit = (event) => {
    event.preventDefault();
    setError("");
    if (!values.email.trim()) return setError("Enter your email or username.");
    if (isSignup && !values.name.trim()) return setError("Add your name to get started.");
    if (isSignup && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email.trim())) return setError("Enter a valid email address.");
    if (values.password.length < 8) return setError("Your password needs at least 8 characters.");
    setBusy(true);
    window.setTimeout(() => {
      const username = isSignup ? values.email.split("@")[0].replace(/[^a-z0-9._]/gi, "").toLowerCase() : (values.email.includes("@") ? values.email.split("@")[0] : values.email).toLowerCase();
      /** @type {typeof currentUser} */
      const nextUser = isSignup ? { ...currentUser, name: values.name.trim(), username, avatar: values.name.trim().split(/\s+/).map(part => part[0]).join("").slice(0, 2).toUpperCase() } : { ...currentUser, username };
      localStorage.setItem("devgram-session", `mock_${Date.now()}`);
      localStorage.setItem("devgram-user", JSON.stringify(nextUser));
      setUser(nextUser);
      setBusy(false);
      navigate(destination, { replace: true });
    }, 280);
  };
  const mockOAuth = (provider) => {
    /** @type {typeof currentUser} */
    const nextUser = { ...currentUser };
    localStorage.setItem("devgram-session", `mock_${provider}_${Date.now()}`);
    localStorage.setItem("devgram-user", JSON.stringify(nextUser));
    setUser(nextUser);
    notify(`${provider} sign-in is mocked for this demo`);
    navigate(destination, { replace: true });
  };

  return (
    <main className="auth-page">
      <div className="auth-brand-panel">
        <Link to="/feed" className="brand auth-brand"><span className="brand-glyph"><Code2 size={20} /></span>devgram</Link>
        <div className="auth-art">
          <div className="auth-orbit orbit-one" /><div className="auth-orbit orbit-two" />
          <div className="auth-code-window"><div className="auth-window-bar"><span className="window-dots"><i /><i /><i /></span><span>your-next-big-thing.ts</span></div><pre><span>01</span> <b>const</b> idea = <i>"build in public"</i>;<br /><span>02</span> <b>await</b> idea.share();<br /><span>03</span> <em>→</em> <b>meet your people</b><br /><span>04</span> <i>// good things happen here</i></pre><div className="auth-window-foot"><span>●&nbsp; BUILD PASSING</span><span>main ↗</span></div></div>
          <div className="auth-floating-card"><span>✳</span><div><strong>ship it together</strong><small>one snippet at a time</small></div></div>
        </div>
        <div className="auth-tagline"><span className="eyebrow">THE INTERNET, BUT MAKE IT DEVELOPER</span><h1>What are you<br />building <i>today?</i></h1><p>A space for the code, systems, and ideas you’re proud of.</p><div className="auth-dots"><i /><i /><i /></div></div>
        <div className="auth-footer">A little corner of the internet for builders. © 2025</div>
      </div>
      <section className="auth-form-panel">
        <div className="auth-form-card">
          <span className="eyebrow">{isSignup ? "YOUR PEOPLE ARE HERE" : "GOOD TO HAVE YOU BACK"}</span>
          <h2>{isSignup ? "Create your account" : "Welcome back"}</h2>
          <p className="auth-description">{isSignup ? "Make a home for what you build." : "Pick up where your last commit left off."}</p>
          <div className="oauth-buttons">
            <button onClick={() => mockOAuth("GitHub")}><Code2 size={17} /> Continue with GitHub</button>
            <button onClick={() => mockOAuth("Google")}><span className="google-g">G</span> Continue with Google</button>
          </div>
          <div className="auth-divider"><span />or continue with email<span /></div>
          <form className="auth-form" onSubmit={submit} noValidate>
            {isSignup && <label><span>Your name</span><input autoComplete="name" value={values.name} onChange={e => setValues({ ...values, name: e.target.value })} placeholder="Alex Chen" /></label>}
            <label><span>Email or username</span><div className="auth-input-icon"><Mail size={16} /><input autoComplete="username" value={values.email} onChange={e => setValues({ ...values, email: e.target.value })} placeholder="you@example.com" /></div></label>
            <label><span>Password</span><div className="auth-input-icon"><LockKeyhole size={16} /><input type="password" autoComplete={isSignup ? "new-password" : "current-password"} value={values.password} onChange={e => setValues({ ...values, password: e.target.value })} placeholder="At least 8 characters" /></div></label>
            {error && <p className="form-error" role="alert">{error}</p>}
            {!isSignup && <button className="forgot-link" type="button" onClick={() => setError("Password reset is part of the demo sign-in flow.")}>Forgot password?</button>}
            <button className="primary-button auth-submit" disabled={busy}>{busy ? "One sec..." : isSignup ? "Create account" : "Log in"} <ArrowRight size={16} /></button>
          </form>
          <p className="auth-switch">{isSignup ? "Already have an account?" : "New around here?"} <Link to={isSignup ? "/login" : "/signup"}>{isSignup ? "Log in" : "Create an account"}</Link></p>
          {isSignup && <p className="auth-legal">By signing up, you agree to our <button type="button" onClick={() => notify("Terms are not configured in this demo.")}>Terms</button> and <button type="button" onClick={() => notify("Privacy policy is not configured in this demo.")}>Privacy Policy</button>.</p>}
        </div>
        <div className="auth-mobile-brand"><span className="brand-glyph"><Code2 size={19} /></span>devgram</div>
      </section>
    </main>
  );
}

export function CreatePostModal({ onClose }) {
  const { posts, setPosts, user, notify } = useDevGram();
  const [step, setStep] = useState(1);
  const [code, setCode] = useState("// Share something you built\nconst shipIt = () => {\n  return '✨';\n};");
  const [caption, setCaption] = useState("");
  const [language, setLanguage] = useState("typescript");
  const [tags, setTags] = useState("");
  const [collaborator, setCollaborator] = useState("");
  const [fileName, setFileName] = useState("");
  const [published, setPublished] = useState(false);
  const fileRef = useRef(null);

  const importFile = async (file) => {
    if (!file) return;
    if (file.size > 200_000) {
      notify("Choose a code file under 200 KB");
      return;
    }
    try {
      const content = await file.text();
      setCode(content);
      setFileName(file.name);
      const ext = file.name.split(".").pop().toLowerCase();
      const languageMap = { js: "javascript", ts: "typescript", py: "python", go: "go", java: "java", css: "css", json: "json" };
      if (languageMap[ext]) setLanguage(languageMap[ext]);
    } catch {
      notify("Could not read that file. Paste your snippet instead.");
    }
  };
  const publish = () => {
    /** @type {typeof seedPosts[number]} */
    const post = {
      id: `p-${Date.now()}`, author: user.username, authorName: user.name, avatar: user.avatar,
      color: user.color, time: "Just now", type: "code", language, code: code.trim() || "// Architecture share",
      caption: caption.trim() || "A little something I’ve been working on.", tags: tags.split(/[ ,#]+/).filter(Boolean).slice(0, 5),
      likes: 0, comments: [],
    };
    setPosts([post, ...posts]);
    setPublished(true);
    window.setTimeout(() => {
      notify("Your post is live!");
      onClose();
    }, 1450);
  };

  return (
    <div className="modal-backdrop" onMouseDown={e => { if (e.target === e.currentTarget) onClose(); }}>
      <section className={`create-modal${published ? " published" : ""}`} aria-labelledby="create-title">
        <header className="modal-header"><button className="icon-button" onClick={step > 1 && !published ? () => setStep(step - 1) : onClose} aria-label={step > 1 ? "Back" : "Close"}>{step > 1 && !published ? <ArrowLeft size={19} /> : <X size={19} />}</button><strong id="create-title">{published ? "Published" : "Create a post"}</strong>{!published && <button className="text-action" onClick={() => step < 3 ? setStep(step + 1) : publish()}>{step === 1 ? "Next" : step === 2 ? "Review" : "Share"}</button>}</header>
        {!published && <div className="step-indicator"><i className={step >= 1 ? "on" : ""} /><i className={step >= 2 ? "on" : ""} /><i className={step >= 3 ? "on" : ""} /><span>STEP {step} OF 3</span></div>}
        {published ? <div className="publish-success"><div className="success-orbit"><span><Check size={27} /></span></div><h2>Out in the world ✳</h2><p>Your build is now part of the conversation.</p><div className="publish-confetti">✳ &nbsp; ✦ &nbsp; ✧ &nbsp; ✳</div></div> :
          step === 1 ? <div className="create-step">
            <div className="step-intro"><span className="eyebrow">SHARE WHAT YOU’RE BUILDING</span><h2>Start with a snippet</h2><p>Paste code, or bring in a file. Your work stays yours.</p></div>
            <div className="create-editor-wrap"><div className="create-editor-bar"><span className="window-dots"><i /><i /><i /></span><span>{fileName || "untitled.ts"}</span><button className="text-action" onClick={() => fileRef.current?.click()}><ImagePlus size={14} /> Import file</button><input ref={fileRef} type="file" accept=".js,.jsx,.ts,.tsx,.py,.go,.java,.css,.json,.sql,.yaml,.yml,.txt" hidden onChange={e => importFile(e.target.files?.[0])} /></div><textarea className="create-code-editor" value={code} onChange={e => setCode(e.target.value)} spellCheck="false" aria-label="Code snippet" /></div>
            <div className="upload-hint"><Code2 size={15} /><span>Code files up to 200 KB · or paste your snippet above</span></div>
            <button className="primary-button step-next" onClick={() => setStep(2)}>Continue <ArrowRight size={15} /></button>
          </div> : step === 2 ?
            <div className="create-step step-details">
              <div className="step-intro"><span className="eyebrow">GIVE YOUR POST A LITTLE CONTEXT</span><h2>The details</h2><p>A good caption brings the right people into the conversation.</p></div>
              <div className="preview-code"><div className="preview-avatar"><Avatar initials={user.avatar} color={user.color} /><strong>{user.username}</strong><span>{language}</span></div><pre>{code.slice(0, 220)}{code.length > 220 ? "..." : ""}</pre></div>
              <label className="field-label">Caption<textarea value={caption} onChange={e => setCaption(e.target.value)} maxLength={500} placeholder="What did you build? What did you learn?" rows={3} /><small>{caption.length}/500</small></label>
              <div className="create-field-row"><label className="field-label">Language<select value={language} onChange={e => setLanguage(e.target.value)}>{["typescript", "javascript", "python", "go", "java", "css", "json", "sql", "other"].map(item => <option key={item}>{item}</option>)}</select></label><label className="field-label">Tech tags<input value={tags} onChange={e => setTags(e.target.value)} placeholder="kubernetes, api, devops" /><small>Separate with commas</small></label></div>
              <label className="field-label collaborator-field">Tag a collaborator<input value={collaborator} onChange={e => setCollaborator(e.target.value)} placeholder="@username (optional)" /><small>{collaborator && `Collaborator ${collaborator} will be tagged when you share.`}</small></label>
              <div className="share-note"><Check size={15} /> Your post will be visible to your developer circle.</div>
              <button className="primary-button step-next" onClick={() => setStep(3)}>Review post <ArrowRight size={15} /></button>
            </div> : null}
        {!published && step === 3 && <div className="create-step review-step">
          <div className="step-intro"><span className="eyebrow">READY FOR THE FEED?</span><h2>One last look</h2><p>Here’s how your post will appear to your circle.</p></div>
          <div className="review-card">
            <div className="preview-avatar"><Avatar initials={user.avatar} color={user.color} /><strong>{user.username}</strong><span>just now</span></div>
            <div className="review-snippet"><span>{language}</span><pre>{code.slice(0, 290)}{code.length > 290 ? "\n..." : ""}</pre></div>
            <p>{caption.trim() || "A little something I’ve been working on."}</p>
            {tags && <div className="post-tags">{tags.split(/[ ,#]+/).filter(Boolean).slice(0, 5).map((tag, i) => <span key={`${tag}-${i}`}>#{tag}</span>)}</div>}
            {collaborator && <small className="review-collaborator">Collaborating with {collaborator}</small>}
          </div>
          <div className="share-note"><Check size={15} /> You’re sharing with your developer circle.</div>
          <button className="primary-button step-next" onClick={publish}>Share post <ArrowRight size={15} /></button>
        </div>}
      </section>
    </div>
  );
}

export function StoryViewer({ story, onClose }) {
  const currentIndex = stories.findIndex(item => item.username === story.username);
  const nextStory = stories[(currentIndex + 1 + stories.length) % stories.length];
  const [current, setCurrent] = useState(story);
  return <div className="story-viewer" onClick={onClose}>
    <button className="story-close" aria-label="Close story"><X size={22} /></button>
    <div className="story-panel" onClick={e => e.stopPropagation()}>
      <div className="story-progress">{[0, 1, 2].map(i => <i key={i} className={i === 0 ? "done" : ""} />)}</div>
      <header><Avatar initials={current.avatar} color={current.color} size="sm" /><div><strong>{current.username}</strong><span>{current.time} ago</span></div><span className="story-label">{current.kind}</span></header>
      <div className={`story-content story-${current.color}`}><span className="story-content-icon"><Code2 size={28} /></span><span className="eyebrow">BUILD UPDATE</span><h2>{current.kind}</h2><p>Another day, another thing shipped. Sharing the small wins along the way.</p><div className="story-terminal"><span>✓ tests passing</span><span>✓ build deployed</span><span>↗ live in production</span></div></div>
      <footer><button className="story-reply" onClick={() => onClose()}>Reply to {current.name}...</button><button className="story-next" onClick={() => setCurrent(nextStory)} aria-label="Next story"><ArrowRight size={18} /></button></footer>
    </div>
  </div>;
}
