#!/bin/sh
set -euf

export VITE_POSTHOG_API_KEY="${VITE_POSTHOG_API_KEY:=$(cat /etc/posthog-secrets/api-key)}"

npm run dev -- --host
