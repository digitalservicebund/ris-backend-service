#!/bin/sh

set -eu

readonly DEBUG="${DEBUG:-unset}"
if [ "${DEBUG}" != unset ]; then
  set -x
fi

_fail() {
  printf "\033[0;31m==> %s\033[0m\n\n" "$1"
}

_success() {
  printf "\033[0;32m==> %s\033[0m\n\n" "$1"
}

_info() {
  printf "\033[1;33m==> %s\033[0m\n\n" "$1"
}

_user() {
  printf "\033[0;33m%s\033[0m" "$1"
}

_setup_git_hooks() {
  _user "Do you want to install the Git hooks ? (y/n) "
  read -r answer
  if [ "$answer" = "y" ]; then

    if ! command -v lefthook > /dev/null 2>&1; then
      _fail "Setup requires Lefthook, please install first: \`brew install lefthook\`"
      exit 1
    fi
    if ! command -v talisman > /dev/null 2>&1; then
      _fail "Setup requires Talisman, please install first: \`brew install talisman\`"
      exit 1
    fi
    lefthook install
    _info "Git hooks installed.."
  fi
}

_setup_direnv() {
  # Allow direnv to use `.env` files as described here: 
  # https://github.com/direnv/direnv/blob/master/man/direnv.toml.1.md#codeloaddotenvcode
  cat > ~/.config/direnv/direnv.toml<< EOF
[global]
load_dotenv = true
EOF

}

_init() {
  _setup_git_hooks
  _setup_direnv
}

_start() {
  cd ./backend/
  gradle --stop
  gradle compileJava --continuous --quiet &
  gradle bootRun
}

_env() {
  if ! command -v gopass > /dev/null 2>&1; then
    GH_PACKAGES_REPOSITORY_USER=$READ_PACKAGES_PAT_USERNAME
    GH_PACKAGES_REPOSITORY_TOKEN=$READ_PACKAGES_PAT_TOKEN
    return
  fi
  
  cat > ./.env<< EOF
export GH_PACKAGES_REPOSITORY_USER=$(gopass show -o -y neuris/maven.pkg.github.com/digitalservicebund/neuris-juris-xml-export/username)
export GH_PACKAGES_REPOSITORY_TOKEN=$(gopass show -o -y neuris/maven.pkg.github.com/digitalservicebund/neuris-juris-xml-export/token)
export OAUTH2_CLIENT_ISSUER=$(gopass show -o -y neuris/maven.pkg.github.com/digitalservicebund/neuris-dev-oauth2-client/issuer-uri)
export OAUTH2_CLIENT_ID=$(gopass show -o -y neuris/maven.pkg.github.com/digitalservicebund/neuris-dev-oauth2-client/client-id)
export OAUTH2_CLIENT_SECRET=$(gopass show -o -y neuris/maven.pkg.github.com/digitalservicebund/neuris-dev-oauth2-client/client-secret)
export E2E_TEST_USER=$(gopass show -o -y neuris/maven.pkg.github.com/digitalservicebund/neuris-e2e-test-user/username)
export E2E_TEST_PASSWORD=$(gopass show -o -y neuris/maven.pkg.github.com/digitalservicebund/neuris-e2e-test-user/password)
EOF

  direnv allow .
}

_dev() {
  if ! command -v docker > /dev/null 2>&1; then
    _fail "Dev requires docker, please install first: \`brew install docker\`"
    exit 1
  fi
  docker build ./frontend -f frontend/Dockerfile -t neuris/frontend --no-cache
  eval "$(_env)"
  docker compose up
}

_commit_message_template() {
  template="$PWD/.git/.gitmessage.txt"
  git config commit.template "$template"
  cat << EOF > "$template"
Subject

Some context/description

Addresses RISDEV-${1#RISDEV-}
EOF
}

_clean_staging() {
  [ -z ${STAGING_URL+x} ] && _user "Staging url? " && read -r STAGING_URL
  [ -z ${STAGING_USER+x} ] && _user "Staging user? " && read -r STAGING_USER
  [ -z ${STAGING_PASSWORD+x} ] && _user "Staging password? " && read -r STAGING_PASSWORD

  endpoint="https://${STAGING_URL#"https://"}/api/v1/caselaw/documentunits/"
  regex="\"uuid\":\"[a-z0-9-]\{36\}\""

  documentunits=$(curl -s "$endpoint" -u "$STAGING_USER":"$STAGING_PASSWORD" \
    | grep -o "$regex" \
    | sed "s/\"uuid\":\"//g" \
    | sed "s/\"//g")

  n_deleted=0
  for documentUnit in $documentunits; do
    curl -X DELETE "$endpoint""$documentUnit" -u "$STAGING_USER":"$STAGING_PASSWORD"
    n_deleted=$((n_deleted + 1))
  done

  _info "$n_deleted documentunits deleted."
}

_help() {
  echo "Usage: ./run.sh [command]"
  echo ""
  echo "Available commands:"
  echo "init                  Set up repository for development"
  echo "env                   Provide shell env build/test tooling; usage: \`eval \"\$(./run.sh env)\"\`"
  echo "dev                   Start full-stack development environment"
  echo "clean-staging         Deletes all existing documentunits on staging"
  echo "cm <issue-number>     Configure commit message template with given issue number;"
  echo "                      issue number can be with or without prefix: 1234, RISDEV-1234."
}

cmd="${1:-}"
case "$cmd" in
  "init") _init ;;
  "env") _env ;;
  "dev") _dev ;;
  "clean-staging") _clean_staging ;;
  "_start") _start ;;
  "cm")
    shift
    _commit_message_template "$@"
    ;;
  *) _help ;;
esac
