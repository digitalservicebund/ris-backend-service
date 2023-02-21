import { mount } from "@vue/test-utils"
import TextButton from "@/components/TextButton.vue"

describe("TextButton", () => {
  it("renders with label prop", () => {
    const wrapper = mount(TextButton, {
      props: {
        label: "foo",
      },
    })
    expect(wrapper.props().label).toBe("foo")
  })

  it("renders a button element per default", () => {
    const wrapper = mount(TextButton)

    expect(wrapper.find("button").exists()).toBeTruthy()
  })

  it("renders an anchor element if the hypertext reference property is set", () => {
    const wrapper = mount(TextButton, {
      props: { href: "https://test.org" },
    })

    expect(wrapper.find("a").exists()).toBeTruthy()
    expect(wrapper.find("button").exists()).toBeFalsy()
  })
})
