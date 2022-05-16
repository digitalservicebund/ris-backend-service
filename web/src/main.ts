import {createApp} from "vue"
import {VuesticPlugin} from "vuestic-ui"
import globalConfig from './services/vuestic-ui/global-config'
import App from "./App.vue"
import "./services/vuestic-ui/overrides.css"
import "./index.css"

createApp(App)
    .use(VuesticPlugin, globalConfig)
    .mount("#app")
