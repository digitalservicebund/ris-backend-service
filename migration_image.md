# Local Migration

> **Work in progress - import through docker image**

To be able to pull the ris-data-migration image, login with the "GitHub Package Repository" username and credential token in 1PW:

```shell
export CR_PAT=ghp_... # use the token
echo $CR_PAT | docker login ghcr.io -u USERNAME --password-stdin # use the username
```
