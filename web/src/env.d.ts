/// <reference types="vite/client" />

interface EnvironmentVariables {
  readonly VITE_BACKEND_HOST: string
}

interface ImportMeta {
  readonly env: EnvironmentVariables
}
