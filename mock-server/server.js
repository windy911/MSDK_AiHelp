// MSDK AiHelp Mock Server
// npm install && npm start

const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const os = require('os');

const PORT = 3000;
const app = express();
const server = http.createServer(app);
const wss = new WebSocket.Server({ server, path: '/ws/chat' });

// Middleware
app.use(express.json());
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// Create uploads directory
const uploadsDir = path.join(__dirname, 'uploads');
if (!fs.existsSync(uploadsDir)) fs.mkdirSync(uploadsDir);

// Multer config for image upload
const storage = multer.diskStorage({
  destination: uploadsDir,
  filename: (req, file, cb) => {
    cb(null, Date.now() + '-' + file.originalname);
  }
});
const upload = multer({ storage });

// CORS - allow all for testing
app.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Headers', '*');
  res.header('Access-Control-Allow-Methods', '*');
  if (req.method === 'OPTIONS') return res.sendStatus(200);
  next();
});

// ===== FAQ Data =====
const faqSections = [
  { sectionId: 'recharge', title: '充值相关', sortOrder: 1 },
  { sectionId: 'account', title: '账号问题', sortOrder: 2 },
  { sectionId: 'gameplay', title: '游戏玩法', sortOrder: 3 }
];

const faqItems = {
  recharge: [
    { faqId: 'r1', question: '充值未到账怎么办？', answer: '<p>请先确认支付是否扣款成功。如果已扣款但未到账，请提供订单截图联系客服处理，一般会在30分钟内补发。</p>', sortOrder: 1 },
    { faqId: 'r2', question: '支持哪些支付方式？', answer: '<p>目前支持：微信支付、支付宝、Apple Pay、Google Play 内购。</p>', sortOrder: 2 }
  ],
  account: [
    { faqId: 'a1', question: '忘记密码怎么办？', answer: '<p>点击登录界面"忘记密码"，通过绑定手机号或邮箱重置密码。</p>', sortOrder: 1 }
  ],
  gameplay: [
    { faqId: 'g1', question: '如何快速升级？', answer: '<p>完成每日任务、参与活动副本、使用双倍经验卡可以大幅提升升级速度。</p>', sortOrder: 1 }
  ]
};

// ===== API Routes =====

// FAQ Sections
app.get('/api/v1/faq/sections', (req, res) => {
  res.json(faqSections);
});

// FAQ Items by section
app.get('/api/v1/faq/sections/:sectionId/items', (req, res) => {
  const items = faqItems[req.params.sectionId] || [];
  res.json(items);
});

// FAQ Detail
app.get('/api/v1/faq/items/:faqId', (req, res) => {
  const all = Object.values(faqItems).flat();
  const item = all.find(i => i.faqId === req.params.faqId);
  if (item) return res.json(item);
  res.status(404).json({ error: 'not found' });
});

// FAQ Search
app.get('/api/v1/faq/search', (req, res) => {
  const q = (req.query.q || '').toLowerCase();
  const all = Object.values(faqItems).flat();
  const results = all.filter(i => i.question.toLowerCase().includes(q));
  res.json(results);
});

// FAQ Feedback
app.post('/api/v1/faq/items/:faqId/feedback', (req, res) => {
  console.log('FAQ feedback:', req.params.faqId, req.body);
  res.json({ success: true });
});

// Image Upload
app.post('/api/v1/upload', upload.single('file'), (req, res) => {
  const url = `http://${req.headers.host}/uploads/${req.file.filename}`;
  console.log('Upload:', url);
  res.json(url);
});

// Unread Count
app.get('/api/v1/chat/unread', (req, res) => {
  res.json(0);
});

// ===== WebSocket Chat =====

wss.on('connection', (ws, req) => {
  const sessionId = 'sess_' + Math.random().toString(36).substr(2, 9);
  console.log('WebSocket connected:', sessionId);

  // Send session assignment
  ws.send(JSON.stringify({ type: 'connect', sessionId: sessionId }));

  // Send a welcome message (AI bot)
  setTimeout(() => {
    ws.send(JSON.stringify({
      type: 'receive',
      msgType: 'text',
      content: '您好！我是AI客服助手，请问有什么可以帮您？',
      sender: 'ai_bot',
      timestamp: Date.now()
    }));
  }, 500);

  ws.on('message', (message) => {
    try {
      const data = JSON.parse(message);
      console.log('Received:', data.type, data.msgType, data.content?.substring(0, 30));

      if (data.type === 'heartbeat') return;

      if (data.type === 'send') {
        // Echo back as received message (simulating AI response)
        setTimeout(() => {
          const responses = [
            '收到您的问题，正在为您查询...',
            '请稍等，客服正在处理中。',
            '关于这个问题，您可以尝试重新登录游戏后再试。',
            '已记录您的问题，人工客服稍后会介入处理。'
          ];
          const reply = responses[Math.floor(Math.random() * responses.length)];

          ws.send(JSON.stringify({
            type: 'receive',
            msgType: 'text',
            content: reply,
            sender: 'ai_bot',
            serverMsgId: 'srv_' + Math.random().toString(36).substr(2, 8),
            timestamp: Date.now()
          }));
        }, 800 + Math.random() * 1000);
      }
    } catch (e) {
      console.error('Invalid message:', message);
    }
  });

  ws.on('close', () => {
    console.log('WebSocket closed:', sessionId);
  });
});

// Health check
app.get('/', (req, res) => {
  res.json({ status: 'ok', service: 'MSDK AiHelp Mock Server' });
});

function getLocalIPs() {
  const interfaces = os.networkInterfaces();
  const ips = [];
  for (const name of Object.keys(interfaces)) {
    for (const iface of interfaces[name]) {
      if (iface.family === 'IPv4' && !iface.internal) {
        ips.push(iface.address);
      }
    }
  }
  return ips;
}

server.listen(PORT, () => {
  const ips = getLocalIPs();
  console.log(`\n=== MSDK AiHelp Mock Server ===`);
  console.log(`HTTP:  http://localhost:${PORT}`);
  console.log(`WS:    ws://localhost:${PORT}/ws/chat`);
  if (ips.length > 0) {
    console.log(`\n本机 IP (Android 设备请使用这些地址):`);
    ips.forEach(ip => console.log(`  http://${ip}:${PORT}`));
  }
  console.log(`\nAndroid Demo config:`);
  console.log(`  domain: "http://${ips[0] || 'YOUR_PC_IP'}:${PORT}"`);
  console.log(`  appId:  "demo"`);
  console.log(`  appSecret: "demo_secret"`);
  console.log(`\nPress Ctrl+C to stop\n`);
});
