// This file is for making customizations to RIS UI specific to Caselaw. Ideally,
// these customizations don't remain here indefinitely, but will me transfered
// to RIS UI eventually.

import { RisUiTheme } from "@digitalservicebund/ris-ui/primevue"
import { usePassThrough } from "primevue/passthrough"
import type { TabPassThroughOptions } from "primevue/tab"
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
    const base = tw`ris-body1-regular h-51 w-full py-12 [&:not(:last-child)]:mr-4`
    const active = tw`bg-blue-200 text-black ris-body2-bold`
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

export default usePassThrough(
  RisUiTheme,
  {
    tab,
    tabPanel,
  },
  { mergeProps: false, mergeSections: true },
)
