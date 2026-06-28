#!/bin/bash
echo "Initializing database..."
docker exec -i ai-log-postgres psql -U admin -d logdb < init-db.sql
echo "Done!"
