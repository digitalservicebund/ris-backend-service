import {
  createRouter,
  createWebHistory,
  RouteLocationNormalized,
} from "vue-router"
import authService from "./services/authService"
import routes from "~pages"

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from) => beforeEach(to, from))

export async function beforeEach(
  _to: RouteLocationNormalized,
  from?: RouteLocationNormalized
) {
  if (from?.name || (await authService.isAuthenticated())) return true
  else return false
}

export default router
