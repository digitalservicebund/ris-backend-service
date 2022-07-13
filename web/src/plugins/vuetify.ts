// Styles
import "material-design-icons-iconfont/dist/material-design-icons.css"
import "vuetify/styles"

// Vuetify
import { createVuetify } from "vuetify"
import { VBtn } from "vuetify/components"
import * as directives from "vuetify/directives"
import { aliases, md } from "vuetify/iconsets/md"

export const risLightTheme = {
  dark: false,
  colors: {
    white: "#FFFFFF",
    black: "#000000",
    blue800: "#004B76",
    blue700: "#336F91",
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
  components: {
    VBtn,
  },
  directives,
  theme: {
    defaultTheme: "risLightTheme",
    themes: {
      risLightTheme,
    },
  },
  icons: {
    defaultSet: "md",
    aliases,
    sets: {
      md,
    },
  },
})
