const express = require('express');
const cors = require('cors');
const path = require('path');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 10000;

app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// In-memory cloud memory cache for Web session
let projects = [
  {
    id: 1,
    name: "Avani 2.O Android",
    description: "Next-generation personal voice assistant with Gemini NLU and Android automation.",
    technologyStack: "Kotlin, Jetpack Compose, Room, OkHttp",
    stage: "Active Development",
    completedTasks: "Voice interface, Phone action router, Project memory, Emergency stop, Background service",
    pendingTasks: "Cloud backend sync on Render"
  }
];

let tasks = [
  { id: 1, title: "Test Avani 2.O on Render Cloud", priority: "High", completed: false }
];

// Health Check for Render
app.get('/health', (req, res) => {
  res.json({ status: 'ok', service: 'Avani 2.O AI Assistant Server', timestamp: new Date() });
});

// Gemini NLU & Action Routing Endpoint
app.post('/api/chat', async (req, res) => {
  const { message, history } = req.body;
  const apiKey = process.env.GEMINI_API_KEY;

  if (!message) {
    return res.status(400).json({ error: 'Message is required' });
  }

  // If no Gemini key is set in Render environment, use intelligent fallback
  if (!apiKey || apiKey === 'MY_GEMINI_API_KEY') {
    const fallbackResponse = handleLocalFallback(message);
    return res.json({ reply: fallbackResponse, source: 'local_engine' });
  }

  try {
    const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${apiKey}`;
    
    const contents = (history || []).map(h => ({
      role: h.role === 'user' ? 'user' : 'model',
      parts: [{ text: h.content }]
    }));
    contents.push({ role: 'user', parts: [{ text: message }] });

    const payload = {
      systemInstruction: {
        parts: [{
          text: "You are Avani 2.O, a voice-first intelligent personal AI assistant. You speak Hindi, English, and Hinglish naturally. Keep spoken responses concise and helpful."
        }]
      },
      contents: contents,
      generationConfig: {
        temperature: 0.7,
        topP: 0.95
      }
    };

    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    const data = await response.json();
    const replyText = data.candidates?.[0]?.content?.parts?.[0]?.text || handleLocalFallback(message);
    res.json({ reply: replyText, source: 'gemini-3.5-flash' });
  } catch (error) {
    console.error('Gemini API Error:', error);
    res.json({ reply: handleLocalFallback(message), source: 'local_fallback' });
  }
});

// Project Memory Endpoints
app.get('/api/projects', (req, res) => {
  res.json(projects);
});

app.post('/api/projects', (req, res) => {
  const newProj = { id: Date.now(), ...req.body };
  projects.push(newProj);
  res.json({ success: true, project: newProj });
});

// Tasks Endpoints
app.get('/api/tasks', (req, res) => {
  res.json(tasks);
});

app.post('/api/tasks', (req, res) => {
  const newTask = { id: Date.now(), title: req.body.title, priority: req.body.priority || 'Medium', completed: false };
  tasks.push(newTask);
  res.json({ success: true, task: newTask });
});

function handleLocalFallback(prompt) {
  const lower = prompt.toLowerCase();
  if (lower.includes('namaste') || lower.includes('kaise ho')) {
    return "नमस्ते! मैं अवनी 2.O हूँ, आपकी पर्सनल एआई असिस्टेंट। मैं रेंडर क्लाउड पर लाइव हूँ और आपके ऑर्डर्स लेने के लिए तैयार हूँ!";
  }
  if (lower.includes('project') || lower.includes('status')) {
    return `Project Memory: Avani 2.O Android - Stage: Active Development. Tech Stack: Kotlin, Jetpack Compose, Room. All core phone operations and background service configured!`;
  }
  if (lower.includes('kya kar sakti ho') || lower.includes('capabilities')) {
    return "मैं ऐप्स ओपन कर सकती हूँ, कॉल और व्हाट्सएप मैसेज तैयार कर सकती हूँ, प्रोजेक्ट मेमोरी ट्रैक कर सकती हूँ और कोडिंग में मदद कर सकती हूँ।";
  }
  return `Avani 2.O: Main aapke commands sun rahi hoon! Render server par message receive ho gaya hai: "${prompt}".`;
}

// Serve Web Client for any unmatched route
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`Avani 2.O Server running on port ${PORT}`);
});
