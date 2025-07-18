#!/bin/bash

echo "=== Testing Friends Fix ==="
echo "1. Make sure backend is running on http://localhost:8080"
echo "2. Login as Test1@gmail.com to get JWT token"
echo "3. Replace YOUR_JWT_TOKEN with actual token"
echo ""

# Test 1: Debug current friendship data
echo "=== Test 1: Debug Current Friendship Data ==="
echo "curl -H \"Authorization: Bearer YOUR_JWT_TOKEN\" \\"
echo "     -X GET http://localhost:8080/api/friends/debug"
echo ""

# Test 2: Create direct friendship
echo "=== Test 2: Create Direct Friendship ==="
echo "curl -H \"Authorization: Bearer YOUR_JWT_TOKEN\" \\"
echo "     -H \"Content-Type: application/json\" \\"
echo "     -X POST http://localhost:8080/api/friends/debug/create-friendship \\"
echo "     -d '{\"friendEmail\": \"Test2@gmail.com\"}'"
echo ""

# Test 3: Check friends list
echo "=== Test 3: Check Friends List ==="
echo "curl -H \"Authorization: Bearer YOUR_JWT_TOKEN\" \\"
echo "     -X GET http://localhost:8080/api/friends"
echo ""

# Test 4: Find user with status
echo "=== Test 4: Find User with Status ==="
echo "curl -H \"Authorization: Bearer YOUR_JWT_TOKEN\" \\"
echo "     -X GET \"http://localhost:8080/api/users/find?q=Test2@gmail.com\""
echo ""

echo "=== Expected Results ==="
echo "- Test 1: Should show debug info with friendContactsCount"
echo "- Test 2: Should return 'Direct friendship created successfully'"
echo "- Test 3: Should return array with at least 1 friend"
echo "- Test 4: Should return user with status 'friends'"
echo ""

echo "=== Frontend Test ==="
echo "1. Open browser console"
echo "2. Login as Test1@gmail.com"
echo "3. Go to Friends tab"
echo "4. Check console logs for API calls"
echo "5. Should see 'ALL — 1' instead of 'ALL — 0'" 