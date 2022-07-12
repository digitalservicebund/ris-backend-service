import { createTestingPinia } from "@pinia/testing"
import { mount } from "@vue/test-utils"
import { describe, it, expect } from "vitest"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import FileUpload from "../../src/components/FileUpload.vue"

describe("FileUpload", () => {
  const vuetify = createVuetify({ components, directives })

  it("renders properly", () => {
    const wrapper = mount(FileUpload, {
      props: {
        docUnitUuid: "1",
      },
      global: {
        stubs: ["router-link"],
        plugins: [vuetify, createTestingPinia()],
      },
    })

    expect(wrapper.text()).toContain("Aktuell")

    // TODO
  })
})
