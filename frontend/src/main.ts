import { plugin as unleashPlugin } from "@unleash/proxy-client-vue"
import { createPinia } from "pinia"
import { createApp } from "vue"
import "@/styles/global.scss"
import App from "./App.vue"
import router from "./router"

const storeManager = createPinia()

const unleashConfig = {
  url: process.env.UNLEASH_PROXY_URL,
  clientKey: process.env.UNLEASH_CLIENT_KEY,
  refreshInterval: 15,
  appName: "unleash-proxy",
}

const app = createApp(App).use(router).use(storeManager)

if (unleashConfig.url && unleashConfig.clientKey) {
  app.use(unleashPlugin, { config: unleashConfig })
}

app.mount("#app")
