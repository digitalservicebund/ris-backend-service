import vue from "@vitejs/plugin-vue"
import { defineConfig } from "vite"

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],

  // [vuestic-ui] Add alias for ~normalize.css.
  resolve: {
    alias: [{ find: /^~(.*)$/, replacement: "$1" }],
  },
})
