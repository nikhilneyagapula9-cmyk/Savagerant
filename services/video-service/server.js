const http = require('node:http');
const fs = require('node:fs');
const path = require('node:path');
const { URL } = require('node:url');
const { randomUUID } = require('node:crypto');

const port = Number(process.env.PORT || 8083);
const dataDirectory = process.env.DATA_DIRECTORY || path.join(__dirname, 'data');
const videosDirectory = path.join(dataDirectory, 'videos');
const metadataFile = path.join(dataDirectory, 'videos.json');
const maxBytes = 50 * 1024 * 1024;
const supportedTypes = new Set(['video/mp4', 'video/webm', 'video/quicktime']);
fs.mkdirSync(videosDirectory, { recursive: true });
if (!fs.existsSync(metadataFile)) fs.writeFileSync(metadataFile, '[]', { flag: 'wx' });

function readVideos() {
    return JSON.parse(fs.readFileSync(metadataFile, 'utf8'));
}

function sendJson(response, status, value) {
    response.writeHead(status, {
        'content-type': 'application/json; charset=utf-8',
        'x-content-type-options': 'nosniff',
        'cache-control': 'no-store'
    });
    response.end(status === 204 ? undefined : JSON.stringify(value));
}

async function readVideo(request) {
    const chunks = [];
    let bytes = 0;
    for await (const chunk of request) {
        bytes += chunk.length;
        if (bytes > maxBytes) throw Object.assign(new Error('Video exceeds the 50 MB limit.'), { status: 413 });
        chunks.push(chunk);
    }
    return Buffer.concat(chunks);
}

function publicVideo(video) {
    return {
        id: video.id,
        authorId: video.authorId,
        author: video.author,
        caption: video.caption,
        contentType: video.contentType,
        sizeBytes: video.sizeBytes,
        createdAt: video.createdAt,
        contentUrl: `/api/videos/${video.id}/content`
    };
}

function matchesSignature(contentType, content) {
    if (contentType === 'video/webm') {
        return content.length >= 4 && content[0] === 0x1a && content[1] === 0x45
            && content[2] === 0xdf && content[3] === 0xa3;
    }
    return content.length >= 8 && content.toString('ascii', 4, 8) === 'ftyp';
}

const server = http.createServer(async (request, response) => {
    try {
        const url = new URL(request.url, `http://${request.headers.host || 'localhost'}`);
        if (request.method === 'GET' && url.pathname === '/health') {
            return sendJson(response, 200, { status: 'ok' });
        }
        if (request.method === 'GET' && url.pathname === '/videos') {
            const scope = url.searchParams.get('scope') || 'public';
            let videos = readVideos();
            if (scope !== 'public') {
                const authorIds = new Set(url.searchParams.getAll('authorIds').map(Number).filter(Number.isSafeInteger));
                videos = videos.filter(video => authorIds.has(video.authorId));
            }
            return sendJson(response, 200, videos.sort((a, b) => b.createdAt.localeCompare(a.createdAt)).map(publicVideo));
        }
        if (request.method === 'POST' && url.pathname === '/videos') {
            const authorId = Number(request.headers['x-user-id']);
            const author = url.searchParams.get('author') || '';
            const caption = url.searchParams.get('caption') || '';
            const contentType = (request.headers['content-type'] || '').split(';')[0].toLowerCase();
            if (!Number.isSafeInteger(authorId) || authorId <= 0 || !author.trim()) {
                return sendJson(response, 401, { error: 'A signed-in creator is required.' });
            }
            if (!caption.trim() || caption.length > 160) return sendJson(response, 400, { error: 'Caption must contain 1 to 160 characters.' });
            if (!supportedTypes.has(contentType)) return sendJson(response, 415, { error: 'Upload an MP4, WebM, or MOV video.' });
            if (Number(request.headers['content-length']) > maxBytes) return sendJson(response, 413, { error: 'Video exceeds the 50 MB limit.' });
            const content = await readVideo(request);
            if (!content.length) return sendJson(response, 400, { error: 'Video file cannot be empty.' });
            if (!matchesSignature(contentType, content)) {
                return sendJson(response, 415, { error: 'Video bytes do not match the declared file format.' });
            }
            const id = randomUUID();
            const filePath = path.join(videosDirectory, id);
            fs.writeFileSync(filePath, content, { flag: 'wx', mode: 0o600 });
            const video = {
                id,
                authorId,
                author: author.trim().slice(0, 100),
                caption: caption.trim(),
                contentType,
                sizeBytes: content.length,
                createdAt: new Date().toISOString()
            };
            const videos = readVideos();
            videos.push(video);
            const temporary = `${metadataFile}.tmp`;
            fs.writeFileSync(temporary, JSON.stringify(videos), { mode: 0o600 });
            fs.renameSync(temporary, metadataFile);
            return sendJson(response, 201, publicVideo(video));
        }
        const contentRequest = url.pathname.match(/^\/videos\/([0-9a-f-]{36})\/content$/i);
        if (request.method === 'GET' && contentRequest) {
            const video = readVideos().find(item => item.id === contentRequest[1]);
            if (!video) return sendJson(response, 404, { error: 'Video not found.' });
            const filePath = path.join(videosDirectory, video.id);
            if (!fs.existsSync(filePath)) return sendJson(response, 404, { error: 'Video file is unavailable.' });
            response.writeHead(200, {
                'content-type': video.contentType,
                'content-length': video.sizeBytes,
                'x-content-type-options': 'nosniff',
                'cache-control': 'public, max-age=3600'
            });
            return fs.createReadStream(filePath).pipe(response);
        }
        return sendJson(response, 404, { error: 'Not found.' });
    } catch (error) {
        if (!response.headersSent) sendJson(response, error.status || 400, { error: error.message || 'Invalid request.' });
        else response.destroy(error);
    }
});

server.listen(port, process.env.HOST || '127.0.0.1');
