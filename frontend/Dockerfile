# ---------- Etapa 1: build ----------
FROM node:20-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci --ignore-scripts

COPY . .
# Normalize Windows checkouts inside the image before validating formatting.
RUN find src -type f \( -name '*.ts' -o -name '*.tsx' -o -name '*.css' \) \
    -exec sed -i 's/\r$//' {} +
RUN npm run lint
RUN ./node_modules/.bin/prettier --check "src/**/*.{ts,tsx,css}"
RUN npm test
RUN npm run build

# ---------- Etapa 2: runtime (Nginx) ----------
FROM nginx:alpine
RUN chown -R nginx:nginx /var/cache/nginx \
    && sed -i 's@^pid .*;@pid /tmp/nginx.pid;@' /etc/nginx/nginx.conf
COPY --from=build /app/dist /usr/share/nginx/html
COPY --chmod=644 nginx.conf /etc/nginx/conf.d/default.conf
RUN chmod -R a+rX /usr/share/nginx/html

USER nginx:nginx

EXPOSE 80
HEALTHCHECK --interval=10s --timeout=3s --start-period=5s --retries=5 \
	CMD wget -Y off -qO- http://127.0.0.1/ || exit 1
