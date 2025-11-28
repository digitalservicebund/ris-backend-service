import { waitFor } from "@testing-library/vue"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import { ref } from "vue"
import { CollectiveAgreementIndustry } from "@/domain/collectiveAgreementIndustry"
import { Court } from "@/domain/court"
import { DocumentType } from "@/domain/documentType"
import { DocumentTypeCategory } from "@/domain/documentTypeCategory"
import { LanguageCode } from "@/domain/foreignLanguageVersion"
import service from "@/services/comboboxItemService"

const court: Court = {
  type: "BGH",
  location: "Karlsruhe",
  label: "BGH Karlsruhe",
}
const doctype: DocumentType = {
  jurisShortcut: "AO",
  label: "Anordnung",
}

const pendingProceedingDoctype: DocumentType = {
  jurisShortcut: "Anh",
  label: "Anhängiges Verfahren",
}

const dependentLiteratureDoctype: DocumentType = {
  jurisShortcut: "Auf",
  label: "Aufsatz",
}
const normAbbreviation = { id: "id", abbreviation: "BGB" }
const languageCode: LanguageCode = { id: "id", label: "Englisch" }
const collectiveAgreementIndustry: CollectiveAgreementIndustry = {
  id: "290b39dc-9368-4d1c-9076-7f96e05cb575",
  label: "Bühne, Theater, Orchester",
}

const server = setupServer(
  http.get("/api/v1/caselaw/courts", () => HttpResponse.json([court])),
  http.get("/api/v1/caselaw/documenttypes", ({ request }) => {
    const url = new URL(request.url)
    const category = url.searchParams.get("category")

    if (category === DocumentTypeCategory.CASELAW_PENDING_PROCEEDING) {
      return HttpResponse.json([pendingProceedingDoctype])
    }
    if (category === DocumentTypeCategory.DEPENDENT_LITERATURE) {
      return HttpResponse.json([dependentLiteratureDoctype])
    }
    if (!category || category === DocumentTypeCategory.CASELAW) {
      return HttpResponse.json([doctype])
    }
    return HttpResponse.json([])
  }),
  http.get("/api/v1/caselaw/normabbreviation/search", () => {
    return HttpResponse.json([normAbbreviation])
  }),
  http.get("/api/v1/caselaw/languagecodes", () => {
    return HttpResponse.json([languageCode])
  }),
  http.get("/api/v1/caselaw/collective-agreement-industries", () => {
    return HttpResponse.json([collectiveAgreementIndustry])
  }),
)

describe("comboboxItemService", () => {
  beforeAll(() => server.listen())
  afterAll(() => server.close())
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it("should fetch caselaw document type from lookup table", async () => {
    const { data, execute } = service.getCaselawDocumentTypes(ref())

    await execute()
    await waitFor(() => {
      expect(data.value?.[0].label).toEqual("Anordnung")
      expect(data.value?.[0].value).toEqual(doctype)
    })
  })

  it("should fetch pending proceeding document type from lookup table", async () => {
    const { data, execute } =
      service.getCaselawAndPendingProceedingDocumentTypes(ref("Anh"))

    await execute()
    await waitFor(() => {
      expect(data.value?.[0].label).toEqual("Anhängiges Verfahren")
      expect(data.value?.[0].value).toEqual(pendingProceedingDoctype)
    })
  })

  it("should fetch dependent literature document type from lookup table", async () => {
    const { data, execute } = service.getDependentLiteratureDocumentTypes(ref())

    await execute()
    await waitFor(() => {
      expect(data.value?.[0].label).toEqual("Aufsatz")
      expect(data.value?.[0].value).toEqual(dependentLiteratureDoctype)
    })
  })

  it("should fetch court from lookup table", async () => {
    const { data, execute } = service.getCourts(ref())

    await execute()
    await waitFor(() => {
      expect(data.value?.[0].label).toEqual("BGH Karlsruhe")
      expect(data.value?.[0].value).toEqual(court)
    })
  })

  it("should fetch risAbbreviations from lookup table", async () => {
    const { data, execute } = service.getRisAbbreviations(ref())

    await execute()
    await waitFor(() => {
      expect(data.value?.[0].label).toEqual("BGB")
      expect(data.value?.[0].value).toEqual(normAbbreviation)
    })
  })

  it("should fetch language code from lookup table", async () => {
    const { data, execute } = service.getLanguageCodes(ref())

    await execute()
    await waitFor(() => {
      expect(data.value?.[0].label).toEqual("Englisch")
      expect(data.value?.[0].value).toEqual(languageCode)
    })
  })

  it("should fetch collective agreement industries from lookup table", async () => {
    const { data, execute } = service.getCollectiveAgreementIndustries(ref())

    await execute()
    await waitFor(() => {
      expect(data.value?.[0].label).toEqual("Bühne, Theater, Orchester")
      expect(data.value?.[0].value).toEqual(collectiveAgreementIndustry)
    })
  })
})
