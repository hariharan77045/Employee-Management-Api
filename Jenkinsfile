// CI/CD for the EmployeeHub API.
//
// Jenkins setup (once):
//   - Agent with Docker, kubectl and JDK 17 (or run Jenkins itself in Docker with the host's Docker socket).
//   - Credentials:
//       dockerhub          Username/password  - your Docker Hub login (use an access token as password)
//       kubeconfig         Secret file        - kubeconfig for the cluster to deploy to (optional)
//       render-deploy-hook Secret text        - Render "Deploy Hook" URL (optional)
//   - Job type: Multibranch Pipeline pointing at this repository.
//
// Only the dev branch is pushed and deployed; other branches just build and test.

pipeline {
    agent any

    environment {
        DOCKERHUB_USER = 'hariharan77045'
        IMAGE          = "${DOCKERHUB_USER}/employeehub-backend"
        DB_IMAGE       = "${DOCKERHUB_USER}/employeehub-db"
        TAG            = "${env.BRANCH_NAME ?: 'dev'}-${env.BUILD_NUMBER}"
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    stages {
        stage('Build and test') {
            steps {
                // Integration tests need PostgreSQL; start a throwaway one for them.
                sh '''
                  docker rm -f ci-postgres-$BUILD_NUMBER >/dev/null 2>&1 || true
                  docker run -d --name ci-postgres-$BUILD_NUMBER -p 15432:5432 \
                    -e POSTGRES_DB=employee_management_project -e POSTGRES_PASSWORD=ci \
                    -v "$WORKSPACE/db/init:/docker-entrypoint-initdb.d:ro" postgres:18-alpine
                  for i in $(seq 1 30); do docker exec ci-postgres-$BUILD_NUMBER pg_isready -U postgres && break; sleep 2; done
                  chmod +x mvnw
                  DB_URL=jdbc:postgresql://localhost:15432/employee_management_project \
                  DB_PASSWORD=ci JWT_SECRET=ci-only-secret-that-is-at-least-32-characters \
                    ./mvnw -B verify
                '''
            }
            post {
                always {
                    sh 'docker rm -f ci-postgres-$BUILD_NUMBER >/dev/null 2>&1 || true'
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker images') {
            steps {
                sh 'docker build -t $IMAGE:$TAG -t $IMAGE:latest .'
                sh 'docker build -t $DB_IMAGE:$TAG -t $DB_IMAGE:latest db'
            }
        }

        stage('Push images') {
            when { branch 'dev' }
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub',
                        usernameVariable: 'DH_USER', passwordVariable: 'DH_TOKEN')]) {
                    sh '''
                      echo "$DH_TOKEN" | docker login -u "$DH_USER" --password-stdin
                      docker push $IMAGE:$TAG && docker push $IMAGE:latest
                      docker push $DB_IMAGE:$TAG && docker push $DB_IMAGE:latest
                      docker logout
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            when { branch 'dev' }
            steps {
                withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
                    sh '''
                      kubectl apply -f k8s/00-namespace.yaml -f k8s/10-postgres.yaml -f k8s/20-backend.yaml
                      kubectl -n employeehub set image deployment/backend backend=$IMAGE:$TAG
                      kubectl -n employeehub rollout status deployment/backend --timeout=300s
                    '''
                }
            }
        }

        stage('Deploy to Render') {
            when { branch 'dev' }
            steps {
                withCredentials([string(credentialsId: 'render-deploy-hook', variable: 'HOOK')]) {
                    sh 'curl -fsS -X POST "$HOOK" > /dev/null'
                }
            }
        }
    }

    post {
        always {
            sh 'docker image prune -f > /dev/null 2>&1 || true'
        }
    }
}
