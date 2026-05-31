#!/bin/bash
for i in $(seq 1 7); do
  code=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" -d '{"account":"fake","password":"wrong"}')
  echo "Attempt $i: HTTP $code"
done
