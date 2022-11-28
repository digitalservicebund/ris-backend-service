#!/bin/bash

runTalisman() {
  currentbranch=$(git rev-parse --abbrev-ref HEAD)
  echo "refs/heads/$currentbranch HEAD refs/heads/$currentbranch $(git log -1 origin/$currentbranch --format='%H')" | talisman --githook pre-push
}

getTalismanFile() {
  cat ./talismanrc
}

fixIt() {
  filePattern=".*\/.*\.[a-z].*"
  checksumPattern="[a-z0-9]{64}"
  declare -a FILES=()
  declare -a CHECKSUMS=()
  for row in $result; do
    if [[ $row =~ $checksumPattern ]]; then
      CHECKSUMS+=("${BASH_REMATCH[0]}")
    fi
    if [[ $row =~ $filePattern ]]; then
      FILES+=("${BASH_REMATCH[0]}")
    fi
  done
  count=0
  talismanFile=".talismanrc"
  for file in "${FILES[@]}"; do
    pattern=$(echo "$file" | sed 's/\//\\\//g')
    pattern="${pattern}\s{5}checksum:\s(${checksumPattern})"
    # Check if the file & checksum were previously added to the talisman file
    if [[ $(cat .talismanrc) =~ $pattern ]]; then
      # Update the old checksums with the new calculated ones
      replacement="s/${BASH_REMATCH[1]}/${CHECKSUMS[count]}/g"
      sed -i "$replacement" $talismanFile
    else
      # Add the new files and checksums to the talisman file
      text="\ \ - filename: ${file}\n    checksum: ${CHECKSUMS[count]}"
      pattern="/ignore_detectors.*/a ${text}"
      sed -i "$pattern" $talismanFile
    fi
    count=$((count + 1))
  done
}

result=$(runTalisman)

if [[ $result =~ fileignoreconfig.* ]]; then
  result="${BASH_REMATCH[0]}"
  echo "Found the following issues through talisman, do you wish to fix them automatically?"
  echo "$result"
  select yn in "Yes" "No"; do
    case $yn in
      Yes)
        fixIt
        break
        ;;
      No) exit 0 ;;
    esac
  done
else
  echo "No issues found in talisman"
  exit 0
fi
