const ALPHABET_CHARACTERS = "abcdefghijklmnopqrstuvwxyz"

export function generateRandomNumber(minimum = 0, maximum = 10): number {
  return Math.floor(Math.random() * (maximum - minimum) + minimum)
}

export function generateString(options?: {
  characterSet?: string
  length?: number
  prefix?: string
}): string {
  const characterSet = options?.characterSet ?? ALPHABET_CHARACTERS
  const length = options?.length ?? 5
  let output = options?.prefix ?? ""

  for (let i = 0; i < length; i++) {
    output += characterSet.charAt(
      generateRandomNumber(0, characterSet.length - 1),
    )
  }

  return output
}
