import { createTestingPinia } from "@pinia/testing"
import { mount } from "@vue/test-utils"
import { describe, it, expect } from "vitest"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import UploadFile from "../UploadFile.vue"

describe("UploadFile", () => {
  const vuetify = createVuetify({ components, directives })

  it("renders properly", () => {
    const wrapper = mount(UploadFile, {
      global: {
        stubs: ["router-link"],
        plugins: [vuetify, createTestingPinia()],
      },
    })

    expect(wrapper.text()).toContain("Original Dokument")

    // TODO
  })
})
