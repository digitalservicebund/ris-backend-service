import "@testing-library/jest-dom"
import { config } from "@vue/test-utils"
import failOnConsole from "jest-fail-on-console"
import PrimeVue from "primevue/config"
import InputText from "primevue/inputtext"
import { vi } from "vitest"
import { useResizeObserverMock } from "./test-helper/useResizeObserverMock"

// Enable PrimeVue plugin because we need that in many tests
config.global.plugins = [PrimeVue]

// Globally replace `InputMask` with `InputText` for all tests
config.global.stubs = {
  InputMask: InputText,
}

// window.matchMedia API not available in vitest but needed by PrimeVue Components (select)
window.matchMedia =
  window.matchMedia ||
  function () {
    return {
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
    }
  }

failOnConsole({
  shouldFailOnAssert: true,
  shouldFailOnDebug: true,
  shouldFailOnError: true,
  shouldFailOnInfo: true,
  shouldFailOnLog: true,
  shouldFailOnWarn: true,
})

useResizeObserverMock()
