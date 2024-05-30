import {
  createRouter,
  createWebHistory,
  RouteLocationNormalized,
} from "vue-router"
import authService from "./services/authService"
import useSessionStore from "./stores/sessionStore"
import routes from "~pages"

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => beforeEach(to))

function redirectToLogin() {
  location.href = authService.getLoginEndpoint()
}

function setLocationCookie(path?: string) {
  document.cookie = `location=${path ?? window.location.pathname}; path=/;`
}

function deleteCookie(name: string) {
  document.cookie = `${name}=; path=/; expires=Thu, 01 Jan 1970 00:00:01 GMT;`
}

function followLocationCookie() {
  if (document.cookie.includes("location=")) {
    const destination = document.cookie.split("location=")[1].split(";")[0]
    deleteCookie("location")
    location.href = destination
  }
}

export async function beforeEach(to: RouteLocationNormalized) {
  const session = useSessionStore()
  if (await session.isAuthenticated()) {
    followLocationCookie()
    return true
  } else {
    setLocationCookie(to.path)
    deleteCookie("SESSION")
    redirectToLogin()
    return false
  }
}

export default router
