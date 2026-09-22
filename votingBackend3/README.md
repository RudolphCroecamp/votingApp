THIS README FILE EXPLAINS THE PURPOSE AND FLOW OF THE BACKEND APPLICATION<br>

CONTROLLERS:<br>
    Admin -> use to handle admin request like manageing elections.<br>
    Auth -> use to login existing users and register new users<br>
    Blockchain -> use to save the vote to the local blockchain database and to verify the chain<br>
    vote -> use to provide vote tokens, broadcast votes to the blockchain nodes and return vote receipts<br>


SETUP INSTRUCTIONS:<br>
    Requirements -> visual studio, docker desktop, github account<br>
    step 1 -> pull this repository to you pc<br>
    step 2 -> open the backend solution in visual studio<br>
    step 3 -> try building the solution<br>
    step 4 -> open the backend file directory in your terminal<br>
    step 5 -> run the following commands:<br>
                -> "ls" (you should see docker-compose.yml inside the directory)<br>
                ->!!!!!!!double check docker and docker engine is running!!!!!!!<br>
                -> "docker compose up --build" (this creates and run the docker containers) <br>
    step 6 -> open http://localhost:5000/scalar/v1 (use to test the api)<br>