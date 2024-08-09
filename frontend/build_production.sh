#!/bin/sh
set -euf

if [ -e /etc/posthog-secrets/api-key ]
then
  export VITE_POSTHOG_API_KEY="${VITE_POSTHOG_API_KEY:=$(cat /etc/posthog-secrets/api-key)}"
  echo "API Key starts with ${VITE_POSTHOG_API_KEY:0:4}"
fi


npm install && npx vite build
