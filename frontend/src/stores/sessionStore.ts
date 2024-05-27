import { defineStore } from "pinia"
import { ref } from "vue"
import { User } from "@/domain/user"
import authService from "@/services/authService"

const useSessionStore = defineStore("session", () => {
  const user = ref<User>()

  async function fetchUser() {
    const authResponse = await authService.getName()
    if (authResponse.data) {
      user.value = authResponse.data
    } else {
      console.error("failed to load user")
    }
  }

  void fetchUser()

  return { user, fetchUser }
})

export default useSessionStore
