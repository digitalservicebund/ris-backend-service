import { onMounted, onUpdated } from "vue"

export function useScrollToHash(route: { hash: string | undefined }) {
  function jumpToHash() {
    // scrollIntoView with smooth behavior only works inside of a timeout
    return new Promise((resolve) => {
      setTimeout(() => {
        if (route.hash) {
          const el = document.querySelector(route.hash)
          el && el.scrollIntoView({ behavior: "smooth" })
        }
        return resolve({ left: 0, top: 0 })
      })
    })
  }
  onMounted(() => {
    jumpToHash()
  })
  onUpdated(() => {
    jumpToHash()
  })
}
