import * as Sentry from "@sentry/vue"
import { createPinia } from "pinia"
import { createApp } from "vue"
import "@/styles/global.scss"
import App from "./App.vue"
import router from "./router"

const storeManager = createPinia()

const app = createApp(App)

function envName(): string | undefined {
  switch (window.location.host) {
    case "ris.prod.ds4g.net":
      return "production"
    case "ris.dev.ds4g.net":
      return "staging"
    case "ris-uat.prod.ds4g.net":
      return "uat"
    default:
      return undefined
  }
}
if (import.meta.env.PROD) {
  Sentry.init({
    app,
    environment: envName(),
    dsn: "https://26a9485d49884fd1aaa8be1489916aa3@o1248831.ingest.sentry.io/4505600659619840",
    integrations: [
      new Sentry.BrowserTracing({
        // Set 'tracePropagationTargets' to control for which URLs distributed tracing should be enabled
        tracePropagationTargets: [
          "http://127.0.0.1/api",
          "https://ris.prod.ds4g.net/api",
          "https://ris.dev.ds4g.net/api",
          "https://ris-uat.prod.ds4g.net/api",
        ],
        routingInstrumentation: Sentry.vueRouterInstrumentation(router),
      }),
    ],
    // Performance Monitoring
    tracesSampleRate: 1.0, // Capture 100% of the transactions, reduce in production!
  })
}

app.use(router).use(storeManager).mount("#app")
