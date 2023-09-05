# Use a smaller base image
FROM node:alpine as installer

# Copy the application code
COPY . /juice-shop

# Set the working directory
WORKDIR /juice-shop

# Install dependencies and build the application
RUN npm i -g typescript ts-node \
    && npm install --omit=dev --unsafe-perm \
    && npm dedupe \
    && rm -rf frontend/node_modules \
    && rm -rf frontend/.angular \
    && rm -rf frontend/src/assets \
    && mkdir logs \
    && chown -R 65532 logs \
    && chgrp -R 0 ftp/ frontend/dist/ logs/ data/ i18n/ \
    && chmod -R g=u ftp/ frontend/dist/ logs/ data/ i18n/ \
    && rm data/chatbot/botDefaultTrainingData.json || true \
    && rm ftp/legal.md || true \
    && rm i18n/*.json || true

# Use a smaller base image for the final image
FROM node:alpine

# Set the working directory
WORKDIR /juice-shop

# Copy the built application from the installer stage
COPY --from=installer --chown=65532:0 /juice-shop .

# Set the user and expose the port
USER 65532
EXPOSE 3002

# Start the application
CMD ["/juice-shop/build/app.js"]