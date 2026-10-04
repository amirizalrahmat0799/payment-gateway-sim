// CI/CD for merchant-service: test -> image -> push to GHCR -> deploy to the Ubuntu VM.
// Every branch is tested and its image built; only main is pushed and deployed.
//
// Jenkins credentials used (Manage Jenkins -> Credentials):
//   ghcr-push      Username with password: GitHub user + classic token with write:packages
//   vm-deploy-ssh  SSH username with private key: user "deploy" + the jenkins_deploy private key
pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
    }

    environment {
        MAVEN_IMAGE = 'maven:3.9-eclipse-temurin-21'
        MODULE      = 'merchant-service'
        IMAGE       = 'ghcr.io/amirizalrahmat0799/pgs-merchant-service'
        DEPLOY_HOST = '192.168.56.101'
        DEPLOY_DIR  = '/opt/pgs'
        SITE        = 'merchant.pgs.test'
    }

    stages {
        stage('Build & test') {
            steps {
                // Throwaway Maven container sharing this workspace (jenkins-ci-lab convention)
                sh '''
                    docker run --rm --volumes-from jenkins --network ci-lab \
                      -v ci-lab-m2:/root/.m2 \
                      -w "$WORKSPACE" \
                      "$MAVEN_IMAGE" mvn -B verify
                '''
            }
            post {
                always {
                    junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Docker image') {
            steps {
                script {
                    // Tag = short commit SHA: every image points to exactly one commit
                    env.IMAGE_TAG = env.GIT_COMMIT.take(7)
                }
                sh 'docker build --build-arg MODULE="$MODULE" -t "$IMAGE:$IMAGE_TAG" .'
            }
        }

        stage('Push to GHCR') {
            when { branch 'main' }
            steps {
                withCredentials([usernamePassword(credentialsId: 'ghcr-push',
                                                  usernameVariable: 'GHCR_USER',
                                                  passwordVariable: 'GHCR_TOKEN')]) {
                    sh '''
                        echo "$GHCR_TOKEN" | docker login ghcr.io -u "$GHCR_USER" --password-stdin
                        docker tag "$IMAGE:$IMAGE_TAG" "$IMAGE:latest"
                        docker push "$IMAGE:$IMAGE_TAG"
                        docker push "$IMAGE:latest"
                    '''
                }
            }
            post {
                always { sh 'docker logout ghcr.io || true' }
            }
        }

        stage('Deploy to VM') {
            when { branch 'main' }
            steps {
                withCredentials([sshUserPrivateKey(credentialsId: 'vm-deploy-ssh',
                                                   keyFileVariable: 'SSH_KEY',
                                                   usernameVariable: 'SSH_USER')]) {
                    // Record the new tag in the VM's .env, then pull and restart only merchant-service
                    sh '''
                        ssh -i "$SSH_KEY" -o StrictHostKeyChecking=accept-new "$SSH_USER@$DEPLOY_HOST" "
                          set -e
                          cd $DEPLOY_DIR
                          if grep -q '^MERCHANT_TAG=' .env; then
                            sed -i 's/^MERCHANT_TAG=.*/MERCHANT_TAG=$IMAGE_TAG/' .env
                          else
                            echo 'MERCHANT_TAG=$IMAGE_TAG' >> .env
                          fi
                          docker compose pull merchant-service
                          docker compose up -d merchant-service
                        "
                    '''
                }
            }
        }

        stage('Smoke test') {
            when { branch 'main' }
            steps {
                // Through Nginx and HTTPS, like a real user: wait up to ~2.5 min for readiness
                sh '''
                    for i in $(seq 1 30); do
                      if curl -fsk --resolve "$SITE:443:$DEPLOY_HOST" "https://$SITE/actuator/health/readiness"; then
                        echo; echo "Deployed $IMAGE:$IMAGE_TAG to https://$SITE"; exit 0
                      fi
                      sleep 5
                    done
                    echo "merchant-service did not become ready"; exit 1
                '''
            }
        }
    }
}
