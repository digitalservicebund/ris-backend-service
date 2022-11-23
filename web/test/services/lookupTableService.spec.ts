import { LookupTableEndpoint } from "@/domain"
import { Court } from "@/domain/documentUnit"
import { DocumentType } from "@/domain/lookupTables"
import service from "@/services/dropdownItemService"
import httpClient from "@/services/httpClient"

vi.mock("@/services/httpClient")

describe("dropdownItemService", () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it("should fetch document type from lookup table", async () => {
    const doctype: DocumentType = {
      id: 1,
      jurisShortcut: "AO",
      label: "Anordnung",
    }

    const httpClientGet = vi
      .mocked(httpClient)
      .get.mockResolvedValueOnce({ status: 200, data: [doctype] })

    const result = await service.fetch(LookupTableEndpoint.documentTypes)

    expect(httpClientGet).toHaveBeenCalledOnce()
    expect(result[0].text).toEqual("AO - Anordnung")
    expect(result[0].value).toEqual("Anordnung")
  })

  it("should fetch court from lookup table", async () => {
    const court: Court = {
      type: "BGH",
      location: "Karlsruhe",
      label: "BGH Karlsruhe",
    }

    const httpClientGet = vi
      .mocked(httpClient)
      .get.mockResolvedValueOnce({ status: 200, data: [court] })

    const result = await service.fetch(LookupTableEndpoint.courts)

    expect(httpClientGet).toHaveBeenCalledOnce()
    expect(result[0].text).toEqual("BGH Karlsruhe")
    expect(result[0].value).toEqual(court)
  })
})
