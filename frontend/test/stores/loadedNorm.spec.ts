import { flushPromises } from "@vue/test-utils"
import { setActivePinia, createPinia } from "pinia"
import { editNormFrame, getNormByGuid } from "@/services/normsService"
import { useLoadedNormStore } from "@/stores/loadedNorm"
import { generateNorm } from "~/test-helper/dataGenerators"

vi.mock("@/services/normsService")

describe("loadedNorm", () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  afterEach(() => {
    flushPromises()
  })

  it("has no norm loaded per default", () => {
    const store = useLoadedNormStore()

    expect(store.loadedNorm).toBeUndefined()
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

    expect(store.loadedNorm).toBeUndefined()
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
    const norm = generateNorm({
      guid: "test guid",
      officialLongTitle: "test long title",
      officialShortTitle: "test official short title",
      officialAbbreviation: "test official abbreviation",
      referenceNumber: "test reference number",
      publicationDate: "test publication date",
      announcementDate: "test announcement date",
      citationDate: "test citation date",
      frameKeywords: "test frame keywords",
      providerEntity: "test author entity",
      providerDecidingBody: "test author deciding body",
      providerIsResolutionMajority: true,
      leadJurisdiction: "test lead jurisdiction",
      leadUnit: "test lead unit",
      participationType: "test participation type",
      participationInstitution: "test participation institution",
      documentTypeName: "test document type name",
      documentNormCategory: "test document norm category",
      documentTemplateName: "test document template name",
      subjectFna: "test subject fna",
      subjectPreviousFna: "test subject previous fna",
      subjectGesta: "test subject gesta",
      subjectBgb3: "test subject bgb3",
    })
    const store = useLoadedNormStore()
    store.loadedNorm = norm

    await store.update()

    expect(editNormFrame).toHaveBeenCalledOnce()
    const { guid, articles, ...frameData } = norm
    expect(editNormFrame).toHaveBeenLastCalledWith(guid, frameData)
  })
})
