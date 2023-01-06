import path from "path"
import vue from "@vitejs/plugin-vue"
import { defineConfig } from "vite"
import EnvironmentPlugin from "vite-plugin-environment"
import Pages from "vite-plugin-pages"

// https://vitejs.dev/config/
export default defineConfig({
  server: {
    port: 3000, // Required for vite to be accessible when running as part of docker compose setup
  },
  plugins: [
    vue(),
    Pages({
      dirs: "src/routes",
    }),
    EnvironmentPlugin(["BACKEND_HOST"]),
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
