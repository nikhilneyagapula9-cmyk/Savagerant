# DevGram

A responsive React social feed concept for sharing code snippets, architecture, deployment notes, and short technical demos. It runs as a standalone frontend and uses a browser-local mock API: posts, likes, comments, bookmarks, appearance, profile edits, and the mock session persist in `localStorage`.

## Run locally

Requires Node.js 18 or newer.

```powershell
cd devgram
npm install
npm run dev
```

Open the local URL printed by Vite. You can create an account with any name and email, and a password of at least eight characters. The GitHub and Google buttons are mock sign-in flows; they do not contact those providers.

Create a production bundle with `npm run build`, then serve the generated `dist/` directory with any static file server. The client routes are `/login`, `/signup`, `/feed`, `/search`, `/reels`, `/messages`, `/notifications`, and `/profile`.

## Notes

- This frontend does not call the existing SavageScreen API. Auth and social actions are demo-only and stored in the current browser.
- Reset the demo by clearing the browser's `devgram-*` local storage keys.
- The responsive desktop navigation collapses to a drawer and bottom bar on mobile.
