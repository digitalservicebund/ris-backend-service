import DocUnit from "../domain/docUnit"
import api from "./api"

export default {
  async getAll(): Promise<DocUnit[]> {
    try {
      const response = await api().get("docunits")
      return response.data as DocUnit[]
    } catch (error) {
      throw new Error(`Cloud not load all docUnits: ${error}`)
    }
  },

  async getById(id: string): Promise<DocUnit> {
    try {
      const response = await api().get(`docunits/${id}`)
      return new DocUnit(response.data.id, response.data)
    } catch (error) {
      throw new Error(`Could not load docUnit: ${error}`)
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
        `docunits/${docUnit.id}/docx`,
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
      throw new Error(`Could not update docUnit: ${error}`)
    }
  },

  async delete(docUnitId: string): Promise<number> {
    try {
      const response = await api().delete(`docunits/${docUnitId}`)
      return response.status
    } catch (error) {
      throw new Error("Could not delete docUnit")
    }
  },
}
