import { reactive } from "vue"
import { getAllDocUnits } from "./api"
import { DocUnit } from "./types/DocUnit"

// TODO wire up with local storage
export const store = reactive({
  docUnits: <DocUnit[]>[],

  getAllDocUnits() {
    getAllDocUnits().then((_docUnits) => {
      this.docUnits = _docUnits
    })
  },

  addDocUnit(docUnit: DocUnit) {
    this.docUnits.push(docUnit)
  },

  getDocUnits() {
    return this.docUnits
  },

  hasDocUnits() {
    return this.docUnits.length > 0
  },
})
