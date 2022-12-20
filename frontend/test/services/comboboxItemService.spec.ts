import { Court } from "@/domain/documentUnit"
import { DropdownItem } from "@/domain/types"
import service from "@/services/comboboxItemService"
import httpClient from "@/services/httpClient"

vi.mock("@/services/httpClient")

describe("comboboxItemService", () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it("should fetch document type from lookup table", async () => {
    const doctype = {
      id: 1,
      jurisShortcut: "AO",
      label: "Anordnung",
    }

    const httpClientGet = vi
      .mocked(httpClient)
      .get.mockResolvedValueOnce({ status: 200, data: [doctype] })

    const result = (await service.getDocumentTypes()).data as DropdownItem[]

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

    const result = (await service.getCourts()).data as DropdownItem[]

    expect(httpClientGet).toHaveBeenCalledOnce()
    expect(result[0].text).toEqual("BGH Karlsruhe")
    expect(result[0].value).toEqual(court)
  })
})
