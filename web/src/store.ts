import { defineStore } from "pinia"
import { fetchAllDocUnits, fetchDocUnitById } from "./api"
import { buildEmptyDocUnit, DocUnit } from "./types/DocUnit"

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
    removeById(id: number) {
      if (this.selected && this.selected.id === id) {
        this.selected = null
      }
      this.docUnits.delete(id)
    },
    clearSelected() {
      this.selected = null
    },
    setSelected(idStr: string | string[]) {
      if (Array.isArray(idStr)) return
      const id = Number(idStr)
      if (!Number.isInteger(Number(id))) return
      const docUnit = this.docUnits.get(id)
      if (docUnit) {
        this.selected = docUnit
        return
      }
      fetchDocUnitById(id).then((du) => {
        this.add(du)
        this.selected = du
      })
    },
    update(docUnit: DocUnit) {
      this.docUnits.set(docUnit.id, docUnit)
      // the docUnit object here can get completely replaced, that's why we need to update selected too
      if (this.selected && this.selected.id === docUnit.id) {
        this.selected = docUnit
      }
    },
    hasSelected(): boolean {
      return this.selected !== null
    },
    getSelected(): DocUnit | null {
      return this.selected
    },
    getSelectedSafe(): DocUnit {
      // is this an ok workaround to avoid null as a possible return value?
      if (this.selected === null) {
        // we should never end up in here because in the <template>s I check for hasSelected() before accessing selected
        return buildEmptyDocUnit()
      }
      return this.selected
    },
    hasFileAttached(id: number): boolean {
      return this.docUnits.has(id) && this.docUnits.get(id)?.s3path !== null
    },
    selectedHasFileAttached(): boolean {
      if (!this.selected) return false
      return this.selected.s3path !== null
    },
  },
})
