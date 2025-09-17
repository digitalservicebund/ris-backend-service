#!/bin/bash

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
      _fail "Setup requires Lefthook, please install first"
      exit 1
    fi
    lefthook install
    _info "Git hooks installed.."
  fi
}

_setup_direnv() {
  # Allow direnv to use `.env` files as described here:
  # https://github.com/direnv/direnv/blob/master/man/direnv.toml.1.md#codeloaddotenvcode
  [ -d ~/.config/direnv/. ] || mkdir ~/.config/direnv
  cat > ~/.config/direnv/direnv.toml<< EOF
[global]
load_dotenv = true
EOF

}

_init() {
  _setup_git_hooks
  _setup_direnv
  _env
}

_start() {
  cd ./backend/
  gradle --stop
  gradle compileJava --build-cache --continuous --quiet &
  gradle bootRun --build-cache
}

_env() {

  if ! command -v op read op://Employee/AWS_ACCESS_KEY_ID/password > /dev/null 2>&1; then
    fail "Setup requires AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY and AWS_BUCKET_NAME to be stored in 1Password. Please see Lookup Tables Initialization section in README.md "
    exit 1
  fi

  cat > ./.env<< EOF
GH_PACKAGES_REPOSITORY_USER=$(op read "op://Team NeuRIS/Github Package Repository/username")
GH_PACKAGES_REPOSITORY_TOKEN=$(op read "op://Team NeuRIS/Github Package Repository/credential")
OAUTH2_CLIENT_ISSUER=$( op read "op://Team NeuRIS/neuris-local oauth client/issuer_url")
OAUTH2_CLIENT_ID=$( op read "op://Team NeuRIS/neuris-local oauth client/username")
OAUTH2_CLIENT_SECRET=$( op read "op://Team NeuRIS/neuris-local oauth client/credential")
OAUTH2_USER_API_CLIENT_ID=$(op read "op://Team NeuRIS/API keycloak bare.id user api client staging/username")
OAUTH2_USER_API_CLIENT_SECRET=$(op read "op://Team NeuRIS/API keycloak bare.id user api client staging/credential")
E2E_TEST_USER=$( op read "op://Team NeuRIS/staging e2e test user DS/username")
E2E_TEST_PASSWORD=$( op read "op://Team NeuRIS/staging e2e test user DS/password")
E2E_TEST_USER_BGH=$( op read "op://Team NeuRIS/e2e_test BGH neuris/username")
E2E_TEST_PASSWORD_BGH=$( op read "op://Team NeuRIS/e2e_test BGH neuris/password-new")
E2E_TEST_USER_BFH=$( op read "op://Team NeuRIS/e2e_test BFH neuris/username")
E2E_TEST_PASSWORD_BFH=$( op read "op://Team NeuRIS/e2e_test BFH neuris/password")
E2E_TEST_USER_EXTERNAL=$( op read "op://Team NeuRIS/Neuris Staging e2e Extern/username")
E2E_TEST_PASSWORD_EXTERNAL=$( op read "op://Team NeuRIS/Neuris Staging e2e Extern/password-new")
MY_UID=$(id -u)
MY_GID=$(id -g)
DB_URL=jdbc:postgresql://localhost:5432/postgres
DB_USER=test
DB_PASSWORD=test
AWS_ACCESS_KEY_ID=$(op read "op://Employee/NeuRIS S3/access-key-id")
AWS_SECRET_ACCESS_KEY=$(op read "op://Employee/NeuRIS S3/secret-access-key")
AWS_BUCKET_NAME=neuris-migration-juris-data
EURLEX_URL=https://eur-lex.europa.eu/EURLexWebService?WSDL
IMPORTER_USERNAME=$( op read "op://Team NeuRIS/Importer - Staging/username")
IMPORTER_PASSWORD=$( op read "op://Team NeuRIS/Importer - Staging/password")
IMPORTER_URL=$( op read "op://Team NeuRIS/Importer - Staging/website")
BAREID_INSTANCE=$(op read "op://Team NeuRIS/API keycloak bare.id user api client staging/instanceUuid")

EOF

  if ! command -v direnv > /dev/null 2>&1; then
    # Direnv is not installed. Source the .env file manually
    set -o allexport
    source .env
    set +o allexport
  else
    direnv allow .
  fi
}

_dev() {
  if ! command -v docker > /dev/null 2>&1; then
    _fail "Dev requires docker, please install first"
    exit 1
  fi

  wait=""
  services="traefik redis postgres14 frontend backend"
  for arg in "$@"; do
    case $arg in
      -i|--init)
        echo $GH_PACKAGES_REPOSITORY_TOKEN | docker login ghcr.io -u $GH_PACKAGES_REPOSITORY_USER --password-stdin
        services="initialization"
        ;;
      -n|--no-backend)
        services="${services/backend/}"
        ;;
      -f|--no-frontend)
        services="${services/frontend/}"
        ;;
      -l|--with-languagetool)
        services="$services languagetool"
        ;;
      -d|--detached)
        wait="--wait"
        ;;
    esac
  done

  if [[ " ${services[*]} " == *" frontend "* ]]; then
    docker build ./frontend -f frontend/Dockerfile -t neuris/frontend
  fi

  docker compose up $wait $services

  echo "The application is available at http://127.0.0.1"
}

_down() {
  docker compose stop
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
  _user "Application url? " && read -r APP_URL
  _user "Session ID? " && read -r SESSION_ID

  endpoint="https://${APP_URL#"https://"}/api/v1/caselaw/documentunits"
  regex="\"uuid\":\"[a-z0-9-]\{36\}\""

  documentUnits=$(curl -s -H "cookie: SESSION=$SESSION_ID" "$endpoint" \
    | grep -o "$regex" \
    | sed "s/\"uuid\":\"//g" \
    | sed "s/\"//g")

  n_deleted=0
  for documentUnit in $documentUnits; do
    curl -X DELETE -H "cookie: SESSION=$SESSION_ID" "$endpoint"/"$documentUnit"
    n_deleted=$((n_deleted + 1))
  done

  _info "$n_deleted documentunits deleted."
}

_help() {
  echo "Usage: ./run.sh [command]"
  echo ""
  echo "Available commands:"
  echo "init                  Initialize development environment (git hooks, env vars)"
  echo "dev                   Start full-stack development environment with loopup table initialization"
  echo "                      Add '-n' or '--no-backend' to start everything but backend and initialization"
  echo "                      Add '-f' or '--no-frontend' to start everything but frontend and initialization"
  echo "                      Add '-l' or '--with-languagetool' to start with languagetool"
  echo "                      Add '-i' or '--init' to only initialize the lookup tables (read ./migration_image.md for prerequisites)"
  echo "                      Add '-d' or '--detached' to check the health of the services in the background instead of showing the log stream"
  echo "down                  Stop development environment"
  echo "clean-staging         Deletes all existing documentunits on staging"
  echo "cm <issue-number>     Configure commit message template with given issue number;"
  echo "                      issue number can be with or without prefix: 1234, RISDEV-1234."
}

cmd="${1:-}"
case "$cmd" in
  "init") _init ;;
  "dev")
    shift
    _dev "$@";;
  "down") _down ;;
  "clean-staging") _clean_staging ;;
  "_start") _start ;;
  "cm")
    shift
    _commit_message_template "$@"
    ;;
  *) _help ;;
esac
