import { createApp } from "vue"
import { VuesticPlugin } from "vuestic-ui"
import App from "./App.vue"

import "./index.css"

createApp(App).use(VuesticPlugin).mount("#app")
