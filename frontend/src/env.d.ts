/// <reference types="vite/client" />

interface EnvironmentVariables {
  readonly VITE_BACKEND_HOST: string
  readonly VITE_POSTHOG_API_KEY: string
}

interface ImportMeta {
  readonly env: EnvironmentVariables
}
