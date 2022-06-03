import { mount } from "@vue/test-utils"
import { describe, it, expect } from "vitest"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import RisButton from "../RisButton.vue"

describe("RisButton", () => {
  const vuetify = createVuetify({ components, directives })

  it("renders with label prop", () => {
    const wrapper = mount(new RisButton(), {
      global: {
        plugins: [vuetify],
      },
      props: {
        label: "foo",
      },
    })
    expect(wrapper.props().label).toBe("foo")
  })

  it("renders with default label prop", () => {
    const wrapper = mount(new RisButton(), {
      global: {
        plugins: [vuetify],
      },
    })
    expect(wrapper.props().label).toBe("Speichern")
  })
})
