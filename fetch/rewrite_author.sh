#!/usr/bin/env bash
# Rewrite Git Author.
#
# Licensed under MIT - see https://github.com/MichaelCurrin/rewrite-git-author/blob/main/LICENSE .

SCRIPT_NAME=$(basename $0)

if [[ "$#" -ne 2 ]]; then
  echo "Rewrite Git Author"
  echo
  echo "Usage: $SCRIPT_NAME OLD_EMAIL NEW_EMAIL"
  echo
  echo "e.g. $SCRIPT_NAME foo@bar.com fizz@buzz.net"

  exit 0
fi

export FILTER_BRANCH_SQUELCH_WARNING=1

OLD_EMAIL="$1"
NEW_EMAIL="$2"

ENV_VARS="
OLD_EMAIL=$OLD_EMAIL
NEW_EMAIL=$NEW_EMAIL
"

CONDITION='
if [ "$GIT_COMMITTER_EMAIL" = "$OLD_EMAIL" ]; then
  export GIT_COMMITTER_EMAIL="$NEW_EMAIL"
fi

if [ "$GIT_AUTHOR_EMAIL" = "$OLD_EMAIL" ]; then
  export GIT_AUTHOR_EMAIL="$NEW_EMAIL"
fi
'

FILTER="$ENV_VARS $CONDITION"

printf "$FILTER\n"

git filter-branch \
  -f \
  --env-filter "$FILTER" \
  --tag-name-filter \
  cat -- \
  --branches \
  --tags