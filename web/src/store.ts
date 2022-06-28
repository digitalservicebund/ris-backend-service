import { defineStore } from "pinia"
import {
  deleteDocUnit,
  fetchAllDocUnits,
  fetchDocUnitByDocumentnumber,
} from "./api/docUnitService"
import { buildEmptyDocUnit, DocUnit } from "./types/DocUnit"

type State = {
  docUnits: Map<string, DocUnit>
  selected: DocUnit | null
}

export const useDocUnitsStore = defineStore("docUnitsStore", {
  state: (): State => ({
    docUnits: new Map<string, DocUnit>(), // key: uuid
    selected: null,
  }),
  actions: {
    fetchAll() {
      fetchAllDocUnits().then((all) => {
        if (!all) return
        for (const docUnit of all) {
          this.docUnits.set(docUnit.uuid, docUnit)
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
      this.docUnits.set(docUnit.uuid, docUnit)
    },
    remove(docUnit: DocUnit) {
      const uuid = docUnit.uuid
      deleteDocUnit(uuid)
      if (this.selected && this.selected.uuid === uuid) {
        this.selected = null
      }
      this.docUnits.delete(uuid)
    },
    clearSelected() {
      this.selected = null
    },
    setSelected(documentnumber: string | string[]) {
      if (Array.isArray(documentnumber)) return
      const docUnitOptional = [...this.docUnits.values()].filter(
        (du) => du.documentnumber === documentnumber
      )
      // if (docUnitOptional.length > 1) {} // error case, should we do something?
      if (docUnitOptional.length === 1) {
        this.selected = docUnitOptional[0]
        return
      }
      fetchDocUnitByDocumentnumber(documentnumber).then((du) => {
        this.add(du)
        this.selected = du
      })
    },
    update(docUnit: DocUnit) {
      this.docUnits.set(docUnit.uuid, docUnit)
      // the docUnit object here can get completely replaced, that's why we need to update selected too
      if (this.selected && this.selected.uuid === docUnit.uuid) {
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
      // remove this workaround TODO
      if (this.selected === null) {
        // we should never end up in here because in the <template>s I check for hasSelected() before accessing selected
        return buildEmptyDocUnit()
      }
      return this.selected
    },
    selectedHasFileAttached(): boolean {
      if (!this.selected) return false
      return this.selected.s3path !== null
    },
    setHTMLOnSelected(htmlStr: string) {
      if (!this.selected) return
      this.selected.originalFileAsHTML = htmlStr
    },
  },
})
