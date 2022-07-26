import path from "path"
import vue from "@vitejs/plugin-vue"
import vuetify from "@vuetify/vite-plugin"
import { defineConfig } from "vite"
import Pages from "vite-plugin-pages"

// https://vitejs.dev/config/
export default defineConfig({
  server: {
    port: 3000, // Required for vite to be accessible when running as part of docker compose setup
    proxy: {
      "^/api.*": {
        target: `http://${process.env.BACKEND_HOST || "localhost"}:8080`,
        prependPath: true,
        changeOrigin: true,
      },
    },
  },
  plugins: [
    vue(),
    // https://github.com/vuetifyjs/vuetify-loader/tree/next/packages/vite-plugin
    vuetify({
      autoImport: true,
    }),
    Pages({
      dirs: "src/routes",
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
    exclude: [
      "test/e2e/**/*.ts",
      "test/a11y/**/*.ts",
      "test/test-helper/**/*.ts",
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
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        // Make the variables defined in these files available to all components, without requiring an explicit
        // @import of the files themselves
        additionalData: `@import "@/styles/main.scss";`,
      },
    },
  },
})
