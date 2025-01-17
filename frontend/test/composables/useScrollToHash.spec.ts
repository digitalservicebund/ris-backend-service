import { nextTick, ref } from "vue"
import { useScrollToHash } from "@/composables/useScrollToHash"

describe("useScrollToHash", async () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it("scrolls element into view if element with hash exists", async () => {
    const hashElement = { getBoundingClientRect: vi.fn(() => ({ top: 100 })) }
    // @ts-expect-error It's not a proper HTML element but good enough for testing
    vi.spyOn(document, "getElementById").mockReturnValue(hashElement)

    const scrollToSpy = vi.fn()
    Object.defineProperty(global.window, "scrollTo", { value: scrollToSpy })

    useScrollToHash(ref("test-hash"))
    vi.runAllTimers()

    expect(document.getElementById).toHaveBeenCalledWith("test-hash")

    expect(scrollToSpy).toHaveBeenCalledWith({
      top: 100,
      behavior: "smooth",
    })
  })

  it("removes the # from the route hash", async () => {
    const hashElement = { getBoundingClientRect: vi.fn(() => ({ top: 100 })) }
    // @ts-expect-error It's not a proper HTML element but good enough for testing
    vi.spyOn(document, "getElementById").mockReturnValue(hashElement)

    useScrollToHash(ref("#test-hash"))
    vi.runAllTimers()

    expect(document.getElementById).toHaveBeenCalledWith("test-hash")
  })

  it("does not scroll if no element exists for this hash", async () => {
    const hashElement = { getBoundingClientRect: vi.fn() }
    vi.spyOn(document, "getElementById").mockReturnValue(null)

    const scrollToSpy = vi.fn()
    Object.defineProperty(global.window, "scrollTo", { value: scrollToSpy })

    useScrollToHash(ref("unknown-hash"))
    vi.runAllTimers()

    expect(document.getElementById).toHaveBeenCalledWith("unknown-hash")
    expect(hashElement.getBoundingClientRect).not.toHaveBeenCalledOnce()
  })

  it("does nothing if given route has no hash", async () => {
    vi.spyOn(document, "getElementById")

    useScrollToHash(ref(undefined))
    vi.runAllTimers()

    expect(document.getElementById).not.toHaveBeenCalledWith()
  })

  it("detects new hash on route changes", async () => {
    const hashElement = { getBoundingClientRect: vi.fn(() => ({ top: 100 })) }
    // @ts-expect-error It's not a proper HTML element but good enough for testing
    vi.spyOn(document, "getElementById").mockReturnValue(hashElement)
    const route = ref<string | undefined>(undefined)
    const scrollToSpy = vi.fn()
    Object.defineProperty(global.window, "scrollTo", { value: scrollToSpy })

    useScrollToHash(route)
    vi.runAllTimers()

    expect(document.getElementById).not.toHaveBeenCalled()
    expect(scrollToSpy).not.toHaveBeenCalled()

    route.value = "new"
    await nextTick()
    vi.runAllTimers()

    expect(document.getElementById).toHaveBeenCalled()
    expect(scrollToSpy).toHaveBeenCalled()
  })

  it("scrolls with offset", async () => {
    const hashElement = { getBoundingClientRect: vi.fn(() => ({ top: 100 })) }
    // @ts-expect-error It's not a proper HTML element but good enough for testing
    vi.spyOn(document, "getElementById").mockReturnValue(hashElement)

    const scrollToSpy = vi.fn()
    Object.defineProperty(global.window, "scrollTo", { value: scrollToSpy })

    useScrollToHash(ref("test-hash"), 50)
    vi.runAllTimers()

    expect(document.getElementById).toHaveBeenCalledWith("test-hash")

    expect(scrollToSpy).toHaveBeenCalledWith({
      top: 50,
      behavior: "smooth",
    })
  })
})
