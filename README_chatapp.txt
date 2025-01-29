-ChatApp-
By:Birkan Ege Çorbacıoğlu 21SOFT1038

This is a Chat Application built as part of the third project of the lecture. The application supports one-on-one chats only.

### How to Run the Application:

1. Run the Server:
   - Start the `Server` class first.
   - The server listens for incoming connections and manages all chat sessions.
   - Output from the server will display when new users connect or when chat requests are made.

2. Run the GUI (Client):
- Start the `GUI` class to create a new chat box for a user.
- Each time the `GUI` class is executed, a new chat window will open, representing a new user.
- Each user must provide a unique nickname when prompted.

### Notes:
- Server must be running first** before starting any `GUI` instances.
- Each time the `GUI` is run, a new chat box opens for a new user.
- To simulate multiple users, open multiple instances of the `GUI`.

### Known Limitations:
- Only users actively connected to the server will appear in the user list.
- A user must explicitly accept a chat request to begin chatting.
- Only one-on-on chats are provided. So group chats are not provided.

Thank you!