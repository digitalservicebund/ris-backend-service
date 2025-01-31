import { debounce } from "lodash"
import { Node as PMNode } from "prosemirror-model"
import { Transaction } from "prosemirror-state"
import { Decoration, DecorationSet, EditorView } from "prosemirror-view"
import { ServiceResponse } from "@/services/httpClient"
import languageToolService from "@/services/languageToolService"
import {
  LanguageToolHelpingWords,
  Match,
  TextCheckResponse,
  TextNodesWithPosition,
} from "@/types/languagetool"
import StringsUtil from "@/utils/stringsUtil"

/**
 * Taken from
 * https://github.com/sereneinserenade/tiptap-languagetool/blob/main/src/components/extensions/languagetool.ts
 *
 * MIT License

 * Copyright (c) 2022 Jeet Mandaliya

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
export default class LanguageTool {
  match: Match | undefined = undefined

  matchRange: { from: number; to: number } | undefined

  private editorView?: EditorView

  public decorationSet: DecorationSet

  public lastOriginalFrom = 0

  readonly batchWordsLimit = 500

  public textNodesWithPosition: TextNodesWithPosition[] = []

  public proofReadInitially = false

  public languageToolActive: boolean = true

  constructor(decorationSet: DecorationSet) {
    this.decorationSet = decorationSet
  }

  public setEditorView(editorView: EditorView) {
    this.editorView = editorView
  }

  public updateMatchAndRange = (
    m?: Match,
    range?: { from: number; to: number },
  ) => {
    this.match = m

    if (range) {
      this.matchRange = range
    } else {
      this.matchRange = undefined
    }

    if (this.editorView) {
      const tr = this.editorView.state.tr
      tr.setMeta(LanguageToolHelpingWords.MatchUpdatedTransactionName, true)
      tr.setMeta(
        LanguageToolHelpingWords.MatchRangeUpdatedTransactionName,
        true,
      )
      this.editorView.dispatch(tr)
    }
  }

  public getMatchAndSetDecorations = async (
    doc: PMNode,
    text: string,
    originalFrom: number,
  ) => {
    const languageToolCheckResponse: ServiceResponse<TextCheckResponse> =
      await languageToolService.check(text)

    const matches = languageToolCheckResponse.data?.matches || []

    this.transformMatchesToDecorationSet(matches, doc, text, originalFrom)
    setTimeout(this.addEventListenersToDecorations, 100)
  }

  private transformMatchesToDecorationSet(
    matches: Match[],
    doc: PMNode,
    text: string,
    originalFrom: number,
  ) {
    const decorations: Decoration[] = []

    for (const match of matches) {
      const docFrom = match.offset + originalFrom
      const docTo = docFrom + match.length

      decorations.push(this.gimmeDecoration(docFrom, docTo, match))
    }

    const decorationsToRemove = this.decorationSet.find(
      originalFrom,
      originalFrom + text.length,
    )

    this.decorationSet = this.decorationSet.remove(decorationsToRemove)

    this.decorationSet = this.decorationSet.add(doc, decorations)

    if (this.editorView)
      this.dispatch(
        this.editorView.state.tr.setMeta(
          LanguageToolHelpingWords.LanguageToolTransactionName,
          true,
        ),
      )
  }

  private gimmeDecoration = (from: number, to: number, match: Match) =>
    Decoration.inline(from, to, {
      class: `lt lt-${match.rule.issueType}`,
      nodeName: "span",
      match: JSON.stringify({ match, from, to }),
    })

  public addEventListenersToDecorations = () => {
    const decorations = document.querySelectorAll("span.lt")

    if (!decorations.length) return

    decorations.forEach((el) => {
      el.addEventListener("mouseover", this.debouncedMouseEventsListener)
      el.addEventListener("mouseenter", this.debouncedMouseEventsListener)
    })
  }

  public mouseEventsListener = (e: Event) => {
    if (!e.target) return

    const matchString = (e.target as HTMLSpanElement)
      .getAttribute("match")
      ?.trim()

    if (!matchString) {
      console.error("No match string provided", { matchString })
      return
    }

    const { match, from, to } = JSON.parse(matchString)

    if (matchString) this.updateMatchAndRange(match, { from, to })
    else this.updateMatchAndRange()
  }

  onNodeChanged = (doc: PMNode, text: string, originalFrom: number) => {
    if (originalFrom !== this.lastOriginalFrom)
      void this.debouncedGetMatchAndSetDecorations(doc, text, originalFrom)
    else void this.debouncedGetMatchAndSetDecorations(doc, text, originalFrom)

    this.lastOriginalFrom = originalFrom
  }

  public proofreadAndDecorateWholeDoc = async (doc: PMNode, nodePos = 0) => {
    this.textNodesWithPosition = []

    let index = 0
    doc?.descendants((node, pos) => {
      if (!node.isText) {
        index += 1
        return
      }

      const intermediateTextNodeWIthPos = {
        text: "",
        from: -1,
        to: -1,
      }

      if (this.textNodesWithPosition[index]) {
        intermediateTextNodeWIthPos.text =
          this.textNodesWithPosition[index].text + node.text
        intermediateTextNodeWIthPos.from =
          this.textNodesWithPosition[index].from + nodePos
        intermediateTextNodeWIthPos.to =
          intermediateTextNodeWIthPos.from +
          intermediateTextNodeWIthPos.text.length +
          nodePos
      } else {
        intermediateTextNodeWIthPos.text = node.text ? node.text : ""
        intermediateTextNodeWIthPos.from = pos + nodePos
        intermediateTextNodeWIthPos.to =
          pos +
          nodePos +
          (node?.text?.length == undefined ? 0 : node.text.length)
      }

      this.textNodesWithPosition[index] = intermediateTextNodeWIthPos
    })

    this.textNodesWithPosition = this.textNodesWithPosition.filter(Boolean)

    let finalText = ""

    const textChunks: { from: number; text: string }[] = []

    let upperFrom = nodePos
    let newDataSet = true

    let lastPos = 1 + nodePos

    for (const { text, from, to } of this.textNodesWithPosition) {
      if (!newDataSet) {
        upperFrom = from

        newDataSet = true
      } else {
        const diff = from - lastPos
        if (diff > 0) finalText += Array(diff + 1).join(" ")
      }

      lastPos = to

      finalText += text

      if (this.hasTooManyWords(finalText)) {
        const updatedFrom = textChunks.length ? upperFrom : upperFrom + 1

        textChunks.push({
          from: updatedFrom,
          text: finalText,
        })

        finalText = ""
        newDataSet = false
      }
    }

    textChunks.push({
      from: textChunks.length ? upperFrom : 1,
      text: finalText,
    })

    const requests = textChunks.map(({ text, from }) =>
      this.getMatchAndSetDecorations(doc, text, from),
    )

    if (this.editorView)
      this.dispatch(
        this.editorView.state.tr.setMeta(
          LanguageToolHelpingWords.LoadingTransactionName,
          true,
        ),
      )

    void Promise.all(requests).then(() => {
      if (this.editorView)
        this.dispatch(
          this.editorView.state.tr.setMeta(
            LanguageToolHelpingWords.LoadingTransactionName,
            false,
          ),
        )
    })

    this.proofReadInitially = true
  }

  public resetLanguageToolMatch = () => {
    this.match = undefined
    this.matchRange = undefined
  }

  public removeDecorationSet = (from: number, to: number) => {
    this.decorationSet.remove(this.decorationSet.find(from, to))
  }

  public dispatch(tr: Transaction) {
    if (!this.editorView) {
      return
    }
    this.editorView.dispatch(tr)
  }

  public hasTooManyWords = (text: string): boolean => {
    return StringsUtil.countWords(text) >= this.batchWordsLimit
  }

  public debouncedMouseEventsListener = debounce(
    this.mouseEventsListener.bind(this),
    0,
  )

  public debouncedProofreadAndDecorate = debounce(
    this.proofreadAndDecorateWholeDoc,
    500,
  )

  public debouncedGetMatchAndSetDecorations = debounce(
    this.getMatchAndSetDecorations,
    300,
  )
}
