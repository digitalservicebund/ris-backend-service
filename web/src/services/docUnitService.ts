import DocUnit from "../domain/docUnit"
import api from "./api"
import { UpdateStatus } from "@/enum/enumUpdateStatus"

export default {
  async getAll(): Promise<DocUnit[]> {
    try {
      const response = await api().get("docunits")
      return response.data as DocUnit[]
    } catch (error) {
      throw new Error(`Cloud not load all docUnits: ${error}`)
    }
  },

  async getByDocumentNumber(documentNumber: string): Promise<DocUnit> {
    try {
      const response = await api().get(`docunits/${documentNumber}`)
      return new DocUnit(response.data.id, response.data)
    } catch (error) {
      throw new Error(`Could not load docUnit by documentNumber: ${error}`)
    }
  },

  async createNew(docCenter: string, docType: string): Promise<DocUnit> {
    try {
      const response = await api().post(
        "docunits",
        JSON.stringify({
          documentationCenterAbbreviation: docCenter,
          documentType: docType,
        }),
        {
          headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
          },
        }
      )
      return new DocUnit(response.data.id, response.data)
    } catch (error) {
      throw new Error(`Could not create new docUnit: ${error}`)
    }
  },

  async update(docUnit: DocUnit): Promise<number> {
    try {
      const response = await api().put(
        `docunits/${docUnit.uuid}/docx`,
        JSON.stringify(docUnit),
        {
          headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
          },
        }
      )
      return response.status
    } catch (error) {
      return UpdateStatus.ERROR
    }
  },

  async delete(docUnitUuid: string): Promise<number> {
    try {
      const response = await api().delete(`docunits/${docUnitUuid}`)
      return response.status
    } catch (error) {
      throw new Error("Could not delete docUnit")
    }
  },
}
