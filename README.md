﻿# FileMongoMicroservice
### Functions:

1. This microservice connects to MongoDB, and its main purpose is to save the body of user files or folders and to monitor the properties of the files.
2. Creates a user body to save user properties of uploaded files and folders.
3. Prevents overloading of the FileFtpMicroservice.
4. Allows users to manage their files and generates unique IDs for files or folders for the FileFtpMicroservice.

### In the next version:

- Add the option to share user files or folders with other users.
- Enable Redis Cache Database.
- Run this microservice using Kubernetes to create replications for this microservice.

### Diagram:

```mermaid
	flowchart TB
		subgraph Kafka
			id3[Create File]
			id4[Create Folder]
			id5[Delete File]
			id6[Delete Folder]
		end
		subgraph MongoDB
			subgraph User
				Nickname
				subgraph ListFiles
					id
					filename
					isFile
					star
					format
					type
					dir
					Files
				end
			end
		end
		subgraph FileMongoMicroservice
			id7[CreateFile]
			id8[CreateFolder]
			id9[DeleteFile]
			id10[DeleteFolder]
			id11[StarFile]
			id12[UnStarFile]
		end
		id1[Client]
		id2[FileFtpicroservice]
		id3 -.-> id2 
		id4 -.-> id2 
		id5 -.-> id2 
		id6 -.-> id2
		id1 --> FileMongoMicroservice
		id7 --> User
		id8 --> User
		id9 --> User
		id10 --> User
		id11 --> User
		id12 --> User
		FileMongoMicroservice -- Success or Exception --> id1
		id7 -.-> id3
		id8 -.-> id4
		id9 -.-> id5
		id10 -.-> id6
```
