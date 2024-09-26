# Migration through Docker

> **Work in progress - import through Docker image**

To pull the `ris-data-migration` image, log in to the GitHub Package Repository using your username and a credential token stored in 1Password (1PW):

```shell
export CR_PAT=ghp_... # Replace with your personal access token
echo $CR_PAT | docker login ghcr.io -u USERNAME --password-stdin # Replace USERNAME with your GitHub username
```

To connect to your S3 bucket, ensure your AWS credentials are stored in 1Password, and then set the following environment variables in your shell:

```shell
export AWS_ACCESS_KEY_ID=$(op read op://Employee/AWS_ACCESS_KEY_ID/password) # Fetch AWS Access Key ID from 1PW
export AWS_SECRET_ACCESS_KEY=$(op read op://Employee/AWS_SECRET_ACCESS_KEY/password) # Fetch AWS Secret Access Key from 1PW
export AWS_BUCKET_NAME=$(op read op://Employee/AWS_BUCKET_NAME/password) # Fetch AWS Bucket Name from 1PW
```

