FROM node:lts-alpine AS builder

RUN mkdir -p /app
COPY . /app
WORKDIR /app

RUN npm install --force
RUN npm run build

FROM nginx:stable-alpine

COPY --from=builder /app/build /usr/share/nginx/html
WORKDIR /usr/share/nginx/html

CMD ["nginx", "-g", "daemon off;"]
