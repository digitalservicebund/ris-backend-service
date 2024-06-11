import { computed, ref } from "vue"
import productionFavicon from "@/assets/favicon-production.svg"
import stagingFavicon from "@/assets/favicon-staging.svg"
import uatFavicon from "@/assets/favicon-uat.svg"

export function useFavicon(env: string | undefined) {
  const favicon = ref()

  return computed(() => {
    if (env == "staging") {
      favicon.value = stagingFavicon
    } else if (env == "uat") {
      favicon.value = uatFavicon
    } else {
      favicon.value = productionFavicon
    }

    return favicon.value
  })
}
