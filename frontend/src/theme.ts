// This file is for making customizations to RIS UI specific to Caselaw. Ideally,
// these customizations don't remain here indefinitely, but will me transfered
// to RIS UI eventually.

import { RisUiTheme } from "@digitalservicebund/ris-ui/primevue"
import { usePassThrough } from "primevue/passthrough"
import type { TabPassThroughOptions } from "primevue/tab"
import type { TabListPassThroughOptions } from "primevue/tablist"
import type { TabPanelPassThroughOptions } from "primevue/tabpanel"

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
    const base = tw`h-64 px-24`
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
    class: tw`flex gap-4 relative`,
  },
}

export default usePassThrough(
  RisUiTheme,
  {
    tab,
    tabPanel,
    tabList,
  },
  { mergeProps: false, mergeSections: true },
)
