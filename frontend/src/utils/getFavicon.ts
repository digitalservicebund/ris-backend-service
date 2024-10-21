import productionFavicon from "@/assets/favicon-production.svg"
import stagingFavicon from "@/assets/favicon-staging.svg"
import uatFavicon from "@/assets/favicon-uat.svg"

export const getFavicon = (env?: string) => {
  if (env == "staging") {
    return stagingFavicon
  } else if (env == "uat") {
    return uatFavicon
  } else {
    return productionFavicon
  }
}
