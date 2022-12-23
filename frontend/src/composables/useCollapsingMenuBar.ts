import { computed, Ref } from "vue"

function setIsLast(buttonList: MenuButton[]) {
  for (let i = 1; i < buttonList.length; i++) {
    if (buttonList[i].group !== buttonList[i - 1].group) {
      buttonList[i - 1].isLast = true
    }
  }
}

function flattenList(array: MenuButton[]) {
  let result: MenuButton[] = []
  array.forEach(function (a) {
    if (a.type !== "menu") result.push(a)
    if (a.childButtons instanceof Array) {
      result = result.concat(flattenList(a.childButtons))
    }
  })
  return result
}

export interface MenuButton {
  type: string
  icon: string
  ariaLabel: string
  group?: string
  childButtons?: MenuButton[]
  isCollapsable?: boolean
  isLast?: boolean
  isActive?: boolean
  isSecondRow?: boolean
  callback?: () => void
}

export function useCollapsingMenuBar(
  buttons: Ref<MenuButton[]>,
  maxBarEntries: Ref<number>
) {
  const collapsedButtons = computed(() => {
    let buttonList = new Array(...buttons.value)
    while (buttonList.length > maxBarEntries.value) {
      const collapsableButtonsWithGroup = buttonList.filter(
        (button) => button.group != undefined && button.isCollapsable === true
      )
      if (collapsableButtonsWithGroup.length == 0) {
        const flatButtonList = flattenList(buttonList)
        const secondRow = flatButtonList.filter((button) => button.isSecondRow)
        const moreButton = {
          type: "more",
          icon: "more_horiz",
          ariaLabel: "more",
          isCollapsable: false,
          childButtons: secondRow,
        }
        buttonList = flatButtonList.filter(
          (button) => !secondRow.includes(button)
        )
        buttonList.push(moreButton)
        return buttonList
      }

      const lastGroupName =
        collapsableButtonsWithGroup[collapsableButtonsWithGroup.length - 1]
          .group

      const buttonsOfGroup = buttonList.filter(
        (button) => button.group == lastGroupName
      )

      const menuButtonIndex = buttonList.indexOf(buttonsOfGroup[0])

      buttonList = buttonList.filter(
        (button) => !buttonsOfGroup.includes(button)
      )
      const menuButton = {
        type: "menu",
        icon: buttonsOfGroup[0].icon,
        ariaLabel: "menu",
        group: buttonsOfGroup[0].group,
        isCollapsable: false,
        childButtons: buttonsOfGroup,
      }

      buttonList.splice(menuButtonIndex, 0, menuButton)
    }
    setIsLast(buttonList)
    return buttonList
  })
  return { collapsedButtons }
}
