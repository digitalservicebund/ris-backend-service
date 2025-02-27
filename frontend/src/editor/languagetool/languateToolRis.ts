import { Editor, Extension } from "@tiptap/core"
import { ServiceResponse } from "@/services/httpClient"
import languageToolService from "@/services/languageToolService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { TextCheckCategoryResponse } from "@/types/languagetool"

declare module "@tiptap/core" {
  interface Commands<ReturnType> {
    languagetool2: {
      textCheck: () => ReturnType
    }
  }
}

const checkCategory = async (editor: Editor, category?: string) => {
  const store = useDocumentUnitStore()

  if (store.documentUnit?.uuid == undefined) {
    return
  }

  const languageToolCheckResponse: ServiceResponse<TextCheckCategoryResponse> =
    await languageToolService.checkCategory(store.documentUnit?.uuid, category)

  if (languageToolCheckResponse.status == 200) {
    editor.commands.setContent(languageToolCheckResponse.data!.htmlText)
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
          void checkCategory(editor, this.options.category)
          return true
        },
    }
  },
})
