FROM node:22-alpine
ENV NODE_ENV=production PORT=8082 HOST=0.0.0.0 DATA_DIRECTORY=/data
WORKDIR /app
COPY server.js .
RUN mkdir -p /data && chown node:node /data
USER node
VOLUME ["/data"]
EXPOSE 8082
CMD ["node", "server.js"]
