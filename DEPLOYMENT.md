# Deploying EmployeeHub

There are two ways to run the app, and they use the same Docker images:

| | Free public deployment | Local DevOps lab |
|---|---|---|
| Frontend (Angular) | **Vercel**: `https://hariharan-emp-project.vercel.app` | nginx container |
| Backend (Spring Boot) | **Render** (Docker web service): `https://hariharan-emp-project-api.onrender.com` | container / Kubernetes Deployment |
| Database | **Neon** (serverless PostgreSQL) | PostgreSQL container / StatefulSet |
| CI/CD | Vercel and Render deploy on every push to `dev` | **Jenkins** → Docker Hub → **Kubernetes** |

No domain purchase is needed: Vercel and Render both give free subdomains.

```
Browser ──► Vercel (Angular)  ──/api/* rewrite──►  Render (Spring Boot container)  ──►  Neon (PostgreSQL)
```

Vercel forwards `/api/*` to Render (see `vercel.json` in the frontend repo), so the browser only talks to
one origin and the JWT never crosses origins.

---

## 1. Database on Neon (free)

1. Sign up at <https://neon.com> and create a project (PostgreSQL 17 or 18, any region close to you).
2. Copy the connection string, which looks like
   `postgresql://USER:PASSWORD@ep-xxxx.REGION.aws.neon.tech/neondb?sslmode=require`.
3. Load the tables and practice data from this repo:

   ```bash
   psql "postgresql://USER:PASSWORD@ep-xxxx.REGION.aws.neon.tech/neondb?sslmode=require" -f db/init/01-employeehub.sql
   ```

   (`psql` comes with PostgreSQL; on this machine it is `F:\postgresql\bin\psql.exe`.)

The script has no login accounts in it; the backend creates them on its first start.

## 2. Backend on Render (free)

1. Sign up at <https://render.com> with GitHub and click **New → Web Service**.
2. Pick the **Employee-Management-Api** repository and set:
   - **Name:** `hariharan-emp-project-api` (this becomes the URL; if it's taken, use another name and
     update `vercel.json` in the frontend repo)
   - **Branch:** `dev`
   - **Runtime / Language:** Docker (it uses the `Dockerfile` in the repo root)
   - **Instance type:** Free
   - **Health check path:** `/actuator/health`
3. Environment variables:

   | Key | Value |
   |---|---|
   | `DB_URL` | `jdbc:postgresql://ep-xxxx.REGION.aws.neon.tech/neondb?sslmode=require` |
   | `DB_USERNAME` | the Neon user |
   | `DB_PASSWORD` | the Neon password |
   | `JWT_SECRET` | a new random string of 48+ characters (Render's **Generate** button works) |
   | `CORS_ALLOWED_ORIGINS` | `https://hariharan-emp-project.vercel.app` |
   | `DEFAULT_PASSWORD` | optional: password for the seeded practice accounts |

4. Create the service. The first build takes a few minutes. When it's live,
   `https://hariharan-emp-project-api.onrender.com/actuator/health` returns `{"status":"UP"}`.

Free-plan behaviour: the service sleeps after about 15 minutes without traffic, and the next request
waits up to a minute while it wakes. If the first page load after a break fails, refresh once.

## 3. Frontend on Vercel (free)

1. Sign up at <https://vercel.com> with GitHub and click **Add New → Project**.
2. Import the **Employee-Management** repository and set the **Project Name** to
   `hariharan-emp-project`. Everything else comes from `vercel.json` (build command, output folder,
   `/api` rewrite and single-page-app fallback).
3. Deploy. Then in **Settings → Environments → Production → Branch Tracking**, set the production
   branch to `dev`, because `main` isn't used.
4. Open `https://hariharan-emp-project.vercel.app` and sign in, for example as
   `employee1@company.com` / `Password@123` (ADMIN).

From now on, every push to `dev` redeploys the frontend on Vercel and the backend on Render.

## 4. Run everything locally with Docker

Install Docker Desktop, then in this folder create `.env` (it is git-ignored):

```
DB_PASSWORD=choose-a-db-password
JWT_SECRET=at-least-32-random-characters-go-here
```

```bash
docker compose up --build
```

Open <http://localhost:8081>. The frontend repo is expected at `../../../employeeManagement`; set
`FRONTEND_DIR` in `.env` if yours is elsewhere. `docker compose down -v` removes everything,
including the database volume.

## 5. Kubernetes (local cluster)

1. In Docker Desktop: **Settings → Kubernetes → Enable Kubernetes**. Check with `kubectl get nodes`.
2. Build the images (Docker Desktop's cluster can use local images directly):

   ```bash
   docker build -t employeehub-db:local db
   docker build -t employeehub-backend:local .
   docker build -t employeehub-frontend:local ../../../employeeManagement
   ```

3. Create the namespace and secret, then apply the manifests:

   ```bash
   kubectl apply -f k8s/00-namespace.yaml
   kubectl -n employeehub create secret generic employeehub-secrets \
     --from-literal=DB_PASSWORD='choose-a-db-password' \
     --from-literal=JWT_SECRET='at-least-32-random-characters-go-here'
   kubectl apply -f k8s/10-postgres.yaml -f k8s/20-backend.yaml -f k8s/30-frontend.yaml
   kubectl -n employeehub get pods -w
   ```

4. Open <http://localhost:30080>.

Useful commands:

```bash
kubectl -n employeehub logs deploy/backend -f
kubectl -n employeehub scale deployment backend --replicas=2
kubectl -n employeehub rollout undo deployment/backend
kubectl delete namespace employeehub        # remove everything
```

Managed Kubernetes in the cloud (EKS, GKE, AKS, DigitalOcean) is not free beyond trial credits,
which is why the public deployment uses Render instead.

## 6. Jenkins pipeline

Run Jenkins in Docker with access to the host's Docker engine:

```bash
docker run -d --name jenkins -p 9090:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts-jdk17
```

The agent that runs the pipeline needs the `docker` and `kubectl` command-line tools. Either install
them in a custom image built `FROM jenkins/jenkins:lts-jdk17`, or attach a separate agent that has them.

1. Open <http://localhost:9090>, finish the setup wizard and install the suggested plugins plus
   **Docker Pipeline**.
2. Add credentials (**Manage Jenkins → Credentials**):
   - `dockerhub`: username + Docker Hub access token
   - `kubeconfig`: secret file containing your cluster's kubeconfig
   - `render-deploy-hook`: secret text containing the Render deploy hook URL (Render → service →
     Settings → Deploy Hook)
3. Create two **Multibranch Pipeline** jobs, one per GitHub repository. Each repo's `Jenkinsfile`
   defines the stages:
   - **Backend:** start a throwaway PostgreSQL → `mvnw verify` → build the backend and database images →
     push them to Docker Hub → `kubectl set image` + rollout → trigger the Render deploy
   - **Frontend:** `npm ci` → production build → nginx image → push → `kubectl set image` + rollout

   Push and deploy stages run only on the `dev` branch.
