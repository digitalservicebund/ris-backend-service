/* -------------------------------------------------- *
 * Shared                                             *
 * -------------------------------------------------- */

type SanitizeRule = {
  pattern: RegExp | string
  replacement: string | null
}

/** Cleans up an input string according to a list of rules. */
function sanitize(input: string, rules: SanitizeRule[]): string {
  let output = input

  rules.forEach((rule) => {
    output = output.replace(rule.pattern, rule.replacement ?? "")
  })

  return output.trim()
}

/** Removes tags that indicate "Kuppelwörter" */
const kwReplacement: SanitizeRule = {
  pattern: /<kw\/>/gi,
  replacement: null,
}

/** Removes FNR tags */
const fnrReplacement: SanitizeRule = {
  pattern: /<FnR ID="[^"]*"\/>/g,
  replacement: null,
}

const bReplacement: SanitizeRule = {
  pattern: /<b\/>/gi,
  replacement: null,
}

const supReplacement: SanitizeRule = {
  pattern: /<sup>.*<\/sup>/gi,
  replacement: null,
}

/* -------------------------------------------------- *
 * Specific sanitizers                                *
 * -------------------------------------------------- */

export function sanitizeTableOfContentEntry(input: string): string {
  return sanitize(input, [
    kwReplacement,
    fnrReplacement,
    bReplacement,
    supReplacement,
    { pattern: /\s*<(br|BR)\/>\s*/g, replacement: " " },
  ])
}

export function sanitizeNormTitle(input: string): string {
  return sanitize(input, [
    kwReplacement,
    fnrReplacement,
    bReplacement,
    supReplacement,
    { pattern: /\s*<(br|BR)\/>\s*/g, replacement: "\n" },
  ])
}
