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
    const hashElement = { scrollIntoView: vi.fn() }
    // @ts-expect-error It's not a proper HTML element but good enough for testing
    vi.spyOn(document, "getElementById").mockReturnValue(hashElement)

    useScrollToHash(ref("test-hash"))
    vi.runAllTimers()

    expect(document.getElementById).toHaveBeenCalledWith("test-hash")
    expect(hashElement.scrollIntoView).toHaveBeenCalledOnce()
  })

  it("removes the # from the route hash", async () => {
    const hashElement = { scrollIntoView: vi.fn() }
    // @ts-expect-error It's not a proper HTML element but good enough for testing
    vi.spyOn(document, "getElementById").mockReturnValue(hashElement)

    useScrollToHash(ref("#test-hash"))
    vi.runAllTimers()

    expect(document.getElementById).toHaveBeenCalledWith("test-hash")
    expect(hashElement.scrollIntoView).toHaveBeenCalledOnce()
  })

  it("does not scroll if no element exists for this hash", async () => {
    const hashElement = { scrollIntoView: vi.fn() }
    vi.spyOn(document, "getElementById").mockReturnValue(null)

    useScrollToHash(ref("unknown-hash"))
    vi.runAllTimers()

    expect(document.getElementById).toHaveBeenCalledWith("unknown-hash")
    expect(hashElement.scrollIntoView).not.toHaveBeenCalledOnce()
  })

  it("does nothing if given route has no hash", async () => {
    vi.spyOn(document, "getElementById")

    useScrollToHash(ref(undefined))
    vi.runAllTimers()

    expect(document.getElementById).not.toHaveBeenCalledWith()
  })

  it("detects new hash on route changes", async () => {
    const hashElement = { scrollIntoView: vi.fn() }
    // @ts-expect-error It's not a proper HTML element but good enough for testing
    vi.spyOn(document, "getElementById").mockReturnValue(hashElement)
    const route = ref<string | undefined>(undefined)

    useScrollToHash(route)
    vi.runAllTimers()

    expect(document.getElementById).not.toHaveBeenCalled()
    expect(hashElement.scrollIntoView).not.toHaveBeenCalled()

    route.value = "yolo"
    await nextTick()
    vi.runAllTimers()

    expect(document.getElementById).toHaveBeenCalled()
    expect(hashElement.scrollIntoView).toHaveBeenCalled()
  })
})
