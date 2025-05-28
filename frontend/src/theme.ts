// This file is for making customizations to RIS UI specific to Caselaw. Ideally,
// these customizations don't remain here indefinitely, but will me transfered
// to RIS UI eventually.

import { RisUiTheme } from "@digitalservicebund/ris-ui/primevue"
import { usePassThrough } from "primevue/passthrough"
import type { TabPassThroughOptions } from "primevue/tab"
import type { TabListPassThroughOptions } from "primevue/tablist"
import type { TabPanelPassThroughOptions } from "primevue/tabpanel"
import type { TooltipDirectivePassThroughOptions } from "primevue/tooltip"

const tag = (strings: TemplateStringsArray, ...values: unknown[]) =>
  String.raw({ raw: strings }, ...values)

const tw = tag

const tabPanel: TabPanelPassThroughOptions = {
  root: {
    class: tw`pt-0`,
  },
}

const tab: TabPassThroughOptions = {
  root: ({ context }) => {
    const base = tw`h-51 flex-1 py-12 `
    const active = tw`bg-blue-200 text-black ris-body1-bold`
    const inactive = tw`cursor-pointer text-gray-900 bg-blue-100 hover:underline`
    return {
      class: {
        [base]: true,
        [active]: context.active,
        [inactive]: !context.active,
      },
    }
  },
}

const tabList: TabListPassThroughOptions = {
  tabList: {
    class: tw`flex w-full gap-4 relative`,
  },
  activeBar: {
    class: tw`h-1 bottom-0 absolute transition-all duration-300`,
  },
}

// Tooltip config must be inside `directives.tooltip`
const directives = {
  tooltip: {
    root: () =>
      tw`ris-label3-regular absolute mt-4 z-20 w-max rounded bg-gray-900 px-8 py-4 text-center whitespace-pre-line text-white`,
    arrow: () => "p-tooltip-arrow",
    text: () => "p-tooltip-text",
  } satisfies TooltipDirectivePassThroughOptions,
}

export default usePassThrough(
  RisUiTheme,
  {
    tab,
    tabPanel,
    tabList,
    directives,
  },
  { mergeProps: false, mergeSections: true },
)
