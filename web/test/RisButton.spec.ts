import { mount } from "@vue/test-utils"
import { describe, it, expect } from "vitest"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import RisButton from "../src/components/RisButton.vue"

describe("RisButton", () => {
  const vuetify = createVuetify({ components, directives })

  it("renders properly", () => {
    const wrapper = mount(RisButton, {
      global: {
        plugins: [vuetify],
      },
      propsData: {
        label: "foo",
      },
    })
    expect(wrapper.props().label).toBe("foo")
  })
})
