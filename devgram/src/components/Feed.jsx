import { useState } from "react";
import {
  Bookmark, Check, Code2, Copy, GitFork, Heart, MessageCircle, MoreHorizontal, Send,
} from "lucide-react";
import { stories } from "../data.js";
import { Avatar } from "./Layout.jsx";
import { useDevGram } from "../App.jsx";

function StoriesBar() {
  const { setActiveStory, user } = useDevGram();
  return (
    <section className="stories-card" aria-label="Infrastructure updates">
      <div className="stories-heading"><span>Updates from your circle</span><span className="live-indicator"><i /> LIVE</span></div>
      <div className="stories-list">
        <button className="story-item own-story" onClick={() => setActiveStory({ username: user.username, name: "Your update", avatar: user.avatar, color: user.color, kind: "Share a build update", time: "now" })}>
          <span className="story-avatar-wrap"><Avatar initials={user.avatar} color={user.color} size="story" /><span className="story-add">+</span></span>
          <span className="story-name">Your update</span>
        </button>
        {stories.map((story) => (
          <button className="story-item" key={story.username} onClick={() => setActiveStory(story)}>
            <span className="story-avatar-wrap unread"><Avatar initials={story.avatar} color={story.color} size="story" /></span>
            <span className="story-name">{story.name}</span>
          </button>
        ))}
      </div>
    </section>
  );
}

function DiagramCard() {
  return (
    <div className="diagram-card">
      <div className="diagram-label"><span className="diagram-dot" /> EVENT PIPELINE <span>v3.2</span></div>
      <div className="diagram-flow">
        <div className="diagram-node node-source"><Code2 size={17} /><strong>Producers</strong><small>REST · gRPC</small></div>
        <span className="flow-arrow">→</span>
        <div className="diagram-node node-broker"><strong>Kafka</strong><small>3 partitions</small></div>
        <span className="flow-arrow">→</span>
        <div className="diagram-node node-worker"><strong>Workers</strong><small>autoscaled</small></div>
      </div>
      <div className="diagram-bottom"><span>◉ Dead-letter queue</span><span>↻ Replay enabled</span><span>p99 42ms</span></div>
    </div>
  );
}

function PostContent({ post }) {
  const [copied, setCopied] = useState(false);
  const { notify } = useDevGram();
  if (post.type === "diagram") return <DiagramCard />;
  if (post.type === "deploy") {
    return (
      <div className="deploy-card">
        <div className="deploy-head"><div className="deploy-symbol"><Check size={18} /></div><div><span>DEPLOYMENT COMPLETE</span><strong>{post.deploy.service}</strong></div><span className="deploy-live">ALL SYSTEMS GO</span></div>
        <div className="deploy-meta"><span><small>VERSION</small><strong>{post.deploy.version}</strong></span><span><small>REGION</small><strong>{post.deploy.region}</strong></span><span><small>STATUS</small><strong className="green-text">● Healthy</strong></span></div>
        <div className="deploy-checks">{post.deploy.checks.map((check) => <span key={check}><Check size={13} />{check}</span>)}</div>
      </div>
    );
  }
  return (
    <div className="code-card">
      <div className="code-toolbar"><div className="code-title"><span className="window-dots"><i /><i /><i /></span><span>retry.ts</span></div><span className="language-pill">{post.language}</span>
        <button className="copy-button" onClick={() => { navigator.clipboard?.writeText(post.code); setCopied(true); notify("Snippet copied to clipboard"); window.setTimeout(() => setCopied(false), 1600); }}>{copied ? <Check size={14} /> : <Copy size={14} />}{copied ? "Copied" : "Copy"}</button>
      </div>
      <pre className="code-body"><code>{post.code.split("\n").map((line, index) => <span className="code-line" key={`${post.id}-${index}`}><i>{String(index + 1).padStart(2, "0")}</i><span>{line.split(/(export|async|function|const|for|try|catch|return|throw|await|new|Error|Promise|Math|random|sleep|backoff|attempts)/g).map((part, tokenIndex) => /^(export|async|function|const|for|try|catch|return|throw|await|new)$/.test(part) ? <b key={tokenIndex}>{part}</b> : /^(Error|Promise|Math|random)$/.test(part) ? <em key={tokenIndex}>{part}</em> : part)}</span></span>)}</code></pre>
    </div>
  );
}

export function PostCard({ post }) {
  const { liked, setLiked, saved, setSaved, setPosts, user, notify } = useDevGram();
  const [comment, setComment] = useState("");
  const isLiked = liked.includes(post.id);
  const isSaved = saved.includes(post.id);
  const count = post.likes + (isLiked ? 1 : 0);
  const toggleLike = () => setLiked(isLiked ? liked.filter((id) => id !== post.id) : [...liked, post.id]);
  const toggleSave = () => setSaved(isSaved ? saved.filter((id) => id !== post.id) : [...saved, post.id]);
  const addComment = (event) => {
    event.preventDefault();
    if (!comment.trim()) return;
    setPosts(posts => posts.map(item => item.id === post.id ? { ...item, comments: [...item.comments, { author: user.username, text: comment.trim() }] } : item));
    setComment("");
    notify("Comment added");
  };
  const share = () => {
    navigator.clipboard?.writeText(`${window.location.origin}/feed#${post.id}`);
    notify("Post link copied");
  };

  return (
    <article className="post-card" id={post.id}>
      <header className="post-header">
        <Avatar initials={post.avatar} color={post.color} />
        <div className="post-author"><strong>{post.author}</strong><span>{post.time} <i>·</i> <span className="post-context">{post.type === "code" ? "Code snippet" : post.type === "diagram" ? "Architecture" : "Deploy log"}</span></span></div>
        <button className="icon-button post-more" aria-label="More options" onClick={() => notify("Post options · report or copy link")}><MoreHorizontal size={21} /></button>
      </header>
      <PostContent post={post} />
      <div className="post-actions">
        <div className="post-action-group">
          <button className={`icon-button action-button${isLiked ? " liked" : ""}`} onClick={toggleLike} aria-label={isLiked ? "Unlike post" : "Like post"}><Heart size={22} fill={isLiked ? "currentColor" : "none"} /></button>
          <button className="icon-button action-button" onClick={() => document.getElementById(`comment-${post.id}`)?.focus()} aria-label="Comment"><MessageCircle size={21} /></button>
          <button className="icon-button action-button" onClick={() => { notify("Fork created in your snippets"); }} aria-label="Fork snippet"><GitFork size={21} /></button>
          <button className="icon-button action-button share-action" onClick={share} aria-label="Share"><Send size={20} /></button>
        </div>
        <button className={`icon-button action-button${isSaved ? " bookmarked" : ""}`} onClick={toggleSave} aria-label={isSaved ? "Remove bookmark" : "Bookmark"}><Bookmark size={21} fill={isSaved ? "currentColor" : "none"} /></button>
      </div>
      <div className="post-engagement">
        <strong>{count.toLocaleString()} likes</strong>
        <p className="post-caption"><strong>{post.author}</strong> {post.caption}</p>
        <div className="post-tags">{post.tags.map((tag, index) => <span key={`${tag}-${index}`}>#{tag}</span>)}</div>
        {post.comments.length > 0 && <div className="comments-preview">
          {post.comments.slice(-2).map((item, index) => <p key={`${item.author}-${index}`}><strong>{item.author}</strong> {item.text}</p>)}
          {post.comments.length > 2 && <button className="view-comments" onClick={() => notify(`${post.comments.length} comments on this post`)}>View all {post.comments.length} comments</button>}
        </div>}
        <form className="comment-form" onSubmit={addComment}>
          <input id={`comment-${post.id}`} aria-label="Add a comment" placeholder="Add a comment..." value={comment} onChange={e => setComment(e.target.value)} />
          {comment.trim() && <button className="text-action" type="submit">Post</button>}
        </form>
      </div>
    </article>
  );
}

export function FeedPage() {
  const { posts } = useDevGram();
  return (
    <div className="feed-page">
      <StoriesBar />
      <div className="feed-intro"><div><span className="eyebrow">YOUR DAILY BUILD</span><h1>Good morning, Alex <span>✳</span></h1><p>Here’s what your developer circle is shipping.</p></div><button className="feed-filter">For you <span>⌄</span></button></div>
      <div className="feed-list">{posts.map(post => <PostCard key={post.id} post={post} />)}</div>
      <div className="feed-end"><span>✳</span><strong>You’re all caught up</strong><p>That’s everything from your circle for now.</p><button onClick={() => window.scrollTo({ top: 0, behavior: "smooth" })}>Back to top ↑</button></div>
    </div>
  );
}
