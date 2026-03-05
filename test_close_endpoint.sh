#!/bin/bash

echo "Testing close ticket endpoint..."

# Test 1: POST with empty body (should return 400 with validation error)
echo "Test 1: POST with empty body"
curl -X POST http://localhost:8080/api/support/tickets/1/close \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d "" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

echo -e "\n---\n"

# Test 2: POST with missing comment field (should return 400)
echo "Test 2: POST with missing comment field"
curl -X POST http://localhost:8080/api/support/tickets/1/close \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d "{}" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

echo -e "\n---\n"

# Test 3: POST with blank comment (should return 400)
echo "Test 3: POST with blank comment"
curl -X POST http://localhost:8080/api/support/tickets/1/close \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{"comment": "   "}' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

echo -e "\n---\n"

# Test 4: POST with valid comment (should work if authenticated properly)
echo "Test 4: POST with valid comment"
curl -X POST http://localhost:8080/api/support/tickets/1/close \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{"comment": "Ticket resolved successfully"}' \
  -w "\nHTTP Status: %{http_code}\n" \
  -s

echo -e "\nTest completed!"