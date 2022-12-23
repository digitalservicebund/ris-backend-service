import { Subscript } from "@tiptap/extension-subscript"
import { Superscript } from "@tiptap/extension-superscript"

export const CustomSuperscript = Superscript.extend({
  priority: 10000,
})
export const CustomSubscript = Subscript.extend({
  priority: 10000,
})
