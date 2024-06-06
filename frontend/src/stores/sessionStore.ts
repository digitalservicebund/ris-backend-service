import { defineStore } from "pinia"
import { Ref, ref } from "vue"
import { useFavicon } from "@/composables/useFavicon"
import { Env } from "@/domain/env"
import { User } from "@/domain/user"
import envService from "@/services/adminService"
import authService from "@/services/authService"

type SessionStore = {
  user: Ref<User | undefined>
  env: Ref<string | undefined>
  isAuthenticated: () => Promise<boolean>
  initSession: () => Promise<void>
}

const useSessionStore = defineStore("session", (): SessionStore => {
  const user = ref<User>()
  const env = ref()

  async function fetchUser(): Promise<User | undefined> {
    return (await authService.getName()).data ?? undefined
  }

  async function fetchEnv(): Promise<Env | undefined> {
    return (await envService.getEnv()).data
  }

  /**
   * Checks with the backend if the user has a valid session and updates the user
   * in store.
   *
   * @returns A promise with a boolean indicating if the user is authenticated.
   */
  async function isAuthenticated(): Promise<boolean> {
    user.value = await fetchUser()
    return !!user.value?.name
  }

  async function initSession(): Promise<void> {
    env.value = await fetchEnv()
    const favicon = document.getElementById("favicon") as HTMLAnchorElement
    favicon.href = useFavicon(env.value).value
  }

  return { user, env, isAuthenticated, initSession }
})

export default useSessionStore
