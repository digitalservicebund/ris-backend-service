import { mount } from "@vue/test-utils"
import { expect, test } from "vitest"
import App from "../src/App.vue"

test("App", async () => {
  expect(App).toBeTruthy()

  const wrapper = mount(App)

  expect(wrapper).toBeTruthy()
  expect(wrapper.text()).toContain("Hello DigitalService")
})
