# Stage 1: Build the React app
FROM node:latest as build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Stage 2: Serve the app with Nginx
FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html

COPY ./certs/cert.pem /etc/ssl/certs/cert.pem
COPY ./certs/key.pem /etc/ssl/private/key.pem

COPY default.conf /etc/nginx/conf.d/default.conf

COPY certs/dhparam.pem /etc/ssl/certs/dhparam.pem

EXPOSE 80 443
CMD ["nginx", "-g", "daemon off;"]