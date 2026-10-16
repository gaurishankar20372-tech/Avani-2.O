# Avani 2.O — Render.com Deployment Guide

Avani 2.O is pre-configured with a 1-Click Render Blueprint (`render.yaml`), a production Node.js Web Server (`server/`), and a `Dockerfile`.

---

## 🚀 How to Deploy on Render (Step-by-Step)

### Option 1: Render Blueprint (Recommended - 1-Click Deploy)

1. **Push your project to GitHub**:
   - In AI Studio, open the project menu and export to GitHub, or commit and push this repository to your GitHub account.

2. **Open Render (render.com)**:
   - Go to [https://dashboard.render.com](https://dashboard.render.com) and log in.

3. **Create New Blueprint Instance**:
   - Click **"New +"** in the top right.
   - Select **"Blueprint"**.
   - Connect your **Avani 2.O** GitHub repository.
   - Render will automatically read `render.yaml` and configure:
     - **Service Name:** `avani-2o-ai-server`
     - **Environment:** `Node`
     - **Build Command:** `cd server && npm install`
     - **Start Command:** `cd server && node server.js`
     - **Plan:** `Free`

4. **Add Your Gemini API Key**:
   - Under Environment Variables in Render, add:
     - `GEMINI_API_KEY`: Your Google Gemini API Key from Google AI Studio.
   - Click **"Apply"** or **"Create Service"**.

5. **Done!**:
   - Render will build and launch your server.
   - You will get a live URL (e.g., `https://avani-2o-ai-server.onrender.com`).
   - Open this URL in any browser on phone, tablet, or desktop to interact with Avani 2.O!

---

### Option 2: Web Service Manual Setup on Render

If you choose **"New + Web Service"** instead of Blueprint:

1. **Root Directory:** Leave empty or set to `.`
2. **Runtime:** `Node`
3. **Build Command:** `cd server && npm install`
4. **Start Command:** `cd server && node server.js`
5. **Environment Variables:**
   - `PORT`: `10000`
   - `GEMINI_API_KEY`: (your Gemini API key)

---

### Features of the Web / Render Server

- **Full Voice Assistant**: Interactive animated glowing Avani Orb with Web Speech recognition (Hindi & English) and Speech Synthesis voice responses.
- **Gemini Proxy**: Direct integration with Gemini models without exposing your secret API key on the client side.
- **REST API Endpoints**:
  - `GET /health` : Health check status
  - `POST /api/chat` : NLU and conversation reasoning
  - `GET /api/projects` & `POST /api/projects` : Project Memory cloud sync
  - `GET /api/tasks` : Task manager
