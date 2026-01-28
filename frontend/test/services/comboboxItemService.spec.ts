import { waitFor } from "@testing-library/vue"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import { ref } from "vue"
import { CollectiveAgreementIndustry } from "@/domain/collectiveAgreementIndustry"
import { Court } from "@/domain/court"
import { DocumentType } from "@/domain/documentType"
import { DocumentTypeCategory } from "@/domain/documentTypeCategory"
import { LanguageCode } from "@/domain/foreignLanguageVersion"
import { CurrencyCode } from "@/domain/objectValue"
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
const currencyCode: CurrencyCode = { id: "id", label: "Euro (EUR)" }
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
  http.get("/api/v1/caselaw/currencycodes", () => {
    return HttpResponse.json([currencyCode])
  }),
)

describe("comboboxItemService", () => {
  beforeAll(() => server.listen())
  afterAll(() => server.close())
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it("should fetch caselaw document type from lookup table", async () => {
    const {
      useFetch: { data, execute },
    } = service.getCaselawDocumentTypes(ref())

    await execute()
    await waitFor(() => {
      expect(data.value?.[0]).toEqual(doctype)
    })
  })

  it("should format caselaw document type", async () => {
    const { format } = service.getCaselawDocumentTypes(ref())
    expect(format(doctype).label).toEqual("Anordnung")
  })

  it("should fetch pending proceeding document type from lookup table", async () => {
    const {
      useFetch: { data, execute },
    } = service.getCaselawAndPendingProceedingDocumentTypes(ref("Anh"))

    await execute()
    await waitFor(() => {
      expect(data.value?.[0]).toEqual(pendingProceedingDoctype)
    })
  })

  it("should format pending proceeding document type", async () => {
    const { format } =
      service.getCaselawAndPendingProceedingDocumentTypes(ref())
    expect(format(pendingProceedingDoctype).label).toEqual(
      "Anhängiges Verfahren",
    )
  })

  it("should fetch dependent literature document type from lookup table", async () => {
    const {
      useFetch: { data, execute },
    } = service.getDependentLiteratureDocumentTypes(ref())

    await execute()
    await waitFor(() => {
      expect(data.value?.[0]).toEqual(dependentLiteratureDoctype)
    })
  })

  it("should format dependent literature document type", async () => {
    const { format } = service.getDependentLiteratureDocumentTypes(ref())
    expect(format(dependentLiteratureDoctype).label).toEqual("Aufsatz")
  })

  it("should fetch court from lookup table", async () => {
    const {
      useFetch: { data, execute },
    } = service.getCourts(ref())

    await execute()
    await waitFor(() => {
      expect(data.value?.[0]).toEqual(court)
    })
  })

  it("should format court", async () => {
    const { format } = service.getCourts(ref())
    expect(format(court).label).toEqual("BGH Karlsruhe")
  })

  it("should fetch risAbbreviations from lookup table", async () => {
    const {
      useFetch: { data, execute },
    } = service.getRisAbbreviations(ref())

    await execute()
    await waitFor(() => {
      expect(data.value?.[0]).toEqual(normAbbreviation)
    })
  })

  it("should format risAbbreviations", async () => {
    const { format } = service.getRisAbbreviations(ref())
    expect(format(normAbbreviation).label).toEqual("BGB")
  })

  it("should fetch language code from lookup table", async () => {
    const {
      useFetch: { data, execute },
    } = service.getLanguageCodes(ref())

    await execute()
    await waitFor(() => {
      expect(data.value?.[0]).toEqual(languageCode)
    })
  })

  it("should format language code", async () => {
    const { format } = service.getLanguageCodes(ref())
    expect(format(languageCode).label).toEqual("Englisch")
  })

  it("should fetch currency code from lookup table", async () => {
    const {
      useFetch: { data, execute },
    } = service.getCurrencyCodes(ref())

    await execute()
    await waitFor(() => {
      expect(data.value?.[0]).toEqual(currencyCode)
    })
  })

  it("should format currency code", async () => {
    const { format } = service.getCurrencyCodes(ref())
    expect(format(currencyCode).label).toEqual("Euro (EUR)")
  })

  it("should fetch collective agreement industries from lookup table", async () => {
    const {
      useFetch: { data, execute },
    } = service.getCollectiveAgreementIndustries(ref())

    await execute()
    await waitFor(() => {
      expect(data.value?.[0]).toEqual(collectiveAgreementIndustry)
    })
  })

  it("should format collective agreement industries", async () => {
    const { format } = service.getCollectiveAgreementIndustries(ref())
    expect(format(collectiveAgreementIndustry).label).toEqual(
      "Bühne, Theater, Orchester",
    )
  })
})
