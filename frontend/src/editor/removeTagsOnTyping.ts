import { ResolvedPos, Node } from "prosemirror-model"
import { EditorState, Plugin, PluginKey, Transaction } from "prosemirror-state"
import { ReplaceStep } from "prosemirror-transform"
import { IgnoreOnceTagName } from "./ignoreOnceMark"
import { TextCheckTagName } from "@/types/textCheck"

const removeTagsOnTypingKey = new PluginKey("removeTagsOnTyping")

// Prüft, ob ein Node die TextCheck-Markierung enthält.
function hasTextCheckMark(node: Node, state: EditorState): boolean {
  if (!node?.isText) return false
  const textCheckMark = state.schema.marks[TextCheckTagName]
  return !!textCheckMark?.isInSet(node.marks)
}

/**
 * Führt eine einfache Wortgrenzen-Suche durch und bestimmt den betroffenen Textbereich im ALTEN Dokument.
 * Dies ist notwendig, um zu verhindern, dass bei jedem Tastendruck der gesamte Parent-Block (z.B. Absatz)
 * nach Tags gescannt werden muss, was die Performance negativ beeinflussen würde.
 *
 * Performs a simple word boundary search and identifies the affected text area in the OLD document.
 * This is necessary to prevent the entire parent block (e.g., paragraph) from having to be scanned for tags
 * with every keystroke, which would negatively impact performance.
 *
 * * @param $pos Die aufgelöste Position (ResolvedPos) im ALTEN Dokument.
 * @returns { start: number, end: number } Die absoluten Grenzen des betroffenen Textbereichs im alten Dokument.
 */
function findWordBoundaries($pos: ResolvedPos): {
  start: number
  end: number
} {
  // Starten an der aktuellen Position des Steps
  let wordStart = $pos.pos
  let wordEnd = $pos.pos

  // Wir suchen die Grenzen innerhalb des direkten Eltern-Blocks (z.B. Paragraph), da Markierungen
  // oft über das aktuelle Wort hinausgehen könnten.
  const parentStart = $pos.start() // Startposition des Eltern-Blocks
  const parentEnd = $pos.end() // Endposition des Eltern-Blocks

  // Vereinfachte Wortgrenzen-Suche: Wir suchen nach links und rechts nach Leerzeichen.
  // Das ist robuster als nur den unmittelbaren Textknoten zu betrachten.

  // Suche nach links (bis zum Leerzeichen oder Blockanfang)
  while (wordStart > parentStart) {
    // textBetween ist besser für die Zeichenprüfung
    const char = $pos.doc.textBetween(wordStart - 1, wordStart, " ")
    if (/\s/.test(char)) {
      break
    }
    wordStart--
  }

  // Suche nach rechts (bis zum Leerzeichen oder Blockende)
  while (wordEnd < parentEnd) {
    const char = $pos.doc.textBetween(wordEnd, wordEnd + 1, " ")
    if (/\s/.test(char)) {
      break
    }
    wordEnd++
  }

  return { start: wordStart, end: wordEnd }
}

const removeTagsOnTypingPlugin = new Plugin({
  key: removeTagsOnTypingKey,
  appendTransaction: (
    transactions: readonly Transaction[],
    oldState: EditorState,
    newState: EditorState,
  ) => {
    // Grundlegende Filterung: nur Änderungen eines einzelnen Textbereichs durch eine Tasten- oder Mauseingabe soll beachtet werden.
    // Das Plugin sollte komplexere Operationen überspringen.
    if (
      transactions.length !== 1 ||
      transactions[0].steps.length !== 1 ||
      !(transactions[0].steps[0] instanceof ReplaceStep)
    ) {
      return null
    }
    // DE: Eine Transaktion ist die logische Einheit einer Zustandsänderung im ProseMirror-Editor von oldState zu newState.
    // Sie kann eine oder mehrere atomare Änderungen umfassen, die der Benutzer oder ein Befehl als einen einzigen Vorgang betrachtet.
    // Zum Beispiel: Text mit fetter Formatierung kopieren und einfügen.
    // EN: A transaction is the logical unit of a state change in the ProseMirror editor from oldState to newState.
    // It can encompass one or more atomic changes that the user or a command considers as a single operation.
    // For example: copying and pasting text with bold formatting.
    const transaction = transactions[0]

    // DE: Ein Step ist die atomare Einheit einer Zustandsänderung. Er definiert die kleinstmögliche, unteilbare Änderung am Dokument.
    // Für das Beispiel mit fetter Formatierung:
    // - Step 1 (ReplaceStep): Fügt den reinen Text ein.
    // - Step 2 (AddMarkStep): Fügt die fette Markierung (Bold Mark) über den eingefügten Text hinzu
    // EN: A step is the atomic unit of a state change. It defines the smallest, indivisible change to the document.
    // FFor the example with bold formatting:
    // - Step 1 (ReplaceStep): Inserts the plain text.
    // - Step 2 (AddMarkStep): Adds the bold mark over the inserted text.
    const step = transaction.steps[0] as ReplaceStep

    // Filterung von History (Undo/Redo)
    if (transaction.getMeta("history$")) return null
    // Filterung von Cursorbewegungen und Klicks
    if (!transaction.docChanged) return null

    // Der Inhalt, der im oldState als geändert markiert wurde
    const deletedSize = step.to - step.from
    // Der Inhalt, der in den newState eingefügt wird
    const insertedSize = step.slice.size

    // Reines Löschen (z.B. Backspace): Hier wird Inhalt entfernt, und nichts wird eingefügt (slice ist leer): deletedSize > 0 & insertedSize = 0.
    // Pure deletion (e.g., Backspace): Here, content is removed, and nothing is inserted (slice is empty): deletedSize > 0 & insertedSize = 0.

    // Reines Tippen (Einfügen): Hier wird ein Bereich der Länge Null (step.from = step.to) entfernt und Inhalt eingefügt: deletedSize = 0 & insertedSize > 0.
    // Pure typing (insertion): Here, a zero-length range (step.from = step.to) is removed and content is inserted: deletedSize = 0 & insertedSize > 0.

    // Ersetzen (Selektion + Paste): Hier wird der markierte Bereich entfernt und der neue Inhalt eingefügt: deletedSize > 0 & insertedSize > 0
    // Replace (selection + paste): Here, the selected area is removed and the new content is inserted: deletedSize > 0 & insertedSize > 0

    // Nur fortfahren, wenn tatsächlich Inhalt hinzugefügt oder gelöscht wurde
    if (deletedSize === 0 && insertedSize === 0) return null

    // Case where a single space is deleted and with that two words are merged
    if (deletedSize === 1 && insertedSize === 0) {
      const deletedText = oldState.doc.textBetween(step.from, step.to, "")

      if (deletedText === " ") {
        return handleSpaceDeletion(newState, step)
      }
    }

    // Position im ALTEN Zustand auflösen (ResolvedPos)
    const $oldPos = oldState.doc.resolve(step.from)

    // Bestimmung des betroffenen Textknoten, basierend auf der Art der Änderung (deletedSize vs. insertedSize) und der Cursorposition ($oldPos.textOffset)
    let affectedNode: Node | null = null
    affectedNode = findAffectedNode(deletedSize, insertedSize, $oldPos)

    // Prüfung: Hat der betroffene Node überhaupt TextCheck-Tags?
    // Wir brechen ab, wenn der gefundene Knoten kein Textknoten ist oder keine Tags hat.
    if (
      !affectedNode ||
      !affectedNode.isText ||
      !hasTextCheckMark(affectedNode, oldState)
    ) {
      return null
    }

    // Bestimmung des zu bereinigenden Bereichs im ALTEN Zustand (Wortgrenzen)
    // Wir können nicht einfach die Start- und Endposition des affectedNode (affectedNode.nodeSize) nehmen, weil die Tags über die Grenzen
    // des einzelnen Textknotens hinausreichen, der bei der Bearbeitung entsteht.
    const { start: oldStart, end: oldEnd } = findWordBoundaries($oldPos)

    // Mappe die Positionen auf den NEUEN Zustand
    // Verwendet das Mapping-System von ProseMirror, um die im alten Dokument gefundenen Wortgrenzen (oldStart, oldEnd) in die korrekten
    // absoluten Positionen im neuen Dokument (newState.doc) zu übersetzen.
    // Die Methode .map macht folgendes:
    // - Sie nimmt eine Position aus dem alten Zustand (oldStart oder oldEnd).
    // - Sie durchläuft alle Steps in der Transaktion und deren Positionsverschiebungen.
    // - Sie gibt die korrespondierende Position im neuen Zustand zurück (newFrom oder newTo).
    const newFrom = transaction.mapping.map(oldStart)
    const newTo = transaction.mapping.map(oldEnd)

    let modified = false
    const newStateTransaction = newState.tr

    // Markierungen im NEUEN Zustand über den gemappten Bereich entfernen
    // Remove markers in the NEW state across the mapped area
    if (newFrom >= 0 && newTo <= newState.doc.content.size && newFrom < newTo) {
      newState.doc.nodesBetween(newFrom, newTo, (node, pos) => {
        // Nur Textknoten mit tatsächlichem Inhalt verarbeiten
        if (node && node.isText && node.text && node.text.trim() !== "") {
          // Entferne beide Markierungen
          newStateTransaction.removeMark(
            pos,
            pos + node.nodeSize,
            newState.schema.marks[TextCheckTagName],
          )
          newStateTransaction.removeMark(
            pos,
            pos + node.nodeSize,
            newState.schema.marks[IgnoreOnceTagName],
          )
          modified = true
        }
      })
    }

    return modified ? newStateTransaction : null
  },
})

function findAffectedNode(
  deletedSize: number,
  insertedSize: number,
  $oldPos: ResolvedPos,
): Node | null {
  let result: Node | null = null

  if (deletedSize > 0) {
    // Bei Löschung: Das ist fast immer der Knoten VOR der Position
    result = $oldPos.nodeBefore
  } else if (insertedSize > 0) {
    // Wenn Inhalt eingefügt wird (Tippen), bestimmen wir den betroffenen Textknoten (im oldState) wie folgt:
    // 1. $oldPos.textOffset > 0: Wir sind IN einem Textknoten (normales Tippen). Der Textknoten VOR der Position ($oldPos.nodeBefore) ist der sicherste Träger der ursprünglichen Tags.
    // 2. $oldPos.textOffset === 0: Wir sind ZWISCHEN zwei Knoten (z.B. nach einem Leerzeichen oder Inline-Element). ProseMirror versucht angrenzende (neue) Textknoten mit dem NACHFOLGENDEN Knoten ($oldPos.nodeAfter) denselben Markierungen automatisch zu verschmelzen oder ihn dort zu erzeugen, daher wird dieser geprüft.
    result = $oldPos.textOffset > 0 ? $oldPos.nodeBefore : $oldPos.nodeAfter
  }

  return result
}

function handleSpaceDeletion(
  newState: EditorState,
  step: ReplaceStep,
): Transaction | null {
  // Check if deletion resulted in word merging in the NEW state
  const $newPos = newState.doc.resolve(step.from)

  // Check characters before and after the deletion point in NEW state
  const charBefore =
    step.from > 0 ? newState.doc.textBetween(step.from - 1, step.from, " ") : ""
  const charAfter =
    step.from < newState.doc.content.size
      ? newState.doc.textBetween(step.from, step.from + 1, " ")
      : ""

  // If both sides have non-whitespace characters, words have been merged
  if (wordsMerged(charBefore, charAfter)) {
    // Find the full extent of both merged words
    const { start, end } = findWordBoundaries($newPos)

    // Remove marks from entire merged range
    let modified = false
    const tr = newState.tr

    if (start < end) {
      newState.doc.nodesBetween(start, end, (node, pos) => {
        if (node && node.isText && node.text && node.text.trim() !== "") {
          tr.removeMark(
            pos,
            pos + node.nodeSize,
            newState.schema.marks[TextCheckTagName],
          )
          tr.removeMark(
            pos,
            pos + node.nodeSize,
            newState.schema.marks[IgnoreOnceTagName],
          )
          modified = true
        }
      })
    }

    return modified ? tr : null
  }

  // If there's still a space between words, do nothing
  return null
}

function wordsMerged(charBefore: string, charAfter: string): boolean {
  return !!(
    charBefore &&
    !/\s/.test(charBefore) &&
    charAfter &&
    !/\s/.test(charAfter)
  )
}

export { removeTagsOnTypingPlugin }
