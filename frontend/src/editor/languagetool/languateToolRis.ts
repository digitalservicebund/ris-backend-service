import { Editor, Extension } from "@tiptap/core"
import { ServiceResponse } from "@/services/httpClient"
import languageToolService from "@/services/languageToolService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { TextCheckResponse } from "@/types/languagetool"

declare module "@tiptap/core" {
  interface Commands<ReturnType> {
    languagetool2: {
      textCheck: () => ReturnType
    }
  }
}

const checkCategoryAndSetDecorations = async (
  editor: Editor,
  category?: string,
) => {
  const store = useDocumentUnitStore()

  if (store.documentUnit?.uuid == undefined) {
    return
  }

  const languageToolCheckResponse: ServiceResponse<TextCheckResponse> =
    await languageToolService.checkCategory(store.documentUnit?.uuid, category)

  const matches = languageToolCheckResponse.data?.matches || []

  for (const match of matches) {
    editor.commands.insertContentAt(
      match.htmlOffset,
      `<span class="lt lt-${match.rule.issueType}">`,
    )
  }
}

export type LanguageToolRisOptions = {
  category?: string
}

export const LanguageToolRis = Extension.create<LanguageToolRisOptions>({
  name: "languagetool2",

  addOptions() {
    return {
      category: undefined,
    }
  },

  addCommands() {
    return {
      textCheck:
        () =>
        ({ editor }) => {
          void checkCategoryAndSetDecorations(editor, this.options.category)
          return true
        },
    }
  },
})
