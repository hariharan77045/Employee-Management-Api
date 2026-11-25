# PostgreSQL with the EmployeeHub tables and practice data loaded on first start.
# The init script runs only when the data directory is empty (a new volume).
FROM postgres:18-alpine
COPY init/ /docker-entrypoint-initdb.d/
