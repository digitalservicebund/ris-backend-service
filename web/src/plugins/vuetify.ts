// Styles
import "@mdi/font/css/materialdesignicons.css"
import "vuetify/styles"

// Vuetify
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"

export const risLightTheme = {
  dark: false,
  colors: {
    white: "#FFFFFF",
    black: "#000000",
    blue800: "#004B76",
    background: "#FFFFFF",
    surface: "#FFFFFF",
    primary: "#6200EE",
    secondary: "#03DAC6",
    error: "#B00020",
    info: "#2196F3",
    success: "#4CAF50",
    warning: "#FB8C00",
  },
}

export default createVuetify({
  components,
  directives,
  theme: {
    defaultTheme: "risLightTheme",
    themes: {
      risLightTheme,
    },
  },
})
