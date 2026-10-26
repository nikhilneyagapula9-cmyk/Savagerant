import { useMemo, useState } from "react";
import {
  ArrowDownToLine, ArrowUpRight, Code2, Grid3X3, Heart, MessageCircle, Play, Search,
  Settings, Sparkles, Tag, X,
} from "lucide-react";
import { currentUser, reels } from "../data.js";
import { Avatar } from "../components/Layout.jsx";
import { FeedPage as Feed, PostCard } from "../components/Feed.jsx";
import { useDevGram } from "../App.jsx";

export function FeedPage() {
  return <Feed />;
}

export function ReelsPage() {
  const { notify } = useDevGram();
  const [liked, setLiked] = useState([]);
  const [comment, setComment] = useState(null);
  return (
    <div className="reels-page">
      <div className="page-heading"><div><span className="eyebrow">SHORT FORM, BIG IDEAS</span><h1>Code Reels</h1></div><span className="reels-date">Fresh ideas from your circle</span></div>
      <div className="reels-list">
        {reels.map(reel => (
          <article className="reel-card" key={reel.id}>
            <div className={`reel-visual reel-${reel.theme}`}>
              <div className="reel-topline"><span className="reel-live"><i /> BUILD IN 60 SEC</span><button aria-label="More reel options" onClick={() => notify("Reel options opened")}><span>•••</span></button></div>
              <div className="reel-art-glow" />
              <div className="reel-terminal">
                <div className="reel-terminal-top"><span className="window-dots"><i /><i /><i /></span><span>~/devgram/demo</span><Code2 size={15} /></div>
                <div className="reel-terminal-body"><strong>{reel.label}</strong>{reel.lines.map((line, i) => <p key={i}><span>{String(i + 1).padStart(2, "0")}</span>{line}</p>)}</div>
              </div>
              {reel.theme === "architecture" && <div className="reel-orbit">↗<span>global</span></div>}
              <div className="reel-play"><Play size={22} fill="currentColor" /></div>
              <div className="reel-progress"><i /></div>
            </div>
            <div className="reel-side-actions">
              <button onClick={() => setLiked(liked.includes(reel.id) ? liked.filter(id => id !== reel.id) : [...liked, reel.id])} aria-label="Like reel"><Heart size={22} fill={liked.includes(reel.id) ? "currentColor" : "none"} className={liked.includes(reel.id) ? "liked-icon" : ""} /><span>{(reel.likes + (liked.includes(reel.id) ? 1 : 0)).toLocaleString()}</span></button>
              <button onClick={() => setComment(reel)} aria-label="Comment on reel"><MessageCircle size={22} /><span>{reel.comments}</span></button>
              <button onClick={() => notify("Reel link copied")} aria-label="Share reel"><ArrowUpRight size={22} /><span>Share</span></button>
              <button onClick={() => notify("Reel saved")} aria-label="Save reel"><ArrowDownToLine size={21} /><span>Save</span></button>
            </div>
            <div className="reel-details">
              <div className="reel-author"><Avatar initials={reel.avatar} color={reel.color} /><strong>{reel.author}</strong><button className="follow-outline" onClick={(e) => { e.currentTarget.textContent = "Following"; }}>Follow</button></div>
              <p>{reel.caption}</p><span className="reel-audio">♫ &nbsp;{reel.audio}</span>
            </div>
          </article>
        ))}
      </div>
      {comment && <div className="modal-backdrop" onClick={() => setComment(null)}><section className="comment-modal" onClick={e => e.stopPropagation()}><button className="modal-close" onClick={() => setComment(null)}><X size={19} /></button><h2>Comments</h2><p><strong>{comment.author}</strong> &nbsp;This is so useful, thanks for sharing!</p><p><strong>buildwithjo</strong> &nbsp;Can you share the repo?</p><form onSubmit={e => { e.preventDefault(); const value = new FormData(e.currentTarget).get("comment")?.toString().trim(); if (value) { notify("Comment added"); setComment(null); } }}><input name="comment" placeholder="Add a comment..." /><button className="text-action">Post</button></form></section></div>}
    </div>
  );
}

function GridTile({ post, index }) {
  const { notify } = useDevGram();
  return (
    <button className={`profile-tile tile-${post.type} tile-index-${index % 3}`} onClick={() => notify(`Opened ${post.type} post`)}>
      {post.type === "code" ? <><span className="tile-file">⌘ &nbsp; snippet.ts</span><code><b>const</b> deploy = <em>async</em> () =&gt; {"{"}<br />&nbsp; await release();<br />&nbsp; console.log(<i>"shipped ✓"</i>);<br />{"};"}</code><span className="tile-lang">TypeScript</span></> :
        post.type === "diagram" ? <><div className="tile-diagram"><i /><span>queue</span><b>→</b><span>workers</span><b>→</b><i /></div><strong>EVENT FLOW</strong></> :
          <><span className="tile-status">● &nbsp; DEPLOY SUCCESSFUL</span><strong>v2.8.4</strong><small>all checks passing</small><span className="tile-check">✓</span></>}
      <span className="tile-overlay"><span><Heart size={19} fill="white" />{post.likes}</span><span><MessageCircle size={19} fill="white" />{post.comments.length}</span></span>
    </button>
  );
}

export function ProfilePage() {
  const { posts, saved, user, setUser, notify } = useDevGram();
  const [tab, setTab] = useState("posts");
  const tabPosts = useMemo(() => {
    if (tab === "saved") return posts.filter(post => saved.includes(post.id));
    if (tab === "tagged") return posts.filter(post => post.id === "p2" || post.id === "p3");
    return posts;
  }, [posts, saved, tab]);
  const editProfile = () => {
    const name = window.prompt("Your display name", user.name);
    if (name?.trim()) {
      /** @type {typeof currentUser} */
      const updatedUser = { ...user, name: name.trim() };
      setUser(updatedUser);
      localStorage.setItem("devgram-user", JSON.stringify(updatedUser));
      notify("Profile updated");
    }
  };
  return (
    <div className="profile-page">
      <section className="profile-header">
        <div className="profile-avatar"><Avatar initials={user.avatar} color={user.color} size="profile" /></div>
        <div className="profile-details">
          <div className="profile-title-row"><h1>{user.username}</h1><button className="secondary-button" onClick={editProfile}>Edit profile</button><button className="icon-button profile-settings" aria-label="Settings" onClick={() => notify("Settings are ready to configure")}><Settings size={20} /></button></div>
          <div className="profile-stats"><span><strong>{posts.length + 12}</strong> posts</span><span><strong>2,481</strong> followers</span><span><strong>684</strong> following</span></div>
          <div className="profile-bio"><strong>{user.name}</strong><p>{user.bio}</p><span className="profile-role">{user.role}</span><div className="stack-badges">{user.stack.map(item => <span key={item}>#{item.replaceAll(" ", "")}</span>)}</div><a href="https://github.com" target="_blank" rel="noreferrer"><Code2 size={14} /> github.com/{user.username}</a></div>
        </div>
      </section>
      <div className="profile-tabbar" role="tablist">
        {[["posts", Grid3X3, "Posts"], ["saved", BookmarkIcon, "Saved"], ["tagged", Tag, "Tagged"]].map(([id, Icon, label]) => <button key={id} role="tab" aria-selected={tab === id} className={tab === id ? "selected" : ""} onClick={() => setTab(id)}><Icon size={13} />{label}</button>)}
      </div>
      {tabPosts.length ? <div className="profile-grid">{tabPosts.map((post, index) => <GridTile key={post.id} post={post} index={index} />)}</div> :
        <div className="empty-state"><BookmarkIcon size={30} /><h2>Save ideas for later</h2><p>Posts you bookmark will show up right here.</p><button onClick={() => setTab("posts")}>Explore posts</button></div>}
    </div>
  );
}

function BookmarkIcon(props) {
  return <svg viewBox="0 0 24 24" width={props.size || 16} height={props.size || 16} fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M6 4.75A1.75 1.75 0 0 1 7.75 3h8.5A1.75 1.75 0 0 1 18 4.75V21l-6-3.75L6 21z" /></svg>;
}

export function SearchPage() {
  const { posts } = useDevGram();
  const [query, setQuery] = useState("");
  const results = posts.filter(post => `${post.author} ${post.caption} ${post.tags.join(" ")}`.toLowerCase().includes(query.toLowerCase()));
  return (
    <div className="search-page">
      <div className="page-heading"><div><span className="eyebrow">FIND YOUR NEXT IDEA</span><h1>Explore</h1></div><Sparkles size={22} className="heading-spark" /></div>
      <label className="search-input-wrap"><Search size={19} /><input autoFocus placeholder="Search code, people, technologies..." value={query} onChange={e => setQuery(e.target.value)} /><kbd>⌘ K</kbd></label>
      {!query && <><div className="trending-title"><div><span className="eyebrow">WHAT BUILDERS ARE TALKING ABOUT</span><h2>Trending in dev</h2></div><span className="trend-period">This week</span></div><div className="topic-grid">{[["01", "Kubernetes", "2.4k posts", "☸"], ["02", "System design", "1.8k posts", "◈"], ["03", "TypeScript", "1.2k posts", "⌘"], ["04", "AI tooling", "984 posts", "✳"]].map(([number, name, count, symbol]) => <button key={name} onClick={() => setQuery(name)}><span>{number}</span><strong>{name}</strong><i>{symbol}</i><small>{count}</small></button>)}</div></>}
      <div className="search-results">{query && <div className="results-label">{results.length} posts matching “{query}”</div>}{results.map(post => <PostCard key={post.id} post={post} />)}{query && !results.length && <div className="empty-state"><Search size={30} /><h2>No matches yet</h2><p>Try a technology like Go or Kubernetes.</p></div>}</div>
    </div>
  );
}

export function MessagesPage() {
  const { notify } = useDevGram();
  const [active, setActive] = useState(0);
  const [text, setText] = useState("");
  const conversations = [
    { user: "maya.builds", name: "Maya Patel", avatar: "MP", color: "coral", message: "That retry helper is so clean!", time: "2m", online: true },
    { user: "devon.codes", name: "Devon Kim", avatar: "DK", color: "blue", message: "Sent you the architecture notes", time: "1h", online: true },
    { user: "sarahinfra", name: "Sarah Kim", avatar: "SK", color: "mint", message: "Thanks for the feedback 🙌", time: "3h", online: false },
  ];
  return (
    <div className="messages-page">
      <div className="page-heading"><div><span className="eyebrow">YOUR DEV CIRCLE</span><h1>Messages</h1></div><button className="secondary-button" onClick={() => notify("New message composer opened")}>New message <ArrowUpRight size={14} /></button></div>
      <div className="inbox-shell">
        <aside className="inbox-list"><div className="inbox-title"><div><strong>alexchen</strong><span>Inbox</span></div><button aria-label="New message"><ArrowUpRight size={18} /></button></div><label className="inbox-search"><Search size={15} /><input placeholder="Search messages" /></label>
          {conversations.map((item, index) => <button key={item.user} onClick={() => setActive(index)} className={`conversation${active === index ? " active" : ""}`}><span className="conversation-avatar"><Avatar initials={item.avatar} color={item.color} /><i className={item.online ? "online" : ""} /></span><span className="conversation-copy"><strong>{item.name}</strong><small>{item.message}</small></span><small className="conversation-time">{item.time}</small></button>)}
        </aside>
        <section className="chat-panel"><div className="chat-header"><Avatar initials={conversations[active].avatar} color={conversations[active].color} /><div><strong>{conversations[active].name}</strong><span>{conversations[active].online ? "● Active now" : "Developer"}</span></div></div><div className="chat-body"><span className="chat-day">TODAY</span><div className="chat-msg incoming">{conversations[active].message}<small>{conversations[active].time}</small></div><div className="chat-msg outgoing">Glad you liked it! Let me know if you try it out. <small>Just now · Seen</small></div></div><form className="chat-composer" onSubmit={e => { e.preventDefault(); if (text.trim()) { notify("Message sent"); setText(""); } }}><input value={text} onChange={e => setText(e.target.value)} placeholder="Message..." /><button type="submit">Send</button></form></section>
      </div>
    </div>
  );
}

export function NotificationsPage() {
  const { notify } = useDevGram();
  const activity = [
    { type: "like", avatar: "MP", color: "coral", user: "maya.builds", text: "liked your post about retry patterns", time: "12m", post: "p1" },
    { type: "follow", avatar: "DK", color: "blue", user: "devon.codes", text: "started following you", time: "1h" },
    { type: "comment", avatar: "SK", color: "mint", user: "sarahinfra", text: "commented: “This is a great breakdown!”", time: "3h", post: "p2" },
    { type: "mention", avatar: "OR", color: "gold", user: "omar.dev", text: "mentioned you in an architecture review", time: "Yesterday" },
    { type: "like", avatar: "LT", color: "pink", user: "linh.codes", text: "and 14 others liked your post", time: "Yesterday", post: "p3" },
  ];
  return (
    <div className="notifications-page">
      <div className="page-heading"><div><span className="eyebrow">ALL CAUGHT UP? ALMOST.</span><h1>Notifications</h1></div><button className="subtle-action" onClick={() => notify("All notifications marked as read")}>Mark all read</button></div>
      <h3 className="notification-group-title">This week</h3><div className="activity-list">{activity.map((item, i) => <div className="activity-row" key={`${item.user}-${i}`}><Avatar initials={item.avatar} color={item.color} /><p><strong>{item.user}</strong> {item.text} <span>{item.time}</span></p>{item.post ? <div className={`activity-post-thumb thumb-${i}`}><Code2 size={18} /></div> : <button className="follow-outline" onClick={e => { e.currentTarget.textContent = "Following"; }}>Follow</button>}</div>)}</div>
    </div>
  );
}
