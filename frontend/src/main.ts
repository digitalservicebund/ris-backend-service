import { captureConsoleIntegration } from "@sentry/integrations"
import * as Sentry from "@sentry/vue"
import { createApp } from "vue"
import "@/styles/global.scss"
import App from "./App.vue"
import router from "./router"

const app = createApp(App)

function targets(): string[] {
  return [`${window.location.origin}/api`]
}

if (import.meta.env.PROD) {
  Sentry.init({
    app,
    environment: window.location.host,
    dsn: "https://26a9485d49884fd1aaa8be1489916aa3@o1248831.ingest.sentry.io/4505600659619840",
    integrations: [
      Sentry.browserTracingIntegration({
        // Set 'tracePropagationTargets' to control for which URLs distributed tracing should be enabled
        tracePropagationTargets: targets(),
        router,
      }),
      captureConsoleIntegration(),
    ],
    // Performance Monitoring
    tracesSampleRate: 0.1, // Capture 100% of the transactions, reduce in production!
    attachProps: true,
    logErrors: true,
  })
}

app.use(router).mount("#app")
