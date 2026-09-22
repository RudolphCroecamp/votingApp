THIS README FILE EXPLAINS THE PURPOSE AND FLOW OF THE BACKEND APPLICATION

CONTROLLERS:
    Admin -> use to handle admin request like manageing elections.
    Auth -> use to login existing users and register new users
    Blockchain -> use to save the vote to the local blockchain database and to verify the chain
    vote -> use to provide vote tokens, broadcast votes to the blockchain nodes and return vote receipts


SETUP INSTRUCTIONS:
    Requirements -> visual studio, docker desktop, github account
    step 1 -> pull this repository to you pc
    step 2 -> open the backend solution in visual studio
    step 3 -> try building the solution
    step 4 -> open the backend file directory in your terminal
    step 5 -> run the following commands:
                -> "ls" (you should see docker-compose.yml inside the directory)
                ->!!!!!!!double check docker and docker engine is running!!!!!!!
                -> "docker compose up --build" (this creates and run the docker containers) 
    step 6 -> open http://localhost:5000/scalar/v1 (use to test the api)