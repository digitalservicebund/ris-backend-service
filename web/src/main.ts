import { createPinia } from "pinia"
import { createApp } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import App from "./App.vue"
import vuetify from "./plugins/vuetify"
import DocUnitDocx from "./views/DocUnitDocx.vue"
import Home from "./views/HomePage.vue"
import Rechtssprechung from "./views/RechtsSprechung.vue"
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
      path: "/rechtssprechung",
      name: "Rechtssprechung",
      component: Rechtssprechung,
    },
    {
      path: "/stammdaten/:id",
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
      path: "/docx",
      name: "DocUnitDocx",
      component: DocUnitDocx,
    },
  ],
})

createApp(App).use(router).use(vuetify).use(createPinia()).mount("#app")
