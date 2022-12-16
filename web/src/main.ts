import { createPinia } from "pinia"
import { createApp } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import "@/styles/global.scss"
import App from "./App.vue"
import routes from "~pages"

const storeManager = createPinia()

const router = createRouter({
  history: createWebHistory(),
  routes,
})

createApp(App).use(router).use(storeManager).mount("#app")
