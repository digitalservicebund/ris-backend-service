import { defineStore } from "pinia"
import { fetchAllDocUnits, fetchDocUnitById } from "./api"
import { DocUnit } from "./types/DocUnit"

export const useDocUnitsStore = defineStore("docUnitsStore", {
  state: () => {
    return {
      docUnits: <DocUnit[]>[],
    }
  },
  actions: {
    fetchAll() {
      fetchAllDocUnits().then((all) => {
        if (all) {
          // can be undefined if endpoint is offline
          this.docUnits = all
        }
      })
    },
    getAll() {
      return this.docUnits
    },
    isEmpty() {
      return this.docUnits.length === 0
    },
    add(docUnit: DocUnit) {
      this.docUnits.push(docUnit)
    },
    getDocUnit(id: number): Promise<DocUnit> {
      const result = this.docUnits.filter((du) => du.id === id)
      if (result.length > 0) {
        console.log("DocUnit " + id + " was already present in the store")
        return Promise.resolve(result[0])
      }
      console.log(
        "DocUnit " + id + " wasn't present in the store and is getting fetched"
      )
      return fetchDocUnitById(id).then((du) => {
        this.add(du)
        return du
      })
    },
  },
})
