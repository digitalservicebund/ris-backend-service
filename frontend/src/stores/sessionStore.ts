import { defineStore } from "pinia"
import { Ref, ref } from "vue"
import { User } from "@/domain/user"
import authService from "@/services/authService"

type SessionStore = {
  user: Ref<User | undefined>
  isAuthenticated: () => Promise<boolean>
}

const useSessionStore = defineStore("session", (): SessionStore => {
  const user = ref<User>()

  async function fetchUser(): Promise<User | undefined> {
    return (await authService.getName()).data ?? undefined
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

  return { user, isAuthenticated }
})

export default useSessionStore
