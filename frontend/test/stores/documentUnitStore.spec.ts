import { setActivePinia, createPinia } from "pinia"
import DocumentUnit from "@/domain/documentUnit"
import { RisJsonPatch } from "@/domain/risJsonPatch"
import errorMessages from "@/i18n/errors.json"
import documentUnitService from "@/services/documentUnitService"
import { ServiceResponse } from "@/services/httpClient"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

vi.mock("@/services/documentUnitService")

describe("useDocumentUnitStore", () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })
  afterEach(() => {
    vi.resetAllMocks()
  })

  describe("loadDocumentUnit", () => {
    it("loads a document unit successfully", async () => {
      const mockDocumentUnit = new DocumentUnit("123", { version: 1 })
      const serviceResponse: ServiceResponse<DocumentUnit> = {
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

    it("handles failure to load a document unit", async () => {
      const serviceResponse: ServiceResponse<DocumentUnit> = {
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
      const mockedLoadResponse: ServiceResponse<DocumentUnit> = {
        status: 200,
        data: new DocumentUnit("123", { version: 0 }),
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

      store.documentUnit = new DocumentUnit("123", {
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

    it("handles no changes in document unit", async () => {
      const mockedLoadResponse: ServiceResponse<DocumentUnit> = {
        status: 200,
        data: new DocumentUnit("123", { version: 0, documentNumber: "abc" }),
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
      expect(documentUnitServiceLoadMock).toHaveBeenCalledTimes(1)

      await store.updateDocumentUnit()

      //Becuase there are no changes, just load the docunit
      expect(documentUnitServiceLoadMock).toHaveBeenCalledTimes(2)
      expect(documentUnitServiceUpdateMock).not.toHaveBeenCalled()
      expect(store.documentUnit?.version).toBe(0)
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
      const mockedLoadResponse: ServiceResponse<DocumentUnit> = {
        status: 200,
        data: new DocumentUnit("123", { version: 0, documentNumber: "abc" }),
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
      store.documentUnit = new DocumentUnit("123", {
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
