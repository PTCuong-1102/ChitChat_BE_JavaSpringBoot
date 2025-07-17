# Friends Debug Test Script

## Prerequisites
- Backend running on `http://localhost:8080`
- User Test1@gmail.com logged in
- User Test2@gmail.com exists in database

## Test Steps

### 1. Debug Current Friendship Data

```bash
# Get debug info for current user
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -X GET http://localhost:8080/api/friends/debug
```

### 2. Test Direct Friendship Creation

```bash
# Create direct friendship between current user and Test2@gmail.com
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -H "Content-Type: application/json" \
     -X POST http://localhost:8080/api/friends/debug/create-friendship \
     -d '{"friendEmail": "Test2@gmail.com"}'
```

### 3. Check Friends List

```bash
# Get friends list after creation
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -X GET http://localhost:8080/api/friends
```

### 4. Test Friend Request Flow

```bash
# Send friend request
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -H "Content-Type: application/json" \
     -X POST http://localhost:8080/api/friends/requests \
     -d '{"email": "Test2@gmail.com"}'

# Get friend requests (as Test2@gmail.com)
curl -H "Authorization: Bearer TEST2_JWT_TOKEN" \
     -X GET http://localhost:8080/api/friends/requests

# Accept friend request (as Test2@gmail.com, use requestId from above)
curl -H "Authorization: Bearer TEST2_JWT_TOKEN" \
     -X PUT http://localhost:8080/api/friends/requests/REQUEST_ID/accept
```

### 5. Find User with Status

```bash
# Find user and check friendship status
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -X GET "http://localhost:8080/api/users/find?q=Test2@gmail.com"
```

## Expected Results

### After Direct Friendship Creation:
- Both users should see each other in friends list
- Frontend should show "ALL — 1" instead of "ALL — 0"
- User search should show "Already Friends"

### Debug Response Should Show:
- `friendContactsCount` > 0
- `friendContacts` array with ACCEPTED status
- Both users with `status: true`

## Common Issues to Check:

1. **Field Mapping**: Ensure UserResponse uses proper @JsonProperty annotations
2. **isActive Field**: Check that UserContact.isActive = true
3. **User Status**: Verify that both users have status = true
4. **Bidirectional**: Ensure both directions of friendship exist
5. **Frontend Mapping**: Check that frontend can read full_name, user_name, avatar_url

## SQL Debug Queries:

```sql
-- Check user contacts table
SELECT * FROM user_contacts WHERE user_id = 'YOUR_USER_ID' OR friend_id = 'YOUR_USER_ID';

-- Check users table
SELECT id, email, full_name, user_name, status FROM users WHERE email IN ('Test1@gmail.com', 'Test2@gmail.com');

-- Check friendship query
SELECT uc.*, u1.email as user_email, u2.email as friend_email 
FROM user_contacts uc 
JOIN users u1 ON u1.id = uc.user_id 
JOIN users u2 ON u2.id = uc.friend_id 
WHERE (uc.user_id = 'YOUR_USER_ID' OR uc.friend_id = 'YOUR_USER_ID') 
AND uc.status = 'ACCEPTED' 
AND uc.is_active = true 
AND u1.status = true 
AND u2.status = true;
``` 