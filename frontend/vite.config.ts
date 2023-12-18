import path from "path"
import * as process from "process"
import { sentryVitePlugin } from "@sentry/vite-plugin"
import vue from "@vitejs/plugin-vue"
import Icons from "unplugin-icons/vite"
import { defineConfig } from "vite"
import EnvironmentPlugin from "vite-plugin-environment"
import Pages from "vite-plugin-pages"

// https://vitejs.dev/config/
export default defineConfig({
  build: {
    sourcemap: true, // Source map generation must be turned on
  },
  server: {
    port: 3000, // Required for vite to be accessible when running as part of docker compose setup
  },
  plugins: [
    vue(),
    Pages({
      dirs: "src/routes",
    }),
    EnvironmentPlugin({
      BACKEND_HOST: "",
    }),
    sentryVitePlugin({
      authToken: process.env.SENTRY_AUTH_TOKEN,
      org: "digitalservice",
      project: "ris-frontend",
      telemetry: process.env.VITEST !== "true",
    }),
    Icons({
      scale: 1.3333, // ~24px at the current default font size of 18px
    }),
  ],
  test: {
    setupFiles: ["test/setup.ts"],
    globals: true,
    environment: "jsdom",
    include: ["test/**/*.ts"],
    exclude: [
      "test/e2e/**/*.ts",
      "test/a11y/**/*.ts",
      "test/test-helper/**/*.ts",
      "test/setup.ts",
    ],
    coverage: {
      reporter: ["lcov"],
      exclude: [
        // Configuration and generated outputs
        "**/[.]**",
        "coverage/**/*",
        "dist/**/*",
        "**/.*rc.{?(c|m)js,yml}",
        "*.config.{js,ts}",

        // Types
        "**/*.d.ts",

        // Tests
        "test/**/*",

        // App content we're not interested in covering with unit tests
        "src/routes/**/*",
        "src/kitchensink/**/*",
        "src/App.vue",
        "src/main.ts",
      ],
    },
  },
  define: {
    "process.env": {},
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "src"),
      "~": path.resolve(__dirname, "test"),
    },
  },
})
