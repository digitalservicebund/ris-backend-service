import { mount } from "@vue/test-utils"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import TextButton from "@/components/TextButton.vue"

describe("TextButton", () => {
  const vuetify = createVuetify({ components, directives })

  it("renders with label prop", () => {
    const wrapper = mount(TextButton, {
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
    const wrapper = mount(TextButton, {
      global: {
        plugins: [vuetify],
      },
    })
    expect(wrapper.props().label).toBe("Speichern")
  })
})
