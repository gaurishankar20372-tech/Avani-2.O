# Production Dockerfile for Avani 2.O Web Server on Render
FROM node:20-alpine

WORKDIR /app

# Copy server dependencies
COPY server/package*.json ./server/
WORKDIR /app/server
RUN npm install --production

# Copy server code and web assets
COPY server/ ./

EXPOSE 10000

ENV PORT=10000
ENV NODE_ENV=production

CMD ["node", "server.js"]
