import { createApp } from "vue"
import { VuesticPluginsWithoutComponents } from "vuestic-ui"
import App from "./App.vue"

import "./index.css"

createApp(App).use(VuesticPluginsWithoutComponents).mount("#app")
