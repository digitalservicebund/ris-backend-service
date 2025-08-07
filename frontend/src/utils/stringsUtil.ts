export default class StringsUtil {
  public static isEmpty(value: string | undefined): boolean {
    return value == null || value.trim().length == 0
  }

  public static mergeNonBlankStrings(
    words: (string | undefined)[],
    separator: string = ", ",
  ): string {
    return words.filter((item) => !this.isEmpty(item)).join(separator)
  }

  public static readonly countWords = (text: string): number => {
    return text.trim().split(/\s+/).length
  }

  public static getFirstAndLastChars(string?: string): string {
    if (!string) return "-"

    const words = string.trim().toUpperCase().split(/\s+/).filter(Boolean)
    if (words.length === 0) return "-"
    if (words.length === 1) return words[0].charAt(0)
    return words[0].charAt(0) + words[words.length - 1].charAt(0)
  }
}
