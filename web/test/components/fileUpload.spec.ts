import { render } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import FileUpload from "../../src/components/FileUpload.vue"

describe("FileUpload", () => {
  const vuetify = createVuetify({ components, directives })
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "",
        name: "jurisdiction-docUnit-:documentNumber-categories#coreData",
        component: {},
      },
    ],
  })

  test("renders file upload with default props", async () => {
    const { getByText } = render(FileUpload, {
      props: {
        docUnitUuid: "1",
      },
      global: { plugins: [vuetify, router] },
    })

    getByText("Aktuell", { exact: false })
  })

  // await fireEvent.click(getByText('Click me'))
})
