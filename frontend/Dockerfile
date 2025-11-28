FROM node:24.11.1
WORKDIR /usr/src/app
COPY package*.json ./
RUN npm update --location=global npm && npm ci --ignore-scripts
COPY . .
EXPOSE 3000
CMD ["npm", "run", "dev", "--", "--host"]
