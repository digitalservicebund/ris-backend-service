import vue from "@vitejs/plugin-vue"
import { defineConfig } from "vite"

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  build: {
    outDir: "../src/main/resources/static",
  },
  test: {
    globals: true,
    environment: "jsdom",
    include: ["test/**/*.ts"],
    exclude: ["test/e2e/**/*.ts", "test/a11y/**/*.ts"],
  },
})
