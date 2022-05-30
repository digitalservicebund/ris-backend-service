import { createPinia } from "pinia"
import { createApp } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import App from "./App.vue"
import vuetify from "./plugins/vuetify"
import DocUnit from "./views/DocUnit.vue"
import Home from "./views/HomePage.vue"
import Upload from "./views/UpLoad.vue"

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/",
      name: "Home",
      component: Home,
    },
    {
      path: "/stammdaten",
      name: "Stammdaten",
      // route level code-splitting
      // this generates a separate chunk (about.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () =>
        import(/* webpackChunkName: "stammdaten" */ "./views/StammDaten.vue"),
    },
    {
      path: "/upload",
      name: "Upload",
      component: Upload,
    },
    {
      path: "/docUnit/:id",
      name: "DocUnit",
      component: DocUnit,
    },
  ],
})

createApp(App).use(router).use(vuetify).use(createPinia()).mount("#app")
