# Dockerfile
FROM nginx:stable-alpine3.19-slim

# Copy the Nginx config
COPY nginx.conf /etc/nginx/sites-available/default

# Expose port 80
EXPOSE 80/tcp

# Copy the business logic to the nginx html directory
COPY business-logic.js /usr/share/nginx/html/
# Copy the json schema of input to the nginx html directory
COPY input-schema.json /usr/share/nginx/html/
# Copy the json schema of output to the nginx html directory
COPY output-schema.json /usr/share/nginx/html/