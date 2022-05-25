import { createApp } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import App from "./App.vue"
import vuetify from "./plugins/vuetify"
import Home from "./views/HomePage.vue"

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
  ],
})

createApp(App).use(router).use(vuetify).mount("#app")
