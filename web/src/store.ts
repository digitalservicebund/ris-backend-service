import { defineStore } from "pinia"
import { getAllDocUnits } from "./api"
import { DocUnit } from "./types/DocUnit"

export const useDocUnitsStore = defineStore("docUnitsStore", {
  state: () => {
    return {
      docUnits: <DocUnit[]>[],
    }
  },
  actions: {
    fetchAll() {
      getAllDocUnits().then((all) => {
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
  },
})
