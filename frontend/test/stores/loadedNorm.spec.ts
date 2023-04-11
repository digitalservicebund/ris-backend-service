import { flushPromises } from "@vue/test-utils"
import { setActivePinia, createPinia } from "pinia"
import { editNormFrame, getNormByGuid } from "@/services/norms"
import { useLoadedNormStore } from "@/stores/loadedNorm"
import {
  generateFlatMetadata,
  generateMetadataSections,
  generateNorm,
} from "~/test-helper/dataGenerators"

vi.mock("@/services/norms/operations")

describe("loadedNorm", () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  afterEach(() => {
    flushPromises()
  })

  it("calls the norms service to load a norm", async () => {
    const norm = generateNorm()
    const response = { status: 200, data: norm }
    vi.mocked(getNormByGuid).mockResolvedValue(response)
    const store = useLoadedNormStore()

    await store.load("guid")

    expect(getNormByGuid).toHaveBeenCalledOnce()
    expect(getNormByGuid).toHaveBeenLastCalledWith("guid")
    expect(store.loadedNorm).toEqual(norm)
  })

  it("first unset the loaded norm before loading the next one", async () => {
    vi.mocked(getNormByGuid).mockImplementation(
      (guid: string) => new Promise(() => guid)
    ) // Never resolves, so loading takes for ever.
    const store = useLoadedNormStore()
    store.loadedNorm = generateNorm()

    store.load("other-guid")
  })

  it("keeps the loaded norm to be undefined if service fails", async () => {
    const response = { status: 404, error: { title: "No norm found" } }
    vi.mocked(getNormByGuid).mockResolvedValue(response)
    const store = useLoadedNormStore()

    await store.load("guid")

    expect(store.loadedNorm).toBeUndefined()
  })

  it("does not call the norms service on update if no norm is loaded", async () => {
    const store = useLoadedNormStore()
    store.loadedNorm = undefined

    await store.update()

    expect(editNormFrame).not.toHaveBeenCalled()
  })

  it("calls the norms service on update with the frame data of the loaded norm", async () => {
    const metadataSections = generateMetadataSections()
    const flatMetadata = generateFlatMetadata()
    const norm = generateNorm({
      guid: "guid",
      metadataSections,
      ...flatMetadata,
    })
    const store = useLoadedNormStore()
    store.loadedNorm = norm

    await store.update()

    expect(editNormFrame).toHaveBeenCalledOnce()
    expect(editNormFrame).toHaveBeenLastCalledWith(
      "guid",
      metadataSections,
      flatMetadata
    )
  })
})
