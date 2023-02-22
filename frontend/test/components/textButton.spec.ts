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

  it("it sets per default no download attribute on anchors", () => {
    const wrapper = mount(TextButton, {
      props: { download: undefined, href: "https://test.org" },
    })
    const anchor = wrapper.get("a")

    expect(anchor.attributes("download")).toBeUndefined()
  })

  it("it can set the download attribute flag if property is flagged", () => {
    const wrapper = mount(TextButton, {
      props: { download: true, href: "https://test.org" },
    })
    const anchor = wrapper.get("a")

    expect(anchor.attributes("download")).toBeDefined()
  })

  it("it can set the download attribute value if property is defined", () => {
    const wrapper = mount(TextButton, {
      props: { download: "file-name.ext", href: "https://test.org" },
    })
    const anchor = wrapper.get("a")

    expect(anchor.attributes("download")).toBe("file-name.ext")
  })

  it("it sets per default no anchor target attribute to use the browser default", () => {
    const wrapper = mount(TextButton, {
      props: { target: undefined, href: "https://test.org" },
    })
    const anchor = wrapper.get("a")

    expect(anchor.attributes("target")).toBeUndefined()
  })

  it.each(["_self", "_blank", "_parent", "_top"])(
    "it can set the anchor target to %i",
    (target) => {
      const wrapper = mount(TextButton, {
        props: {
          target: target as "_self" | "_blank" | "_parent" | "_top",
          href: "https://test.org",
        },
      })
      const anchor = wrapper.get("a")

      expect(anchor.attributes("target")).toBe(target)
    }
  )
})
