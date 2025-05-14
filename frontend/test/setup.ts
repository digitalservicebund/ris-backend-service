import "@testing-library/jest-dom"
import { config } from "@vue/test-utils"
import failOnConsole from "jest-fail-on-console"
import PrimeVue from "primevue/config"
import { vi } from "vitest"
import { useResizeObserverMock } from "./test-helper/useResizeObserverMock"
import { useBubbleMenuMock } from "~/test-helper/useBubbleMenuMock"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"

// Enable PrimeVue plugin because we need that in many tests
config.global.plugins = [PrimeVue]

process.env.TZ = "Europe/Berlin"

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
useFeatureToggleServiceMock()
useBubbleMenuMock()
useResizeObserverMock()
