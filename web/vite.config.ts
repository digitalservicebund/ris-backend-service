import path from "path"
import vue from "@vitejs/plugin-vue"
import vuetify from "@vuetify/vite-plugin"
import { defineConfig } from "vite"

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    // https://github.com/vuetifyjs/vuetify-loader/tree/next/packages/vite-plugin
    vuetify({
      autoImport: true,
    }),
  ],
  build: {
    outDir: "../src/main/resources/static",
  },
  test: {
    setupFiles: "vuetify.config.ts",
    deps: {
      inline: ["vuetify"],
    },
    globals: true,
    environment: "jsdom",
    include: ["test/**/*.ts"],
    exclude: ["test/e2e/**/*.ts", "test/a11y/**/*.ts"],
  },
  define: { "process.env": {} },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "src"),
    },
  },
})
