import { Court } from "@/domain/documentUnit"
import service from "@/services/comboboxItemService"
import httpClient from "@/services/httpClient"
import { ComboboxItem } from "@/shared/components/input/types"

vi.mock("@/services/httpClient")

describe("comboboxItemService", () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it("should fetch document type from lookup table", async () => {
    const doctype = {
      jurisShortcut: "AO",
      label: "Anordnung",
    }

    const httpClientGet = vi
      .mocked(httpClient)
      .get.mockResolvedValueOnce({ status: 200, data: [doctype] })

    const result = (await service.getDocumentTypes()).data as ComboboxItem[]

    expect(httpClientGet).toHaveBeenCalledOnce()
    expect(result[0].label).toEqual("Anordnung")
    expect(result[0].value).toEqual(doctype)
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

    const result = (await service.getCourts()).data as ComboboxItem[]

    expect(httpClientGet).toHaveBeenCalledOnce()
    expect(result[0].label).toEqual("BGH Karlsruhe")
    expect(result[0].value).toEqual(court)
  })

  it("should return local items if no filter", async () => {
    const result = (
      await service.filterItems([
        {
          label: "testItem1",
          value: {
            type: "courttype1",
            location: "courtlocation1",
            label: "courtlabel1",
          },
        },
        {
          label: "testItem2",
          value: {
            type: "courttype1",
            location: "courtlocation1",
            label: "courtlabel1",
          },
        },
        {
          label: "testItem3",
          value: {
            type: "courttype1",
            location: "courtlocation1",
            label: "courtlabel1",
          },
        },
      ])()
    ).data as ComboboxItem[]

    expect(result.length).toEqual(3)
    expect(result[1]).toEqual({
      label: "testItem2",
      value: {
        type: "courttype1",
        location: "courtlocation1",
        label: "courtlabel1",
      },
    })
  })

  it("should filter local items", async () => {
    const result = (
      await service.filterItems([
        {
          label: "testItem1",
          value: {
            type: "courttype1",
            location: "courtlocation1",
            label: "courtlabel1",
          },
        },
        {
          label: "testItem2",
          value: {
            type: "courttype1",
            location: "courtlocation1",
            label: "courtlabel1",
          },
        },
        {
          label: "testItem3",
          value: {
            type: "courttype1",
            location: "courtlocation1",
            label: "courtlabel1",
          },
        },
      ])("Item3")
    ).data as ComboboxItem[]

    expect(result.length).toEqual(1)
    expect(result[0]).toEqual({
      label: "testItem3",
      value: {
        type: "courttype1",
        location: "courtlocation1",
        label: "courtlabel1",
      },
    })
  })
})
