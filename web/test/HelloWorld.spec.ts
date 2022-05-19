import { mount } from "@vue/test-utils"
import { describe, it, expect, vitest } from "vitest"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import HelloWorld from "../src/components/HelloWorld.vue"

function useResizeObserverMock() {
  return {
    width: 1000,
  }
}

vitest.mock("use-resize-observer", () => useResizeObserverMock)

describe("HelloWorld", () => {
  const vuetify = createVuetify({ components, directives })

  it("renders properly", () => {
    const wrapper = mount(HelloWorld, {
      global: {
        plugins: [vuetify],
      },
    })
    expect(wrapper.text()).toContain("Hello DigitalService")
  })
})
