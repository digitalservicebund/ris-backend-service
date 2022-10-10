import { createApp } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import "@/styles/global.scss"
import App from "./App.vue"
import routes from "~pages"

const router = createRouter({
  history: createWebHistory(),
  routes,
})

createApp(App).use(router).mount("#app")
