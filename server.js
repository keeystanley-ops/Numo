const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const PUBLIC_DIR = path.join(__dirname, 'public');
const APK_FALLBACK = path.join(__dirname, 'app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk');

function getApkPath(requestedFile) {
  const specificPath = path.join(PUBLIC_DIR, requestedFile);
  if (fs.existsSync(specificPath)) return specificPath;
  const numoPath = path.join(PUBLIC_DIR, 'numo-debug.apk');
  if (fs.existsSync(numoPath)) return numoPath;
  const newPath = path.join(PUBLIC_DIR, 'new-debug.apk');
  if (fs.existsSync(newPath)) return newPath;
  if (fs.existsSync(APK_FALLBACK)) return APK_FALLBACK;
  return null;
}

const server = http.createServer((req, res) => {
  const url = req.url.split('?')[0];

  if (url === '/numo-debug.apk' || url === '/new-debug.apk' || url === '/app-debug.apk' || url === '/download') {
    const filename = url === '/download' ? 'numo-debug.apk' : path.basename(url);
    const apkPath = getApkPath(filename);

    if (!apkPath || !fs.existsSync(apkPath)) {
      res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
      return res.end('APK not found. Please wait for the build to finish.');
    }

    const stat = fs.statSync(apkPath);
    res.writeHead(200, {
      'Content-Type': 'application/vnd.android.package-archive',
      'Content-Length': stat.size,
      'Content-Disposition': `attachment; filename="${filename}"`,
      'Cache-Control': 'no-cache, no-store, must-revalidate',
      'Access-Control-Allow-Origin': '*'
    });

    const readStream = fs.createReadStream(apkPath);
    readStream.pipe(res);
    return;
  }

  // Root landing page
  const apkPath = getApkPath('numo-debug.apk');
  const sizeMb = apkPath && fs.existsSync(apkPath) 
    ? (fs.statSync(apkPath).size / (1024 * 1024)).toFixed(1) + ' MB'
    : '28 MB';

  res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
  res.end(`<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Download Numo Android App</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; }
    body { background: #E5F6F1; color: #1E5631; display: flex; justify-content: center; align-items: center; min-height: 100vh; padding: 20px; }
    .card { background: #FFFFFF; border-radius: 28px; padding: 36px 26px; max-width: 440px; width: 100%; box-shadow: 0 16px 40px rgba(30, 86, 49, 0.12); border: 1px solid #D8EFE8; text-align: center; }
    .badge { display: inline-flex; align-items: center; gap: 6px; background: #E8FBF4; color: #1E5631; font-weight: 700; font-size: 12px; padding: 6px 14px; border-radius: 20px; margin-bottom: 20px; border: 1px solid #A7F3D0; }
    .badge-dot { width: 8px; height: 8px; border-radius: 50%; background: #10B981; }
    h1 { font-size: 26px; font-weight: 800; color: #1E5631; margin-bottom: 6px; }
    .subtitle { font-size: 14px; color: #2C7360; line-height: 1.5; margin-bottom: 26px; }
    .btn { display: block; background: #1E5631; color: #FFFFFF; text-decoration: none; font-weight: 700; font-size: 16px; padding: 18px 24px; border-radius: 18px; box-shadow: 0 8px 24px rgba(30, 86, 49, 0.28); transition: transform 0.15s ease, background 0.15s ease; }
    .btn:active { transform: scale(0.98); }
    .btn:hover { background: #164326; }
    .alt-btn { display: block; margin-top: 12px; background: #F0FAF8; color: #1E5631; text-decoration: none; font-weight: 600; font-size: 14px; padding: 12px 20px; border-radius: 14px; border: 1px solid #D8EFE8; }
    .instructions { text-align: left; background: #F0FAF8; border-radius: 18px; padding: 20px; margin-top: 26px; border: 1px solid #E2EEEA; }
    .instructions h3 { font-size: 14px; font-weight: 700; color: #1E5631; margin-bottom: 12px; display: flex; align-items: center; gap: 6px; }
    .instructions ol { padding-left: 20px; font-size: 13px; color: #2C7360; line-height: 1.6; }
    .instructions li { margin-bottom: 8px; }
    .note { margin-top: 18px; font-size: 11px; color: #5A7A70; text-align: center; }
  </style>
</head>
<body>
  <div class="card">
    <div class="badge"><span class="badge-dot"></span> Ready for Installation</div>
    <h1>Numo Financial OS</h1>
    <p class="subtitle">Personal Finance & Wealth Operating System</p>

    <a href="/numo-debug.apk" class="btn" download="numo-debug.apk">
      ⬇ Download Numo APK (${sizeMb})
    </a>

    <a href="/new-debug.apk" class="alt-btn" download="new-debug.apk">
      Alternative Download Link
    </a>

    <div class="instructions">
      <h3>📱 Android Installation Steps</h3>
      <ol>
        <li>Tap <b>Download Numo APK</b> above.</li>
        <li>If your phone displays <i>"File might be harmful"</i>, tap <b>Download anyway</b>.</li>
        <li>When download finishes, tap the notification or open from Downloads.</li>
        <li>If prompted with <i>"Unrecognized app"</i>, tap <b>More details</b> &rarr; <b>Install anyway</b>.</li>
        <li><b>Tip:</b> If you previously installed an older version, please uninstall it first to ensure clean installation.</li>
      </ol>
    </div>

    <div class="note">Package: com.aistudio.numo.qireqb • Built with Jetpack Compose & M3</div>
  </div>
</body>
</html>`);
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`APK download server listening on 0.0.0.0:${PORT}`);
});
