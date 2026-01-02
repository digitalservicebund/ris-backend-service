import "@testing-library/jest-dom"
import { config } from "@vue/test-utils"
import PrimeVue from "primevue/config"
import { vi } from "vitest"
import failOnConsole from "vitest-fail-on-console"
import { useResizeObserverMock } from "./test-helper/useResizeObserverMock"
import { onSearchShortcutDirective } from "@/utils/onSearchShortcutDirective"
import { useBubbleMenuMock } from "~/test-helper/useBubbleMenuMock"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"

// Enable PrimeVue plugin because we need that in many tests
config.global.plugins = [PrimeVue]

config.global.directives = {
  tooltip: {}, // Mock v-tooltip
  "ctrl-enter": onSearchShortcutDirective, // Mock v-ctrl-enter
}

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
