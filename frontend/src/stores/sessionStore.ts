import { defineStore } from "pinia"
import { Ref, ref } from "vue"
import { useFavicon } from "@/composables/useFavicon"
import { Env } from "@/domain/env"
import { User } from "@/domain/user"
import adminService from "@/services/adminService"
import authService from "@/services/authService"
import featureToggleService from "@/services/featureToggleService"

type SessionStore = {
  user: Ref<User | undefined>
  env: Ref<Env | undefined>
  featureToggles: Ref<Record<string, boolean>>
  isAuthenticated: () => Promise<boolean>
  initSession: () => Promise<void>
}

const useSessionStore = defineStore("session", (): SessionStore => {
  const user = ref<User>()
  const env = ref<Env>()
  const featureToggles = ref<Record<string, boolean>>({})

  async function fetchUser(): Promise<User | undefined> {
    return (await authService.getName()).data ?? undefined
  }

  async function fetchEnv(): Promise<Env | undefined> {
    return (await adminService.getEnv()).data
  }

  async function fetchFeatureToggles(): Promise<Record<string, boolean>> {
    return featureToggleService.getEnabledToggles()
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
    featureToggles.value = await fetchFeatureToggles()
  }

  return { user, env, featureToggles, isAuthenticated, initSession }
})

export default useSessionStore
