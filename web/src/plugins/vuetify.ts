// Styles
import "@mdi/font/css/materialdesignicons.css"
import "vuetify/styles"

// Vuetify
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import { COLOR_THEMES } from "./themes"

export const risLightTheme = {
  dark: false,
  ...COLOR_THEMES,
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
