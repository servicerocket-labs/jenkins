// Reusable Jenkinsfile for Terraform projects. An Atlas replacement, needs AWS.
// 
// Vars:
//
//   AWS_CREDS: AWS access key ID and secret access key.
//   DK_RUN_ARGS: Additional arguments for Docker, e.g. "-e FOO=bar".
//   GIT_CREDS: SSH username with private key.
//   GIT_URL: 
//   GIT_SUBDIR: 
//   TF_REMOTE_ARGS: Arguments to configure Terraform remote state, e.g. "-backend=s3 -backend-config=...".
//   TF_VERSION: Version of Terraform. If "full" is used, beware that version of your state file could be updated.
//   TF_CMD_ARGS: Additional arguments for Terraform command, e.g. "-var foo=bar".
//   TF_CMD_SARGS: Additional arguments for Terraform command but with sensitive content.

node {

  def id = "${env.JOB_NAME}-${env.BUILD_ID}"
  def td = "/tmp/${id}"
  def wd = "${pwd()}/${GIT_SUBDIR}"
  def tfra = "${TF_REMOTE_ARGS}"

  def tv = tfVersion("light")
  def dkra  = dkRunArgs("")
  def tfca = tfCmdArgs("")
  def tfcs = tfCmdSargs("")

  git credentialsId: "${GIT_CREDS}", url: "${GIT_URL}"
  withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: AWS_CREDS, usernameVariable: 'ak', passwordVariable: 'sk']]) {

    def run = "docker run --rm -u `id -u \044USER` -v ${wd}:${td} -w=${td} -e AWS_ACCESS_KEY_ID=${env.ak} -e AWS_SECRET_ACCESS_KEY=${env.sk} ${dkra} hashicorp/terraform:${tv}"
    def args = "-var aws_access_key=${env.ak} -var aws_secret_key=${env.sk} ${tfca}"
    sh "(head -n20 ${wd}/.terraform/terraform.tfstate 2>/dev/null | grep -q remote) || ${run} remote config ${tfra}" 

    if (tfcs.trim()) {
      withCredentials([[$class: 'StringBinding', credentialsId: TF_CMD_SARGS, variable: 'tfcs']]) {
        args = "${args} ${env.tfcs}"
      }
    }

    stage 'Plan'
    sh "${run} plan ${args}"

    input 'Apply the plan?'
    stage 'Apply'
    sh "${run} apply ${args}"
  }
}

def dkRunArgs(val) { try { return "$DK_RUN_ARGS" } catch (MissingPropertyException e) { return val } }
def tfCmdArgs(val) { try { return "$TF_CMD_ARGS" } catch (MissingPropertyException e) { return val } }
def tfCmdSargs(val) { try { return "$TF_CMD_SARGS" } catch (MissingPropertyException e) { return val } }
def tfVersion(val) { try { return "$TF_VERSION" } catch (MissingPropertyException e) { return val } }
