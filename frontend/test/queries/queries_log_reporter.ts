import type {
  FullConfig,
  Reporter,
  Suite,
  TestCase,
  TestResult,
} from "@playwright/test/reporter"
import playwrightConfig from "./../../playwright.config"

class QueriesReporter implements Reporter {
  private resultsWithDuration: { test: TestCase; result: TestResult }[] = []

  onBegin(_config: FullConfig, suite: Suite) {
    console.log(
      `Starting the run with ${this.getRunsWithoutSetup(suite)} database tests`,
    )
  }

  onTestEnd(test: TestCase, result: TestResult) {
    if (
      result.attachments.length &&
      result.attachments.find((attachment) => attachment.name == "durations")
    )
      this.resultsWithDuration.push({ test, result })
  }

  onEnd() {
    const stats = this.resultsWithDuration.map(({ test, result }) => {
      const durations = result.attachments
        .find((attachment) => attachment.name == "durations")
        ?.body?.toJSON().data

      return (
        durations && {
          title: test.title,
          average_duration:
            durations.reduce((a, b) => a + b, 0) / durations.length,
          max_duration: Math.max(...durations),
          min_duration: Math.min(...durations),
          runs: durations.length,
        }
      )
    })

    console.table(stats)
  }

  private getRunsWithoutSetup(suite: Suite): number {
    const setupFiles = playwrightConfig.projects?.find(
      (project) => project.name == "setup",
    )?.testMatch as RegExp

    return suite
      .allTests()
      .filter((test) => !setupFiles.test(test.location.file)).length
  }
}

export default QueriesReporter
