#!/usr/bin/env node
/** Tiny static preview server (no dependencies). Usage: npm start, then open http://localhost:8080/ */
import http from 'node:http';
import { promises as fs } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const port = Number(process.env.PORT ?? 8080);
const HIDDEN = ['/scripts/', '/tests/', '/content/', '/.github/', '/node_modules/', '/.git/'];
const TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.mjs': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.md': 'text/markdown; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.webp': 'image/webp',
  '.gif': 'image/gif',
  '.txt': 'text/plain; charset=utf-8',
};

const server = http.createServer(async (request, response) => {
  const url = new URL(request.url, 'http://localhost');
  const pathname = decodeURIComponent(url.pathname);
  if (HIDDEN.some(prefix => pathname.startsWith(prefix))) {
    response.writeHead(404).end('Not found');
    return;
  }
  const target = path.normalize(path.join(root, pathname.endsWith('/') ? `${pathname}index.html` : pathname));
  if (target !== root && !target.startsWith(root + path.sep)) {
    response.writeHead(403).end('Forbidden');
    return;
  }
  try {
    const body = await fs.readFile(target);
    response.writeHead(200, { 'Content-Type': TYPES[path.extname(target)] ?? 'application/octet-stream', 'Cache-Control': 'no-cache' });
    response.end(body);
  } catch {
    response.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' }).end('Not found');
  }
});

server.listen(port, () => {
  console.log(`Zovrax preview running at http://localhost:${port}/`);
});
