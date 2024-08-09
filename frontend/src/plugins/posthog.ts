// eslint-disable-next-line import/no-named-as-default
import posthog, { PostHog } from "posthog-js"
import { App, Plugin } from "vue"
import useSessionStore from "@/stores/sessionStore"

const posthogApiKey = import.meta.env.VITE_POSTHOG_API_KEY

declare module "@vue/runtime-core" {
  //Bind to `this` keyword
  interface ComponentCustomProperties {
    $posthog: PostHog | void
  }
}

export const posthogPlugin: Plugin = {
  install(app: App) {
    const { env } = useSessionStore()
    if ((env as unknown) === "environment" && posthogApiKey)
      app.config.globalProperties.$posthog = posthog.init(posthogApiKey, {
        api_host: "https://eu.i.posthog.com",
        disable_session_recording: true,
        mask_all_text: false,
        mask_all_element_attributes: false,
      })
  },
}
