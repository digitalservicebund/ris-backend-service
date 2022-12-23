import { watch } from "vue"
import type { Ref } from "vue"

export function useScrollToHash(routeHash: Ref<string | undefined>) {
  function jumpToHash() {
    // scrollIntoView with smooth behavior only works inside of a timeout
    setTimeout(() => {
      if (routeHash.value) {
        const hashElement = document.querySelector(routeHash.value)
        hashElement && hashElement.scrollIntoView({ behavior: "smooth" })
      }
    })
  }

  watch(routeHash, jumpToHash, { immediate: true })
}
