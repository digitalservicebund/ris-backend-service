import { describe, expect, vi } from "vitest"
import { provide as vueProvide, inject as vueInject, ref } from "vue"
import { CourtTypeInjectionKey } from "@/composables/useCourtType"

// Create a mocked provide/inject implementation that works outside of setup()
const provideData = new Map()
const provide: typeof vueProvide = (key, value) => {
  provideData.set(key, value)
}
const inject: typeof vueInject = (key, defaultValue = undefined) => {
  return provideData.get(key) ?? defaultValue
}

describe("useCourtType", () => {
  beforeEach(() => {
    vi.resetModules()
    vi.resetAllMocks()

    provideData.clear()

    vi.doMock("vue", async (importOriginal) => ({
      ...(await importOriginal<typeof import("vue")>()),
      provide,
      inject,
    }))
  })

  it("should provide court type value default injection key", async () => {
    const { useProvideCourtType, useInjectCourtType } = await import(
      "@/composables/useCourtType"
    )

    const courtTypeRef = ref("BAG")

    useProvideCourtType(courtTypeRef)
    const result = useInjectCourtType()

    expect(result.value).equals(courtTypeRef.value)
  })

  it("should provide court type value using custom injection key", async () => {
    const { useProvideCourtType, useInjectCourtType } = await import(
      "@/composables/useCourtType"
    )
    const courtTypeRef = ref("BAG")
    const testKey: CourtTypeInjectionKey = Symbol("Test key")

    useProvideCourtType(courtTypeRef, testKey)
    const result = useInjectCourtType(testKey)

    expect(result.value).equals(courtTypeRef.value)
  })
})
