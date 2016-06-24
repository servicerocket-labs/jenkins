// Reusable Jenkinsfile for Terraform projects. An Atlas replacement.
// 
// Vars: AWS_CREDS, GIT_CREDS, GIT_URL, GIT_SUBDIR, TF_REMOTE_ARGS, TF_VERSION

node {

  def id = "${env.JOB_NAME}-${env.BUILD_ID}"
  def td = "/tmp/${id}"
  def tv = "${TF_VERSION}"
  def wd = "${pwd()}/${GIT_SUBDIR}"

  git credentialsId: "${GIT_CREDS}", url: "${GIT_URL}"

  withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: "${AWS_CREDS}", usernameVariable: 'ak', passwordVariable: 'sk']]) {

    def run = "docker run --rm -u `id -u jenkins` -v ${wd}:${td} -w=${td} -e AWS_ACCESS_KEY_ID=${env.ak} -e AWS_SECRET_ACCESS_KEY=${env.sk} --entrypoint=/go/bin/terraform hashicorp/terraform:${tv}"

    def args = "-var aws_access_key=${env.ak} -var aws_secret_key=${env.sk}"

    sh "(head -n20 ${wd}/.terraform/terraform.tfstate 2>/dev/null | grep -q remote) || ${run} remote config ${TF_REMOTE_ARGS}"

    stage 'Plan'    
    sh "${run} plan ${args}"
    input 'Apply the plan?'
    
    stage 'Apply'
    sh "${run} apply ${args}"
  }
}
