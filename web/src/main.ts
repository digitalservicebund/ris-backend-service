import { createApp } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import App from "./App.vue"
import vuetify from "./plugins/vuetify"
import routes from "~pages"

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to) {
    if (to.hash) {
      return {
        // in our case this hides the header we are scrolling to, so a few pixels up? TODO
        el: to.hash,
        behavior: "smooth", // does this cause problems on browsers that don't support it? https://router.vuejs.org/guide/advanced/scroll-behavior.html#scroll-behavior
      }
    }
    return { top: 0 }
  },
})

createApp(App).use(router).use(vuetify).mount("#app")
