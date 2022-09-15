import { createApp } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import "@/styles/global.scss"
import App from "./App.vue"
import vuetify from "./plugins/vuetify"
import routes from "~pages"

const router = createRouter({
  history: createWebHistory(),
  routes,
})

createApp(App).use(router).use(vuetify).mount("#app")
