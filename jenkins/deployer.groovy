deployer_image = "egovio/deployer:1.0.0"

def takeSnapshot(group, env){
    stage("Snapshot ${env} env"){
        def cmd = "python scripts/snapshot.py ${group}"
        run(env, cmd)
    }
}

def deploy(env){
    stage("Deploy to ${env} env"){
        def cmd = "python scripts/deploy.py ${env}"
        run(env, cmd)
    }
}

def deployStandAlone(env, service, image, tag){
    stage("Deploy to ${env} env"){
        def cmd = "python scripts/apply.py  -e ${env} -m ${service} -i egovio/${image}:${tag} -dmi egovio/${image}-db:${tag} -conf -secret"
        run(env, cmd)
    }
}

def run(env, cmd){
    docker.image("${deployer_image}").inside {
        set_kube_config(env)

// aws auth, inject aws specific keys        
        if (env == "pbuatv2") {
            
            withCredentials([
                string(credentialsId: "${env}-aws-access-key", variable: "AWS_ACCESS_KEY"),
                string(credentialsId: "${env}-aws-secret-access-key", variable: "AWS_SECRET_ACCESS_KEY"),
                string(credentialsId: "${env}-aws-region", variable: "AWS_REGION"),
                string(credentialsId: "pwcqa_secret_passcode", variable: "PWCQA_SECRET_PASSCODE")
            ]){
                sh cmd;
            }
    } else{
            withCredentials([
                string(credentialsId: "pwcqa_secret_passcode", variable: "PWCQA_SECRET_PASSCODE")
            ]){
                sh cmd;
                echo $PWCQA_SECRET_PASSCODE;
            }        
        }

    }
}

def set_kube_config(env){
    withCredentials([file(credentialsId: "${env}-kube-config", variable: "KUBE_CONFIG")]){
        sh "cp ${KUBE_CONFIG} /.kube/config"
    }

}

return this;
