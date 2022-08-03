import { createApp } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import App from "./App.vue"
import vuetify from "./plugins/vuetify"
import routes from "~pages"

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to) {
    return new Promise((resolve) => {
      setTimeout(() => {
        if (to.hash) {
          resolve({
            el: to.hash,
            behavior: "smooth",
          })
          return
        }
        return resolve({ left: 0, top: 0 })
      }, 200)
    })
  },
})

createApp(App).use(router).use(vuetify).mount("#app")
