# Отчёт

1. Docker-образ: взял Go-мок, готовый Dockerfile. `/ping` -> `pong`, при `ENABLE_FEATURE_X=true` появляется `/feature`
2. Helm: в `deployment.yaml` дабавил probes (`/ping:8080`), `resources`, `env`, `imagePullPolicy` — всё из values
3. Два values: `staging-values.yaml` (64Mi, флаг on), `prod-values.yaml` (256Mi, флаг off). `pullPolicy: Never` — образ из minikube
4. CI (`.gitlab-ci.yml`): стадии build / test / deploy / tag, запуск `npx gitlab-ci-local build test deploy tag`
5. DNS: Service `booking-service` резолвится внтри кластера, `check-dns.sh` из пода busybox -> `pong`.
6. Артефакты в results
