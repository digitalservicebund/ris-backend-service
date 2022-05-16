import {COLOR_THEMES} from "./themes";
import {
    colorBackgroundButtonPrimaryDefault, colorTextButtonPrimaryDefault
} from "@digitalservice4germany/style-dictionary";

export default {
    ...COLOR_THEMES,
    components: {
        VaButton: {
            color: colorBackgroundButtonPrimaryDefault,
            textColor: colorTextButtonPrimaryDefault,
            rounded: false,
            round: false,
            flat: false,
        },
    },
}
