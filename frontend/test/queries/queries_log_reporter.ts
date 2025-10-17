import fs from "fs"
import type {
  FullConfig,
  FullResult,
  Reporter,
  Suite,
  TestCase,
  TestResult,
} from "@playwright/test/reporter"
import playwrightConfig from "./../../playwright.config"

class QueriesReporter implements Reporter {
  private readonly resultsWithDuration: {
    test: TestCase
    result: TestResult
  }[] = []
  private readonly failedTests: { test: TestCase; result: TestResult }[] = []

  onBegin(_config: FullConfig, suite: Suite) {
    console.log(
      `Starting the run with ${this.getRunsWithoutSetup(suite)} database tests`,
    )
  }

  onTestEnd(test: TestCase, result: TestResult) {
    if (["failed", "timedOut", "interrupted"].includes(result.status))
      this.failedTests.push({ test, result })
    else if (
      result.attachments.length &&
      result.attachments.find((attachment) => attachment.name == "durations")
    )
      this.resultsWithDuration.push({ test, result })
  }

  onEnd(result: FullResult) {
    const getStats = (test: TestCase, result: TestResult) => {
      const durations: number[] = JSON.parse(
        result.attachments
          .find((attachment) => attachment.name === "durations")
          ?.body?.toString() || "[]",
      )

      const maxDuration: number = result.attachments
        .find((attachment) => attachment.name == "maxDuration")
        ?.body?.toString() as unknown as number

      return (
        durations && {
          title: test.title,
          average_duration: Math.round(
            durations.reduce((a, b) => a + b, 0) / durations.length,
          ),
          expected: maxDuration,
          max_duration: Math.round(Math.max(...durations)),
          min_duration: Math.round(Math.min(...durations)),
          runs: durations.length,
        }
      )
    }

    if (result.status !== "passed") {
      console.error("\nThe following test cases failed: ")
      const failedStats = this.failedTests.map(({ test, result }) =>
        getStats(test, result),
      )
      console.table(failedStats)
    }

    const stats = this.resultsWithDuration.map(
      ({ test, result }) => getStats(test, result) || null,
    )
    fs.writeFileSync("test-results.json", JSON.stringify(stats, null, 2))
    console.table(stats)
  }

  private getRunsWithoutSetup(suite: Suite): number {
    const setupFiles = playwrightConfig.projects?.find((project) =>
      ["setup-chromium", "setup-firefox"].includes(project.name!),
    )?.testMatch as RegExp

    return suite
      .allTests()
      .filter((test) => !setupFiles.test(test.location.file)).length
  }
}

export default QueriesReporter
