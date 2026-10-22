const http = require('node:http');
const fs = require('node:fs');
const path = require('node:path');
const { URL } = require('node:url');

const port = Number(process.env.PORT || 8082);
const dataDirectory = process.env.DATA_DIRECTORY || path.join(__dirname, 'data');
const dataFile = path.join(dataDirectory, 'connections.json');
fs.mkdirSync(dataDirectory, { recursive: true });
if (!fs.existsSync(dataFile)) fs.writeFileSync(dataFile, '[]', { flag: 'wx' });

function readConnections() {
    return JSON.parse(fs.readFileSync(dataFile, 'utf8'));
}

function saveConnections(connections) {
    const temporary = `${dataFile}.tmp`;
    fs.writeFileSync(temporary, JSON.stringify(connections), { mode: 0o600 });
    fs.renameSync(temporary, dataFile);
}

function positiveId(value) {
    const id = Number(value);
    return Number.isSafeInteger(id) && id > 0 ? id : null;
}

function send(response, status, value) {
    response.writeHead(status, {
        'content-type': 'application/json; charset=utf-8',
        'x-content-type-options': 'nosniff',
        'cache-control': 'no-store'
    });
    response.end(JSON.stringify(value));
}

async function readJson(request) {
    let body = '';
    for await (const chunk of request) {
        body += chunk;
        if (body.length > 4096) throw new Error('Request body is too large.');
    }
    return JSON.parse(body || '{}');
}

const server = http.createServer(async (request, response) => {
    try {
        const url = new URL(request.url, `http://${request.headers.host || 'localhost'}`);
        if (request.method === 'GET' && url.pathname === '/health') return send(response, 200, { status: 'ok' });
        if (request.method === 'GET' && url.pathname === '/connections') {
            const userId = positiveId(url.searchParams.get('userId'));
            if (!userId) return send(response, 400, { error: 'A valid userId is required.' });
            const connectedIds = readConnections()
                .filter(connection => connection.userId === userId)
                .map(connection => connection.targetUserId);
            return send(response, 200, connectedIds);
        }
        if (request.method === 'POST' && url.pathname === '/connections') {
            const body = await readJson(request);
            const userId = positiveId(body.userId);
            const targetUserId = positiveId(body.targetUserId);
            if (!userId || !targetUserId || userId === targetUserId) {
                return send(response, 400, { error: 'Choose another valid account to connect with.' });
            }
            const connections = readConnections();
            if (!connections.some(item => item.userId === userId && item.targetUserId === targetUserId)) {
                connections.push({ userId, targetUserId, createdAt: new Date().toISOString() });
                saveConnections(connections);
            }
            return send(response, 204, null);
        }
        const disconnect = url.pathname.match(/^\/connections\/(\d+)$/);
        if (request.method === 'DELETE' && disconnect) {
            const userId = positiveId(url.searchParams.get('userId'));
            const targetUserId = positiveId(disconnect[1]);
            if (!userId || !targetUserId) return send(response, 400, { error: 'A valid account is required.' });
            const connections = readConnections().filter(item => !(item.userId === userId && item.targetUserId === targetUserId));
            saveConnections(connections);
            return send(response, 204, null);
        }
        return send(response, 404, { error: 'Not found.' });
    } catch (error) {
        return send(response, 400, { error: error.message || 'Invalid request.' });
    }
});

server.listen(port, process.env.HOST || '127.0.0.1');
