import { waitFor } from "@testing-library/vue"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import { ref } from "vue"
import { DocumentType } from "@/domain/documentType"
import { Court } from "@/domain/documentUnit"
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
const server = setupServer(
  http.get("/api/v1/caselaw/courts", () => HttpResponse.json([court])),
  http.get("/api/v1/caselaw/documenttypes", () => HttpResponse.json([doctype])),
)

describe("comboboxItemService", () => {
  beforeAll(() => server.listen())
  afterAll(() => server.close())
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it("should fetch document type from lookup table", async () => {
    const { data, execute } = service.getCaselawDocumentTypes(ref())

    await execute()
    await waitFor(() => {
      expect(data.value?.[0].label).toEqual("Anordnung")
      expect(data.value?.[0].value).toEqual(doctype)
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
})
