export const currentUser = {
  username: "alexchen",
  name: "Alex Chen",
  role: "Staff engineer · building things that scale",
  avatar: "AC",
  color: "lavender",
  bio: "Making distributed systems feel a little less distributed. Currently building @northstar.",
  stack: ["Kubernetes", "Go", "PostgreSQL", "Open Source"],
};

export const stories = [
  { username: "maya.builds", name: "Maya", avatar: "MP", color: "coral", kind: "Deploy log", time: "12m" },
  { username: "devon.codes", name: "Devon", avatar: "DK", color: "blue", kind: "New release", time: "28m" },
  { username: "sarahinfra", name: "Sarah", avatar: "SK", color: "mint", kind: "Architecture", time: "1h" },
  { username: "omar.dev", name: "Omar", avatar: "OR", color: "gold", kind: "Build update", time: "2h" },
  { username: "linh.codes", name: "Linh", avatar: "LT", color: "pink", kind: "Ship it", time: "3h" },
  { username: "jules.systems", name: "Jules", avatar: "JW", color: "peach", kind: "Incident notes", time: "4h" },
];

export const seedPosts = [
  {
    id: "p1",
    author: "maya.builds",
    authorName: "Maya Patel",
    avatar: "MP",
    color: "coral",
    time: "2 hours ago",
    type: "code",
    language: "typescript",
    caption: "A tiny retry helper that finally made our queue workers boring (in the best way). Exponential backoff + jitter, zero dependencies. ✨",
    tags: ["typescript", "backend", "reliability"],
    likes: 128,
    comments: [{ author: "devon.codes", text: "The jitter is such an underrated detail." }, { author: "sarahinfra", text: "Stealing this for our workers 👀" }],
    code: `export async function retry<T>(\n  task: () => Promise<T>,\n  attempts = 4,\n): Promise<T> {\n  for (let i = 0; i < attempts; i++) {\n    try {\n      return await task();\n    } catch (error) {\n      if (i === attempts - 1) throw error;\n      const backoff = 2 ** i * 250;\n      await sleep(backoff + Math.random() * 100);\n    }\n  }\n  throw new Error("Unreachable");\n}`,
  },
  {
    id: "p2",
    author: "devon.codes",
    authorName: "Devon Kim",
    avatar: "DK",
    color: "blue",
    time: "5 hours ago",
    type: "diagram",
    caption: "Our event pipeline after three iterations and one very educational 3am incident. The dead-letter lane was the missing piece.",
    tags: ["architecture", "kafka", "systemsdesign"],
    likes: 86,
    comments: [{ author: "alexchen", text: "Love the explicit replay path here." }],
    diagram: true,
  },
  {
    id: "p3",
    author: "sarahinfra",
    authorName: "Sarah Kim",
    avatar: "SK",
    color: "mint",
    time: "Yesterday",
    type: "deploy",
    caption: "Friday deploy, zero downtime, and the dashboard is all green. We moved our API to a rolling canary with a 5% traffic gate. Here’s the rollout checklist we now swear by.",
    tags: ["kubernetes", "devops", "shipping"],
    likes: 214,
    comments: [{ author: "maya.builds", text: "That 5% gate saved us last week!" }],
    deploy: { service: "api-gateway", version: "v2.8.4", region: "us-east-1", checks: ["Health checks passing", "Latency within SLO", "Error rate stable"] },
  },
];

export const reels = [
  { id: "r1", author: "linh.codes", name: "Linh Tran", avatar: "LT", color: "pink", caption: "A 30 second tour of the new query planner 👇", audio: "Original audio · linh.codes", likes: 2480, comments: 84, theme: "terminal", label: "QUERY PLAN", lines: ["→ Index Scan using users_email_idx", "  cost=0.42..8.44 rows=1", "  actual time=0.019..0.021 ms", "✓ 42ms → 3ms · that's 14× faster"] },
  { id: "r2", author: "jules.systems", name: "Jules Walker", avatar: "JW", color: "peach", caption: "Drawing our multi-region failover in 60 seconds. No magic, just good boundaries.", audio: "System design · jules.systems", likes: 1820, comments: 52, theme: "architecture", label: "FAILOVER, EXPLAINED", lines: ["us-east-1  ── primary", "      ↓ health probe", "eu-west-1  ── warm standby", "RTO: 30s  ·  RPO: 0"] },
];
