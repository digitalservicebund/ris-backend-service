# Local Migration

> **Work in progress - import through docker image**

To be able to pull the ris-data-migration image, login with the "GitHub Package Repository" username and credential token in 1PW:

```shell
export CR_PAT=ghp_... # use the token
echo $CR_PAT | docker login ghcr.io -u USERNAME --password-stdin # use the username
```

To connect to s3 bucket you need to add your aws credentials to 1PW and add the paths and to your shell:
```shell
export AWS_ACCESS_KEY_ID=$(op read op://Employee/AWS_ACCESS_KEY_ID/password)
export AWS_SECRET_ACCESS_KEY=$(op read op://Employee/AWS_SECRET_ACCESS_KEY/password)
```

