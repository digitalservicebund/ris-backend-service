import { createTestingPinia } from "@pinia/testing"
import { mount } from "@vue/test-utils"
import { describe, it, expect } from "vitest"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import RisStammDaten from "../RisStammDaten.vue"

describe("RisStammDaten", () => {
  const vuetify = createVuetify({ components, directives })

  it("renders properly", () => {
    const wrapper = mount(RisStammDaten, {
      global: {
        plugins: [vuetify, createTestingPinia()],
      },
    })

    expect(wrapper.text()).toContain("Loading")

    // TODO
  })
})
