// Reusable Jenkinsfile for Terraform projects. An Atlas replacement.
// 
// Vars: AWS_ACCESS_KEY, AWS_CREDS_ID, GIT_CREDS_ID, GIT_URL, GIT_SUBDIR, TF_REMOTE_ARGS

node {

  def id = "${env.JOB_NAME}-${env.BUILD_ID}"
  def ak = "${AWS_ACCESS_KEY}"
  def td = "/tmp/${id}"
  def wd = "${pwd()}/${GIT_SUBDIR}"

  git credentialsId: "${GIT_CREDS_ID}", url: "${GIT_URL}"
  
  withCredentials([[$class: 'StringBinding', credentialsId: "${AWS_CREDS_ID}", variable: 'as']]) {

    def run = "docker run --rm -u `id -u jenkins` -v ${wd}:${td} -w=${td} -e AWS_ACCESS_KEY_ID=${ak} -e AWS_SECRET_ACCESS_KEY=${env.as} --entrypoint=/go/bin/terraform hashicorp/terraform:full"

    sh "(head -n20 ${wd}/.terraform/terraform.tfstate 2>/dev/null | grep -q remote) || ${run} remote config ${TF_REMOTE_ARGS}"

    stage 'Plan'    
    sh "${run} plan -var aws_access_key=${ak} -var aws_secret_key=${env.as}"
    input 'Apply the plan?'
    
    stage 'Apply'
    sh "${run} apply -var aws_access_key=${ak} -var aws_secret_key=${env.as}"

  }
}
