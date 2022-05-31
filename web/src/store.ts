import { defineStore } from "pinia"
import { fetchAllDocUnits, fetchDocUnitById } from "./api"
import { DocUnit } from "./types/DocUnit"

export const useDocUnitsStore = defineStore("docUnitsStore", {
  state: () => {
    return {
      docUnits: new Map<number, DocUnit>(),
    }
  },
  actions: {
    fetchAll() {
      fetchAllDocUnits().then((all) => {
        if (!all) return
        for (const docUnit of all) {
          this.docUnits.set(docUnit.id, docUnit)
        }
      })
    },
    getAll() {
      return this.docUnits.values()
    },
    isEmpty() {
      return this.docUnits.size === 0
    },
    add(docUnit: DocUnit) {
      this.docUnits.set(docUnit.id, docUnit)
    },
    getDocUnit(id: number): Promise<DocUnit> {
      const docUnit = this.docUnits.get(id)
      if (docUnit) {
        return Promise.resolve(docUnit)
      }
      return fetchDocUnitById(id).then((du) => {
        this.add(du)
        return du
      })
    },
    update(docUnit: DocUnit) {
      this.docUnits.set(docUnit.id, docUnit)
    },
  },
})
