import { createPinia, setActivePinia } from "pinia"
import { Decision } from "@/domain/decision"
import { RisJsonPatch } from "@/domain/risJsonPatch"
import errorMessages from "@/i18n/errors.json"
import documentUnitService from "@/services/documentUnitService"
import { ServiceResponse } from "@/services/httpClient"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { generateMatch } from "~/test-helper/text-check-service-mock"

vi.mock("@/services/documentUnitService")

describe("useDocumentUnitStore", () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })
  afterEach(() => {
    vi.resetAllMocks()
  })

  describe("remove TextCheck tags from given text", () => {
    it("removes text-check tags from text actual", async () => {
      // given
      const mockDocumentUnit = new Decision("123", { version: 1 })
      const mockOriginalDocumentUnit = new Decision("123", { version: 1 })
      mockOriginalDocumentUnit.longTexts = {
        tenor: "<p>Dies ist ein Beispielfall für Textprüfungs-Tags.</p>",
        participatingJudges: [],
      }
      mockDocumentUnit.longTexts = {
        tenor:
          "<p>Dies ist ein <text-check id='1'>Beispielfall</text-check> für Textprüfungs-Tags.</p>",
        participatingJudges: [],
      }

      const expectedResponse: ServiceResponse<RisJsonPatch> = {
        status: 200,
        data: {
          documentationUnitVersion: 1,
          patch: [],
          errorPaths: [],
        },
        error: undefined,
      }

      const documentUnitServiceUpdateMock = vi
        .spyOn(documentUnitService, "update")
        .mockResolvedValueOnce(expectedResponse)

      const store = useDocumentUnitStore()

      store.originalDocumentUnit = mockOriginalDocumentUnit
      store.documentUnit = mockDocumentUnit

      // when
      const response = await store.updateDocumentUnit()

      // then
      expect(documentUnitServiceUpdateMock).toHaveBeenCalledWith(
        store.documentUnit!.uuid,
        expect.objectContaining({ patch: [] }),
      )
      expect(response).toEqual(expectedResponse)
      expect(store.documentUnit?.version).toBe(1)
    })
  })

  describe("loadDocumentUnit", () => {
    it("loads a document unit successfully", async () => {
      const mockDocumentUnit = new Decision("123", { version: 1 })
      const serviceResponse: ServiceResponse<Decision> = {
        status: 200,
        data: mockDocumentUnit,
        error: undefined,
      }

      const documentUnitServiceMock = vi
        .spyOn(documentUnitService, "getByDocumentNumber")
        .mockResolvedValueOnce(serviceResponse)

      const store = useDocumentUnitStore()
      const response = await store.loadDocumentUnit("123")

      expect(documentUnitServiceMock).toHaveBeenCalledOnce()
      expect(response).toEqual(serviceResponse)
      expect(store.documentUnit).toEqual(mockDocumentUnit)
    })

    it("loads a document unit resets the matches", async () => {
      const mockDocumentUnit = new Decision("123", { version: 1 })
      const serviceResponse: ServiceResponse<Decision> = {
        status: 200,
        data: mockDocumentUnit,
        error: undefined,
      }

      const documentUnitServiceMock = vi
        .spyOn(documentUnitService, "getByDocumentNumber")
        .mockResolvedValueOnce(serviceResponse)

      const store = useDocumentUnitStore()
      store.matches = new Map([["tenor", [generateMatch(3)]]])
      expect(store.matches.size).toBe(1)

      const response = await store.loadDocumentUnit("123")

      expect(documentUnitServiceMock).toHaveBeenCalledOnce()
      expect(response).toEqual(serviceResponse)
      expect(store.matches.size, "matches map should be cleared").toBe(0)
    })

    it("unloads a document unit resets the matches", async () => {
      const store = useDocumentUnitStore()
      store.matches = new Map([["tenor", [generateMatch(3)]]])
      expect(store.matches.size).toBe(1)

      await store.unloadDocumentUnit()

      expect(store.matches.size, "matches map should be cleared").toBe(0)
    })

    it("handles failure to load a document unit", async () => {
      const serviceResponse: ServiceResponse<Decision> = {
        status: 200,
        data: undefined,
        error: errorMessages.DOCUMENT_UNIT_COULD_NOT_BE_LOADED,
      }

      const documentUnitServiceMock = vi
        .spyOn(documentUnitService, "getByDocumentNumber")
        .mockResolvedValueOnce(serviceResponse)

      const store = useDocumentUnitStore()
      const response = await store.loadDocumentUnit("123")

      expect(documentUnitServiceMock).toHaveBeenCalledOnce()
      expect(response).toEqual(serviceResponse)
      expect(store.documentUnit).toBeUndefined()
    })
  })

  describe("updateDocumentUnit", () => {
    it("updates a document unit successfully", async () => {
      const mockedLoadResponse: ServiceResponse<Decision> = {
        status: 200,
        data: new Decision("123", { version: 0 }),
        error: undefined,
      }

      const mockedUpdateResponse: ServiceResponse<RisJsonPatch> = {
        status: 200,
        data: {
          documentationUnitVersion: 1,
          patch: [],
          errorPaths: [],
        },
        error: undefined,
      }

      const documentUnitServiceLoadMock = vi
        .spyOn(documentUnitService, "getByDocumentNumber")
        .mockResolvedValueOnce(mockedLoadResponse)

      const documentUnitServiceUpdateMock = vi
        .spyOn(documentUnitService, "update")
        .mockResolvedValueOnce(mockedUpdateResponse)

      const store = useDocumentUnitStore()

      //load docunit to fill "originalDocumentUnit"
      await store.loadDocumentUnit("123")
      expect(documentUnitServiceLoadMock).toHaveBeenCalledOnce()

      store.documentUnit = new Decision("123", {
        version: 0,
        coreData: {
          fileNumbers: ["123"],
        },
      })

      const response = await store.updateDocumentUnit()

      expect(documentUnitServiceUpdateMock).toHaveBeenCalledOnce()
      expect(response).toEqual(mockedUpdateResponse)
      expect(store.documentUnit?.version).toBe(1)
    })

    it("updates a document unit with newer version of updated path", async () => {
      const mockedLoadResponse: ServiceResponse<Decision> = {
        status: 200,
        data: new Decision("123", { version: 0 }),
        error: undefined,
      }

      const mockedUpdateResponse: ServiceResponse<RisJsonPatch> = {
        status: 200,
        data: {
          documentationUnitVersion: 1,
          patch: [{ op: "add", path: "/coreData/ecli", value: "123" }],
          errorPaths: ["/coreData/ecli"],
        },
        error: undefined,
      }

      const documentUnitServiceLoadMock = vi
        .spyOn(documentUnitService, "getByDocumentNumber")
        .mockResolvedValueOnce(mockedLoadResponse)

      const documentUnitServiceUpdateMock = vi
        .spyOn(documentUnitService, "update")
        .mockResolvedValueOnce(mockedUpdateResponse)

      const store = useDocumentUnitStore()

      //load docunit to fill "originalDocumentUnit"
      await store.loadDocumentUnit("123")
      expect(documentUnitServiceLoadMock).toHaveBeenCalledOnce()

      store.documentUnit = new Decision("123", {
        version: 0,
        coreData: {
          ecli: "321",
        },
      })

      const response = await store.updateDocumentUnit()

      expect(documentUnitServiceUpdateMock).toHaveBeenCalledOnce()
      expect(response).toEqual({
        status: 207,
        data: undefined,
        error: {
          title: "ECLI",
        },
      })
      expect(store.documentUnit?.version).toBe(1)
      // Since last saving, someone else edited this input and this newer version overrides the local docunit
      expect(store.documentUnit?.coreData?.ecli).toBe("123")
    })

    it("handles no changes in document unit", async () => {
      const mockedLoadResponse: ServiceResponse<Decision> = {
        status: 200,
        data: new Decision("123", { version: 0, documentNumber: "abc" }),
        error: undefined,
      }

      const mockedUpdateResponse: ServiceResponse<RisJsonPatch> = {
        status: 200,
        data: {
          documentationUnitVersion: 1,
          patch: [],
          errorPaths: [],
        },
        error: undefined,
      }

      const documentUnitServiceLoadMock = vi
        .spyOn(documentUnitService, "getByDocumentNumber")
        .mockResolvedValue(mockedLoadResponse)

      const documentUnitServiceUpdateMock = vi
        .spyOn(documentUnitService, "update")
        .mockResolvedValueOnce(mockedUpdateResponse)

      const store = useDocumentUnitStore()

      //load docunit to fill "originalDocumentUnit"
      await store.loadDocumentUnit("123")

      await store.updateDocumentUnit()

      expect(
        documentUnitServiceUpdateMock,
        "doc unit update should be called once during update with no changes",
      ).toHaveBeenCalledTimes(1)

      expect(
        documentUnitServiceLoadMock,
        "doc unit load should be called once to load document unit",
      ).toHaveBeenCalledTimes(1)

      expect(
        store.documentUnit?.version,
        "version needs to be bumped after update",
      ).toBe(1)
    })

    it("handles update failure when document unit is not loaded", async () => {
      const store = useDocumentUnitStore()
      const response = await store.updateDocumentUnit()

      expect(response.status).toBe(404)
      expect(response.error).toBe(
        errorMessages.DOCUMENT_UNIT_COULD_NOT_BE_LOADED,
      )
    })

    it("handles update failure when document unit is not yet loaded", async () => {
      const store = useDocumentUnitStore()
      const response = await store.updateDocumentUnit()

      expect(response.status).toBe(404)
      expect(response.error).toBe(
        errorMessages.DOCUMENT_UNIT_COULD_NOT_BE_LOADED,
      )
    })

    it("update not successful", async () => {
      const mockedLoadResponse: ServiceResponse<Decision> = {
        status: 200,
        data: new Decision("123", { version: 0, documentNumber: "abc" }),
        error: undefined,
      }

      const mockedUpdateResponse: ServiceResponse<RisJsonPatch> = {
        status: 404,
        error: errorMessages.DOCUMENT_UNIT_UPDATE_FAILED,
      }

      vi.spyOn(
        documentUnitService,
        "getByDocumentNumber",
      ).mockResolvedValueOnce(mockedLoadResponse)

      vi.spyOn(documentUnitService, "update").mockResolvedValueOnce(
        mockedUpdateResponse,
      )

      const store = useDocumentUnitStore()

      // load docunit to fill "originalDocumentUnit"
      await store.loadDocumentUnit("123")

      // update docunit in frontend
      store.documentUnit = new Decision("123", {
        version: 0,
        documentNumber: "abc",
        coreData: {
          fileNumbers: ["123"],
        },
      })

      const response = await store.updateDocumentUnit()

      expect(response.status).toBe(404)
      expect(response.error).toBe(errorMessages.DOCUMENT_UNIT_UPDATE_FAILED)
    })
  })
})
