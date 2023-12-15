import { watch } from "vue"
import type { Ref } from "vue"

export function useScrollToHash(
  routeHash: Ref<string | undefined>,
  offset?: number,
) {
  function jumpToHash() {
    // scrollIntoView with smooth behavior only works inside of a timeout
    setTimeout(() => {
      if (!routeHash.value) return
      const idFromHash = routeHash.value.replace(/^#/, "")
      const element = document.getElementById(idFromHash)
      const headerOffset = offset ?? 0
      const elementPosition = element ? element.getBoundingClientRect().top : 0
      const offsetPosition = elementPosition + window.scrollY - headerOffset
      window.scrollTo({
        top: offsetPosition,
        behavior: "smooth",
      })
    })
  }

  watch(routeHash, jumpToHash, { immediate: true })
}
