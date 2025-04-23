FROM node:22.15.0
WORKDIR /usr/src/app
COPY package*.json ./
RUN npm update --location=global npm && npm install
COPY . .
EXPOSE 3000
CMD ["npm", "run", "dev", "--", "--host"]
