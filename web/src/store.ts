import { defineStore } from "pinia"
import { fetchAllDocUnits, fetchDocUnitById } from "./api"
import { DocUnit } from "./types/DocUnit"

type State = {
  docUnits: Map<number, DocUnit>
  selected: DocUnit | null
}

export const useDocUnitsStore = defineStore("docUnitsStore", {
  state: (): State => ({
    docUnits: new Map<number, DocUnit>(),
    selected: null,
  }),
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
    getAndSetSelected(id: number): Promise<DocUnit> {
      const docUnit = this.docUnits.get(id)
      if (docUnit) {
        this.selected = docUnit
        return Promise.resolve(docUnit)
      }
      return fetchDocUnitById(id).then((du) => {
        this.add(du)
        this.selected = du
        return du
      })
    },
    update(docUnit: DocUnit) {
      this.docUnits.set(docUnit.id, docUnit)
    },
    getSelected(): DocUnit | null {
      return this.selected
    },
    hasSelected(): boolean {
      return this.selected !== null
    },
  },
})
