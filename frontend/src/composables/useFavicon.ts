import { computed, ref } from "vue"

export function useFavicon(env: string) {
  const favicon = ref()

  return computed(() => {
    if (env == "staging") {
      favicon.value = "/src/assets/favicon-staging.svg"
    } else if (env == "uat") {
      favicon.value = "/src/assets/favicon-uat.svg"
    } else {
      favicon.value = "/src/assets/favicon-production.svg"
    }

    return favicon.value
  })
}
