import { createPinia } from "pinia"
import { createApp } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import App from "./App.vue"
import vuetify from "./plugins/vuetify"
import DocUnitDocx from "./views/DocUnitDocx.vue"
import DokumenteView from "./views/DokumenteView.vue"
import Home from "./views/HomePage.vue"
import Rechtsprechung from "./views/RechtSprechung.vue"

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/",
      name: "Home",
      component: Home,
    },
    {
      path: "/rechtsprechung",
      name: "Rechtsprechung",
      component: Rechtsprechung,
    },
    {
      path: "/rechtsprechung/:id/rubriken",
      name: "Rubriken",
      // route level code-splitting
      // this generates a separate chunk (about.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () =>
        import(/* webpackChunkName: "stammdaten" */ "./views/RubrikenView.vue"),
    },
    {
      path: "/rechtsprechung/:id/dokumente",
      name: "Dokumente",
      component: DokumenteView,
    },
    {
      path: "/docx",
      name: "DocUnitDocx",
      component: DocUnitDocx,
    },
  ],
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

createApp(App).use(router).use(vuetify).use(createPinia()).mount("#app")
