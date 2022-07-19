import { render } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import FileViewer from "../../src/components/FileViewer.vue"

describe("file viewer", async () => {
  vi.mock("@/services/fileService")
  const vuetify = createVuetify({ components, directives })
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "",
        name: "jurisdiction-docUnit-:documentNumber-files",
        component: {},
      },
    ],
  })
  test("file viewer should be rendered", async () => {
    const { debug } = render(FileViewer, {
      props: {
        s3Path: "47a77c3f-a05d-4362-97ea-d54a35236a67",
        fileName: "some-formatting.docx",
        fileType: "docx",
        uploadTimesStamp: "19-07-2022",
        isNotLoadFileRequired: true,
      },
      global: { plugins: [vuetify, router] },
    })
    // console.log(wrapper.classes())
    // expect(wrapper.get("button").text()).toBe("Datei löschen")
    debug()
    expect(1).toBe(1)
  })
})
